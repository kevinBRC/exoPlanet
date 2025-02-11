package main.java.exoPlanet.exoPlanet;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

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


public class Bodenstation {
	private DatabaseManager dm;
	private RoverManager rm;
	private String serverAddress;
	private int port;
	private String databaseAddress;
	private boolean hasNotification;
	private Queue<JSONObject> buffer;
	private JSONArray notifications;
	private BufferedReader reader;
	private JSONObject allRoverInformation;
	private boolean advancedModeOn;
	private int planet;
	
	private JFrame frame;
    private JTabbedPane tabbedPane;
    private JPanel mainmenuPanel;
    private JPanel roverPanel;
    private JPanel roverContainer;
    private Map<Integer, JPanel> roverControlPanels = new HashMap<>();

	class RoverManager extends Thread{
		private PrintWriter writeInput;
		private BufferedReader readInput;
		private Socket client;
		private JSONObject roverAlive;

        public RoverManager() 
		{
        	this.roverAlive = new JSONObject();
        	this.roverAlive.put("amountOfRover", 0);							// amount of active rover (created and not exited)
        	this.roverAlive.put("latestId", 0);									// latest given id
        	this.roverAlive.put("rover", new JSONArray());						// ids of deployed rover that aren't exited yet
        	this.roverAlive.put("alreadyDeployed", new JSONArray());			// all ids of deployed rover (also those who are exited)
		}

        /*
         * @brief: connects to the rover server and build all reader/writer to it
         * @retVal: boolean whether the connection was successful
         */
		public boolean connectToServer()
		{
			try 
			{
				this.client = new Socket(serverAddress, port);
				this.writeInput = new PrintWriter(client.getOutputStream());
				this.readInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
				return true;
			} 
			catch (IOException ex) 
			{
				return false;
			}
		}

		
		/*
		 * @brief: Reads the messages from the rover server as a thread
		 */
		public void run() {
		    try {
		        String serverMessage;
		        
		        // if a message is detected -> write into buffer of the Bodenstation
		        while ((serverMessage = this.readInput.readLine()) != null) {
		        	try 
		        	{
		        		JSONObject entry = new JSONObject(serverMessage);
			        	buffer.add(entry);
		        	}
		        	catch (Exception e)
		        	{
		        		System.err.println("Invalid JSON-format: " + serverMessage);
		        	}
		        	
		        }
		    } catch (IOException e) {
		        System.err.println("Error while reading the server messages: " + e.getMessage());
		    } finally {
		        
		        try {
		            if (this.readInput != null) this.readInput.close();
		            if (this.writeInput != null) this.writeInput.close();
		            if (this.client != null) this.client.close();
		        } catch (IOException e) {
		            System.err.println("Error while closing the ressources: " + e.getMessage());
		        }
		    }
		}

		/*
		 * @brief: Creates a rover in the local rover jsonObejct in order to use it later (maximum of 5 rover at the same time) and sends a create command 
		 * @retVal: boolean whether the creation was successful
		 */
		public boolean createRover()
		{	
			if(this.roverAlive.getInt("amountOfRover") < 5) 
			{
				int newAmount = (this.roverAlive.getInt("amountOfRover") + 1);		// Increment amount of rover
				int newId = (this.roverAlive.getInt("latestId") + 1);				// increment id
				this.roverAlive.put("latestId", newId);								// save the new latest id
				JSONArray newRover = this.roverAlive.getJSONArray("rover");			// add latest id
				newRover.put(newId);
				
				this.roverAlive.put("rover", newRover);								// put the newest values into the JSONObject
				this.roverAlive.put("amountOfRover", newAmount);
				
				JSONObject message = new JSONObject();
				message.put("type", "CREATE");
				message.put("id", newId);
				boolean success = sendToServer(message.toString());
				
				if(success)
				{
					success = deployRover(newId);
				}
				
				if(success) {
                    Bodenstation.this.addRoverControlPanel(newId); 					// GUI aktualisieren
                }
				
				return success;
			}
			else 
			{
				return false;														// already maximum amount of rover
			}
			
		}

		/*
		 * @brief: Sends a deployment command
		 * @retVal: boolean whether the deployment was successful
		 */
		public boolean deployRover(int id)
		{
			int internId;
			JSONArray usedId = this.roverAlive.getJSONArray("alreadyDeployed");
			int latestId = this.roverAlive.getInt("latestId");
			
			if(id > latestId)														// id to deploy is higher than the latest created rover
				return false;
			
			// check if the rover was already deployed
			for(Object value: usedId) 
			{
				internId = (int)value;
				if(id == internId)
				{
					return false;													// id was already deployed
				}
			}
			
			this.roverAlive.getJSONArray("alreadyDeployed").put(id);				// add the id to the deployed ids
			
			JSONObject message = new JSONObject();
			message.put("type", "DEPLOY");
			message.put("id", id);
		
			return sendToServer(message.toString());
		}
		
		/*
		 * @brief: Sends a message to the rover server
		 * @retVal: boolean whether the sending was successful
		 */
		public boolean sendToServer(String message) 
		{
			// this.writeInput.print(message);
			// this.writeInput.flush();			
			return true;
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
		 * @brief: Sends an exiting command
		 * @retVal: boolean whether the exiting was successful
		 */
		public boolean exitRover(int id)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			for (int i = 0; i < this.roverAlive.length(); i++) 
			{					// delete the entry of the rover
			    if (this.roverAlive.getJSONArray("rover").getInt(i) == id) 
			    {
			        this.roverAlive.getJSONArray("rover").remove(i);
			        break; 
			    }
			}
			
			int newAmount = (this.roverAlive.getInt("amountOfRover") - 1);
			this.roverAlive.put("amountOfRover", newAmount);
			
			JSONObject message = new JSONObject();
			message.put("id", id);
			message.put("type", "EXIT");
			boolean success = sendToServer(message.toString());
            
            if(success) 
            {
                Bodenstation.this.removeRoverControlPanel(id); // GUI aktualisieren
            }
            
            return success;
        }
		
		
		/*
		 * @brief: Sends a moving command
		 * @retVal: boolean whether the moving was successful
		 */
		public boolean move(int id, boolean scanAfterwards)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			JSONObject message = new JSONObject();
			
			message.put("id", id);
			if (scanAfterwards) 
			{				
				message.put("type", "SCAN_MOVE");

			}
			else
			{
				message.put("type", "SCAN");
			}
			
			return sendToServer(message.toString());
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
		public boolean land(int id, int x, int y)
		{
			if(!checkIfRoverAlive(id))
				return false;
			JSONObject message = new JSONObject();
			int[] coords = {x, y};
			
			message.put("type", "LAND");
			message.put("id", id);
			message.put("Coords", coords);
			
			return sendToServer(message.toString());
		}
		
		/*
		 * @brief: Sends a rotate command
		 * @retVal: boolean whether the rotation was successful
		 */
		public boolean rotate(int id, String direction)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			JSONObject message = new JSONObject();
			
			message.put("type", "LAND");
			message.put("id", id);
			message.put("direction", direction.toUpperCase());
			
			return sendToServer(message.toString());
		}
		
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

			return sendToServer(message.toString());
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

			return sendToServer(message.toString());
			
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

			return sendToServer(message.toString());
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

			return sendToServer(message.toString());
			// The answer will be read by the run method and handled by the handleNotification 
		}
		
		
			
	
	}
	// TODO: parameters for DatabaseManager
	public Bodenstation(String serverAddress, int port, String databaseAddress, String username, String password)
	{
		this.reader = new BufferedReader(new InputStreamReader(System.in));
		this.serverAddress = serverAddress;
		this.port = port;
		this.databaseAddress = databaseAddress;
		this.hasNotification = false;
		this.dm = new DatabaseManager(databaseAddress, username, password);
		this.rm = new RoverManager();
		this.buffer = new ArrayDeque<>();
		this.notifications = new JSONArray();
		this.advancedModeOn = false;
		this.planet = -1;
		this.rm.start();
	}
	
	 /*
     * @brief: Initializes the GUI with tabs "Hauptmenü", "Rover" and "Karte"
     */
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
        	boolean success = this.rm.connectToServer();
        	if (success)
        	{
        		JOptionPane.showMessageDialog(frame, "Connected to rover server");
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(frame, "Failed to connect to rover server");
        	}
        });    
        
        
        JButton btnCreatRover = new JButton("Create Rover");
        btnCreatRover.addActionListener(e -> {
        	if(this.rm.roverAlive.getInt("amountOfRover") < 5)
        	{        		
        		this.rm.createRover();
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(frame, "Maximum of 5 Rover reached");
        	}
        });
       
		JToggleButton advancedModeButton = new JToggleButton("Turn On Advanced Mode");
		advancedModeButton.addActionListener(e ->{
            this.advancedModeOn = advancedModeButton.isSelected();
		});

        mainmenuPanel.add(btnConnect);
        mainmenuPanel.add(btnCreatRover);
        mainmenuPanel.add(advancedModeButton);
        
        // Füge die Panels als Tabs hinzu
        tabbedPane.addTab("Mainmenu", mainmenuPanel);
        tabbedPane.addTab("Rover", roverPanel);
        
        // Füge das TabbedPane zum Frame hinzu
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Mache das Fenster sichtbar
        frame.setVisible(true);
    }
    
    /*
     * @brief: Fügt ein neues Rover-Kontrollpanel zur GUI hinzu
     */
    public void addRoverControlPanel(int roverId) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder("Rover " + roverId));
            panel.setLayout(new FlowLayout());

            // Erstelle Buttons für die verschiedenen Befehle
            JButton btnMove = new JButton("Move");
            btnMove.addActionListener(e -> {

                JSONArray cmd = new JSONArray();
                cmd.put("move");
                cmd.put(roverId);
                handleUserInput(cmd);
                
            });
            
            JButton btnMoveScan = new JButton("Scan and Move");
            btnMove.addActionListener(e -> {

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
                    cmd.put("move");
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
            panel.add(btnMove);
            panel.add(btnMoveScan);
            panel.add(btnLand);
            panel.add(btnRotate);
            panel.add(btnExit);
			if (this.advancedModeOn)
			{
				panel.add(btnGetPos);
				panel.add(btnCharge);
				panel.add(btnGetCharge);
				panel.add(btnAutopilot);
			}

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
			switch (command) 
			{
				case "deploy":
				case "DEPLOY":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed"); 
					this.rm.deployRover(digit);
					break;
			
				case "move":
				case "MOVE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					this.rm.move(digit, false);
					break;
					
				// TODO add planet to land on
				case "land":
				case "LAND":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (coords[0] == -1 && coords[1] == -1)
						throw new IllegalArgumentException("No Coords was passed");
					this.rm.land(digit, coords[0], coords[1]);
					break;
				
				case "scan":
				case "SCAN":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					String message = "{'type': 'SCAN',\n 'id': '" + digit + "'}";
					this.rm.sendToServer(message);
					break;
				
				case "mvscan":
				case "MVSCAN":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					this.rm.move(digit, true);
					break;
				
				case "rotate":
				case "ROTATE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					this.rm.rotate(digit, appendix);
					break;
	
				case "exit":
				case "EXIT":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.exitRover(digit);
					break;
	
				case "getpos":
				case "GETPOS":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.getPos(digit);
					break;
	
				case "charge":
				case "CHARGE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.charge(digit);
					break;
					
				case "getcharge":
				case "GETCHARGE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.getCharge(digit);
					break;
					
				case "autopilot":
				case "AUTOPILOT":
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
					this.rm.setAutopliot(digit, autopilot);
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

	/*
	 * @brief: Looks for new content within the buffer. If new content is detected it handles it
	 */
	public void showError(String error)
	{
		SwingUtilities.invokeLater(() -> {
	        JOptionPane.showMessageDialog(null, "Following error occurred: " + error, "Error", JOptionPane.ERROR_MESSAGE);
	    });
	}

	/*
	 * @brief: reads the user input
	 */
	public void readUserInput() 
	{
		try
		{
			System.out.println("The Groundstation is ready for commands:");
			System.out.println("Command Syntax: {command}_{Digit if needed}");
			String input;
			while ((input = this.reader.readLine()) != null) 
			{
                if ("disconnect".equalsIgnoreCase(input)) 
				{
                    System.out.println("Groundstation closed");
                    break;
                }
                
                if (input.contains("_"))
                {
                		JSONArray commandArray = new JSONArray();
                    	String[] splitInput = input.split("_");
            			String command = splitInput[0];
            			int digit = Integer.parseInt(splitInput[1]);
                    	commandArray.put(command);
                    	commandArray.put(digit);
                    	handleUserInput(commandArray);
                }
                else
                {
                	JSONArray commandArray = new JSONArray();
                	commandArray.put(input);
                	handleUserInput(commandArray);
                }
                System.out.println("The Groundstation is ready for the next command: ");
			} 
		}   
		catch (IOException ex)
		{
			System.err.println("Error during reading user input: " + ex);
		}
	}

	
	/*
	 * @brief: reacts to the answers of the rover server saved into the buffer
	 */
	private void updateBuffer()
	{
		JSONObject entry = new JSONObject();
		do
		{
			entry = this.buffer.poll();
		}while (entry == null);
		String type = entry.getString("type");
		boolean success = entry.getBoolean("success");
		int highestA_id = this.getNewHighestPrimaryKey("a_id", "lastActivity");
		this.dm.insertLastActivity(highestA_id, type, success);
		int highestS_id = this.getNewHighestPrimaryKey("s_id", "statusHistory");
		switch (type) 
		{
			case "DEPLOY":
				if(this.planet < 0)
				{
					this.planet = this.convertPlanetStringToInt(entry.getString("planet"));
				}
				this.dm.insertRover(entry.getInt("id"), "rover", this.planet, -1, 1, -1 , -1, -1, -1, LocalDateTime.now(), "rover deployed", -1, -1);
				this.dm.insertStatusHistory(highestS_id, entry.getInt("id"), highestA_id, "", entry.getBoolean("success"), "", "");
				break;

			case "MOVE":
			case "LAND":
				this.dm.updateRoverXCoord(entry.getJSONArray("position").getInt(0), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getJSONArray("position").getInt(1), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				if (entry.getBoolean("crashed"))
				{
					this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), true);
				}
				break;


			case "SCAN":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("m_id", "GroundPosMapping"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("scanResponse").getString("surface")), entry.getJSONObject("scanResponse").getJSONArray("Coords").getInt(0), entry.getJSONObject("scanResponse").getJSONArray("Coords").getInt(1), entry.getJSONObject("scanResponse").getInt("Temperature"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "MOVE_SCAN":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("m_id", "GroundPosMapping"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("scanResponse").getString("surface")), entry.getJSONObject("scanResponse").getJSONArray("Coords").getInt(0), entry.getJSONObject("scanResponse").getJSONArray("Coords").getInt(1), entry.getJSONObject("scanResponse").getInt("Temperature"));
				this.dm.updateRoverXCoord(entry.getJSONArray("position").getInt(0), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getJSONArray("position").getInt(1), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				if (entry.getBoolean("crashed"))
				{
					this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), true);
				}
				break;

			case "ROTATE":
				this.dm.updateRoverDirection(entry.getInt("direction"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;

			case "EXIT":
				this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), true);								// Exit = crashed
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;

			case "GETPOS":
				this.dm.updateRoverXCoord(entry.getJSONArray("position").getInt(0), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getJSONArray("position").getInt(1), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;

			case "CHARGE":
				break;
			case "GET_CHARGE":
				this.dm.updateRoverCharge(entry.getInt("charge"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "SWITCH_AUTOPILOT":
				break;
			case "ERROR":
				this.dm.updateStatusHistoryLastError(entry.getInt("id"), entry.getString("errorMessage"));
				this.dm.updateStatusHistoryErrorProtocoll(entry.getInt("id"), entry.getString("errorMessage"));
				this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), entry.getBoolean("crashed"));
				this.dm.updateRoverXCoord(entry.getJSONArray("position").getInt(0), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getJSONArray("position").getInt(1), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				this.showError(entry.getString("errorMessage"));
				break;

			default:
				System.err.println("Invalid answer: "+ type);
				break;
		}

	}

	public int getNewHighestPrimaryKey(String table, String searchedKey) throws IllegalArgumentException 
	{
		int highestKey = this.dm.getHighestPrimaryKey(table, searchedKey);
		
		if(highestKey > 0)
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
		switch(planet)
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
		switch(ground)
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
		switch(direction)
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
	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation("", 0, "localhost:3306", "root", "Kevin");
		DatabaseManager dm = new DatabaseManager("jdbc:mysql://localhost:3306/exoplanet?useSSL=false&serverTimezone=UTC", "root", "Kevin");
		bs.initializeGUI();
		bs.updateBuffer();
		
	}


}
