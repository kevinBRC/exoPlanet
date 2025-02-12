package exoPlanet.exoPlanet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;


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
//	    return DriverManager.getConnection("jdbc:mariadb://localhost:3306/exoplanet?user=root&password=1182");
	}
	
	public void insertRover(int roverId, String name, int planet, int last_activityId, int groundId, int direction, int xCoord, int yCoord, LocalDateTime timestamp, String textprotocoll, int energy, float temp) {
		String sql = "INSERT INTO robot (r_id, Name, p_id, a_id, g_id, d_id, posx, posy, dateandtime, textprotocoll, energy, temp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
										//R_ID, Name, Temp, PosX, PosY, Energy, DateAndTime, Textprotocoll, D_ID, A_ID, P_ID, G_ID
	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	    	pstmt.setInt(1, roverId);
	        pstmt.setString(2, name);
	        pstmt.setInt(3, planet);
	        pstmt.setInt(4, last_activityId);
	        pstmt.setInt(5, groundId);
	        pstmt.setInt(6, direction);
	        pstmt.setInt(7, xCoord);
	        pstmt.setInt(8, yCoord);
	        pstmt.setTimestamp(9, Timestamp.valueOf(timestamp));
	        pstmt.setString(10, textprotocoll);	
	        pstmt.setInt(11, energy);
	        pstmt.setFloat(12, temp);
	        
	        int rowsInserted = pstmt.executeUpdate();

	        // Falls Einfügen erfolgreich war, Nachricht ausgeben
	        System.out.println(rowsInserted + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	}
	
	
	public void updateRoverName(String name, int id) {
		String sql = "UPDATE robot SET name = ? WHERE r_id = ?";

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

	public void updateRoverCharge(int charge, int id) {
		String sql = "UPDATE robot SET charge = ? WHERE r_id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, charge);
	        pstmt.setInt(2, id);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateRoverPlanet(int planet, int id) {
	    String sql = "UPDATE robot SET p_id = ? WHERE r_id = ?";

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
	
	public void updateRoverLastActivity(int last_activity, int id) {
	    String sql = "UPDATE robot SET last a_id = ? WHERE r_id = ?";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, last_activity);
	        pstmt.setInt(2, id);
	        
	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public  void updateRoverGround(int ground, int id) {
	    String sql = "UPDATE robot SET g_id = ? WHERE r_id = ?";

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
	
	public  void updateRoverDirection(int direction, int id) {
	    String sql = "UPDATE robot SET d_id = ? WHERE r_id = ?";

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
	
	public  void updateRoverXCoord(int xCoord, int id) {
	    String sql = "UPDATE robot SET posx = ? WHERE r_id = ?";

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
	
	public  void updateRoverYCoord(int yCoord, int id) {
	    String sql = "UPDATE robot SET posy = ? WHERE r_id = ?";

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
	
	public  void updateRoverTimestamp(LocalDateTime timestamp, int id) {
	    String sql = "UPDATE robot SET timestamp = ? WHERE r_id = ?";

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
	
	public  void updateRoverTextprotocoll(String textprotocol, int id) {
	    String sql = "UPDATE robot SET textprotocoll = ? WHERE r_id = ?";

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
	
	public void insertLastActivity(int a_id, String name, boolean success) {
		String sql = "INSERT INTO lastActivity (a_id, name, success) VALUES (?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	    	
	    	pstmt.setInt(1, a_id);
	        pstmt.setString(2, name);
	        pstmt.setBoolean(3, success);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public  void insertStatusHistory(int statusId, int roverId, int activityId, String messdatenhistorie, boolean isCrashed,String errorProtocoll, String lastError) {
		String sql = "INSERT INTO StatusHistory (s_id, r_id, a_id, isCrashed, errorProtocoll, lasterror) VALUES (?, ?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, statusId);
	        pstmt.setInt(2, roverId);
	        pstmt.setInt(3, activityId);
	        pstmt.setBoolean(4, isCrashed);
	        pstmt.setString(5, errorProtocoll);
	        pstmt.setString(6, lastError);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public  void updateStatusHistoryActivityId(int roverId, int activityId) {
		String sql = "UPDATE StatusHistory SET a_id = ? WHERE r_id = ?";

	    try (Connection conn = getConnection();
	        PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, activityId);
	        pstmt.setInt(2, roverId);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public  void updateStatusHistoryIsCrashed(int roverId, boolean isCrashed) {
		String sql = "UPDATE StatusHistory SET isCrashed = ? WHERE r_id = ?";

	    try (Connection conn = getConnection();
	        PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setBoolean(1, isCrashed);
	        pstmt.setInt(2, roverId);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void updateStatusHistoryLastError(int roverId, String lastError) {
		String sql = "UPDATE StatusHistory SET lastError = ? WHERE r_id = ?";

	    try (Connection conn = getConnection();
	        PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, lastError);
	        pstmt.setInt(2, roverId);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void updateStatusHistoryMessdatenhistorie(int roverId, String newData) {
		String sql = "SELECT Messdatenhistorie FROM statusHistory WHERE r_id = ?";
		String messdatenhistorie = "";

		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, roverId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				messdatenhistorie = rs.getString("messdatenhistorie");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "UPDATE StatusHistory SET messdatenhistorie = ? WHERE r_id = ?";
		String newHistorie = messdatenhistorie + "\n" + newData;


		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, newHistorie);
			pstmt.setInt(2, roverId);

			int rowsUpdated = pstmt.executeUpdate();
			System.out.println(rowsUpdated + " Zeile(n) aktualisiert.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateStatusHistoryErrorProtocoll(int roverId, String newError) {
		String sql = "SELECT errorProtocoll FROM statusHistory WHERE r_id = ?";
		String errorProtocoll = "";

		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, roverId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				errorProtocoll = rs.getString("errorProtocoll");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "UPDATE StatusHistory SET newErrorProtocoll = ? WHERE r_id = ?";
		String newErrorProtocoll = errorProtocoll + "\n" + newError;


		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, newErrorProtocoll);
			pstmt.setInt(2, roverId);

			int rowsUpdated = pstmt.executeUpdate();
			System.out.println(rowsUpdated + " Zeile(n) aktualisiert.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getHighestPrimaryKey(String table, String searchedKey)
	{
		String sql = "SELECT coalesce(MAX(" + searchedKey + "),0) FROM " + table;
		int maxId = -1; // Default-Wert, falls keine Daten vorhanden sind

		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) { // Falls ein Ergebnis vorhanden ist
				maxId = rs.getInt(1); // Die erste Spalte enthält das MAX-Ergebnis
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return maxId; // Gibt den höchsten Wert zurück (oder -1, falls kein Wert vorhanden)
	}

	public void insertGroundPosMapping(int m_id, int p_id, int g_id, int posx, int posy, float temp)
	{
		String sql = "INSERT INTO GroundPosMapping (m_id, p_id, g_id, posx, posy, temp) VALUES (?, ?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, m_id);
	        pstmt.setInt(2, p_id);
	        pstmt.setInt(3, g_id);
	        pstmt.setInt(4, posx);
	        pstmt.setInt(5, posy);
	        pstmt.setFloat(6, temp);

	        int affectedRows = pstmt.executeUpdate();
	        System.out.println(affectedRows + " row(s) inserted.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public int getRobotX(int id)
	{
		String sql = "SELECT PosX FROM Robot where R_ID=?";
		ResultSet rs = null;
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0; // Gibt den höchsten Wert zurück (oder -1, falls kein Wert vorhanden)
	}
	
	public int getRobotY(int id)
	{
		String sql = "SELECT PosY FROM Robot where R_ID=?";
		ResultSet rs = null;
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0; // Gibt den höchsten Wert zurück (oder -1, falls kein Wert vorhanden)
	}
	  
	public int getMaxRoverID()
	{
		String sql = "SELECT COALESCE(Max(R_ID),0) FROM Robot";
		ResultSet rs = null;
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0; // Gibt den höchsten Wert zurück (oder -1, falls kein Wert vorhanden)
	}

}
