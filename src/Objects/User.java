package Objects;

import Decorateur.FieldName;
import Decorateur.LengthMax;
import Decorateur.NotNull;
import Decorateur.TableName;


//@TableName(name = "Utilisateur")
public class User {
    private int id;

    //@FieldName(name = "Prenom")
    @LengthMax(length = 5)
    @NotNull(errorMessage = "Firstname required")
    private String firstname;

    private String lastname;

    public User(){
        this.id = 0;
        this.firstname = "";
        this.lastname = "";
    }

    public int getid() {
        return id;
    }

    public void setid(int id) {
        this.id = id;
    }

    public String getfirstname() {
        return firstname;
    }

    public void setfirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getlastname() {
        return this.lastname;
    }

    public void setlastname(String lastname) {
        this.lastname = lastname;
    }

    public User(int i, String f, String l){
        this.id = i;
        this.firstname = f;
        this.lastname = l;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("User : ").append(this.id).append(" ").append(this.firstname).append(" ").append(this.lastname);
        return str.toString();
    }
}
