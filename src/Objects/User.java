package Objects;

import java.sql.Date;

public class User {
    private int id;
    private String firstname;
    private String lastname;

    public User(){
        this.id = 0;
        this.firstname = "";
        this.lastname = "";
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public User(int i, String f, String l){
        this.id = i;
        this.firstname = f;
        this.lastname = l;
    }
}
