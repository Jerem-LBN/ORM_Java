package ORM;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JRelate {


    public String getTableName(Object o){
        return o.getClass().getSimpleName();
    }

    public Field[] getFields(Object o){
        return o.getClass().getDeclaredFields();
    }

    public String getDatabaseType(String ConnexionString){
        String[] a = ConnexionString.split(":");
        return a[1];
    }

    public Object getFieldsTypes(Field field){
        return field.getType();
    }

    public String createTable(Object o, String connexionString, String login, String mdp) throws SQLException, ClassNotFoundException{
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        try(con){
            String tableName = getTableName(o);
            req.append("CREATE TABLE ? (");
            int lenFields = o.getClass().getDeclaredFields().length;

            for(int i = 0; i <= lenFields; i++){
                if(i == lenFields){
                    req.append("?);");
                }else{
                    req.append("?,");
                }  
            }
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                statement.setString(1, tableName);
                int i = 2;
                for(Field f : o.getClass().getDeclaredFields()){
                    statement.setString(i, f.getName());
                    i++;
                }
                int rs = statement.executeUpdate(req.toString());
                if(rs == 1){
                    System.out.println("Enrgistrement fait");
                }else{
                    System.out.println("Erreur lors de l'enregistrement");
                }
            }
        }
        return req.toString();
    }
    
    public void InsertObject(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException{
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        StringBuilder reqValues = new StringBuilder();
        String tableName = getTableName(o);
        try(con){
            req.append("INSERT INTO ");
            req.append(tableName);
            req.append(" (");
            reqValues.append("VALUES (");
            Field[] fields = o.getClass().getDeclaredFields();
            int lenFields = o.getClass().getDeclaredFields().length;
            //-2 pour enlever l'id qui n'est pas inséré
            for(int i = 0; i < lenFields; i++){
                Field f = fields[i];
                f.setAccessible(true);
                String fieldName = f.getName();
                if(fieldName.toLowerCase() != "id"){
                    req.append(fieldName);
                    if(i == lenFields-1){
                        req.append(") ");
                        reqValues.append("?);");
                    }else{
                        req.append(",");
                        reqValues.append("?,");
                    }
                }
            }
            req.append(reqValues);
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(int i = 0; i < lenFields; i++){
                    Field f = fields[i];
                    f.setAccessible(true);
                    String fieldName = f.getName();
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(o);
                        
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            statement.setString(i, valueString);
                        }else if(type.equals("int")){
                            int Int = (Integer) valueField;
                            statement.setInt(i, Int);
                        }else if(type.equals("Date")){
                            Date date = (Date) valueField;
                            statement.setDate(i, date);
                        }else if(type.equals("boolean")){
                            boolean bool = (Boolean) valueField;
                            statement.setBoolean(i, bool);
                        }
                    }
                }
                System.out.println(statement.toString());
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Enregistrement effectué !");
                }else{
                    System.out.println("Erreur lors de l'enregistrement !");
                }
            }
        }
    }

    public void UpdateObject(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException{
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        StringBuilder reqWhere = new StringBuilder();
        String tableName = getTableName(o);
        try(con){
            req.append("UPDATE ");
            req.append(tableName);
            req.append(" SET ");
            reqWhere.append("WHERE id=?");
            Field[] fields = o.getClass().getDeclaredFields();
            int lenFields = o.getClass().getDeclaredFields().length;
            //-2 pour enlever l'id qui n'est pas inséré
            for(int i = 0; i < lenFields; i++){
                Field f = fields[i];
                f.setAccessible(true);
                String fieldName = f.getName();
                if(fieldName.toLowerCase() != "id"){
                    req.append(fieldName);
                    if(i == lenFields-1){
                        req.append("=?");
                    }else{
                        req.append("=?, ");
                    }
                }
            }
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(int i = 0; i < lenFields; i++){
                    Field f = fields[i];
                    f.setAccessible(true);
                    String fieldName = f.getName();
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(o);
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            statement.setString(i, valueString);
                        }else if(type.equals("int")){
                            int Int = (Integer) valueField;
                            statement.setInt(i, Int);
                        }else if(type.equals("Date")){
                            Date date = (Date) valueField;
                            statement.setDate(i, date);
                        }else if(type.equals("boolean")){
                            boolean bool = (Boolean) valueField;
                            statement.setBoolean(i, bool);
                        }
                    }else{
                        Object valueField = f.get(o);
                        int Int = (Integer) valueField;
                        statement.setInt(lenFields, Int);
                    }
                }
                System.out.println(statement.toString());
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Enregistrement effectué !");
                }else{
                    System.out.println("Erreur lors de l'enregistrement !");
                }
            }
        }
    }

    public void DeleteObject(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException  {
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        try(con){
            req.append("DELETE FROM ");
            req.append(tableName);
            req.append(" WHERE id=?");
            Field f = o.getClass().getDeclaredField("id");
            //-2 pour enlever l'id qui n'est pas inséré
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                f.setAccessible(true);
                Object valueField = f.get(o);
                int valueId = (Integer) valueField;
                statement.setInt(1, valueId);
                System.out.println(statement.toString());
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Suppression effectué !");
                }else{
                    System.out.println("Erreur lors de la suppression !");
                }
            }
        }
    }

    public List<Object> SelectAllData(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InstantiationException, InvocationTargetException, NoSuchFieldException{
        List<Object> objets = new ArrayList<>();
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        try(con){
            req.append("SELECT * FROM ");
            req.append(tableName);
            req.append(";");
            StringBuilder result = new StringBuilder();
            System.out.println(req.toString());
            result.append(o.getClass().getSimpleName());
            result.append(" : ");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object instance = o.getClass().getConstructor().newInstance();
                        Field[] fields = instance.getClass().getDeclaredFields();
                        for(Field f : fields){
                            f.setAccessible(true);
                            String fieldName = f.getName();
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            if(type.equals("String")){
                                valueField =  rs.getString(fieldName);
                            }else if(type.equals("int")){
                                valueField =  rs.getInt(fieldName);
                            }else if(type.equals("Date")){
                                valueField =  rs.getDate(fieldName);
                            }else if(type.equals("boolean")){
                                valueField =  rs.getBoolean(fieldName);
                            }
                            instance.getClass().getDeclaredField(fieldName).set(f, valueField);
                            result.append(fieldName);
                            result.append(" : ");
                            result.append(valueField);
                            result.append(" ");
                        }
                        objets.add(instance);   
                    }
                }
            }  
        }
        return objets;
    }

    public List<Object> SelectObjectByID(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException{
        List objets = new ArrayList<>();
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        Field[] fields = getFields(o);
        try(con){
            req.append("SELECT * FROM ");
            req.append(tableName);
            req.append(" WHERE id=?;");
            StringBuilder result = new StringBuilder();
            System.out.println(req.toString());
            result.append(o.getClass().getSimpleName());
            result.append(" : ");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(Field f : fields){
                    String fieldName = f.getName();
                    if(fieldName.toLowerCase() == "id"){
                        Object valueField = f.get(o);
                        int Int = (Integer) valueField;
                        statement.setInt(1, Int);
                    }
                }
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object instance = o.getClass().getConstructor().newInstance();
                        for(Field f : fields){
                            String fieldName = f.getName();
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            
                            if(type.equals("String")){
                                valueField =  rs.getString(fieldName);
                            }else if(type.equals("int")){
                                valueField =  rs.getInt(fieldName);
                            }else if(type.equals("Date")){
                                valueField =  rs.getDate(fieldName);
                            }else if(type.equals("boolean")){
                                valueField =  rs.getBoolean(fieldName);
                            }
                            instance.getClass().getDeclaredField(fieldName).set(f, valueField);
                            result.append(fieldName);
                            result.append(" : ");
                            result.append(valueField);
                            result.append(" ");
                        }
                        
                    }
                    System.out.println(result.toString());
                }
            }  
        }
        return objets;
    }

    public List<Object> SelectObjectOrdered(Object o, Field field, int ascOrDesc, String connexionString, String login, String mdp) throws Exception{
        List objets = new ArrayList<>();
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        Field[] fields = getFields(o);
        try(con){
            req.append("SELECT * FROM ");
            req.append(tableName);
            req.append(" ORDER BY ");
            req.append(field.getName());
            if(ascOrDesc == 0){
                req.append(" DESC");
            }else if(ascOrDesc == 1){
                req.append(" ASC");
            }else{
                throw new Exception("Saisie incorrecte");
            }
            
            StringBuilder result = new StringBuilder();
            System.out.println(req.toString());
            result.append(o.getClass().getSimpleName());
            result.append(" : ");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object instance = o.getClass().getConstructor().newInstance();
                        for(Field f : fields){
                            String fieldName = f.getName();
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            
                            if(type.equals("String")){
                                valueField =  rs.getString(fieldName);
                            }else if(type.equals("int")){
                                valueField =  rs.getInt(fieldName);
                            }else if(type.equals("Date")){
                                valueField =  rs.getDate(fieldName);
                            }else if(type.equals("boolean")){
                                valueField =  rs.getBoolean(fieldName);
                            }
                            instance.getClass().getDeclaredField(fieldName).set(f, valueField);
                            result.append(fieldName);
                            result.append(" : ");
                            result.append(valueField);
                            result.append(" ");
                        }
                    }
                    System.out.println(result.toString());
                }
            }  
        }
        return objets;
    }

    public Connection getConnexion(String ConnexionString, String login, String mdp) throws ClassNotFoundException, SQLException{
        String MyDB = getDatabaseType(ConnexionString);

        switch(MyDB){
            case "mysql": Class.forName("com.mysql.cj.jdbc.Driver");
            case "postgresql": Class.forName("org.postgresql.Driver");
            case "sqlserver": Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        
        Connection connexion = DriverManager.getConnection(ConnexionString, login, mdp);
        return connexion;
    }
}
