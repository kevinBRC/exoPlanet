package exoPlanet.exoPlanet;
import java.io.*;
import org.json.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import exoPlanet.exoPlanet.Direction;
import exoPlanet.exoPlanet.Ground;
import exoPlanet.exoPlanet.Planet;


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
		
		// TODO check if message received Server
		/*
		 * @brief: Sends a message to the rover server
		 * @retVal: boolean whether the sending was successful
		 */
		public boolean sendToServer(String message) 
		{
			//this.writeInput.print(message);
			//this.writeInput.flush();			
			//return true;
			System.out.println("Sending message: "+ message);
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
		
		
		public void calcNewRoverPosition(JSONObject movedUnits) 
		{
		}
		
		/*
		 * @brief: Sends a moving command
		 * @retVal: boolean whether the moving was successful
		 */
		public boolean move(int id, boolean scanAfterwards, String direction)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			JSONObject message = new JSONObject();
			
			message.put("id", id);
			message.put("direction", direction.toUpperCase());
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
		//this.rm.start();
		
		
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
       
        mainmenuPanel.add(btnConnect);
        mainmenuPanel.add(btnCreatRover);
        
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
            	String[] options = {"Left", "Right", "Up", "Down"};
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
            
            JButton btnMoveScan = new JButton("Scan and Move");
            btnMove.addActionListener(e -> {
            	String[] options = {"Left", "Right", "Up", "Down"};
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
                    cmd.put("mvscan");
                    cmd.put(roverId);
                    cmd.put(direction.toLowerCase());
                    handleUserInput(cmd);
                }
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
            	String[] options = {"Left", "Right", "Up", "Down"};
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

            // Füge die Buttons dem Panel hinzu
            panel.add(btnMove);
            panel.add(btnMoveScan);
            panel.add(btnLand);
            panel.add(btnRotate);
            panel.add(btnExit);
            panel.add(btnGetPos);
            panel.add(btnCharge);
            panel.add(btnGetCharge);
            panel.add(btnAutopilot);

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
			
				case "create":
				case "CREATE":
					this.rm.createRover();
					break;
			
				case "move":
				case "MOVE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					if (appendix.equals(""))
						throw new IllegalArgumentException("No appendix was passed");
					this.rm.move(digit, false, appendix);
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
					this.rm.move(digit, true, appendix);
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

	// TODO programm this method
	/*
	 * @brief: Looks for new content within the buffer. If new content is detected it handles it
	 */
	private void handleNotification()
	{
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
	 * @brief: deletes the oldest buffer entry
	 */
	private void updateBuffer()
	{
		JSONObject entry = this.buffer.poll();
		String type = entry.getString("type");
		switch (type) 
		{
			case "DEPLOY":
				boolean success = entry.getBoolean("success");
				if (success)
				{
					
				}
				
			case "CREATE":
			case "MOVE":
			case "LAND":
			case "SCAN":
			case "MOVE_SCAN":
			case "ROTATE":
			case "EXIT":
			case "GETPOS":
			case "CHARGE":
			case "GET_CHARGE":
			case "SWITCH_AUTOPILOT":
			default:
				System.err.println("Invalid answer: "+ type);
				break;
		}

	}

	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation(null, 0, null, null, null);
		bs.initializeGUI();
		//bs.updateBuffer();
		
	}

}
