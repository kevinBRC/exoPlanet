package exoPlanet.exoPlanet;

import java.sql.*;
import java.time.LocalDateTime;
import org.json.*;
import exoPlanet.exoPlanet.Direction;
import exoPlanet.exoPlanet.Ground;
import exoPlanet.exoPlanet.Planet;


public class DatabaseManager {
	private static String password;
	private static String url;
	private static String username;
	

	public DatabaseManager(String url, String username, String password) 
	{
	    DatabaseManager.url = url;
	    DatabaseManager.username = username;
	    DatabaseManager.password = password;
	}

	public static Connection getConnection() throws SQLException 
	{
	    return DriverManager.getConnection(url, username, password);
	}
	
	public static void insertRover(int roverId, String name, Planet planet, LocalDateTime last_activity, String lastInput, int clientId, Ground ground, Direction direction, int xCoord, int yCoord, LocalDateTime timestamp, String textprotocol) {
	    
		insertRoverId(roverId);
		insertRoverName(name);
		insertRoverPlanet(planet);
		insertRoverLastActivity(last_activity);
		insertRoverLastInput(lastInput);
		insertRoverClientId(clientId);
		insertRoverGround(ground);
		insertRoverDirection(direction);
		insertRoverXCoord(xCoord);
		insertRoverYCoord(yCoord);
		insertRoverTimestamp(timestamp);
		insertRoverTextprotocol(textprotocol);	    
	    
	}
	
	public static void insertRoverId(int roverId) {
	    String sql = "INSERT INTO robot (r-id) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, roverId);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverName(String name) {
	    String sql = "INSERT INTO robot (Name) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, name);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverPlanet(Planet planet) {
	    String sql = "INSERT INTO exoplanets (P-ID) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, planet.ordinal());	        

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverLastActivity(LocalDateTime last_activity) {
	    String sql = "INSERT INTO exoplanets (Last Activity) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setTimestamp(1, Timestamp.valueOf(last_activity));
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverLastInput(String lastInput) {
	    String sql = "INSERT INTO exoplanets (Last Input) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, lastInput);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverClientId(int clientId) {
	    String sql = "INSERT INTO exoplanets (ClientID) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, clientId);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverGround(Ground ground) {
	    String sql = "INSERT INTO exoplanets (G-ID) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	    	
	        pstmt.setInt(1, ground.ordinal());

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverDirection(Direction direction) {
	    String sql = "INSERT INTO exoplanets (D-ID) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, direction.ordinal());

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverXCoord(int xCoord) {
	    String sql = "INSERT INTO exoplanets (PosX) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, xCoord);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverYCoord(int yCoord) {
	    String sql = "INSERT INTO exoplanets (PosY) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, yCoord);	        

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverTimestamp(LocalDateTime timestamp) {
	    String sql = "INSERT INTO exoplanets (Timestamp) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setTimestamp(1, Timestamp.valueOf(timestamp));

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverTextprotocol(String textprotocol) {
	    String sql = "INSERT INTO exoplanets (Textprotocoll) VALUES (?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, textprotocol);	        

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertLastActivity(String name, double duration, boolean success) {
		String sql = "INSERT INTO lastActivity (name, duration, successful) VALUES (?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, name);
	        pstmt.setDouble(2, duration);
	        pstmt.setBoolean(3, success);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertStatusHistory(int roverId, int clientId, int activityId, JSONObject history, boolean isCrashed, String errorProtocol, String lastError) {
		String sql = "INSERT INTO StatusHistory (r-id, c-id, a-id, messdatenhistorie, isCrashed, errorProtocoll, lastError) VALUES (?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, roverId);
	        pstmt.setInt(2, clientId);
	        pstmt.setInt(3, activityId);
	        pstmt.setString(4, history.toString());
	        pstmt.setBoolean(5, isCrashed);
	        pstmt.setString(6, errorProtocol);
	        pstmt.setString(7, lastError);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	  
}
