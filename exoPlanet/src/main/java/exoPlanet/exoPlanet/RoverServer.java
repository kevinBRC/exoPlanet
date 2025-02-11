package main.java.exoPlanet.exoPlanet;

import java.io.*;
import org.json.*;
import java.net.*;
import java.util.Queue;
import exoPlanet.*;



public class RoverServer 
{
	private Queue<JSONObject> sharedBuffer;
	private PrintWriter out;
	private ConnectionListener cl;
	private Rover roverOne = JSONObject.NULL;
	private Rover roverTwo = JSONObject.NULL;
	private Rover roverThree = JSONObject.NULL;
	private Rover roverFour = JSONObject.NULL;
	private Rover roverFive = JSONObject.NULL;
	private JSONArray roverEntries;
	
	// This class reads the input of the client and saves it into the buffer so the main thread isn't needed for reading
	public class ConnectionListener extends Thread{
		private BufferedReader in;
		public ConnectionListener(Socket client) {
			try {
				this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void closeConnection(){
			try {
				this.in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			String inputLine;
			try {
				while ((inputLine = this.in.readLine()) != null) {
				    JSONObject input = new JSONObject(inputLine);
				    sharedBuffer.add(input);
				    
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public RoverServer(Socket client) {
		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.cl = new ConnectionListener(client);
			this.cl.start();
			this.roverEntries.put(this.roverOne);
			this.roverEntries.put(this.roverTwo);
			this.roverEntries.put(this.roverThree);
			this.roverEntries.put(this.roverFour);
			this.roverEntries.put(this.roverFive);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleClientMessage() {
		boolean success;
		JSONObject entry = new JSONObject();
		JSONObject answer = new JSONObject();
		
		while(entry == null) 
		{
			entry = this.sharedBuffer.poll();
		}
		
		switch(entry.getString("type"))
		{
			case "DEPLOY":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "DEPLOY");
				answer.put("id", entry.getInt("id"));
				sendToClient(answer);
				break;
			case "MOVE":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "MOVE");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "LAND":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "LAND");
				answer.put("id", entry.getInt("id"));
				answer.put("Coords", entry.optString("Coords"))
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "SCAN":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "SCAN");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "MOVE_SCAN":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "MOVE-AND-SCAN");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "ROTATE":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "ROTATE");
				answer.put("id", entry.getInt("id"));
				answer.put("rotation", entry.optString("rotation"))
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "EXIT":
				success = exitRover(entry.getInt("id"));
				answer.put("type", "EXIT");
				answer.put("id", entry.getInt("id"));
				sendToClient(answer);
				break;
			case "GETPOS":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "GETPOS");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "CHARGE":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "CHARGE");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "GET_CHARGE":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "GET_CHARGE");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			case "SWITCH_AUTOPILOT":
				success = deployRover(entry.getInt("id"));
				answer.put("type", "ENABLE-AUTO");
				answer.put("id", entry.getInt("id"));
				answer.put("success", success);
				sendToClient(answer);
				break;
			default:
				break;
		}
	}
	
	public JSONObject SendRoverAnswer(Rover roverInstace)
	{
		
		PrintWriter toBS = new PrintWriter(client.getOutputStream(), true);
		while (true) 
		   { 
			if (roverInstace.getInfoFlag()) 
		       {					
				JSONObject roverAnswer = roverInstance.GetInternalBuffer();
				roverAnswer.put("id", roverInstance.GetId());
				toBS.println(roverAnswer.toString());
				roverInstance.SetInfoFlagTo0();
		       } 
		       try 
		       { 
		    	   Thread.sleep(100); 
		       } 
		       catch (InterruptedException e) 
		       { 
		    	   e.printStackTrace();
		       } 
		  } 
	}
	
	public void sendToClient(JSONObject message) {
		this.out.print(message.toString());
	}
	
	// For easier handling -> detect if one rover is unused (null)
	public boolean deployRover(int id){
		for(int i = 0; i < this.roverEntries.length(); i++)
		{
			if(this.roverEntries.get(i) == JSONObject.NULL)
			{
				Rover rover = new Rover(id);
				this.roverEntries.put(i, rover);
				return true;
			}
		}
		return false;
	}
	
	public boolean exitRover(int id)
	{
		for(int i = 0; i < this.roverEntries.length(); i++)
		{
			if (this.roverEntries.get(i) != null)
			{
				if (this.roverEntries.getInt(i) == id)
				{
					this.roverEntries.put(i, JSONObject.NULL);
					return true;
				}
			}
		}
		return false;
	}

	public boolean moveRover
	
	
    public static void main(String[] args) {
        int port = 12345;  // Port, auf dem der Server horcht
        
        // Erstellen des Serversockets innerhalb eines try-with-resources-Blocks
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server gestartet. Warte auf Verbindungen...");
            try (Socket client = serverSocket.accept()) 
            {
	            // Endlosschleife, um immer wieder auf neue Verbindungen zu warten
	            while (true)
	            {
	                // Warten auf eine eingehende Client-Verbindung,
	                    System.out.println("Client verbunden: " + client.getInetAddress());
	                    RoverServer rs = new RoverServer(client);
	                    rs.handleClientMessage();
	                    for(int i = 0; i < this.roverEntries.length(); i++)
	            		{
	            			if (roverEntries.get(i) != null)
	            			{
	            				SendRoverAnswer(roverEntries.get(i));
	            			}
	            		}
	            }
	            
            } catch (IOException e) 
            {
            	System.err.println("Fehler bei der Bearbeitung der Client-Verbindung: " + e.getMessage());
            }
        } catch (IOException e) 
        {
            System.err.println("Fehler beim Erstellen des Serversockets: " + e.getMessage());
        }
    }
}