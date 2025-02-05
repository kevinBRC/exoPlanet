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
	
	public static void insertRover(int roverId, String name, int planet, LocalDateTime last_activity, String lastInput, int clientId, int ground, int direction, int xCoord, int yCoord, LocalDateTime timestamp, String textprotocol) {
		String sql = "INSERT INTO robot (r-id, Name, p-id, last activity, last input, clientid, g-id, d-id, posx, posy, timestamp, textprotocol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	    	pstmt.setInt(1, roverId);
	        pstmt.setString(2, name);
	        pstmt.setInt(3, planet);
	        pstmt.setTimestamp(4, Timestamp.valueOf(last_activity));
	        pstmt.setString(5, lastInput);
	        pstmt.setInt(6, clientId);
	        pstmt.setInt(7, ground);
	        pstmt.setInt(8, direction);
	        pstmt.setInt(9, xCoord);
	        pstmt.setInt(10, yCoord);
	        pstmt.setTimestamp(11, Timestamp.valueOf(timestamp));
	        pstmt.setString(12, textprotocol);	
	        
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	}
	
	
	public static void insertRoverName(String name, int id) {
		String sql = "UPDATE robot SET name = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, name);
	        pstmt.setInt(2, id);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverPlanet(int planet, int id) {
	    String sql = "UPDATE robot SET planet = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, planet);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverLastActivity(LocalDateTime last_activity, int id) {
	    String sql = "UPDATE robot SET last activity = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setTimestamp(1, Timestamp.valueOf(last_activity));
	        pstmt.setInt(2, id);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverLastInput(String lastInput, int id) {
	    String sql = "UPDATE robot SET last input = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, lastInput);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverClientId(int clientId, int id) {
	    String sql = "UPDATE robot SET clientid = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, clientId);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverGround(int ground, int id) {
	    String sql = "UPDATE robot SET ground = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	    	
	        pstmt.setInt(1, ground);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverDirection(int direction, int id) {
	    String sql = "UPDATE robot SET direction = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, direction);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverXCoord(int xCoord, int id) {
	    String sql = "UPDATE robot SET posx = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, xCoord);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverYCoord(int yCoord, int id) {
	    String sql = "UPDATE robot SET posy = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, yCoord);
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverTimestamp(LocalDateTime timestamp, int id) {
	    String sql = "UPDATE robot SET timestamp = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setTimestamp(1, Timestamp.valueOf(timestamp));
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertRoverTextprotocol(String textprotocol, int id) {
	    String sql = "UPDATE robot SET timestamp = ? WHERE r-id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, textprotocol);	
	        pstmt.setInt(2, id);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertLastActivity(String name, boolean success) {
		String sql = "INSERT INTO lastActivity (name, successful) VALUES (?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, name);
	        pstmt.setBoolean(2, success);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void insertStatusHistory(int roverId, int clientId, int activityId, boolean isCrashed, String errorProtocol) {
		String sql = "INSERT INTO StatusHistory (r-id, c-id, a-id, isCrashed, errorProtocoll) VALUES (?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, roverId);
	        pstmt.setInt(2, clientId);
	        pstmt.setInt(3, activityId);
	        pstmt.setBoolean(4, isCrashed);


	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	  
}
