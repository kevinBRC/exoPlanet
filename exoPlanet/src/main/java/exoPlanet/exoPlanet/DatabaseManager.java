package exoPlanet.exoPlanet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
	  private static String url;
	  private static String username;

	  public DatabaseManager(String url)
	  {
		  DatabaseManager.url = url;
	  }
	  
	  public static Connection getConnection() throws SQLException {
	        return DriverManager.getConnection(url);
	    }
	  
}
