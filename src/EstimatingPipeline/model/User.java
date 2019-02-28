package EstimatingPipeline.model;

/**
 *
 * @author Jenny Nguyen
 */
public class User {
    private int userID;
    private String username;
    private String password;
    private String type;
    private int active;

    public User(int userID, String username, String password, String type, int active) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.type = type;
        this.active = active;
    }

    public User() {
        
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }   

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
    
    
    
}