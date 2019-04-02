package EstimatingPipeline;

import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.User;
import java.io.IOException;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author Jenny Nguyen
 */
public class EstimatingPipeline extends Application {
    
    private static Connection connection;
    private Stage primaryStage;
    private Stage popStage = new Stage();
    private Scene scene, mscene, cuscene, pscene, rscene, escene, uscene;

    
    @Override
    public void start(Stage primaryStage) throws Exception {        
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Estimating Pipeline");
        primaryStage.getIcons().add(new Image(EstimatingPipeline.class.getResourceAsStream("/resources/projects.png")));
        popStage.getIcons().add(new Image(EstimatingPipeline.class.getResourceAsStream("/resources/projects.png")));

        showLogin();   
    }
   
    
    
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Login.fxml"));
            Pane loginPane = (Pane) loader.load();
            LoginController lController = loader.getController();
            lController.setupLogin(this);
            scene = new Scene(loginPane);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // need to condense this -- could pass in string for resource and controller?
    public void showMainMenu (User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/MainMenu.fxml"));
            Pane layout = (Pane) loader.load();
            mscene = new Scene(layout);
            primaryStage.setScene(mscene);
            MainMenuController controller = loader.getController();
            controller.setupMenu(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showClients (User activeUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Clients.fxml"));
            Pane customersLayout = (Pane) loader.load();
            cuscene = new Scene(customersLayout);
            primaryStage.setScene(cuscene);
            ClientsController controller = loader.getController();
            controller.setupClients(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
        public void showEstimators (User activeUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Estimators.fxml"));
            Pane estimatorLayout = (Pane) loader.load();
            escene = new Scene(estimatorLayout);
            primaryStage.setScene(escene);
            EstimatorsController controller = loader.getController();
            controller.setupEstimators(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showProjects (User activeUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Projects.fxml"));
            Pane projLayout = (Pane)loader.load();
            pscene = new Scene(projLayout);
            primaryStage.setScene(pscene);
            ProjectsController controller = loader.getController();
            controller.setupProjects(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showReports(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Reports.fxml"));
            Pane reportsLayout = (Pane) loader.load();
            rscene = new Scene(reportsLayout);
            primaryStage.setScene(rscene);
            ReportsController controller = loader.getController();
            controller.setUpReports(this, activeUser);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
     public void showUsers(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Users.fxml"));
            Pane projectFLayout = (Pane) loader.load();
            uscene = new Scene(projectFLayout);
            primaryStage.setScene(uscene);
            UsersController controller = loader.getController();
            controller.setupUsers(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    public void showClientFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/ClientFields.fxml"));
            Pane customerFLayout = (Pane) loader.load();
            scene = new Scene(customerFLayout);
            popStage.setScene(scene);     
            ClientFieldsController controller = loader.getController();
            controller.setUpClientFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    public void showEstimatorFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/EstimatorFields.fxml"));
            Pane estimatorFLayout = (Pane) loader.load();
            scene = new Scene(estimatorFLayout);
            popStage.setScene(scene);     
            EstimatorFieldsController controller = loader.getController();
            controller.setUpEstimatorFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    public void showProjectFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/ProjectFields.fxml"));
            Pane projectFLayout = (Pane) loader.load();
            scene = new Scene(projectFLayout);
            popStage.setScene(scene);
            ProjectFieldsController controller = loader.getController();
            controller.setUpProjectFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
     public void showUserFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/UserFields.fxml"));
            Pane projectFLayout = (Pane) loader.load();
            scene = new Scene(projectFLayout);
            popStage.setScene(scene);
            UserFieldsController controller = loader.getController();
            controller.setUpUserFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
     
    public void closePopup(User currentUser) {
        popStage.close();
        
        // following code is used to refresh the active scene contents
        Scene activeScene = primaryStage.getScene();
        if (activeScene == pscene)
            showProjects(currentUser);
        else if (activeScene == rscene)
            showReports(currentUser);
        else if (activeScene == cuscene)
            showClients(currentUser);
        else if (activeScene == escene)
            showEstimators(currentUser);
        else if (activeScene == uscene)
            showUsers(currentUser);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
        DBConnection.init();
        connection = DBConnection.getConn();

        launch(args);
        DBConnection.closeConn();
    }
    
}
