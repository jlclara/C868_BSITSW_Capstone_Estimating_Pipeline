package EstimatingPipeline;

import EstimatingPipeline.util.DBConnection;
import EstimatingPipeline.model.Address;
import EstimatingPipeline.model.User;
import EstimatingPipeline.model.Client;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
public class ClientFieldsController {

    @FXML
    private TextField clientIdTf;
    @FXML
    private TextField clientNameTf;
    @FXML
    private TextField clientStreetTf;
    @FXML
    private TextField clientStreet2Tf;
    @FXML
    private TextField clientCityTf;
    @FXML
    private TextField clientPostalTf;
    @FXML
    private TextField clientStateTf;
    @FXML
    private TextField clientPhoneTf;
    @FXML
    private TextArea clientNotesTf;
    @FXML
    private Text errorMsgTxt;
    @FXML
    private Button clientSaveBtn;
    @FXML
    private Button clientCancelBtn;
    private EstimatingPipeline mainApp;
    private User currentUser;
    private final  Connection connection = DBConnection.getConn();

    
    public ClientFieldsController(){
    }
    
    private String checkUserInput()
    {
        String errorMsg = "";
        String phoneStripped = clientPhoneTf.getText().replaceAll("[^0-9]", "");
        String postalCodeStripped = clientPostalTf.getText().replaceAll("[^0-9]", "");
        
        if (clientNameTf.getText().length() == 0)
            errorMsg += "Please enter the client's company name. \n";
        if (clientStreetTf.getText().length() == 0)
            errorMsg += "Please enter the client's street address. \n";
        if (clientCityTf.getText().length() == 0)
            errorMsg += "Please enter the client's city. \n";
        if (postalCodeStripped.length() == 0)
            errorMsg += "Please enter the client's postal code. \n";
        else if (postalCodeStripped.length() < 5)
            errorMsg += "Please enter a valid postal code. \n";
        if (clientStateTf.getText().length() == 0)
            errorMsg += "Please enter the client's state. \n";
        else if (clientStateTf.getText().length() < 2)
            errorMsg += "Please enter a valid state. \n";
        if (clientPhoneTf.getText().length() == 0)
            errorMsg += "Please enter the client's phone number. \n";
        else if (phoneStripped.length() < 9  || phoneStripped.length() > 11 
                || (phoneStripped.length() == 11 && !phoneStripped.startsWith("1")))  // US, Canada, or US Territories only
            errorMsg += "Please enter a valid phone number including "
                    + "\n country code and area code. \n";

        return errorMsg;
    }
    @FXML
    private void handleClientSaveBtn(ActionEvent e) {
        if (!checkUserInput().isEmpty())
            errorMsgTxt.setText(checkUserInput());
        else {
            // store user input in variables
            String name = clientNameTf.getText();
            String street = clientStreetTf.getText();
            String street2 = clientStreet2Tf.getText();
            String city = clientCityTf.getText();
            String postal = clientPostalTf.getText();
            String state = clientStateTf.getText();
            String phone = clientPhoneTf.getText();
            String phoneStripped = phone.replaceAll("[^0-9]", "");
            if (phoneStripped.startsWith("1")) // US, Canada, and US Territories
                 phoneStripped = phoneStripped.substring(1);
            String phoneFormatted = "(" + phoneStripped.substring(0,3) + ") " 
            + phoneStripped.substring(3,6) + "-" + phoneStripped.substring(6,10);
            
            
     
            String notes = clientNotesTf.getText();

            String addressQuery = null;
            Address currentAddress = null;
        
            if (ClientsController.getModClient()== null){ // ADDING CLIENT, NOT EDITING
            try {           
               currentAddress = Address.validateAddress(street, street2, 
                       city, state, postal, phone);
               if (currentAddress == null)  // address does not exist yet
               {
                    addressQuery = "INSERT INTO address ("
                    + " address, address2, city, state,"
                    + " postalCode, phone) VALUES ("
                    + " ?, ?, ?, ?, ?, ?)";
               }
                PreparedStatement addressSmt = connection.prepareStatement(addressQuery);
                addressSmt.setString(1, street);
                addressSmt.setString(2, street2);
                addressSmt.setString(3, city);
                addressSmt.setString(4, state);
                addressSmt.setString(5, postal);
                addressSmt.setString(6, phoneFormatted);
                
                addressSmt.executeUpdate();
                currentAddress = Address.validateAddress(street, street2, city, state, postal, phoneFormatted);
                addressSmt.close();
                
                String query = "INSERT INTO client ("              
                + " name,"
                + " addressId,"
                + " notes,"
                + " createdDate,"
                + " createdBy,"
                + " modifiedDate,"
                + " modifiedBy) VALUES ("
                + "?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";            
                PreparedStatement st = connection.prepareStatement(query);
                st.setString(1, name);
                st.setInt(2, currentAddress.getAddressId());  
                st.setString(3,notes); 
                st.setInt(4, currentUser.getUserID());
                st.setInt(5, currentUser.getUserID());
                st.executeUpdate();
                st.close();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                }
        } // end of adding client  
               
        // UPDATE Client if a client is selected from Clients screen
        else if (ClientsController.getModClient()!= null) 
        {
            try {
                String updateQuery = "UPDATE client"
                + " SET client.name = ?,"
                + " client.notes = ?,"
                + " client.modifiedDate = CURRENT_TIMESTAMP,"
                + " client.modifiedBy = ?"
                + " WHERE client.clientId = ?";
                String updateAddress = " UPDATE address"
                + " SET address.address = ?,"
                + " address.address2 = ?,"
                + " address.city = ?,"
                + " address.state = ?,"
                + " address.postalCode = ?,"
                + " address.phone = ?"        
                + " WHERE address.addressId = (SELECT client.addressId FROM client WHERE client.clientId = ?)";
                int i = 1;
                PreparedStatement st = connection.prepareStatement(updateQuery);
                st.setString(i++, name);
                st.setString(i++, notes);
                st.setInt(i++, currentUser.getUserID());
                st.setInt(i++, ClientsController.getModClient().getId());
                i = 1;
                PreparedStatement st2 = connection.prepareStatement(updateAddress);
                st2.setString(i++, street);
                st2.setString(i++, street2);
                st2.setString(i++, city);
                st2.setString(i++, state);
                st2.setString(i++, postal);
                st2.setString(i++, phoneFormatted);
                st2.setInt(i++, ClientsController.getModClient().getId());

                st.executeUpdate();
                st.close();
                st2.executeUpdate();
                st2.close();
             } catch (SQLException ex) {
                ex.printStackTrace();
                }
            }
            ClientsController.resetModClient();
            mainApp.closePopup(currentUser);  
        }
    }
    
    @FXML
    private void handleClientCancelBtn(ActionEvent e) {
        Alert cancelAlert = new Alert (AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Clients?");
        cancelAlert.setHeaderText("Are you sure you want to cancel "
                + "and return to the clients screen?");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            ClientsController.resetModClient();
            mainApp.closePopup(currentUser);
        }
    }
    
    public void setUpClientFields(EstimatingPipeline mainApp, User activeUser){
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        if (currentUser.getType().equalsIgnoreCase("limited"))
            clientSaveBtn.setDisable(true);
        
        // load client data if edit is selected from the clients scene
        Client modClient = ClientsController.getModClient();
        if (modClient != null)
        {
            Integer clientAddressId;
            Address clientAddress = new Address();
            try {
            String addressQuery = "SELECT * "
                + "FROM address, client " 
                + "WHERE address.addressId = client.addressId "
                    + "AND client.clientId = ?";
        
             PreparedStatement addressSmt = connection.prepareStatement(addressQuery);
                addressSmt.setInt(1, modClient.getId());
                ResultSet addressFound = addressSmt.executeQuery();            

            while (addressFound.next()) {
                clientAddress.setAddressId(addressFound.getInt("address.addressId"));
                clientAddress.setAddress(addressFound.getString("address.address"));
                clientAddress.setAddress2(addressFound.getString("address.address2"));
                clientAddress.setCity(addressFound.getString("address.city"));
                clientAddress.setState(addressFound.getString("address.state"));                
                clientAddress.setZip(addressFound.getString("address.postalcode"));
                clientAddress.setPhone(addressFound.getString("address.phone"));
            }
            addressSmt.close();
            
            String phone = clientAddress.getPhone();
            String phoneStripped = phone.replaceAll("[^0-9]", "");
            if (phoneStripped.startsWith("1"))
                phoneStripped = phoneStripped.substring(1);
            String phoneFormatted = "(" + phoneStripped.substring(0,3) + ") " 
                + phoneStripped.substring(3,6) + "-" + phoneStripped.substring(6,10);
            clientIdTf.setText(Integer.toString(modClient.getId()));
            clientNameTf.setText(modClient.getName());
            clientStreetTf.setText(clientAddress.getAddress());
            clientStreet2Tf.setText(clientAddress.getAddress2());
            clientCityTf.setText(clientAddress.getCity());
            clientPostalTf.setText(clientAddress.getZip());
            clientStateTf.setText(clientAddress.getState());
            clientPhoneTf.setText(phoneFormatted);
            clientNotesTf.setText(modClient.getNotes());

        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
            
        }
    }
}
