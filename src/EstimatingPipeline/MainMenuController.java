package EstimatingPipeline;

import EstimatingPipeline.model.User;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 *
 * @author Jenny Nguyen
 */
public class MainMenuController {
    
    private EstimatingPipeline mainApp;
    private User currentUser;
    @FXML
    private Button mainReports;
    @FXML
    private Button addUserBtn;
    
    public MainMenuController() {
    }
    
    @FXML
    private Label mainUsernameLbl;
    
    @FXML
    private void showClients(ActionEvent e) throws IOException {
        mainApp.showClients(currentUser);
    }
    
    @FXML
    private void showProjects(ActionEvent e) throws IOException {
        mainApp.showProjects(currentUser);
    }
    
    @FXML
    private void showEstimators(ActionEvent e) throws IOException {
        mainApp.showEstimators(currentUser);
    }
    
    @FXML
    private void showReports(ActionEvent e) throws IOException {
        mainApp.showReports(currentUser);
    }
    
    @FXML
    private void logoutBtnClicked(ActionEvent event) throws Exception {
        mainApp.showLogin();
        
    }

    @FXML
    private void addUserBtnClicked(ActionEvent event) throws Exception {
        mainApp.showUsers(currentUser);
        
    }
    public void setupMenu(EstimatingPipeline mainApp, User activeUser)
    {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        mainUsernameLbl.setText(currentUser.getUsername() + "!");
        if (currentUser.getType().equalsIgnoreCase("limited"))
            mainReports.setDisable(true);
        
        if (!currentUser.getType().equalsIgnoreCase("administrator"))
            addUserBtn.setVisible(false);
    }

 
}
