package EstimatingPipeline;

import EstimatingPipeline.model.User;
import EstimatingPipeline.util.DBConnection;
import static EstimatingPipeline.util.UserAuthentication.generateSalt;
import static EstimatingPipeline.util.UserAuthentication.getEncryptedPassword;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class UserFieldsController {

    @FXML
    private TextField usernameTf;
    @FXML
    private ChoiceBox<String> typeCb;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Text errorMsgTxt;
    private EstimatingPipeline mainApp;
    private User currentUser;
    private final  Connection connection = DBConnection.getConn();
    @FXML
    private CheckBox activeCxBx;

 
    
     private String checkUserInput()
    {
        String username = usernameTf.getText();
        String type = typeCb.getSelectionModel().getSelectedItem();
        String password = passwordField.getText();
      
        String errorMsg = "";
        
        if (username.length() == 0)
            errorMsg += "Please enter a username. \n";
        if (type.length() == 0)
            errorMsg += "Please select a user type. \n";
        if (password.length() == 0 && UsersController.getModUser()== null)
            errorMsg += "Please enter a password for the user. \n";
       
        return errorMsg;
    }
     
    @FXML
    private void handleUserSaveBtn(ActionEvent e) throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        String userInputErrMsg = checkUserInput();
        if (!userInputErrMsg.isEmpty())
            errorMsgTxt.setText(userInputErrMsg);
        else {
            // store user input in variables
            String username = usernameTf.getText();
            String type = typeCb.getSelectionModel().getSelectedItem();
            String password = passwordField.getText();
            Integer active = 0;
            if (activeCxBx.isSelected())
                active = 1;
            
            if (UsersController.getModUser()== null){ // ADDING USER, NOT EDITING
            try {           
                byte[] salt = generateSalt();
                byte[] ePassword = getEncryptedPassword(password, salt);
                String query = "INSERT INTO user ("
                    + "userName,"
                    + " password,"
                    + " type, salt, active) VALUES ("
                    + "?, ?, ?, ?, ?)";            
                    PreparedStatement st = connection.prepareStatement(query);
                    st.setString(1, username);
                    st.setBytes(2, ePassword);
                    st.setString(3, type); 
                    st.setBytes(4, salt);
                    st.setInt(5, active);
                    st.executeUpdate();
                    st.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                }
            } // end of adding user  
               
             // EDITING User 
            else if (UsersController.getModUser()!= null) 
            {
                try {

                    String updateQuery = "UPDATE user"
                    + " SET userName = ?,";
                    if (!"***********".equals(passwordField.getText()))
                    {
                        updateQuery = updateQuery.concat(" password = ?, salt = ?,");
                    }
                    updateQuery += " type = ?,"
                    + " active = ?"
                    + " WHERE user.userId = ?";

                    int i = 1;
                                        
                    PreparedStatement st = connection.prepareStatement(updateQuery);
                    st.setString(i++, username);
                    if (!"***********".equals(passwordField.getText()))
                    {
                        byte[] salt = generateSalt();
                        byte[] ePassword = getEncryptedPassword(password, salt);
                        st.setBytes(i++, ePassword);  
                        st.setBytes(i++, salt);
                    }
                    st.setString(i++, type); 
                    st.setInt(i++, active);
                    st.setInt(i++, UsersController.getModUser().getUserID());

                    st.executeUpdate();
                    st.close();

                 } catch (SQLException ex) {
                    ex.printStackTrace();
                    }
                }
            UsersController.resetModUser();
            mainApp.closePopup(currentUser);  
        }
    }

    public void setUpUserFields(EstimatingPipeline mainApp, User activeUser){
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        typeCb.getItems().addAll("administrator", "standard", "limited");
        
        // load user data if edit is selected from the users scene
        User modUser = UsersController.getModUser();
        if (modUser != null)
        {
            usernameTf.setText(modUser.getUsername());
            passwordField.setText("***********"); // placeholder ***********
            typeCb.getSelectionModel().select(modUser.getType());
            if(modUser.getActive() == 1)
                activeCxBx.setSelected(true);
        }
    }
    
    @FXML
    private void handleUserCancelBtn(ActionEvent e) {
        Alert cancelAlert = new Alert (Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Users?");
        cancelAlert.setHeaderText("Are you sure you want to cancel "
                + "and return to the users screen?");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            UsersController.resetModUser();
            mainApp.closePopup(currentUser);

        }
    }

    
}

     