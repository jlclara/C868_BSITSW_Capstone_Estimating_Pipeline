/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EstimatingPipeline.model;

/**
 *
 * @author Jenny Nguyen
 */

public class Client {
    private Integer id;
    private String name;
    private Integer addressId;
    private String notes;
    
    public Client(){  
    }

    public Client(Integer id, String name, Integer addressId,
            String notes) {
        this.id = id;
        this.name = name;
        this.addressId = addressId;
        this.notes = notes;
    }
    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    
    
}
