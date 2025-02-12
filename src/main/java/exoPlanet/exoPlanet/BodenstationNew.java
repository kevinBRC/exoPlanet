package exoPlanet.exoPlanet;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.json.JSONArray;
import org.json.JSONObject;

public class BodenstationNew {
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private JFrame frame;
    private JTabbedPane tabbedPane;
    private JPanel mainmenuPanel;
    private JPanel roverPanel;
    private JPanel roverContainer;
    private Map<Integer, JPanel> roverControlPanels = new HashMap<>();
	private DatabaseManager dm;
	private JSONObject roverAlive;
	private int planet = -1;

	public BodenstationNew() {
		this.roverAlive = new JSONObject();
    	this.roverAlive.put("amountOfRover", 0);							// amount of active rover (created and not exited)
    	this.roverAlive.put("latestId", 0);									// latest given id
    	this.roverAlive.put("rover", new JSONArray());						// ids of deployed rover that aren't exited yet
    	this.roverAlive.put("alreadyDeployed", new JSONArray());			// all ids of deployed rover (also those who are exited)
		this.dm = new DatabaseManager( "jdbc:mariadb://localhost:3306/exoplanet", "root", "1182");
		initializeGUI();
		
	}

	public void startConnection(String ip, int port) {
		try {
			clientSocket = new Socket(ip, port);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
//			LOG.debug("Error when initializing connection", e);
		}

	}

	public boolean sendToServer(String msg, int id) {
		try {
			out.println(msg);
			updateBuffer(in.readLine(), id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void initializeGUI() {
        // Erstelle das Hauptfenster
        frame = new JFrame("Bodenstation GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Zentriert das Fenster

        // Erstelle das TabbedPane
        tabbedPane = new JTabbedPane();

        // Erstelle die Panels für jeden Tab
        mainmenuPanel = new JPanel();
        roverPanel = new JPanel();
        roverContainer = new JPanel();
        roverContainer.setLayout(new BoxLayout(roverContainer, BoxLayout.Y_AXIS));
        
        roverPanel.setLayout(new BorderLayout());
        roverPanel.add(new JScrollPane(roverContainer), BorderLayout.CENTER);

        JButton btnConnect = new JButton("Connect to rover server");
        btnConnect.addActionListener(e -> {
//        	boolean success = this.connectToServer();
        	startConnection("localhost", 8150);
//        	if (success)
//        	{
//        		JOptionPane.showMessageDialog(frame, "Connected to rover server");
//        	}
//        	else
//        	{
//        		JOptionPane.showMessageDialog(frame, "Failed to connect to rover server");
//        	}
//        	this.start();
//        	bs.updateBuffer();
//        	updateBuffer();
        });    
        
        
        JButton btnCreatRover = new JButton("Create Rover");
        btnCreatRover.addActionListener(e -> {
        	if(this.roverAlive.getInt("amountOfRover") < 5)
        	{        		
        		this.createRover();
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(frame, "Maximum of 5 Rover reached");
        	}
        });
       
//		JToggleButton advancedModeButton = new JToggleButton("Turn On Advanced Mode");
//		advancedModeButton.addActionListener(e ->{
//            this.advancedModeOn = advancedModeButton.isSelected();
//		});

        mainmenuPanel.add(btnConnect);
        mainmenuPanel.add(btnCreatRover);
//        mainmenuPanel.add(advancedModeButton);
//        
        // Füge die Panels als Tabs hinzu
        tabbedPane.addTab("Mainmenu", mainmenuPanel);
        tabbedPane.addTab("Rover", roverPanel);
        
        // Füge das TabbedPane zum Frame hinzu
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Mache das Fenster sichtbar
        frame.setVisible(true);
    }
	
	public void addRoverControlPanel(int roverId) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder("Rover " + roverId));
            panel.setLayout(new FlowLayout());

            // Erstelle Buttons für die verschiedenen Befehle
            JButton btnScan = new JButton("Scan");
            btnScan.addActionListener(e -> {
            	
            	JSONArray cmd = new JSONArray();
            	cmd.put("scan");
            	cmd.put(roverId);
            	handleUserInput(cmd);
            	
            });
            
            JButton btnMove = new JButton("Move");
            btnMove.addActionListener(e -> {

                JSONArray cmd = new JSONArray();
                cmd.put("move");
                cmd.put(roverId);
                handleUserInput(cmd);
                
            });
            
            JButton btnMoveScan = new JButton("Scan and Move");
            btnMoveScan.addActionListener(e -> {

                    JSONArray cmd = new JSONArray();
                    cmd.put("mvscan");
                    cmd.put(roverId);
                    handleUserInput(cmd);
                
            });

            JButton btnLand = new JButton("Land");
            btnLand.addActionListener(e -> {
                // Erstellen Sie ein Panel mit GridLayout für eine geordnete Darstellung
                JPanel panelCoord = new JPanel(new GridLayout(3, 2, 5, 5));

                // X-Koordinate
                JLabel lblX = new JLabel("X-Coordinate:");
                JTextField txtX = new JTextField();

                // Y-Koordinate
                JLabel lblY = new JLabel("Y-Coordinate:");
                JTextField txtY = new JTextField();


                // Komponenten dem Panel hinzufügen
                panelCoord.add(lblX);
                panelCoord.add(txtX);
                panelCoord.add(lblY);
                panelCoord.add(txtY);
                // Zeigen Sie das Dialogfenster an
                int result = JOptionPane.showConfirmDialog(
                    frame,
                    panelCoord,
                    "Enter Coordinates and Choose a Planet",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        // Koordinaten parsen
                        int x = Integer.parseInt(txtX.getText().trim());
                        int y = Integer.parseInt(txtY.getText().trim());


                        // Erstellen Sie den JSON-Befehl
                        JSONArray cmd = new JSONArray();
                        cmd.put("land");
                        cmd.put(roverId);
                        cmd.put(x);
                        cmd.put(y);
                        
                        // Führen Sie den Befehl aus
                        handleUserInput(cmd);
                    } catch (NumberFormatException ex) {
                        // Fehlermeldung, falls ungültige Eingaben vorliegen
                        JOptionPane.showMessageDialog(
                            frame,
                            "Please enter valid integers for both coordinates.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            });

            JButton btnRotate = new JButton("Rotate");
            btnRotate.addActionListener(e -> {
            	String[] options = {"Left", "Right"};
            	String direction = (String) JOptionPane.showInputDialog(
                        frame,
                        "Choose the direction:",
                        "Move Rover",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        "Left");
                
                if (direction != null) {
                    // Erstelle den JSON-Befehl mit Richtung
                    JSONArray cmd = new JSONArray();
                    cmd.put("rotate");
                    cmd.put(roverId);
                    cmd.put(direction.toLowerCase());
                    handleUserInput(cmd);
                }
            });

            JButton btnExit = new JButton("Exit");
            btnExit.addActionListener(e -> {
                JSONArray cmd = new JSONArray();
                cmd.put("exit");
                cmd.put(roverId);
                handleUserInput(cmd);
            });

            JButton btnGetPos = new JButton("Get Position");
            btnGetPos.addActionListener(e -> {
                JSONArray cmd = new JSONArray();
                cmd.put("getpos");
                cmd.put(roverId);
                handleUserInput(cmd);
            });

            JButton btnCharge = new JButton("Charge");
            btnCharge.addActionListener(e -> {
                JSONArray cmd = new JSONArray();
                cmd.put("charge");
                cmd.put(roverId);
                handleUserInput(cmd);
            });

            JButton btnGetCharge = new JButton("Get Charge");
            btnGetCharge.addActionListener(e -> {
                JSONArray cmd = new JSONArray();
                cmd.put("getcharge");
                cmd.put(roverId);
                handleUserInput(cmd);
            });
            
            JButton btnAutopilot = new JButton("Autopilot");
            btnAutopilot.addActionListener(e -> {
            	String[] options = {"On", "Off"};
            	String mode = (String) JOptionPane.showInputDialog(
                        frame,
                        "Turn Autopilot:",
                        "Turn Autopilot",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        "Off");
                
                if (mode != null) {
                    JSONArray cmd = new JSONArray();
                    cmd.put("autopilot");
                    cmd.put(roverId);
                    cmd.put(mode);
                    handleUserInput(cmd);
                }
            });

            // Füge die Buttons dem Panel  
            panel.add(btnLand);
            panel.add(btnScan);
            panel.add(btnMove);
            panel.add(btnMoveScan);
            panel.add(btnRotate);
            panel.add(btnExit);
//			if (this.advancedModeOn)
//			{
//				panel.add(btnGetPos);
//				panel.add(btnCharge);
//				panel.add(btnGetCharge);
//				panel.add(btnAutopilot);
//			}

            // Füge das Rover-Panel dem Rover-Container hinzu
            roverContainer.add(panel);
            roverContainer.revalidate();
            roverContainer.repaint();

            // Verfolge das Panel in der Map
            roverControlPanels.put(roverId, panel);
        });
    }
    
    /*
     * @brief: Entfernt das Rover-Kontrollpanel aus der GUI
     */
    public void removeRoverControlPanel(int roverId) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = roverControlPanels.get(roverId);
            if (panel != null) {
                roverContainer.remove(panel);
                roverContainer.revalidate();
                roverContainer.repaint();
                roverControlPanels.remove(roverId);
            }
        });
    }

	/*
	 * @brief: Executes a command based on the user input
	 * @param: The user input
	 */
	private void handleUserInput(JSONArray input)
	{
		String command = "";
		int digit = -1;
		String appendix = "";
		int[] coords = new int[]{-1, -1};
		boolean autopilot = false;
		
		if(input.length() == 1)
		{
			command = input.getString(0);
		}
		else if(input.length() == 2)
		{
			command = input.getString(0);
			digit = input.getInt(1);
		}
		else if(input.length() == 3)
		{
			command = input.getString(0);
			digit = input.getInt(1);
			appendix = input.getString(2);
		}
		else if(input.length() == 4)
		{
			command = input.getString(0);
			digit = input.getInt(1);
			coords = new int[]{input.getInt(2), input.getInt(3)};
		}

		
		try 
		{
			switch (command.toLowerCase()) 
			{
				case "deploy":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed"); 
					this.deployRover(digit);
					break;
			
				case "move":
//					if(digit < 0)
//						throw new IllegalArgumentException("No digit was passed");
//					if (appendix.equals(""))
//						throw new IllegalArgumentException("No appendix was passed");
					this.moveRover(digit);
					break;
					
				// TODO add planet to land on
				case "land":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (coords[0] == -1 && coords[1] == -1)
						throw new IllegalArgumentException("No Coords was passed");
					this.landRover(digit, coords[0], coords[1]);
					break;
				
				case "scan":
//					if(digit < 0)
//						throw new IllegalArgumentException("No digit was passed");
//					String message = "{'type': 'SCAN',\n 'id': '" + digit + "'}";
					this.scan(digit);
					break;
				
				case "mvscan":
//					if(digit < 0)
//						throw new IllegalArgumentException("No digit was passed");
//					if (appendix.equals(""))
//						throw new IllegalArgumentException("No appendix was passed");
					this.mvscan(digit);
					
					break;
				
				case "rotate":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					this.rotateRover(digit, appendix);
					break;
	
				case "exit":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.exitRover(digit);
					break;
	
				case "getpos":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.getPos(digit);
					break;
	
				case "charge":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.charge(digit);
					break;
					
				case "getcharge":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.getCharge(digit);
					break;
					
				case "autopilot":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					
					if (appendix.equals("On"))
					{
						autopilot = true;
					}
					else
					{
						autopilot = false;
					}
					this.setAutopliot(digit, autopilot);
					break;
				default:
					System.err.println("Invalid Command: "+ input);
					break;

			}
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("Invalid Command: " + input);
		}
		
	

		
	}


	public boolean createRover()
	{	
		if(this.roverAlive.getInt("amountOfRover") < 5) 
		{
			int newAmount = (this.roverAlive.getInt("amountOfRover") + 1);		// Increment amount of rover
			int newId = this.dm.getMaxRoverID() + 1; //(this.roverAlive.getInt("latestId") + 1);				// increment id
			this.roverAlive.put("latestId", newId);								// save the new latest id
			JSONArray newRover = this.roverAlive.getJSONArray("rover");			// add latest id
			newRover.put(newId);
			
			this.roverAlive.put("rover", newRover);								// put the newest values into the JSONObject
			this.roverAlive.put("amountOfRover", newAmount);
			
//			JSONObject message = new JSONObject();
//			message.put("type", "CREATE");
//			message.put("id", newId);
//			boolean success = sendToServer(message.toString(),newId);
			
//			if(success)
//			{
			boolean success = deployRover(newId);
//			}
			
			if(success) {
                this.addRoverControlPanel(newId); 					// GUI aktualisieren
            }
			
			return success;
		}
		else 
		{
			return false;														// already maximum amount of rover
		}
		
	}

	
	
	/*
	 * @brief: Returns all existing rover
	 * @retVal: all existing rover
	 */
	public JSONObject getAllRoverAlive()
	{
		return this.roverAlive.getJSONObject("rover");
	}
	
	
	/*
	 * @brief: Checks if a rover is alive
	 * @retVal: boolean whether the rover is alive
	 */
	public boolean checkIfRoverAlive(int id) 
	{
		int tempId;
		for(Object val : this.roverAlive.getJSONArray("rover"))					// check if the rover that wants to be exited is currently alive 
		{
			tempId = (int)val;
			if (tempId == id)
			{
				return true;
			}
		}
		return false;
	}
	
	/*
	 * @brief: Sends a landing command 
	 * @retVal: boolean whether the landing was successful
	 */
	
	
	/*
	 * @brief: Sends a rotate command
	 * @retVal: boolean whether the rotation was successful
	 */
//	public boolean rotate(int id, String direction)
//	{
//		if(!checkIfRoverAlive(id))
//			return false;
//		
//		JSONObject message = new JSONObject();
//		
//		message.put("type", "ROTATE");
//		message.put("id", id);
//		message.put("rotation", direction.toUpperCase());
//		
//		return sendToServer(message.toString(), id);
//	}
	
	/*
	 * @brief: Sends a get position command
	 * @retVal: boolean whether the getPos was successful
	 */
	public boolean getPos(int id) 
	{
		if(!checkIfRoverAlive(id))
			return false;
		
		JSONObject message = new JSONObject();
		
		message.put("type", "GETPOS");
		message.put("id", id);

		return sendToServer(message.toString(), id);
		// The answer will be read by the run method and handled by the handleNotification method
	}
	
	/*
	 * @brief: Sends a charge command
	 * @retVal: boolean whether the charge was successful
	 */
	public boolean charge(int id)
	{
		if(!checkIfRoverAlive(id))
			return false;
		
		JSONObject message = new JSONObject();
		
		message.put("type", "CHARGE");
		message.put("id", id);

		return sendToServer(message.toString(), id);
		
	}
	
	/*
	 * @brief: Sends a get charge command
	 * @retVal: boolean whether the get charge was successful
	 */
	public boolean getCharge (int id)
	{
		if(!checkIfRoverAlive(id))
			return false;
		
		JSONObject message = new JSONObject();
		
		message.put("type", "GET_CHARGE");
		message.put("id", id);

		return sendToServer(message.toString(), id);
		// The answer will be read by the run method and handled by the handleNotification 
	}
	
	/*
	 * @brief: Sends a get charge command
	 * @retVal: boolean whether the get charge was successful
	 */
	public boolean setAutopliot (int id, boolean autopilot)
	{
		if(!checkIfRoverAlive(id))
			return false;
		
		JSONObject message = new JSONObject();
		
		message.put("type", "SWITCH_AUTOPILOT");
		message.put("id", id);
		message.put("autopilot", autopilot);

		return sendToServer(message.toString(), id);
		// The answer will be read by the run method and handled by the handleNotification 
	}
	
	/*
	 * @brief: reacts to the answers of the rover server saved into the buffer
	 */
	private void updateBuffer(String msg, int id)
	{
		System.out.println(msg);
		JSONObject entry = new JSONObject(msg);
		
		System.out.println(entry);
		
		String type = entry.getString("CMD");
//		boolean success = entry.getBoolean("success");
		int highestA_id = this.getNewHighestPrimaryKey("lastActivity","a_id");
		this.dm.insertLastActivity(highestA_id, type, true);
		int highestS_id = this.getNewHighestPrimaryKey("statusHistory", "s_id");
		switch (type.toUpperCase()) 
		{
			case "INIT":
				if(this.planet  -1 < 0)
				{
					this.planet = this.convertPlanetStringToInt("default");//entry.getString("planet"));
				}
				this.dm.insertRover(id, "rover", this.planet, highestA_id, 1, 1, 0, 0, LocalDateTime.now(), "rover deployed", 100, -1);
				this.dm.insertStatusHistory(highestS_id, id, highestA_id, "", false, "", "");
				break;

			case "MOVED":
				this.dm.updateRoverXCoord(entry.getJSONObject("POSITION").getInt("X"), id);
				this.dm.updateRoverYCoord(entry.getJSONObject("POSITION").getInt("Y"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;
			case "LANDED":				
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
//				if (entry.getBoolean("crashed"))
//				{
//					this.dm.updateStatusHistoryIsCrashed(id, true);
//				}
				break;


			case "SCANED":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("GroundPosMapping","m_id"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("MEASURE").getString("GROUND")),this.dm.getRobotX(id), this.dm.getRobotY(id), entry.getJSONObject("MEASURE").getFloat("TEMP"));
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;
			case "MVSCANED":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("GroundPosMapping","m_id"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("MEASURE").getString("GROUND")), entry.getJSONObject("POSITION").getInt("X"), entry.getJSONObject("POSITION").getInt("Y"), entry.getJSONObject("MEASURE").getInt("TEMP"));
				this.dm.updateRoverXCoord(entry.getJSONObject("POSITION").getInt("X"), id);
				this.dm.updateRoverYCoord(entry.getJSONObject("POSITION").getInt("Y"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
//				if (entry.getBoolean("crashed"))
//				{
//					this.dm.updateStatusHistoryIsCrashed(id, true);
//				}
				break;

			case "ROTATED":
				this.dm.updateRoverDirection(convertDirectionStringToInt(entry.getString("DIRECTION")), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;

			case "EXIT":
				this.dm.updateStatusHistoryIsCrashed(id, true);								// Exit = crashed
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;

			case "GETPOS":
				this.dm.updateRoverXCoord(entry.getJSONObject("POSITION").getInt("X"), id);
				this.dm.updateRoverYCoord(entry.getJSONObject("POSITION").getInt("Y"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;

			case "CHARGE":
				break;
			case "GET_CHARGE":
				this.dm.updateRoverCharge(entry.getInt("charge"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				break;
			case "SWITCH_AUTOPILOT":
				break;
			case "ERROR":
				this.dm.updateStatusHistoryLastError(id, entry.getString("ERROR"));
				this.dm.updateStatusHistoryErrorProtocoll(id, entry.getString("errorMessage"));
				this.dm.updateStatusHistoryIsCrashed(id, entry.getBoolean("crashed"));
				this.dm.updateRoverXCoord(entry.getJSONObject("POSITION").getInt("X"), id);
				this.dm.updateRoverYCoord(entry.getJSONObject("POSITION").getInt("Y"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				this.showError(entry.getString("errorMessage"));
				break;
			case "CRASHED":
				this.dm.updateStatusHistoryLastError(id, entry.getString("errorMessage"));
				this.dm.updateStatusHistoryErrorProtocoll(id, entry.getString("errorMessage"));
				this.dm.updateStatusHistoryIsCrashed(id, entry.getBoolean("crashed"));
				this.dm.updateRoverXCoord(entry.getJSONObject("POSITION").getInt("X"), id);
				this.dm.updateRoverYCoord(entry.getJSONObject("POSITION").getInt("Y"), id);
				this.dm.updateStatusHistoryActivityId(id, highestA_id);
				this.showError(entry.getString("errorMessage"));
				break;

			default:
				System.err.println("Invalid answer: "+ type);
				break;
		}

	}
		
	
	/*
	 * @brief: Looks for new content within the buffer. If new content is detected it handles it
	 */
	public void showError(String error)
	{
		SwingUtilities.invokeLater(() -> {
	        JOptionPane.showMessageDialog(null, "Following error occurred: " + error, "Error", JOptionPane.ERROR_MESSAGE);
	    });
	}
	
	public int getNewHighestPrimaryKey(String table, String searchedKey) throws IllegalArgumentException 
	{
		int highestKey = this.dm.getHighestPrimaryKey(table, searchedKey);
		
		if(highestKey >= 0)
		{
			return highestKey + 1;
		}

		else
		{
			throw new IllegalArgumentException("No highest Primary Key was found");
		}
	}

	public int convertPlanetStringToInt(String planet)
	{
		switch(planet.toLowerCase())
		{
			case "default":
				return 0;
			
			case "io":
				return 1;

			case "pandora":
				return 2;
			default:
				break;
		}
		return -1;
	}

	public int convertGroundStringToInt(String ground)
	{
		switch(ground.toLowerCase())
		{
			case "nichts":
				return 0;
			
			case "sand":
				return 1;

			case "geroell":
				return 2;
			
			case "fels":
				return 3;
			
			case "wasser":
				return 4;
			
			case "pflanzen":
				return 5;
			
			case "morast":
				return 6;
			
			case "lava":
				return 7;
			
			default:
				break;
		}
		return -1;
	}

	public int convertDirectionStringToInt(String direction)
	{
		switch(direction.toLowerCase())
		{
			case "north":
				return 0;
			
			case "west":
				return 1;

			case "east":
				return 2;
			
			case "south":
				return 3;
						
			default:
				break;
		}
		return -1;
	}
	
	
	public boolean deployRover(int id){
//		for(int i = 0; i < this.roverEntries.size(); i++)
//		{
//			if(this.roverEntries.get(i) == null)
//			{
//				Rover rover = new Rover(id);
//				this.roverEntries.set(i, rover);
    	
    	
    	JSONObject message = new JSONObject();
    	message.put("CMD", "orbit");
    	message.put("NAME", "" + id);
    	return sendToServer(message.toString(), id);
    	
//				return true;
//			}
//		}
//		return false;
//    	return null;
    }
    public boolean moveRover(int id){
    	JSONObject message = new JSONObject();
    	message.put("CMD", "move");
    	return sendToServer(message.toString(), id);
    }
    
    public boolean scan(int id){
    	JSONObject message = new JSONObject();
    	message.put("CMD", "scan");
    	return sendToServer(message.toString(), id);
    }
    
    
    public boolean mvscan(int id){
    	JSONObject message = new JSONObject();
    	message.put("CMD", "mvscan");
    	return sendToServer(message.toString(), id);
    }
    
	public boolean rotateRover(int id, String rotation){
		JSONObject message = new JSONObject();
		message.put("CMD", "rotate");
		message.put("ROTATION", rotation.toUpperCase());
		return sendToServer(message.toString(), id);
	}
	
	public boolean landRover(int id, int x, int y){				
		JSONObject message = new JSONObject();
		message.put("CMD", "land");
		message.put("POSITION", new JSONObject().put("X", x).put("Y", y).put("DIRECTION", "EAST"));
		return sendToServer(message.toString(), id);				
	}
	
	public boolean exitRover(int id)
	{
		JSONObject message = new JSONObject();
		message.put("CMD", "exit");
		
		return sendToServer(message.toString(), id);
	}
}
