package EstimatingPipeline;


import EstimatingPipeline.util.DBConnection;
import static EstimatingPipeline.ProjectsController.setItemSelected;
import EstimatingPipeline.model.Project;
import EstimatingPipeline.model.User;
import com.sun.glass.ui.Window;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;


/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class ReportsController {

    @FXML
    private Button backBtn;
    @FXML
    private TableColumn<Project, String> titleCol;
    @FXML
    private ChoiceBox<String> firstCb;
    @FXML
    private ChoiceBox<String> secondCb;
    @FXML
    private ChoiceBox<String> thirdCb;
    @FXML
    private ChoiceBox<String> fourthCb;
    @FXML
    private ChoiceBox<String> fifthCb;
    @FXML
    private Label resultsLbl;
    @FXML
    private Button printBtn;
    @FXML
    private TableView<Project> reportsTableView;
    @FXML
    private TableColumn<Project, String> valueCol;
    @FXML
    private TableColumn<Project, LocalDateTime> dueCol;
    @FXML
    private TableColumn<Project, String> categoryCol;
    @FXML
    private TableColumn<Project, String> statusCol;
    @FXML
    private TableColumn<Project, String> clientCol;
    @FXML
    private TableColumn<Project, String> estimatorCol;

    private EstimatingPipeline mainApp;
    private User currentUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    private ObservableList<Project> projectList = FXCollections.observableArrayList();
    private final Connection connection = DBConnection.getConn();



    
    public ReportsController() {
    }
  
    public void setUpReports(EstimatingPipeline mainApp, User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        
       // Set Up TableView with Date Formatting
        dueCol.setCellValueFactory(new PropertyValueFactory<>("due"));
        dueCol.setCellFactory(column -> new TableCell<Project, LocalDateTime>() {
            public void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) 
                    setText("");
                else 
                    setText(formatter.format(date));
            }
        });
       
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        estimatorCol.setCellValueFactory(new PropertyValueFactory<>("estimatorName"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        
        // Get ProjectList for ALL appts, NOT loading into TableView yet
        projectList.addAll(ProjectsController.getProjectData("All"));
        
        // double-click on row opens project editor
        reportsTableView.setRowFactory(ttv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setItemSelected(reportsTableView.getSelectionModel().getSelectedItem());
                    mainApp.showProjectFields(currentUser);
                    
                }
            });
            return row;
        });

        if (currentUser.getType().equalsIgnoreCase("limited"))
        {
            firstCb.setDisable(true);
            printBtn.setDisable(true);
        }
         firstCb.getItems().add("All Estimators");
        try {
            String userQuery = "SELECT estimator.firstname, estimator.lastname"
                + " FROM estimator";
        
            PreparedStatement smt = connection.prepareStatement(userQuery);
            ResultSet usersFound = smt.executeQuery();

            while (usersFound.next()) {
                String estimatorNames = usersFound.getString("estimator.firstname")
                        + " " + usersFound.getString("estimator.lastname");
                firstCb.getItems().add(estimatorNames);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }
        
        //populate Category ChoiceBox
        secondCb.getItems().addAll("All Categories", "Entertaiment", "Private Education", 
                "Medical Office Building", "Industrial", "Commercial", 
                "Food & Beverage", "Hospitality", "Other");
        
        // populate Clients ChoiceBox
        thirdCb.getItems().add("All Clients");
        try {
            String clientQuery = "SELECT client.name"
                + " FROM client" 
                + " ORDER BY client.name";
            PreparedStatement smt = connection.prepareStatement(clientQuery);
            ResultSet clientsFound = smt.executeQuery();
            while (clientsFound.next()) {
                String dClientName = clientsFound.getString("client.name");
                thirdCb.getItems().add(dClientName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } 
        
        fourthCb.getItems().addAll("Q1", "Q2", "Q3", "Q4", "This Year", "Last Year", "All Time");
        fifthCb.getItems().addAll("All Statuses", "Active", "Pending", "Awarded", "Not Awarded");
        
        // pre-select the first choice for CB1-3. For C4, use this quarter.
        firstCb.getSelectionModel().selectFirst();
        secondCb.getSelectionModel().selectFirst();
        thirdCb.getSelectionModel().selectFirst();
        fourthCb.getSelectionModel().selectLast();
        fifthCb.getSelectionModel().selectFirst();

        updateResults();
        
    }
    
//     This method filters out projectList for results that matches the user's query and 
//     updates the results found label with the correct number of items found
    @FXML
    private void updateResults() {
        reportsTableView.getItems().setAll(projectList.filtered(isInReport()));   
        resultsLbl.setText("Hit Ratio: " + calculateHitRatio() + reportsTableView.getItems().size() + " Total Matching Bids");
    }
    
    private String calculateHitRatio()
    {
        ObservableList<Project> hitRatioList =  reportsTableView.getItems().filtered
        (p->p.getStatus().equals("Awarded") || p.getStatus().equals("Not Awarded"));
        if (hitRatioList.isEmpty())
            return "No Completed Bids / ";
        else
        {
            Integer awarded = hitRatioList.filtered(p -> p.getStatus().equals("Awarded")).size();
            Double hitRatio = (double)awarded/hitRatioList.size() * 100;
            String hitRatioString = new DecimalFormat("#.##").format(hitRatio) 
                    + "% Awarded  /  (Awarded " + awarded + " out of " + hitRatioList.size() + ")  /  " ;
            return hitRatioString;
        }
    }
    
    // This predicate returns a filtered list of appts that matches the user's query for report 1
    private Predicate<Project> isInReport() {
        
        return p -> 
                (p.getEstimatorName().equalsIgnoreCase(firstCb.getSelectionModel().getSelectedItem()) 
                || firstCb.getSelectionModel().getSelectedItem() == "All Estimators")
            && (p.getCategory().equalsIgnoreCase(secondCb.getSelectionModel().getSelectedItem())
                    || secondCb.getSelectionModel().getSelectedItem() == "All Categories")
            && (p.getClientName().equalsIgnoreCase(thirdCb.getSelectionModel().getSelectedItem())
                    || thirdCb.getSelectionModel().getSelectedItem() == "All Clients")            
            && (getQuarter(p.getDue().getMonthValue()).equals(fourthCb.getSelectionModel().getSelectedItem())
                    && p.getDue().getYear() == LocalDateTime.now().getYear()
                || p.getDue().getYear() == LocalDateTime.now().getYear() 
                        && fourthCb.getSelectionModel().getSelectedItem() == "This Year"
                || p.getDue().getYear() == LocalDateTime.now().minusYears(1).getYear() 
                        && fourthCb.getSelectionModel().getSelectedItem() == "Last Year"
                || fourthCb.getSelectionModel().getSelectedItem() == "All Time")
            && (p.getStatus().equalsIgnoreCase(fifthCb.getSelectionModel().getSelectedItem())
                || fifthCb.getSelectionModel().getSelectedItem() == "All Statuses");
    }
    
    private String getQuarter(Integer month)
    {
        if(month < 4 )
            return "Q1";
        else if (month < 7)
            return "Q2";
        else if (month < 10)
            return "Q3";
        else if (month < 13)
            return "Q4";
        else
            return "Something went wrong";
    }
    
    @FXML
    private void handlePrint(ActionEvent e) throws IOException {
        Writer writer = null;
        ObservableList<Project> tableOutput = FXCollections.observableArrayList();
        tableOutput = reportsTableView.getItems();
        String dialog;
        try{
            do{
                dialog = JOptionPane.showInputDialog("Rename your file (no spaces, /, \\, or .):");
            }while(dialog.contains(".") || dialog.contains("\\") || dialog.contains("/")|| dialog.isEmpty());
           
            //String workingDir = new File(".").getAbsolutePath() + "\\" + dialog + ".txt";
            File file = new File(dialog + ".txt");
            writer = new BufferedWriter(new FileWriter(file));

            String queryInfoString =  "Hit Ratio Report" + "\r\n\r\nReport Details - Estimator: " 
                + firstCb.getSelectionModel().getSelectedItem() 
                + ", Category: " + secondCb.getSelectionModel().getSelectedItem()
                + ", Client: " + thirdCb.getSelectionModel().getSelectedItem() 
                + ", Quarter: " + fourthCb.getSelectionModel().getSelectedItem() + "\r\n\r\n";
            writer.write(queryInfoString);

            String headerString = "Due" + 
                    "\t" + "Title" + "\t" + "Category" + "\t" + 
                    "Status" + "\t" + "Client" + "\t" + "Estimator" + "\t" + "Value" + "\r\n";
            writer.write(headerString);
            
            for (int i = 0; tableOutput.size()>i; i++){
            
                    Project apt = tableOutput.get(i);
                   
                    String result = apt.getDue().format(formatter) 
                            + "\t" +  apt.getTitle() + "\t" + apt.getCategory() 
                            + "\t" + apt.getStatus() +"\t" + apt.getClientName() 
                             +"\t" + apt.getEstimatorName() +"\t" + apt.getValue() + "\r\n";
                    writer.write(result);
            }
            String count = "\r\n" + resultsLbl.getText() + "\r\n\r\n";
            writer.write(count);
            writer.flush();
            writer.close();
            
            Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
            successAlert.setHeaderText("Successfully Generated Report!");
            successAlert.setContentText("Report Generated. Do you want "
                    + "to open the report file?  \r\n\r\nHint: You can copy and paste the data into an Excel spreadsheet.");
            Optional<ButtonType> result = successAlert.showAndWait();
            if (result.get() == ButtonType.OK)
                Desktop.getDesktop().open(file);
            else
            {
                Alert failedAlert = new Alert(Alert.AlertType.ERROR);
                failedAlert.setContentText("Unable to generate report/open report!");
            }
        }catch (Exception ex) {
            Alert failedAlert = new Alert(Alert.AlertType.ERROR);
            failedAlert.setContentText("Unable to generate report/open report!");
            failedAlert.setContentText(ex.getMessage());
        ex.printStackTrace();
        }

    }
 
    @FXML
    private void handleBackButton(ActionEvent event) {
        mainApp.showMainMenu(currentUser);
    }
    

}
