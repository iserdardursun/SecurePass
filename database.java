import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

public class database {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    
    public void readDataBase() throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://securepass-db.c4p1nn53ccup.eu-central-1.rds.amazonaws.com:3305/securepass?"
                            + "user=admin&password=secureroot");
            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
        } catch (Exception e) {
            throw e;
        }
    }
    
    public void register(String email, String password) throws SQLException {
        preparedStatement = connect.prepareStatement("select email from securepass.Users where email=\""+email+"\"");
        resultSet = preparedStatement.executeQuery();        
        String emailCheck = "";
        while (resultSet.next()) {
        	emailCheck = resultSet.getString("email");
        }
        if(!emailCheck.equals("")) {
        	System.out.println("This user already exists.");
        }else {
            preparedStatement = connect.prepareStatement("insert into securepass.Users (email,password) values (\""+email+"\",\""+password+"\")");
            preparedStatement.executeUpdate();
        }
    }
    
    public boolean loginSuccess(String email, String password) throws SQLException {
        preparedStatement = connect.prepareStatement("select password from securepass.Users where email =\""+email+"\"");
        resultSet = preparedStatement.executeQuery();
        String pass = "";
        while (resultSet.next()) {
        	pass = resultSet.getString("password");
        }
        if(pass == "") {
        	System.out.println("Your email or password is wrong.");
        	return false;
        }else {
        	if(pass.equals(password)) return true;
        	System.out.println("Your email or password is wrong.");
        	return false;
        }
    }
    
    public HashMap getPasswords(String email) throws SQLException {
    	preparedStatement = connect.prepareStatement("select id from securepass.Users where email =\""+email+"\"");
        resultSet = preparedStatement.executeQuery();
        int userId = 0;
        while (resultSet.next()) {
	        userId = resultSet.getInt("id");
        }
    	preparedStatement = connect.prepareStatement("select appName,Password from securepass.Passwords where userId =\""+userId+"\"");
        resultSet = preparedStatement.executeQuery();
        HashMap<String, String> appData = new HashMap<String, String>();
        while (resultSet.next()) {
        	appData.put(resultSet.getString("appName"),resultSet.getString("Password"));
        }
        return appData;
    }
    
    public void setAppPassword(String email, String appName, String appPass) throws SQLException {
        // ResultSet is initially before the first data set	 
    	preparedStatement = connect.prepareStatement("select id from securepass.Users where email =\""+email+"\"");
        resultSet = preparedStatement.executeQuery();
        int userId = 0;
        while (resultSet.next()) {
	        userId = resultSet.getInt("id");
        }
    	preparedStatement = connect.prepareStatement("select appName from securepass.Passwords where userId =\""+userId+"\" and appName=\""+appName+"\"");
        resultSet = preparedStatement.executeQuery();
        String appNameExists = "";
        while (resultSet.next()) {
        	appNameExists = resultSet.getString("appName");
        }
        if(!appNameExists.equals("")) {
            preparedStatement = connect.prepareStatement("update securepass.Passwords set Password = \""+appPass+"\" where userId=\""+userId+"\" and appName= \""+appName+"\"");
            preparedStatement.executeUpdate(); 
        }else {
            preparedStatement = connect.prepareStatement("insert into securepass.Passwords values (\""+userId+"\",\""+appName+"\",\""+appPass+"\")");
            preparedStatement.executeUpdate(); 
        }
    }
    
    // You need to close the resultSet
    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
        }
    }
}
