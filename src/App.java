import Objects.User;
import ORM.JRelate;

public class App {
    public static void main(String[] args) throws Exception {
        User u = new User(1, "Jeremie", "LBN");
        User u2 = new User(2, "Jeremie", "LBN");
        JRelate jr = new JRelate();
        
        //jr.createTable(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //jr.InsertObject(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        //jr.DeleteObject(u2, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        System.out.println(jr.SelectAllData(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob"));
        jr.UpdateObject(u, "jdbc:mysql://localhost:3306/testdb", "toto", "bob");
        jr.SelectObjectByID(u2, null, null, null);
        jr.SelectObjectOrdered(jr, null, 0, null, null, null);
        
    }
}
