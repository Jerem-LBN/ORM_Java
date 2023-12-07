package ORM;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Decorateur.FieldName;
import Decorateur.LengthMax;
import Decorateur.NotNull;
import Decorateur.TableName;

public class JRelate {


    public String getTableName(Object o){
        String tableName;
        if (o.getClass().isAnnotationPresent(TableName.class)) {
            TableName Tab = o.getClass().getAnnotation(TableName.class);
            tableName = Tab.name().toString();
        } else {
            tableName = o.getClass().getSimpleName();
        }
        return tableName;
    }

    public String getFieldName(Field f){
        String fieldName;
        if (f.isAnnotationPresent(FieldName.class)) {
            FieldName Field = f.getAnnotation(FieldName.class);
            fieldName = Field.name().toString();
        } else {
            fieldName = f.getName();
        }
        return fieldName;
    }

    public boolean verifyLength(Field f, String valueField){
        int lengthMax;
        if (f.isAnnotationPresent(LengthMax.class)) {
            LengthMax Field = f.getAnnotation(LengthMax.class);
            lengthMax = Field.length();
            if(lengthMax >= valueField.length()){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }
    }

    public boolean verifyRequired(Field f, Object valueField){
        System.out.println(valueField.toString());
        System.out.println(!(valueField.toString() != null && !valueField.toString().isEmpty()));
        if (f.isAnnotationPresent(NotNull.class)) {
            if (!(valueField.toString() != null && !valueField.toString().isEmpty())) {
                return true;
            }else{
                return true;
            }
        }else{
            return true;
        }
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
    
    public void InsertObject(Object o, String connexionString, String login, String mdp) throws Exception{
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
                String fieldName = getFieldName(f);
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
                    String fieldName = getFieldName(f);
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(o);
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            if(verifyRequired(f, valueField)){
                                if(verifyLength(f, valueString)){
                                    statement.setString(i, valueString);
                                }else{
                                    StringBuilder erreur = new StringBuilder();
                                    erreur.append("La longueur du champ '");
                                    erreur.append(fieldName);
                                    erreur.append("' : '");
                                    erreur.append(valueString);
                                    erreur.append("' est trop grande");
                                    throw new Exception(erreur.toString());
                                }
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '");
                                erreur.append(fieldName);
                                erreur.append("' : '");
                                erreur.append(valueField.toString());
                                erreur.append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("int")){
                            int Int = (Integer) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setInt(i, Int);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '");
                                erreur.append(fieldName);
                                erreur.append("' : '");
                                erreur.append(valueField.toString());
                                erreur.append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("Date")){
                            Date date = (Date) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setDate(i, date);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '");
                                erreur.append(fieldName);
                                erreur.append("' : '");
                                erreur.append(valueField.toString());
                                erreur.append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("boolean")){
                            boolean bool = (Boolean) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setBoolean(i, bool);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '");
                                erreur.append(fieldName);
                                erreur.append("' : '");
                                erreur.append(valueField.toString());
                                erreur.append("' est requis");
                                throw new Exception(erreur.toString());
                            }
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

    public void UpdateObject(Object o, String connexionString, String login, String mdp) throws Exception{
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
                String fieldName = getFieldName(f);
                if(fieldName.toLowerCase() != "id"){
                    req.append(fieldName);
                    if(i == lenFields-1){
                        req.append("=? ");
                    }else{
                        req.append("=?, ");
                    }
                }
            }
            req.append(reqWhere);
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(int i = 0; i < lenFields; i++){
                    Field f = fields[i];
                    f.setAccessible(true);
                    String fieldName = getFieldName(f);
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(o);
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            if(verifyLength(f, valueString)){
                                statement.setString(i, valueString);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("La longueur du champ '");
                                erreur.append(fieldName);
                                erreur.append("' : '");
                                erreur.append(valueString);
                                erreur.append("' est trop grande");
                                throw new Exception(erreur.toString());
                            }
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
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object instance = o.getClass().getConstructor().newInstance();
                        Field[] fields = instance.getClass().getDeclaredFields();
                        for(Field f : fields){
                            f.setAccessible(true);
                            String fieldName = getFieldName(f);
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
                            Method[] methods = instance.getClass().getDeclaredMethods();
                            StringBuilder StringMethod = new StringBuilder();
                            StringMethod.append("set");
                            StringMethod.append(fieldName);
                            for(Method m : methods){
                                if(m.getName().equals(StringMethod.toString())){
                                    System.out.println(m.getName().toString());
                                    m.invoke(instance, valueField);
                                }
                            }
                        }
                        objets.add(instance);
                    }
                }
            }  
        }
        return objets;
    }

    public List<Object> SelectObjectByID(Object o, String connexionString, String login, String mdp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException{
        List<Object> objets = new ArrayList<>();
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        Field[] fields = getFields(o);
        try(con){
            req.append("SELECT * FROM ");
            req.append(tableName);
            req.append(" WHERE id=?;");
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(Field f : fields){
                    f.setAccessible(true);
                    String fieldName = getFieldName(f);
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
                            f.setAccessible(true);
                            String fieldName = getFieldName(f);
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
                            Method[] methods = instance.getClass().getDeclaredMethods();
                            StringBuilder StringMethod = new StringBuilder();
                            StringMethod.append("set");
                            StringMethod.append(fieldName);
                            System.out.println(StringMethod.toString());
                            for(Method m : methods){
                                if(m.getName().equals(StringMethod.toString())){
                                    System.out.println(m.getName().toString());
                                    m.invoke(instance, valueField);
                                }
                            }
                        }
                    objets.add(instance);  
                    }
                }
            }  
        }
        return objets;
    }

    public List<Object> SelectObjectOrdered(Object o, String champ, int ascOrDesc, String connexionString, String login, String mdp) throws Exception{
        List<Object> objets = new ArrayList<>();
        Connection con = getConnexion(connexionString, login, mdp);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(o);
        Field[] fields = getFields(o);
        try(con){
            req.append("SELECT * FROM ");
            req.append(tableName);
            req.append(" ORDER BY ");
            req.append(champ);
            if(ascOrDesc == 0){
                req.append(" DESC");
            }else if(ascOrDesc == 1){
                req.append(" ASC");
            }else{
                throw new Exception("Saisie incorrecte");
            }
            System.out.println(req.toString());
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object instance = o.getClass().getConstructor().newInstance();
                        for(Field f : fields){
                            String fieldName = getFieldName(f);
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
                            Method[] methods = instance.getClass().getDeclaredMethods();
                            StringBuilder StringMethod = new StringBuilder();
                            StringMethod.append("set");
                            StringMethod.append(fieldName);
                            for(Method m : methods){
                                if(m.getName().equals(StringMethod.toString())){
                                    m.invoke(instance, valueField);
                                }
                            }
                        }
                    objets.add(instance);
                    }
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
