package EstimatingPipeline;

import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.Project;
import EstimatingPipeline.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class ProjectFieldsController {

    @FXML
    private Label errorMsgLbl;
    @FXML
    private TextField projIdTf;
    @FXML
    private ChoiceBox<String> clientNameCb;
    @FXML
    private ChoiceBox<String> estimatorNameCb;
    @FXML
    private TextField projTitleTf, projLocationTf, projValueTf;
    @FXML
    private ChoiceBox<String> categoryCb;
    @FXML
    private ChoiceBox<String> statusCb;
    @FXML
    private TextArea projDescriptionTa;
    @FXML
    private Button projSaveBtn;
    @FXML
    private Button projCancelBtn;
    @FXML
    private DatePicker dueDtp;

    private final Connection connection = DBConnection.getConn();
    private User currentUser;
    private EstimatingPipeline mainApp;
    private Project selectedProj;

   private String checkUserInput() 
   {
        LocalDate dueDate = null;
        if (dueDtp.getValue()!=null)
            dueDate = dueDtp.getValue();

        String errorMsg = "";
        
        if (clientNameCb.getSelectionModel().isEmpty())
            errorMsg += "Please choose a client. \n";
        if (estimatorNameCb.getSelectionModel().isEmpty())
            errorMsg += "Please choose an estimator. \n";
        if (projTitleTf.getText().length() == 0)
            errorMsg += "Please enter a project title. \n";
        if (dueDate == null)
            errorMsg += "Please enter a bid due date. \n";
        if (categoryCb.getSelectionModel().isEmpty())
            errorMsg += "Please enter the project's category. \n";
        if (statusCb.getSelectionModel().isEmpty())
            errorMsg += "Please enter the project's status. \n";
        if (projLocationTf.getText().length() == 0)
            errorMsg += "Please enter the project's location. \n";
        return errorMsg;
   }
    
    @FXML 
    private void handleProjSave (ActionEvent e)
    {
        // test if fields are filled out
        String clientInputErrMsg = checkUserInput();
        
        if (!clientInputErrMsg.isEmpty())
            errorMsgLbl.setText(clientInputErrMsg);
        else {
            Integer clientId = null;
            Integer estimatorId = null;
            String clientName = clientNameCb.getSelectionModel().getSelectedItem();
            String estimatorName = estimatorNameCb.getSelectionModel().getSelectedItem();
            String projTitle = projTitleTf.getText();
            String category = categoryCb.getSelectionModel().getSelectedItem();
            String status = statusCb.getSelectionModel().getSelectedItem();
            String description = projDescriptionTa.getText();
            String location = projLocationTf.getText();
            String value = projValueTf.getText();

            LocalDate dueDate = null;
            if (dueDtp.getValue()!=null)
                dueDate = dueDtp.getValue();
        
       
            LocalDateTime dueLdt = LocalDateTime.of(dueDate.getYear(), 
                dueDate.getMonthValue(), dueDate.getDayOfMonth(), 0, 0); 

            try {
                // retrieve client ID using client name
                PreparedStatement custIdSmt = connection.prepareStatement("SELECT client.clientId"
                        + " FROM client"
                        + " WHERE client.name = ?");
                custIdSmt.setString(1, clientName);
                ResultSet custIdRs = custIdSmt.executeQuery();
                if (custIdRs.next())
                        clientId = custIdRs.getInt("client.clientId");

                // retrieve estimator ID using name
                PreparedStatement estimatorIdSmt = connection.prepareStatement("SELECT estimator.estimatorId"
                        + " FROM estimator"
                        + " WHERE estimator.firstName = ?");
                String [] estimatorNames = estimatorName.split(" ");
                estimatorIdSmt.setString(1, estimatorNames[0]);
                ResultSet EstimatorIdRs = estimatorIdSmt.executeQuery();
                if (EstimatorIdRs.next())
                        estimatorId = EstimatorIdRs.getInt("estimator.estimatorId");
                    
                selectedProj = ProjectsController.getItemSelected();
                String projQuery = null;
                if (selectedProj == null)
                {
                    projQuery = "INSERT INTO project ("
                            + " name,"
                            + " clientId,"
                            + " estimatorId," 
                            + " due,"  
                            + " status,"
                            + " category,"
                            + " location,"
                            + " value,"
                            + " description,"
                            + " createdDate,"
                            + " createdBy,"
                            + " modifiedDate,"
                            + " modifiedBy) VALUES ("
                            + " ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
                }
                else
                {
                    projQuery = "UPDATE project "
                            + " SET name = ?,"
                            + " clientId = ?,"
                            + " estimatorId = ?,"
                            + " due = ?,"
                            + " status = ?,"
                            + " category = ?,"
                            + " location = ?,"
                            + " value = ?,"
                            + " description = ?, "
                            + " modifiedDate = CURRENT_TIMESTAMP,"
                            + " modifiedBy = ?"
                            + " WHERE projectId = ?";
                }
                
                PreparedStatement st = connection.prepareStatement(projQuery);
                int i = 1;
                st.setString(i++, projTitle);
                st.setInt(i++, clientId);
                st.setInt(i++, estimatorId);
                st.setTimestamp(i++, Timestamp.valueOf(dueLdt));
                st.setString(i++, status);
                st.setString(i++, category);
                st.setString(i++, location);
                st.setString(i++, value);
                st.setString(i++, description);
                st.setInt(i++, currentUser.getUserID());
                //extra value for updating project (project id)
                if (selectedProj != null)
                    st.setInt(i++, selectedProj.getProjectId());
                else 
                    st.setInt(i++, currentUser.getUserID());

                st.executeUpdate();
                st.close();

                mainApp.closePopup(currentUser);

            } catch (SQLException sqe) {
                sqe.printStackTrace();
            }
        }
    }
    
    public void setUpProjectFields(EstimatingPipeline scheduler, User activeUser){
        this.mainApp = scheduler;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
            projSaveBtn.setDisable(true);
        
        categoryCb.getItems().addAll("Entertaiment", "Private Education", "Medical Office Building",
                "Industrial", "Commercial", "Food & Beverage", "Hospitality", "Other");
        statusCb.getItems().addAll("Active", "Pending", "Awarded", "Not Awarded");
        
        // populate Clients ChoiceBox
        try {
            String clientQuery = "SELECT client.name"
                + " FROM client" 
                + " ORDER BY client.name";
            PreparedStatement smt = connection.prepareStatement(clientQuery);
            ResultSet clientsFound = smt.executeQuery();
            while (clientsFound.next()) {
                String dClientName = clientsFound.getString("client.name");
                clientNameCb.getItems().add(dClientName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } 
        
        // populate Estimators ChoiceBox
        try {
            String userQuery = "SELECT estimator.firstname, estimator.lastname"
                + " FROM estimator";
        
            PreparedStatement smt = connection.prepareStatement(userQuery);
            ResultSet usersFound = smt.executeQuery();

            while (usersFound.next()) {
                String estimatorNames = usersFound.getString("estimator.firstname")
                        + " " + usersFound.getString("estimator.lastname");
                estimatorNameCb.getItems().add(estimatorNames);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }
        
        // populate fields when editing appoinments with existing data
        Project modProj = ProjectsController.getItemSelected();
        if (modProj != null)
        {
            projIdTf.setText(modProj.getProjectId().toString());
            clientNameCb.getSelectionModel().select(modProj.getClientName());
            estimatorNameCb.getSelectionModel().select(modProj.getEstimatorName());
            projTitleTf.setText(modProj.getTitle());
            dueDtp.setValue(modProj.getDue().toLocalDate());
            categoryCb.getSelectionModel().select(modProj.getCategory());
            statusCb.getSelectionModel().select(modProj.getStatus());
            projLocationTf.setText(modProj.getLocation());
            projValueTf.setText(modProj.getValue());
            projDescriptionTa.setText(modProj.getDescription());
        }
    }
    
    @FXML
    private void handleProjCancel (ActionEvent e)
    {
        Alert cancelAlert = new Alert (Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Projects?");
        cancelAlert.setHeaderText("Are you sure you want to cancel? "
                + "Any unsaved progress will be lost.");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            ProjectsController.resetItemSelected();
            mainApp.closePopup(currentUser);
        }
    }
 
}
