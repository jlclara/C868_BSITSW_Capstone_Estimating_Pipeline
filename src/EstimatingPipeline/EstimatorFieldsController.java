package EstimatingPipeline;

import EstimatingPipeline.model.Address;
import EstimatingPipeline.model.Estimator;
import EstimatingPipeline.model.User;
import EstimatingPipeline.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class EstimatorFieldsController {

    @FXML
    private TextField firstNameTf;
    @FXML
    private TextField lastNameTf;
    @FXML
    private TextField positionTf;
    @FXML
    private TextField emailAddressTf;
    @FXML
    private TextField phoneTf;
    @FXML
    private TextArea notesTf;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Text errorMsgTxt;
    private EstimatingPipeline mainApp;
    private User currentUser;
    private final  Connection connection = DBConnection.getConn();
    
     private String checkUserInput()
    {
        String firstName = firstNameTf.getText();
        String lastName = lastNameTf.getText();
        String position = positionTf.getText();
        String email = emailAddressTf.getText();
        String phone = phoneTf.getText();
        String errorMsg = "";
        
        if (firstName.length() == 0)
            errorMsg += "Please enter the estimator's first name. \n";
        if (lastName.length() == 0)
            errorMsg += "Please enter the estimator's last name. \n";
        if (position.length() == 0)
            errorMsg += "Please enter the estimator's position. \n";
        if (email.length() == 0)
            errorMsg += "Please enter the estimator's email. \n";
        if (phone.length() == 0)
            errorMsg += "Please enter the estimator's phone number. \n";
        else if (phone.length() < 9 || phone.length() > 15)  
            errorMsg += "Please enter a valid phone number including "
                    + "\n country code and area code. \n";

        return errorMsg;
    }
     
    @FXML
    private void handleEstimatorSaveBtn(ActionEvent e) {
        
        String estimatorInputErrMsg = checkUserInput();
        if (!estimatorInputErrMsg.isEmpty())
            errorMsgTxt.setText(estimatorInputErrMsg);
        else {
            // store user input in variables
            String firstName = firstNameTf.getText();
            String lastName = lastNameTf.getText();
            String position = positionTf.getText();
            String email = emailAddressTf.getText();
            String phone = phoneTf.getText();
            String phoneStripped = phone.replaceAll("[^0-9]", "");
            if (phoneStripped.startsWith("1"))
                phoneStripped = phoneStripped.substring(1);
            String phoneFormatted = "(" + phoneStripped.substring(0,3) + ") " + 
                    phoneStripped.substring(3,6) + "-" + phoneStripped.substring(6,10);
            String notes = notesTf.getText();

            String addressQuery = null;
            Address currentAddress = null;
       
            if (EstimatorsController.getModEstimator()== null){ // ADDING ESTIMATOR, NOT EDITING
            try {           
                String query = "INSERT INTO estimator ("              
                + " firstName,"
                + " lastName,"
                + " position,"
                + " email,"
                + " phone,"
                + " notes) VALUES ("
                + "?, ?, ?, ?, ?, ?)";            
                PreparedStatement st = connection.prepareStatement(query);
                st.setString(1, firstName);
                st.setString(2, lastName);  
                st.setString(3, position); 
                st.setString(4, email);
                st.setString(5, phoneFormatted);
                st.setString(6, notes);
                st.executeUpdate();
                st.close();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                }
        } // end of adding estimator  
               
        // EDITING Estimator 
        else if (EstimatorsController.getModEstimator()!= null) 
        {
            try {
                String updateQuery = "UPDATE estimator"
                + " SET estimator.firstname = ?,"
                + " estimator.lastname = ?,"
                + " estimator.position = ?,"
                + " estimator.email = ?,"
                + " estimator.phone = ?,"
                + " estimator.notes = ?"
                + " WHERE estimator.estimatorId = ?";
                
                PreparedStatement st = connection.prepareStatement(updateQuery);
                st.setString(1, firstName);
                st.setString(2, lastName);  
                st.setString(3, position); 
                st.setString(4, email);
                st.setString(5, phoneFormatted);
                st.setString(6, notes);
                st.setInt(7, EstimatorsController.getModEstimator().getId());
                
                st.executeUpdate();
                st.close();
                
             } catch (SQLException ex) {
                ex.printStackTrace();
                }
            }
            EstimatorsController.resetModEstimator();
            mainApp.closePopup(currentUser);  
        }
    }

    public void setUpEstimatorFields(EstimatingPipeline mainApp, User activeUser){
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
            saveBtn.setDisable(true);
        
        // load estimator data if edit is selected from the estimators scene
        Estimator modEstimator = EstimatorsController.getModEstimator();
        if (modEstimator != null)
        {
            String phone = modEstimator.getPhone();
            String phoneStripped = phone.replaceAll("[^0-9]", "");
            if (phoneStripped.startsWith("1"))
                phoneStripped = phoneStripped.substring(1);
            String phoneFormatted = "(" + phoneStripped.substring(0,3) + ") " 
                    + phoneStripped.substring(3,6) + "-" + phoneStripped.substring(6,10);
            firstNameTf.setText(modEstimator.getFirstName());
            lastNameTf.setText(modEstimator.getLastName());
            positionTf.setText(modEstimator.getPosition());
            emailAddressTf.setText(modEstimator.getEmail());
            phoneTf.setText(phoneFormatted);
            notesTf.setText(modEstimator.getNotes());
        }
    }
    
    @FXML
    private void handleEstimatorCancelBtn(ActionEvent e) {
        Alert cancelAlert = new Alert (Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Estimators?");
        cancelAlert.setHeaderText("Are you sure you want to cancel "
                + "and return to the estimators screen?");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            EstimatorsController.resetModEstimator();
            mainApp.closePopup(currentUser);

        }
    }

    
}
