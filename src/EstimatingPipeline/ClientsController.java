package EstimatingPipeline;

import EstimatingPipeline.model.Address;
import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.User;
import EstimatingPipeline.model.Client;
import java.io.IOException;
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
public class ClientsController {

    @FXML
    private TableView<Client> clientTableView;
    @FXML
    private TableColumn<Client, String> nameCol;
    @FXML
    private Button clientNewBtn;
    @FXML
    private Button clientEditBtn;
    @FXML
    private Button clientDeleteBtn;

    private EstimatingPipeline mainApp;
    private User currentUser;
    private static Client modClientSelected;
    
    
    public ClientsController() {
    }
    
    @FXML
    private void handleNewClient(ActionEvent e) throws IOException {
        resetModClient();
        mainApp.showClientFields(currentUser);
    }
    
    @FXML 
    private void handleEditClient(ActionEvent e) throws IOException {
        // if there is an item selected in the TableView, then open ClientFields.fxml
        modClientSelected = clientTableView.getSelectionModel().getSelectedItem();
        
        if (modClientSelected == null) {
            Alert noClientAlert = new Alert (Alert.AlertType.WARNING);
            noClientAlert.setTitle("No Clients Selected");
            noClientAlert.setHeaderText("Error: No Clients Selected");
            noClientAlert.setContentText("Please select a client and try again.");
            noClientAlert.showAndWait();
        }
        else 
          mainApp.showClientFields(currentUser);
    }
    
    @FXML
    private void handleDeleteClient(ActionEvent e) {
        
        // if there is an item selected in the TableView, then delete client
        // note: client cannot be deleted if they are listed as a client on a project
        modClientSelected = clientTableView.getSelectionModel().getSelectedItem();

        if (modClientSelected == null) {
            Alert noClientAlert = new Alert (Alert.AlertType.WARNING);
            noClientAlert.setTitle("No Clients Selected");
            noClientAlert.setHeaderText("Error: No Clients Selected");
            noClientAlert.setContentText("Please select a clinet and try again.");
            noClientAlert.showAndWait();
        }
        else {
            
            Alert cannotDelete = null;
             try {
                String deleteQuery = "SELECT *"
                        + " FROM project"
                        + " WHERE project.clientId = ?";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, modClientSelected.getId());
                ResultSet clientsFound = deleteSmt.executeQuery();
                if (clientsFound.next()) {
                    cannotDelete = new Alert (Alert.AlertType.ERROR);
                    cannotDelete.setTitle("Cannot Delete Client");
                    cannotDelete.setHeaderText("This client is associated with a project (" 
                            + clientsFound.getString("project.name")+ ") and cannot be deleted.");
                    cannotDelete.show();
                }
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
            if (cannotDelete == null)
            {
                Alert deleteClientAlert = new Alert (Alert.AlertType.CONFIRMATION);
                deleteClientAlert.setTitle("Confirm Client Deletion");
                deleteClientAlert.setHeaderText("Are you sure you want to delete " + modClientSelected.getName() + "?");
                deleteClientAlert.setContentText("This cannot be undone!");
                Optional<ButtonType> result = deleteClientAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        
                    
                    String deleteQuery = "SELECT addressId"
                        + " FROM client"
                        + " WHERE clientId = ?";
                    Integer foundAddressId;
                    PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                    deleteSmt.setInt(1, modClientSelected.getId());
                    ResultSet clientsFound = deleteSmt.executeQuery();  
                    while (clientsFound.next()) {
                        foundAddressId = clientsFound.getInt("addressId");
                        String deleteAddressSmt = "DELETE "
                            + " FROM address"
                            + " WHERE address.addressId = ?";
                        PreparedStatement AddressSmt = DBConnection.getConn().prepareStatement(deleteAddressSmt);
                        AddressSmt.setInt(1, foundAddressId);
                        AddressSmt.executeUpdate();
                    }
                        
                    String deleteClientSmt = "DELETE FROM client "
                            + "WHERE client.clientId = ?";
                   
                    PreparedStatement Smt = DBConnection.getConn().prepareStatement(deleteClientSmt);
                    Smt.setInt(1, modClientSelected.getId());
                    Smt.executeUpdate();
                    
                   } catch (SQLException exc) {
                       exc.printStackTrace();;
                   }
                   // reload the clients list
                   setupClients(mainApp, currentUser);
                }
            }
        }
    }
    
    @FXML 
    private void handleBackButton (ActionEvent e) {
        mainApp.showMainMenu(currentUser);
    }

    // populates table with client data
    public void setupClients(EstimatingPipeline mainApp, User activeUser) {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
        {
            clientNewBtn.setDisable(true);
            clientDeleteBtn.setDisable(true);
            clientEditBtn.setText("VIEW");
        }
        
        // set up table columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        //adding on double click event. Opens the client field editor.
        clientTableView.setRowFactory(ttv -> {
            TableRow<Client> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setModClient(clientTableView.getSelectionModel().getSelectedItem());
                    mainApp.showClientFields(currentUser);
                }
            });
            return row;
        });
        
        // load table with existing client data
        clientTableView.getItems().setAll(getClientData());
    }
    
    private List<Client> getClientData(){
        ObservableList<Client> clientList = FXCollections.observableArrayList();
        try {
            String clientQuery = "SELECT client.clientId, client.name, "
//                + "address.address, address.address2, address.city, address.state, "
//                    + "address.postalcode, address.phone, "
                    + "client.notes " 
                + "FROM client " 
//                + "WHERE client.addressId = address.addressId "
                + "ORDER BY client.clientId";
        
            PreparedStatement smt = DBConnection.getConn().prepareStatement(clientQuery);
            ResultSet clientsFound = smt.executeQuery();

            while (clientsFound.next()) {
                Client nClient = new Client();
                
                nClient.setId(clientsFound.getInt("client.clientId"));
                nClient.setName(clientsFound.getString("client.name"));
                nClient.setNotes(clientsFound.getString("client.notes"));
                clientList.add(nClient);

            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientList;
    }
    
    public static Client getModClient() {
        return modClientSelected;
    }
    public static void resetModClient() {
        modClientSelected = null;
    }
    public static void setModClient(Client c) {
        modClientSelected = c;
    }
}
   
    

