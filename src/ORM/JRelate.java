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

    /**
     * Cette méthode renvoie le nom de la classe de l'objet fournis en paramètre.
     * Ou le nom spécifié par le décorateur 'TableName' si il est présent.
     * 
     * @param object
     * @return
     */
    public String getTableName(Object object){
        String tableName;
        if (object.getClass().isAnnotationPresent(TableName.class)) {
            TableName Tab = object.getClass().getAnnotation(TableName.class);
            tableName = Tab.name().toString();
        } else {
            tableName = object.getClass().getSimpleName();
        }
        return tableName;
    }

    /**
     * Cette méthode renvoie le nom du champ fournis en paramètre.
     * Ou le nom spécifié par le décorateur 'FieldName' si il est présent.
     * 
     * @param field
     * @return
     */
    public String getFieldName(Field field){
        String fieldName;
        if (field.isAnnotationPresent(FieldName.class)) {
            FieldName Field = field.getAnnotation(FieldName.class);
            fieldName = Field.name().toString();
        } else {
            fieldName = field.getName();
        }
        return fieldName;
    }

    /**
     * Cette méthode regarde si le décorateur LengthMax est présent. Et si la longueur de valueField n'est pas trop grande.
     * 
     * @param field
     * @param valueField
     * @return
     */
    public boolean verifyLength(Field field, String valueField){
        int lengthMax;
        if (field.isAnnotationPresent(LengthMax.class)) {
            LengthMax Field = field.getAnnotation(LengthMax.class);
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

    /**
     * Cette méthode regarde si le décorateur NotNull est présent. Et si la valeur de valueField n'est pas null.
     * 
     * @param field
     * @param valueField
     * @return
     */
    public boolean verifyRequired(Field field, Object valueField){
        if (field.isAnnotationPresent(NotNull.class)) {
            if (!(valueField.toString() != null && !valueField.toString().isEmpty())) {
                return true;
            }else{
                return true;
            }
        }else{
            return true;
        }
    }

    /**
     * Cette méthode renvoie la liste des champs présents dans l'objet.
     * 
     * @param object
     * @return
     */
    public Field[] getFields(Object object){
        return object.getClass().getDeclaredFields();
    }

    /**
     * Cette méthode renvoie le type de base de données.
     * 
     * @param ConnexionString
     * @return
     */
    public String getDatabaseType(String ConnexionString){
        String[] a = ConnexionString.split(":");
        return a[1];
    }

    /**
     * Cette méthode renvoie une connexion à la BDD en chargant le bon driver.
     * 
     * @param ConnexionString
     * @param login
     * @param password
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnexion(String ConnexionString, String login, String password) throws ClassNotFoundException, SQLException{
        String MyDB = getDatabaseType(ConnexionString);
        switch(MyDB){
            case "mysql": Class.forName("com.mysql.cj.jdbc.Driver");
            case "postgresql": Class.forName("org.postgresql.Driver");
            case "sqlserver": Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        Connection connexion = DriverManager.getConnection(ConnexionString, login, password);
        return connexion;
    }

    /**
     * Cette méthode insert un objet dans la BDD.
     * 
     * @param object
     * @param connexionString
     * @param login
     * @param password
     * @throws Exception
     */
    public void InsertObject(Object object, String connexionString, String login, String password) throws Exception{
        //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        StringBuilder reqValues = new StringBuilder();
        String tableName = getTableName(object);
        try(con){
            //Création de la requête
            req.append("INSERT INTO ").append(tableName).append(" (");
            reqValues.append("VALUES (");
            Field[] fields = object.getClass().getDeclaredFields();
            int lenFields = object.getClass().getDeclaredFields().length;
            //-2 pour enlever l'id qui n'est pas inséré
            //Boucle qui permet d'ajouter les paramètres ('?')
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
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(int i = 0; i < lenFields; i++){
                    Field f = fields[i];
                    f.setAccessible(true);
                    String fieldName = getFieldName(f);
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(object);
                        //A partir d'ici, je vais vérifier le type du champ, et si il a les décorateurs : 'LengthMax', 'NotNull'.
                        //Afin de lancer une erreur spécifique si une condition imposée par un décorateur n'est pas respéctée.
                        //Si il n'y a pas d'erreur, je set les paramètres avec le bon set"Type" qui va bien.
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            if(verifyRequired(f, valueField)){
                                if(verifyLength(f, valueString)){
                                    statement.setString(i, valueString);
                                }else{
                                    StringBuilder erreur = new StringBuilder();
                                    erreur.append("La longueur du champ '").append(fieldName).append("' : '").append(valueString).append("' est trop grande");
                                    throw new Exception(erreur.toString());
                                }
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '").append(fieldName).append("' : '").append(valueField.toString()).append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("int")){
                            int Int = (Integer) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setInt(i, Int);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '").append(fieldName).append("' : '").append(valueField.toString()).append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("Date")){
                            Date date = (Date) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setDate(i, date);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '").append(fieldName).append("' : '").append(valueField.toString()).append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }else if(type.equals("boolean")){
                            boolean bool = (Boolean) valueField;
                            if(verifyRequired(f, valueField)){
                                statement.setBoolean(i, bool);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("Le champ '").append(fieldName).append("' : '").append(valueField.toString()).append("' est requis");
                                throw new Exception(erreur.toString());
                            }
                        }
                    }
                }
                //Exécution de la requête
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Enregistrement effectué !");
                }else{
                    System.out.println("Erreur lors de l'enregistrement !");
                }
            }
        }
    }

    /**
     * Cette méthode modifie une ligne dans la BDD.
     * L'id de l'objet correspond à l'id qui est en base.
     * 
     * @param object
     * @param connexionString
     * @param login
     * @param password
     * @throws Exception
     */
    public void UpdateObject(Object object, String connexionString, String login, String password) throws Exception{
        //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        StringBuilder reqWhere = new StringBuilder();
        String tableName = getTableName(object);
        try(con){
            //Création de la requête
            req.append("UPDATE ").append(tableName).append(" SET ");
            reqWhere.append("WHERE id=?");
            Field[] fields = object.getClass().getDeclaredFields();
            int lenFields = object.getClass().getDeclaredFields().length;
            //-2 pour enlever l'id qui n'est pas inséré
            //Boucle qui permet d'ajouter les paramètres ('?')
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
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                //Dans cette boucle on récupère les valeurs des champs de l'objet et on set les paramètres
                for(int i = 0; i < lenFields; i++){
                    Field f = fields[i];
                    f.setAccessible(true);
                    String fieldName = getFieldName(f);
                    if(fieldName.toLowerCase() != "id"){
                        Object type = f.getType().getSimpleName();
                        Object valueField = f.get(object);
                        if(type.equals("String")){
                            String valueString = (String) valueField;
                            if(verifyLength(f, valueString)){
                                statement.setString(i, valueString);
                            }else{
                                StringBuilder erreur = new StringBuilder();
                                erreur.append("La longueur du champ '").append(fieldName).append("' : '").append(valueString).append("' est trop grande");
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
                        Object valueField = f.get(object);
                        int Int = (Integer) valueField;
                        statement.setInt(lenFields, Int);
                    }
                }
                //Exécution de la requête
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Enregistrement effectué !");
                }else{
                    System.out.println("Erreur lors de l'enregistrement !");
                }
            }
        }
    }

    /**
     * Cette méthode supprime une ligne de la BDD.
     * 
     * @param object
     * @param connexionString
     * @param login
     * @param password
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void DeleteObject(Object object, String connexionString, String login, String password) throws ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException  {
        //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(object);
        try(con){
            //Création de la requête
            req.append("DELETE FROM ").append(tableName).append(" WHERE id=?");
            Field f = object.getClass().getDeclaredField("id");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                f.setAccessible(true);
                Object valueField = f.get(object);
                int valueId = (Integer) valueField;
                //Set le paramètre
                statement.setInt(1, valueId);
                //Exécution de la requête
                int rs = statement.executeUpdate();
                if(rs == 1){
                    System.out.println("Suppression effectué !");
                }else{
                    System.out.println("Erreur lors de la suppression !");
                }
            }
        }
    }

    /**
     * Cette méthode renvoie une liste d'objets, qui auront le même type que l'objet passé en paramètre.
     * 
     * @param object
     * @param connexionString
     * @param login
     * @param password
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    public List<Object> SelectAllData(Object object, String connexionString, String login, String password) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InstantiationException, InvocationTargetException, NoSuchFieldException{
        List<Object> objets = new ArrayList<>();
        //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(object);
        try(con){
            //Création de la requête
            req.append("SELECT * FROM ").append(tableName).append(";");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    //Récupération des lignes 
                    while (rs.next()) {
                        //Création d'une instance de l'objet
                        Object instance = object.getClass().getConstructor().newInstance();
                        Field[] fields = instance.getClass().getDeclaredFields();
                        for(Field f : fields){
                            f.setAccessible(true);
                            String fieldName = getFieldName(f);
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            //On vérifie le type du champ
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
                            StringMethod.append("set").append(fieldName);
                            //Ici, j'utilise la méthode set de la classe de l'objet
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


    /**
     * Cette méthode retourne un objet qui est sélectionné grâce à son id.
     * Prend l'id de l'objet fournis en paramètre
     * 
     * @param object
     * @param connexionString
     * @param login
     * @param password
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public List<Object> SelectObjectByID(Object object, String connexionString, String login, String password) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException{
        List<Object> objets = new ArrayList<>();
         //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(object);
        Field[] fields = getFields(object);
        try(con){
            //Création de la requête
            req.append("SELECT * FROM ").append(tableName).append(" WHERE id=?;");
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                for(Field f : fields){
                    f.setAccessible(true);
                    String fieldName = getFieldName(f);
                    if(fieldName.toLowerCase() == "id"){
                        Object valueField = f.get(object);
                        int Int = (Integer) valueField;
                        statement.setInt(1, Int);
                    }
                }
                try (ResultSet rs = statement.executeQuery()) {
                    //Récupération de la ligne
                    while (rs.next()) {
                        //Création d'une instance de l'objet
                        Object instance = object.getClass().getConstructor().newInstance();
                        for(Field f : fields){
                            f.setAccessible(true);
                            String fieldName = getFieldName(f);
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            //On vérifie le type du champ
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
                            StringMethod.append("set").append(fieldName);
                            //Ici, j'utilise la méthode set de la classe de l'objet
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


    /**
     * Cette fonction renvoie une liste d'objets ordonés de façon croissante ou décroissante, 
     * en fonction d'un attribut fournit en paramètre. (0 : DESC; 1 : ASC)
     * 
     * @param object
     * @param field
     * @param ascOrDesc
     * @param connexionString
     * @param login
     * @param password
     * @return
     * @throws Exception
     */
    public List<Object> SelectObjectOrdered(Object object, String field, int ascOrDesc, String connexionString, String login, String password) throws Exception{
        List<Object> objets = new ArrayList<>();
        //Création de la connexion
        Connection con = getConnexion(connexionString, login, password);
        StringBuilder req = new StringBuilder();
        String tableName = getTableName(object);
        Field[] fields = getFields(object);
        try(con){
            //Création de la requête
            req.append("SELECT * FROM ").append(tableName).append(" ORDER BY ").append(field);
            if(ascOrDesc == 0){
                req.append(" DESC");
            }else if(ascOrDesc == 1){
                req.append(" ASC");
            }else{
                throw new Exception("Saisie incorrecte");
            }
            try (PreparedStatement statement = con.prepareStatement(req.toString())) {
                try (ResultSet rs = statement.executeQuery()) {
                    //Récupération des lignes et traitement
                    while (rs.next()) {
                        //Création d'une nouvelle instance de l'objet
                        Object instance = object.getClass().getConstructor().newInstance();
                        for(Field f : fields){
                            String fieldName = getFieldName(f);
                            Object type = f.getType().getSimpleName();
                            Object valueField="";
                            //On vérifie le type du champ
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
                            //Ici, j'utilise la méthode set de la classe de l'objet
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
}
