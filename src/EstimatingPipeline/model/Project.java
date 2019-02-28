/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EstimatingPipeline.model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Jenny Nguyen
 */

public class Project {
   
    private Integer projectId;
    private Integer clientId;
    private String clientName;
    private Integer estimatorId;
    private String estimatorName; 
    private String title;
    private String category;
    private String description;
    private String location; 
    private String value;
    private String status;
    private LocalDateTime due;
    private Integer userId;
    private String userName;

    public Project() {
    }

    public Project(Integer projectId, Integer clientId, String clientName, 
            Integer estimatorId, String estimatorName, String title, String category, 
            String description, String location, String value, String status, 
            LocalDateTime due, LocalDateTime submitted, Integer userId, String userName) {
        this.projectId = projectId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.estimatorId = estimatorId;
        this.estimatorName = estimatorName;
        this.title = title;
        this.category = category;
        this.description = description;
        this.location = location;
        this.value = value;
        this.status = status;
        this.due = due;
//        this.submitted = submitted;
        this.userId = userId;
        this.userName = userName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Integer getEstimatorId() {
        return estimatorId;
    }

    public void setEstimatorId(Integer estimatorId) {
        this.estimatorId = estimatorId;
    }

    public String getEstimatorName() {
        return estimatorName;
    }

    public void setEstimatorName(String estimatorName) {
        this.estimatorName = estimatorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getDue() {
        return due;
    }

    public void setDue(LocalDateTime due) {
        this.due = due;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
   
    
}
