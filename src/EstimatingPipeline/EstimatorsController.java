package EstimatingPipeline;

import static EstimatingPipeline.EstimatorsController.resetModEstimator;
import static EstimatingPipeline.EstimatorsController.setModEstimator;
import EstimatingPipeline.model.Estimator;
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
public class EstimatorsController {

    @FXML
    private TableView<Estimator> estimatorTableView;
    @FXML
    private TableColumn<Estimator, String> firstNameCol;
    @FXML
    private TableColumn<Estimator, String> lastNameCol;
    @FXML
    private TableColumn<Estimator, String> positionCol;
    @FXML
    private TableColumn<Estimator, String> emailCol;
    @FXML
    private TableColumn<Estimator, String> phoneCol;
    @FXML
    private Button estimatorNewBtn;
    @FXML
    private Button estimatorEditBtn;
    @FXML
    private Button estimatorDeleteBtn;
    
    private EstimatingPipeline mainApp;
    private User currentUser;
    private static Estimator modEstimatorSelected;


    // populates table with estimator data
    public void setupEstimators(EstimatingPipeline mainApp, User activeUser) {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
        {
            estimatorNewBtn.setDisable(true);
            estimatorDeleteBtn.setDisable(true);
            estimatorEditBtn.setText("VIEW");
        }
     
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        
        //adding on double click event. Opens the estimator field editor.
        estimatorTableView.setRowFactory(ttv -> {
            TableRow<Estimator> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setModEstimator(estimatorTableView.getSelectionModel().getSelectedItem());
                    mainApp.showEstimatorFields(currentUser);   // need to change this!
                }
            });
            return row;
        });
        
        estimatorTableView.getItems().setAll(getEstimatorData());
    }

     public List<Estimator> getEstimatorData(){
        ObservableList<Estimator> estimatorList = FXCollections.observableArrayList();
        try {
            String estimatorQuery = "SELECT estimator.estimatorId, estimator.firstname, "
                + "estimator.lastname, estimator.position, estimator.email, estimator.phone, estimator.notes " 
                + "FROM estimator " 
                + "ORDER BY estimator.estimatorId";
        
            PreparedStatement smt = DBConnection.getConn().prepareStatement(estimatorQuery);
            ResultSet estimatorsFound = smt.executeQuery();

            while (estimatorsFound.next()) {
                Estimator nEstimator = new Estimator();
                nEstimator.setId(estimatorsFound.getInt("estimator.estimatorId"));
                nEstimator.setFirstName(estimatorsFound.getString("estimator.firstname"));
                nEstimator.setLastName(estimatorsFound.getString("estimator.lastname"));
                nEstimator.setPosition(estimatorsFound.getString("estimator.position"));
                nEstimator.setEmail(estimatorsFound.getString("estimator.email"));
                nEstimator.setPhone(estimatorsFound.getString("estimator.phone"));
                nEstimator.setNotes(estimatorsFound.getString("estimator.notes"));
                estimatorList.add(nEstimator);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return estimatorList;
    }
    @FXML
    private void handleNewEstimator(ActionEvent event) {
        resetModEstimator();
        mainApp.showEstimatorFields(currentUser);
    }

    @FXML
    private void handleEditEstimator(ActionEvent event) {
        // if there is an item selected in the TableView, then open EstimatorFields.fxml
        modEstimatorSelected = estimatorTableView.getSelectionModel().getSelectedItem();
        
        if (modEstimatorSelected == null) {
            Alert noEstimatorAlert = new Alert (Alert.AlertType.WARNING);
            noEstimatorAlert.setTitle("No Estimators Selected");
            noEstimatorAlert.setHeaderText("Error: No Estimators Selected");
            noEstimatorAlert.setContentText("Please select a estimator and try again.");
            noEstimatorAlert.showAndWait();
        }
        else 
          mainApp.showEstimatorFields(currentUser);
    }

    @FXML
    private void handleDeleteEstimator(ActionEvent event) {
        
        // if there is an item selected in the TableView, then delete estimator
        // note: cannot delete estimators assigned to a project
        modEstimatorSelected = estimatorTableView.getSelectionModel().getSelectedItem();

        if (modEstimatorSelected == null) {
            Alert noEstimatorAlert = new Alert (Alert.AlertType.WARNING);
            noEstimatorAlert.setTitle("No Estimators Selected");
            noEstimatorAlert.setHeaderText("Error: No Estimators Selected");
            noEstimatorAlert.setContentText("Please select a clinet and try again.");
            noEstimatorAlert.showAndWait();
        }
        else {
            
            Alert cannotDelete = null;
             try {
                String deleteQuery = "SELECT *"
                        + " FROM project"
                        + " WHERE project.estimatorId = ?";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, modEstimatorSelected.getId());
                ResultSet estimatorFound = deleteSmt.executeQuery();
                if (estimatorFound.next()) {
                    cannotDelete = new Alert (Alert.AlertType.ERROR);
                    cannotDelete.setTitle("Cannot Delete Estimator");
                    cannotDelete.setHeaderText("This estimator is associated with a project (" 
                            + estimatorFound.getString("project.name")+ ") and cannot be deleted.");
                    cannotDelete.show();
                }
            } catch (SQLException exc) {
                exc.printStackTrace();;
            }
            if (cannotDelete == null)
            {
                Alert deleteEstimatorAlert = new Alert (Alert.AlertType.CONFIRMATION);
                deleteEstimatorAlert.setTitle("Confirm Estimator Deletion");
                deleteEstimatorAlert.setHeaderText("Are you sure you want to delete " + modEstimatorSelected.getFirstName() + "?");
                deleteEstimatorAlert.setContentText("This cannot be undone!");
                Optional<ButtonType> result = deleteEstimatorAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                    String deleteQuery = "DELETE"
                            + " FROM estimator"
                            + " WHERE estimator.estimatorId = ?";
                    PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                    deleteSmt.setInt(1, modEstimatorSelected.getId());
                    deleteSmt.executeUpdate();
                   } catch (SQLException exc) {
                       exc.printStackTrace();;
                   }
                   // reload the estimators list
                   setupEstimators(mainApp, currentUser);
                }
            }
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        mainApp.showMainMenu(currentUser);
    }
    
    public static void setModEstimator(Estimator c) {
        modEstimatorSelected = c;
    }
     
    public static Estimator getModEstimator() {
        return modEstimatorSelected;
    }
    public static void resetModEstimator() {
        modEstimatorSelected = null;
    }
    
}
