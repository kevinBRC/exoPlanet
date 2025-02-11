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
	// Attributes for functionality
	private DatabaseManager dm;					// Instance of the DatabaseManager
	private RoverManager rm;					// Instance of the Communication 
	private String serverAddress;				// IP-Address of the Server that manages the rover 
	private int port;							// Port of the Server that manages the rover
	private String databaseAddress;				// Address of the instance that hosts the database (including the used protocol)
	private boolean hasNotification;			//
	private Queue<JSONObject> buffer;			// Buffer that saves all incoming json-messages
	private BufferedReader reader;				// Reader to read the console (in case of using the console instead of the GUI)
	private boolean advancedModeOn;				// Flag whether the advanced mode is activated
	private int planet;							// id of the planet that is visited in this run
	
	// Attributes for the GUI
	private JFrame frame;
    private JTabbedPane tabbedPane;
    private JPanel mainmenuPanel;
    private JPanel roverPanel;
    private JPanel roverContainer;
    private Map<Integer, JPanel> roverControlPanels = new HashMap<>();

	// This class manages the communication with the server instance that manages the rovers
	class RoverManager extends Thread{
		private PrintWriter writeInput;					// output to the rover-managing server
		private BufferedReader readInput;				// input to the rover-managing server
		private Socket client;							// connection to the rover-managing server
		private JSONObject roverAlive;					// JSON of all existing rover

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
		 * @brief: Reads the messages from the rover server and saves it into the buffer of the Bodenstation
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
                    Bodenstation.this.addRoverControlPanel(newId); 					// update GUI 
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
		 * @param: id of the rover
		 * @retVal: boolean whether the deployment was successful
		 */
		public boolean deployRover(int id)
		{
			int internId;
			JSONArray usedId = this.roverAlive.getJSONArray("alreadyDeployed");
			int latestId = this.roverAlive.getInt("latestId");
			
			if(id > latestId)														// id to deploy is higher than the latest created rover -> can't exist
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
		 * @param: message to send
		 * @retVal: boolean whether the sending was successful
		 */
		public boolean sendToServer(String message) 
		{
			// this.writeInput.print(message);
			// this.writeInput.flush();			
			return true;
		}
		
		
		/*
		 * @brief: Sends an exiting command and removes the rover in the local roverAlive object
		 * @param: id of the rover
		 * @retVal: boolean whether the exiting was successful
		 */
		public boolean exitRover(int id)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			// delete the entry of the rover
			for (int i = 0; i < this.roverAlive.length(); i++) 				
			{					
			    if (this.roverAlive.getJSONArray("rover").getInt(i) == id) 
			    {
			        this.roverAlive.getJSONArray("rover").remove(i);
			        break; 
			    }
			}
			
			// saves new amount of rover
			int newAmount = (this.roverAlive.getInt("amountOfRover") - 1);
			this.roverAlive.put("amountOfRover", newAmount);
			
			JSONObject message = new JSONObject();
			message.put("id", id);
			message.put("type", "EXIT");
			boolean success = sendToServer(message.toString());
            
			// update GUI
            if(success) 
            {
                Bodenstation.this.removeRoverControlPanel(id);  		
            }
            
            return success;
        }
		
		
		/*
		 * @brief: Sends a moving command
		 * @param id: id of the rover
		 * @param scanAfterwards: flag if the rover should scan after it moved
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
				message.put("type", "MOVE_SCAN");

			}
			else
			{
				message.put("type", "SCAN");
			}
			
			return sendToServer(message.toString());
		}
		
		/*
		 * @brief: Checks if a rover is alive
		 * @param: id of the rover
		 * @retVal: boolean whether the rover is alive
		 */
		public boolean checkIfRoverAlive(int id) 
		{
			int tempId;
			for(Object val : this.roverAlive.getJSONArray("rover"))					// check if the rover that is searched is currently alive 
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
		 * @param id: id of the rover
		 * @param x: x-coordinate to land
		 * @param y: y-coordinate to land
		 * @retVal: boolean whether the landing was successful
		 */
		public boolean land(int id, int x, int y)
		{
			if(!checkIfRoverAlive(id))
				return false;
			JSONObject message = new JSONObject();
			
			message.put("type", "LAND");
			message.put("id", id);
			message.put("xCoord", x);
			message.put("yCoord", y);
			
			return sendToServer(message.toString());
		}
		
		/*
		 * @brief: Sends a rotate command
		 * @param id: id of the rover
		 * @param direction: direction to move to
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
		 * @param: id of the rover
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
		}
		
		/*
		 * @brief: Sends a charge command
		 * @param: id of the rover
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
		 * @param: id of the rover
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
		}
		
		/*
		 * @brief: Sends a get charge command
		 * @param id: id of the rover
		 * @param autopilot: flag if the autopilot should be activated
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
		}
		
		
			
	
	}
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
		this.advancedModeOn = false;
		this.planet = -1;
		this.rm.start();
	}
	
	 /*
     * @brief: Initializes the GUI with tabs "Hauptmenü", "Rover" and "Karte"
     */
    private void initializeGUI() {
        // create the mainframe
        frame = new JFrame("Bodenstation GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Zentriert das Fenster

        // create a TabbedPane
        tabbedPane = new JTabbedPane();

        // create a panel for every tab
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
        
        
        JButton btnCreateRover = new JButton("Create Rover");
        btnCreateRover.addActionListener(e -> {
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
        mainmenuPanel.add(btnCreateRover);
        mainmenuPanel.add(advancedModeButton);
        
        tabbedPane.addTab("Mainmenu", mainmenuPanel);
        tabbedPane.addTab("Rover", roverPanel);
        
        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
    
    /*
     * @brief: adds an own control section per rover
	 * @param: id of the rover
     */
    public void addRoverControlPanel(int roverId) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder("Rover " + roverId));
            panel.setLayout(new FlowLayout());

            // button creation for every command
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
                JPanel panelCoord = new JPanel(new GridLayout(3, 2, 5, 5));

                JLabel lblX = new JLabel("X-Coordinate:");
                JTextField txtX = new JTextField();

                JLabel lblY = new JLabel("Y-Coordinate:");
                JTextField txtY = new JTextField();


                panelCoord.add(lblX);
                panelCoord.add(txtX);
                panelCoord.add(lblY);
                panelCoord.add(txtY);
                int result = JOptionPane.showConfirmDialog(
                    frame,
                    panelCoord,
                    "Enter Coordinates and Choose a Planet",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int x = Integer.parseInt(txtX.getText().trim());
                        int y = Integer.parseInt(txtY.getText().trim());


                        JSONArray cmd = new JSONArray();
                        cmd.put("land");
                        cmd.put(roverId);
                        cmd.put(x);
                        cmd.put(y);
                        
                        handleUserInput(cmd);
                    } catch (NumberFormatException ex) {
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

            roverContainer.add(panel);
            roverContainer.revalidate();
            roverContainer.repaint();

            roverControlPanels.put(roverId, panel);
        });
    }
    
    /*
     * @brief: remove the own rover section of the gui
	 * @param: id of the rover
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
					
					autopilot = appendix.equals("On");
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
	 * @param: The occured error as a String
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
	private void handleBuffer()
	{
		JSONObject entry = new JSONObject();
		do{entry = this.buffer.poll();}while (entry == null);			// wait until an entry was detected
		String type = entry.getString("type");							// get the type of the command
		boolean success = entry.getBoolean("success");					
		int highestA_id = this.getNewHighestPrimaryKey("a_id", "lastActivity");
		this.dm.insertLastActivity(highestA_id, type, success);						// save the latest activtiy
		int highestS_id = this.getNewHighestPrimaryKey("s_id", "statusHistory");

		// check what kind of command was sent
		switch (type) 
		{
			case "DEPLOY":
				// if the planet id wasn't set yet -> set it 
				if(this.planet < 0)
				{
					this.planet = this.convertPlanetStringToInt(entry.getString("planet"));
				}
				this.dm.insertRover(entry.getInt("id"), "rover", this.planet, -1, 1, -1, -1, -1, -1, LocalDateTime.now(), "rover deployed", -1, -1);		// -1 -> not initialited yet
				this.dm.insertStatusHistory(highestS_id, entry.getInt("id"), highestA_id, entry.getBoolean("success"), "", "");
				break;

			case "MOVE":
				// same as for landing
			case "LAND":
				this.dm.updateRoverXCoord(entry.getInt("xPositionRover"), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getInt("yPositionRover"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				if (entry.getBoolean("crashed"))
				{
					this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), true);
				}
				break;


			case "SCAN":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("m_id", "GroundPosMapping"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("scanResponse").getString("surface")), entry.getJSONObject("scanResponse").getInt("xCoords"), entry.getJSONObject("scanResponse").getInt("yCoords"), entry.getJSONObject("scanResponse").getInt("Temperature"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "MOVE_SCAN":
				this.dm.insertGroundPosMapping(this.getNewHighestPrimaryKey("m_id", "GroundPosMapping"), this.planet, this.convertGroundStringToInt(entry.getJSONObject("scanResponse").getString("surface")),  entry.getJSONObject("scanResponse").getInt("xCoords"), entry.getJSONObject("scanResponse").getInt("yCoords"), entry.getJSONObject("scanResponse").getInt("Temperature"));
				this.dm.updateRoverXCoord(entry.getInt("xPositionRover"), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getInt("yPositionRover"), entry.getInt("id"));
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
				this.dm.updateRoverXCoord(entry.getInt("xPositionRover"), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getInt("yPositionRover"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;

			case "CHARGE":
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "GET_CHARGE":
				this.dm.updateRoverCharge(entry.getInt("charge"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "SWITCH_AUTOPILOT":
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				break;
			case "ERROR":
				this.dm.updateStatusHistoryLastError(entry.getInt("id"), entry.getString("errorMessage"));
				this.dm.updateStatusHistoryErrorProtocoll(entry.getInt("id"), entry.getString("errorMessage"));
				this.dm.updateStatusHistoryIsCrashed(entry.getInt("id"), entry.getBoolean("crashed"));
				this.dm.updateRoverXCoord(entry.getInt("xPositionRover"), entry.getInt("id"));
				this.dm.updateRoverYCoord(entry.getInt("yPositionRover"), entry.getInt("id"));
				this.dm.updateStatusHistoryActivityId(entry.getInt("id"), highestA_id);
				this.showError(entry.getString("errorMessage"));
				break;

			default:
				System.err.println("Invalid answer: "+ type);
				break;
		}

	}

	/*
	 * @brief: search for the highest value of the given attribute of the given table
	 * @param table: name of the table that will be searched through
	 * @param searchedKey: name of the attribute that the maximum will be searched of
	 * @retVal: incremented highest value
	 */
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

	/*
	 * @brief: converts the string of a planet to an id
	 * @param: name of the value that should be converted
	 * @retVal: id of the name
	 */
	public int convertPlanetStringToInt(String planet)
	{
		switch(planet)
		{
			case "default":
				return 1;
			
			case "io":
				return 2;

			case "pandora":
				return 3;
			default:
				break;
		}
		return -1;
	}

	/*
	 * @brief: converts the string of a ground to an id
	 * @param: name of the value that should be converted
	 * @retVal: id of the name
	 */
	public int convertGroundStringToInt(String ground)
	{
		switch(ground)
		{
			case "nichts":
				return 1;
			
			case "sand":
				return 2;

			case "geroell":
				return 3;
			
			case "fels":
				return 4;
			
			case "wasser":
				return 5;
			
			case "pflanzen":
				return 6;
			
			case "morast":
				return 7;
			
			case "lava":
				return 8;
			
			default:
				break;
		}
		return -1;
	}

	/*
	 * @brief: converts the string of a direction to an id
	 * @param: name of the value that should be converted
	 * @retVal: id of the name
	 */
	public int convertDirectionStringToInt(String direction)
	{
		switch(direction)
		{
			case "north":
				return 1;
			
			case "east":
				return 2;

			case "south":
				return 3;
			
			case "west":
				return 4;
						
			default:
				break;
		}
		return -1;
	}
	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation("", 0, "localhost:3306", "root", "Kevin");
		DatabaseManager dm = new DatabaseManager("jdbc:mysql://localhost:3306/exoplanet?useSSL=false&serverTimezone=UTC", "root", "Kevin");
		bs.initializeGUI();
		bs.handleBuffer();
		
	}


}
