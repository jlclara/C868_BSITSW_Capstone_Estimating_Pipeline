/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EstimatingPipeline;

import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.Project;
import EstimatingPipeline.model.User;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class ProjectsController {

    @FXML
    private TableView<Project> projectTableView;
    @FXML
    private TableColumn<Project, Integer> projectIdCol;
    @FXML
    private TableColumn<Project, LocalDateTime> dueCol;
    @FXML
    private TableColumn<Project, String> titleCol;
    @FXML
    private TableColumn<Project, String> categoryCol;
    @FXML
    private TableColumn<Project, String> statusCol;
    @FXML
    private TableColumn<Project, String> clientCol;
    @FXML
    private TableColumn<Project, String> estimatorCol;
    @FXML
    private Button projectNewBtn;
    @FXML
    private Button projectEditBtn;
    @FXML
    private Button projectDeleteBtn;
    @FXML
    private Button btnActive;
    @FXML
    private Button btnPending;
    @FXML
    private Button btnNRewarded;
    @FXML
    private Button btnRewarded; 
    @FXML
    private Button btnAll; 
 
    private EstimatingPipeline mainApp;
    private User currentUser;
    private static Project modProjectSelected;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

    public ProjectsController () {
    }
    
    @FXML 
    private void handleNewProject(ActionEvent e) throws IOException {
        resetItemSelected();
        mainApp.showProjectFields(currentUser);
    }
    
    @FXML 
    private void handleEditProject(ActionEvent e) throws IOException {
        modProjectSelected = projectTableView.getSelectionModel().getSelectedItem();
        if (modProjectSelected == null)
        {
            Alert noProjAlert = new Alert (Alert.AlertType.WARNING);
            noProjAlert.setTitle("No Bid Selected");
            noProjAlert.setHeaderText("Error: No Bid Selected");
            noProjAlert.setContentText("Please select an bid and try again.");
            noProjAlert.showAndWait();
        }
        else 
            mainApp.showProjectFields(currentUser);
    }
    @FXML 
    private void handleDeleteProject(ActionEvent e) throws IOException {
        modProjectSelected = projectTableView.getSelectionModel().getSelectedItem();
        if (modProjectSelected == null)
        {
            Alert noProjAlert = new Alert (Alert.AlertType.WARNING);
            noProjAlert.setTitle("No Bid Selected");
            noProjAlert.setHeaderText("Error: No Bid Selected");
            noProjAlert.setContentText("Please select an bid and try again.");
            noProjAlert.showAndWait();
        }
        else
        {
            Alert deleteProjAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteProjAlert.setTitle("Delete Bid?");
            deleteProjAlert.setHeaderText("Are you sure you want to delete this bid?");
            deleteProjAlert.setContentText("This cannot be undone!");
            Optional<ButtonType> result = deleteProjAlert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
               try {
                String deleteQuery = "DELETE "
                        + " FROM project"
                        + " WHERE project.projectId = ?";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, modProjectSelected.getProjectId());
                deleteSmt.executeUpdate();
                setupProjects(mainApp, currentUser);
               } catch (SQLException exc)
               {
                   exc.printStackTrace();;
               }
            }
        }
    }
    
    @FXML 
    private void handleBackBtn(ActionEvent e) throws IOException {
        mainApp.showMainMenu(currentUser);
    }
    @FXML
    private void getActiveTab(ActionEvent e) throws IOException
    {
        setButtonDefaultStyle();
        String[] activeBtn = e.getSource().toString().split("'");

        if (null != activeBtn[1])
            switch (activeBtn[1]) {
            case "Active":
                setButtonActiveStyle(btnActive);
                break;
            case "Pending":
                setButtonActiveStyle(btnPending);
                break;        
            case "Not Awarded":
                setButtonActiveStyle(btnNRewarded);
                break;
            case "Awarded":
                setButtonActiveStyle(btnRewarded);
                break;
            case "All":
                setButtonActiveStyle(btnAll);
                break;
            default:
                break;
        }
         projectTableView.getItems().setAll(getProjectData(activeBtn[1]));
    }
    
    public void setupProjects(EstimatingPipeline mainApp, User activeUser)
    {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
        {
            projectNewBtn.setDisable(true);
            projectDeleteBtn.setDisable(true);
            projectEditBtn.setText("VIEW");
        }
        resetItemSelected();
        projectIdCol.setCellValueFactory(new PropertyValueFactory<>("projectId"));
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
        
        projectTableView.setRowFactory(ttv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setItemSelected(projectTableView.getSelectionModel().getSelectedItem());
                    mainApp.showProjectFields(currentUser);
                }
            });
            return row;
        });
        
        projectTableView.getItems().setAll(getProjectData("All"));
        setButtonDefaultStyle();
        setButtonActiveStyle(btnAll);
    }
    
    private void setButtonActiveStyle(Button b)
    {
        b.setStyle("-fx-background-color:white; -fx-border-color:black;");
    }
    
    private void setButtonDefaultStyle()
    {
        btnActive.setStyle("-fx-background-color:#f2f2f2; -fx-border-color:black;");
        btnPending.setStyle("-fx-background-color:#f2f2f2; -fx-border-color:black;");
        btnNRewarded.setStyle("-fx-background-color:#f2f2f2; -fx-border-color:black;");
        btnRewarded.setStyle("-fx-background-color:#f2f2f2; -fx-border-color:black;");
        btnAll.setStyle("-fx-background-color:#f2f2f2; -fx-border-color:black;");

    }
    
    public static List<Project> getProjectData(String status) {
        ObservableList<Project> projectList = FXCollections.observableArrayList();
        try {
            String projQuery = "SELECT project.projectId, project.name,"
                    + " project.category, client.name, project.clientId, project.location,"
                    + " project.status, project.estimatorId, project.description, project.value,"
                    + " project.due, estimator.firstname, estimator.lastname" 
                    + " FROM project, client, estimator"
                    + " WHERE project.clientId = client.clientId"
                    + " AND project.estimatorId = estimator.estimatorId"
                    + " ORDER BY project.projectId";
            PreparedStatement smt = DBConnection.getConn().prepareStatement(projQuery);
            
            ResultSet projectsFound = smt.executeQuery();
            while (projectsFound.next()) {
                Project nProject = new Project();
                String dstatus = projectsFound.getString("project.status");
                if(!status.contentEquals("All")) // if status is NOT all, then filter by status
                    if(!dstatus.equals(status))
                        continue;    
                nProject.setStatus(dstatus);
//                Integer zOs = OffsetDateTime.now().getOffset().getTotalSeconds();
                nProject.setProjectId(projectsFound.getInt("project.projectId"));
                nProject.setClientId(projectsFound.getInt("project.projectId"));
                nProject.setClientName(projectsFound.getString("client.name"));
                nProject.setEstimatorId(projectsFound.getInt("project.estimatorid"));
                nProject.setEstimatorName((projectsFound.getString("estimator.firstname")) + " " + (projectsFound.getString("estimator.lastname")));     
                nProject.setTitle(projectsFound.getString("project.name"));
                nProject.setCategory(projectsFound.getString("project.category")); 
                nProject.setDescription(projectsFound.getString("project.description"));
                nProject.setLocation(projectsFound.getString("project.location"));
                nProject.setValue(projectsFound.getString("project.value"));
                nProject.setDue(projectsFound.getTimestamp("project.due").toLocalDateTime());
               
                // only add projects that are not filtered out to the project list
                projectList.add(nProject);
            }
        } catch (SQLException sqe) {
            System.out.println("Check SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        return projectList;
    }
    
    public static void resetItemSelected() {
        modProjectSelected = null;
    }
    
    public static Project getItemSelected() {
        return modProjectSelected;
    }
    
    public static void setItemSelected(Project a) {
        modProjectSelected = a;
    }
   
}
 