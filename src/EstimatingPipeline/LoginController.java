package EstimatingPipeline;

import EstimatingPipeline.util.UserAuthentication;
import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.User;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class LoginController {

    @FXML
    private TextField usernameTf;
    @FXML
    private PasswordField passwordPf;
    @FXML
    private Label errorMsgLbl;
    @FXML
    private Button loginBtn;
    @FXML
    private Label usernameLbl;
    @FXML
    private Label passwordLbl;
    
    private EstimatingPipeline mainApp;
    private User user = new User();
    
    // set up for Locale, DB connection
    ResourceBundle rb = ResourceBundle.getBundle("resources/login", Locale.getDefault());
    
    public LoginController () {
    }
    
    @FXML
    private void loginBtnClicked (ActionEvent e) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String username = usernameTf.getText();
        String password = passwordPf.getText();
        if (username.length()==0 || password.length()==0)
            errorMsgLbl.setText(rb.getString("empty"));
        else {
            User user = checkUserCreds(username,password);
            if (user == null) {
                errorMsgLbl.setText(rb.getString("incorrect"));
                return;
            }
            mainApp.showMainMenu(user);
        }
    }


    private User checkUserCreds(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = null;
        byte[] ePassword = null;
        try {
            PreparedStatement pst = DBConnection.getConn().prepareStatement
                ("SELECT * FROM user WHERE UPPER(userName) =? AND active = ?");
            pst.setString(1, username.toUpperCase());
            pst.setInt(2, 1);
            ResultSet rs = pst.executeQuery();
            if (rs.next()){
                user.setUsername(rs.getString("userName"));
                ePassword = rs.getBytes("password");
                user.setUserID(rs.getInt("userId"));
                user.setType(rs.getString("type"));
                salt = rs.getBytes("salt");
              }
            else
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                errorMsgLbl.setText("something went wrong");
            }
         if (!UserAuthentication.authenticate(password, ePassword, salt))
             return null;
         
         return user;
    }

    public void setupLogin(EstimatingPipeline mainApp) {
        this.mainApp = mainApp;
        // set text based on locale
        usernameLbl.setText(rb.getString("username"));
        passwordLbl.setText(rb.getString("password"));
        loginBtn.setText(rb.getString("signin"));
        
        
    }

  }

