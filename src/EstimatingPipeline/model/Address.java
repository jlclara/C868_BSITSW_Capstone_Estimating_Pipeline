/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EstimatingPipeline.model;

import EstimatingPipeline.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jenny Nguyen
 */
public class Address {
    
    private Integer addressId;
    private String address;
    private String address2;
    private String city;    
    private String state;
    private String zip;
    private String phone;

    public Address() {
    }

    public Address(String address, String address2, String city, String zip, String phone) {
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    // checks if an address already exists
    public static Address validateAddress(String aAddress, String aAddress2, String aCity, 
             String aState, String aPostal, String aPhone) {
        Address vAddress = new Address(); 
        try{           
            PreparedStatement pst = DBConnection.getConn()
                    .prepareStatement("SELECT * FROM address "
                            + "WHERE address.address=? AND address.city=? "
                            + "AND address.postalCode =? AND address.phone = ? "
                            + "AND address.state = ? AND address.address2=? ");
            pst.setString(1, aAddress); 
            pst.setString(2, aCity); 
            pst.setString(3, aPostal); 
            pst.setString(4, aPhone); 
            pst.setString(5, aState); 
            pst.setString(6, aAddress2);
            ResultSet rs = pst.executeQuery();                        
            if(rs.next()){
                vAddress.setAddressId(rs.getInt("addressId"));
                vAddress.setAddress(rs.getString("address"));
                vAddress.setAddress2(rs.getString("address2"));
                vAddress.setCity(rs.getString("city"));
                vAddress.setState(rs.getString("state"));
                vAddress.setZip(rs.getString("postalCode"));
                vAddress.setPhone(rs.getString("phone"));
                
            } else {
                return null;    
            }            
        } catch(SQLException e){
            e.printStackTrace();
        }       
        return vAddress;
    }
    
}
