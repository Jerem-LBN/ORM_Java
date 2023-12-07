import Objects.User;

import java.util.ArrayList;
import java.util.List;

import ORM.JRelate;

public class App {
    public static void main(String[] args) throws Exception {
        User u = new User(1, "Jeremie", "LBN");
        User u2 = new User(3, "Artur", "Poirot");
        User u3 = new User(1, "Kevin", "Ramiray");
        JRelate jr = new JRelate();
        List<Object> users = new ArrayList<Object>();
        
        //jr.createTable(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //jr.InsertObject(u2, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //jr.DeleteObject(u2, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        
        users = jr.SelectAllData(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //users = jr.SelectObjectByID(u2, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //users = jr.SelectObjectOrdered(u, "firstname", 1, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        for(Object o : users){
            System.out.println(o.toString());
        }
        
        //jr.UpdateObject(u3, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        
    }
}
