package exoPlanet.exoPlanet;
import java.io.*;
import org.json.*;
import java.net.*;
import java.util.*;


public class Bodenstation {
	private DatabaseManager dm;
	private RoverManager rm;
	private String serverAddress;
	private int port;
	private String databaseAddress;
	private boolean hasNotification;
	private Queue<JSONObject> buffer;
	private JSONArray notifications;

	class RoverManager extends Thread{
		private PrintWriter writeInput;
		private BufferedReader readInput;
		private Socket client;
		private JSONObject roverAlive;

        public RoverManager() 
		{
        	this.roverAlive = new JSONObject();
        	this.roverAlive.put("amountOfRover", 0);
        	this.roverAlive.put("latestId", 0);
        	this.roverAlive.put("rover", new JSONArray());
        	this.roverAlive.put("alreadyDeployed", new JSONArray());
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

		@Override
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
				
				String message = "{'type': 'CREATE',\n 'id':'" + newId +"'}";		
				return sendToServer(message);
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
			
			for(Object value: usedId) 
			{
				internId = (int)value;
				if(id == internId)
				{
					return false;													// id was already deployed
				}
			}
			
			this.roverAlive.getJSONArray("alreadyDeployded").put(id);				// add the id to the deployed ids
			
			String message = "{'type': 'DEPLOY',\n 'id:'" + id + "'}";			
			return sendToServer(message);
		}
		
		// TODO check if message received Server
		/*
		 * @brief: Sends a message to the rover server
		 * @retVal: boolean whether the sending was successful
		 */
		public boolean sendToServer(String message) 
		{
			this.writeInput.print(message);
			this.writeInput.flush();			
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
			
			String message = "{'type': 'EXIT',\n 'id':'" + id + "'}";
			return sendToServer(message);
		}
		
		public void calcNewRoverPosition(JSONObject movedUnits) 
		{
		}
		
		/*
		 * @brief: Sends a moving command
		 * @retVal: boolean whether the moving was successful
		 */
		public boolean move(int id, boolean scanAfterwards)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			boolean dirMatch = false;
			String message;
			Scanner scan = new Scanner(System.in);
			System.out.println("Which direction do you want to go?");
			System.out.println("Please choose: left, right, up or down");
			String direction = "";
			while(!dirMatch)
			{
				direction = scan.nextLine();
				if (direction == "right" || direction == "left" || direction == "up" || direction == "down")
				{
					dirMatch = true;
					break;
				}
				
				System.out.println("Invalid direction!");
				System.out.println("Please choose: left, right, up or down");
			}
			
			scan.close();
			direction = direction.toUpperCase();
			
			if (scanAfterwards) 
			{				
				message = "{'type': 'SCAN_MOVE',\n 'id':'" + id + ",\n 'direction':'" + direction + "'}";
			}
			else
			{
				message = "{'type': 'MOVE',\n 'id':'" + id + ",\n 'direction':'" + direction + "'}";
			}
			
			return sendToServer(message);
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
		public boolean land(int id)
		{
			if(!checkIfRoverAlive(id))
				return false;
			int xCoord;
			int yCoord;
			
			Scanner scan = new Scanner(System.in);
			while(true)
			{
				try 
				{
					System.out.println("On what X-Coordinate you want to land?");
					xCoord = scan.nextInt();
					System.out.println("On what Y-Coordinate you want to land?");
					yCoord = scan.nextInt();
					break;
				}
				catch(Exception e)
				{
					System.out.println("Failure, input wasn't a digit");
				}
			}
			scan.close();
			
			String message = "{'type': 'LAND',\n 'content':'" + id + "',\n 'x':'" + xCoord + "',\n 'y': '" + yCoord + "'}";
			
			return sendToServer(message);
		}
		
		/*
		 * @brief: Sends a rotate command
		 * @retVal: boolean whether the rotation was successful
		 */
		public boolean rotate(int id)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			boolean dirMatch = false;
			Scanner scan = new Scanner(System.in);
			System.out.println("Which direction do you want to go?");
			System.out.println("Please choose: left, right, up or down");
			String direction = "";
			while(!dirMatch)
			{
				direction = scan.nextLine();
				if (direction == "right" || direction == "left" || direction == "up" || direction == "down")
				{
					dirMatch = true;
					break;
				}
				
				System.out.println("Invalid direction!");
				System.out.println("Please choose: left, right, up or down");
			}
			
			scan.close();
			direction = direction.toUpperCase();
			
			String message = "{'type': 'ROTATE',\n 'id': '" + id + ",\n 'direction': '" + direction + "'}";
			return sendToServer(message);
		}
		
		/*
		 * @brief: Sends a get position command
		 * @retVal: boolean whether the getPos was successful
		 */
		public boolean getPos(int id) 
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			String message = "{'type': 'GETPOS',\n 'id': '" + id + "'}";
			return sendToServer(message);
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
			
			String message = "{'type': 'CHARGE',\n 'id': '" + id + "'}";
			
			return sendToServer(message);
			
		}
		
		/*
		 * @brief: Sends a get charge command
		 * @retVal: boolean whether the get charge was successful
		 */
		public boolean getCharge (int id)
		{
			if(!checkIfRoverAlive(id))
				return false;
			
			String message = "{'type': 'CHARGE',\n 'id': '" + id + "'}";
			
			return sendToServer(message);
			// The answer will be read by the run method and handled by the handleNotification 
		}
		
		
		
			
	}

	// TODO: parameters for DatabaseManager and RoverManager
	public Bodenstation(String serverAddress, int port, String databaseAddress)
	{
		this.serverAddress = serverAddress;
		this.port = port;
		this.databaseAddress = databaseAddress;
		this.hasNotification = false;
		this.dm = new DatabaseManager();
		this.rm = new RoverManager();
		this.buffer = new ArrayDeque<>();
		this.notifications = new JSONArray();
		this.rm.start();
	}

	/*
	 * @brief: Executes a command based on the user input
	 * @param: The user input
	 */
	private void handleUserInput(JSONArray input)
	{
		String command = "";
		int digit = -1;
		if(input.length() == 1)
		{
			command = input.getString(0);
		}
		else if(input.length() == 2)
		{
			command = input.getString(0);
			digit = input.getInt(1);
		}
		
		try 
		{
			switch (command) 
			{
				
				
				case "deplpoy":
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
					this.rm.move(digit, false);
					break;
				case "land":
				case "LAND":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.land(digit);
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
					this.rm.move(digit, true);
					break;
				
				case "rotate":
				case "ROTATE":
					if(digit < 0)
						throw new IllegalArgumentException("No digit was passed");
					this.rm.rotate(digit);
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
				default:
					System.err.println("Invalid Command: "+ input);
					break;

			}
		}
		catch(Exception e)
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("The Groundstation is ready for commands:");
			System.out.println("Command Syntax: {command}_{Digit if needed}");
			String input;
			while ((input = reader.readLine()) != null) 
			{
                if ("disconnect".equalsIgnoreCase(input)) 
				{
                    System.out.println("Groundstation closed");
                    break;
                }
                
                if (input.contains("_"))
                {
                	try 
                	{
                		JSONArray commandArray = new JSONArray();
                    	String[] splitInput = input.split("_");
            			String command = splitInput[0];
            			int digit = Integer.parseInt(splitInput[1]);
                    	commandArray.put(command);
                    	commandArray.put(digit);
                    	handleUserInput(commandArray);
                	}
                	catch(Exception e)
                	{
                		System.err.println("Invalid ID: " + input);
                	}
                	
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
			System.out.println("Fehler! Konsole konnte nicht eingelesen werden!");
		}
	}

	
	
	// TODO deletes the oldest entry after it was worked through
	/*
	 * @brief: deletes the oldest buffer entry
	 */
	private void updateBuffer()
	{
		this.buffer.poll();
	}
	

	

	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation(null, 0, null);
		bs.readUserInput();
		
	}

}
