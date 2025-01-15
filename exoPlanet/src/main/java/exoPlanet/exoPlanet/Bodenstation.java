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
		public void run() {
		    try {
		        String serverMessage;
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
				
				String message = "{'type': 'command'\n 'content': 'CREATE_ROVER ID_" + newId + "}";		
				sendToServer(message);												// send create command
				
				return true;
			}
			else 
			{
				return false;														// already maximum amount of rover
			}
			
		}

		//TODO sends command to server to deploy a rover
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
			
			String message = "{'type': 'command'\n 'content': 'DEPLOY_ROVER ID_" + id + "}";
			sendToServer(message);
			
			return true;
		}
		
		// TODO check if message received Server
		public boolean sendToServer(String message) 
		{
			this.writeInput.print(message);
			this.writeInput.flush();			
			return true;
		}
		
		public JSONObject getAllRoverAlive()
		{
			return this.roverAlive.getJSONObject("rover");
		}
		
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
			
			String message = "{'type': 'command'\n 'content': 'EXIT_ROVER ID_" + id + "}";
			sendToServer(message);
			
			return true;
		}
		
		public void calcNewRoverPosition(JSONObject movedUnits) 
		{
		}
		
		public boolean move(int id)
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
			String message = "{'type': 'command'\n 'content': 'MOVE_ROVER ID_" + id + "_" + direction + "}";
			sendToServer(message);
			
			return true;
			
		}
		
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

	// TODO: program this method
	private void handleUserInput(JSONArray input)
	{
		String command = "";
		int digit = 0;
		if(input.length() == 1)
		{
			command = input.getString(0);
		}
		else if(input.length() == 2)
		{
			command = input.getString(0);
			digit = input.getInt(1);
		}
		
		switch (command) {
		
			case "deplpoy":
			case "DEPLOY":
				this.rm.deployRover(digit);
				break;
		
			case "create":
			case "CREATE":
				this.rm.createRover();
				break;
		
			case "move":
			case "MOVE":
			
				break;
			case "land":
			case "LAND":
				break;
			
			case "scan":
			case "SCAN":
				break;
			
			case "mvscan":
			case "MVSCAN":
				break;
			
			case "rotate":
			case "ROTATE":
				break;
			
			case "exit":
			case "EXIT":
				break;

			case "getpos":
			case "GETPOS":
				break;

			case "charge":
			case "CHARGE":
				break;
				
			case "getcharge":
			case "GETCHARGE":
				break;
			default:
				System.err.println("Invalid Command: "+ input);
				break;

		}

	}

	// TODO programm this method
	private void handleNotification()
	{

	}


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

	
	private void handleRoverServerMessage()
	{
	}
	
	// TODO deletes the oldest entry after it was worked through
	private void updateBuffer()
	{
	}
	

	

	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation(null, 0, null);
		bs.readUserInput();
		
	}

}
