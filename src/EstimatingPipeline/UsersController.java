package EstimatingPipeline;

import static EstimatingPipeline.UsersController.resetModUser;
import static EstimatingPipeline.UsersController.setModUser;
import EstimatingPipeline.model.User;
import EstimatingPipeline.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class UsersController {

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> usernameCol;
    @FXML
    private TableColumn<User, String> typeCol;
    @FXML
    private Button userNewBtn;
    @FXML
    private Button userEditBtn;
    @FXML
    private Button userDeleteBtn;
    
    private EstimatingPipeline mainApp;
    private User currentUser;
    private static User modUserSelected;
   
    @FXML
    private Button backButton;


    // populates table with user data
    public void setupUsers(EstimatingPipeline mainApp, User activeUser) {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
             
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
       
        //adding on double click event. Opens the user field editor.
        userTableView.setRowFactory(ttv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setModUser(userTableView.getSelectionModel().getSelectedItem());
                    
                    mainApp.showUserFields(currentUser);   // need to change this!
                }
            });
            return row;
        });
        
        userTableView.getItems().setAll(getUserData());
    }

     public List<User> getUserData(){
        ObservableList<User> userList = FXCollections.observableArrayList();
        try {
            String userQuery = "SELECT * " 
                + "FROM user "
                + "ORDER BY user.userId";
        
            PreparedStatement smt = DBConnection.getConn().prepareStatement(userQuery);
            ResultSet usersFound = smt.executeQuery();

            while (usersFound.next()) {
                User nUser = new User();
                nUser.setUserID(usersFound.getInt("user.userId"));
                nUser.setUsername(usersFound.getString("user.userName"));
                nUser.setType(usersFound.getString("user.type"));
                nUser.setActive(usersFound.getInt("user.active"));
                nUser.setPassword(usersFound.getString("user.password"));
                userList.add(nUser);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }
    @FXML
    private void handleNewUser(ActionEvent event) {
        resetModUser();
        mainApp.showUserFields(currentUser);
    }

    @FXML
    private void handleEditUser(ActionEvent event) {
        // if there is an item selected in the TableView, then open UserFields.fxml
        modUserSelected = userTableView.getSelectionModel().getSelectedItem();
        
        if (modUserSelected == null) {
            Alert noUserAlert = new Alert (Alert.AlertType.WARNING);
            noUserAlert.setTitle("No Users Selected");
            noUserAlert.setHeaderText("Error: No Users Selected");
            noUserAlert.setContentText("Please select a user and try again.");
            noUserAlert.showAndWait();
        }
        else 
          mainApp.showUserFields(currentUser);
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        
        // if there is an item selected in the TableView, then delete user
        // note: cannot delete users assigned to a project
        modUserSelected = userTableView.getSelectionModel().getSelectedItem();

        if (modUserSelected == null) {
            Alert noUserAlert = new Alert (Alert.AlertType.WARNING);
            noUserAlert.setTitle("No Users Selected");
            noUserAlert.setHeaderText("Error: No Users Selected");
            noUserAlert.setContentText("Please select a user and try again.");
            noUserAlert.showAndWait();
        }
        else {
            Alert cannotDelete = null;
            if (modUserSelected.getUserID() == (currentUser.getUserID()))
            {
                    cannotDelete = new Alert (Alert.AlertType.ERROR);
                    cannotDelete.setTitle("Cannot Delete Your Own Account");
                    cannotDelete.setHeaderText("You cannot delete your own user account. "
                            + " \nThis account can only be deleted from another administrator account.");
                    cannotDelete.show();
            }
            else if (cannotDelete == null) {
            
                try {
                   String deleteQuery = "SELECT *"
                           + " FROM project"
                           + " WHERE project.createdby = ?";
                   PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                   deleteSmt.setInt(1, modUserSelected.getUserID());
                   ResultSet userFound = deleteSmt.executeQuery();
                   if (userFound.next()) {
                       cannotDelete = new Alert (Alert.AlertType.ERROR);
                       cannotDelete.setTitle("Cannot Delete User");
                       cannotDelete.setHeaderText("This user created a project (" 
                               + userFound.getString("project.name")+ ") and cannot be deleted. "
                                       + "\n You may limit their access instead by changing the type of user and password.");
                       cannotDelete.show();
                   }
               } catch (SQLException exc) {
                   exc.printStackTrace();
               }
            }
            if (cannotDelete == null) 
            {
                Alert deleteUserAlert = new Alert (Alert.AlertType.CONFIRMATION);
                deleteUserAlert.setTitle("Confirm User Deletion");
                deleteUserAlert.setHeaderText("Are you sure you want to delete " + modUserSelected.getUsername()+ "?");
                deleteUserAlert.setContentText("This cannot be undone! You can opt to deactivate the user instead.");
                Optional<ButtonType> result = deleteUserAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {

                    String deleteQuery = "DELETE FROM user "
                            + "WHERE user.userId = ?";
                        
                    PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                    deleteSmt.setInt(1, modUserSelected.getUserID());
                    deleteSmt.executeUpdate();
                   } catch (SQLException exc) {
                       exc.printStackTrace();;
                   }
                   // reload the users list
                   setupUsers(mainApp, currentUser);
                }
            }
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        mainApp.showMainMenu(currentUser);
    }
    
    public static void setModUser(User c) {
        modUserSelected = c;
    }
     
    public static User getModUser() {
        return modUserSelected;
    }
    public static void resetModUser() {
        modUserSelected = null;
    }
    
}
