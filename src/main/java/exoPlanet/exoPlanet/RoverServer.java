package exoPlanet.exoPlanet;

import java.io.*;
import org.json.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;



public class RoverServer 
{
	private Queue<JSONObject> sharedBuffer = new LinkedList<JSONObject>();
	private PrintWriter out;
	private ConnectionListener cl;
	private Rover roverOne = null;
	private Rover roverTwo = null;
	private Rover roverThree = null;
	private Rover roverFour = null;
	private Rover roverFive = null;
	private ArrayList<Rover> roverEntries = new ArrayList<Rover>();
	
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
				System.out.println(inputLine);
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
			this.roverEntries.add(this.roverOne);
			this.roverEntries.add(this.roverTwo);
			this.roverEntries.add(this.roverThree);
			this.roverEntries.add(this.roverFour);
			this.roverEntries.add(this.roverFive);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleClientMessage() {
		boolean success;
		JSONObject entry = new JSONObject();
		JSONObject answer = new JSONObject();
		
		while(entry == null || entry.isEmpty()) 
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
				answer.put("Coords", entry.optString("Coords"));
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
				answer.put("rotation", entry.optString("rotation"));
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
	
	public JSONObject SendRoverAnswer(Rover roverInstance)
	{
		
//		PrintWriter toBS = new PrintWriter(client.getOutputStream(), true);
		while (true) 
		   { 
			if (roverInstance.getInfoFlag()) 
		       {					
				JSONObject roverAnswer = roverInstance.GetInternalBuffer();
				roverAnswer.put("id", roverInstance.GetId());
				out.println(roverAnswer.toString());
				roverInstance.setInfoFlag(false);
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
		for(int i = 0; i < this.roverEntries.size(); i++)
		{
			if(this.roverEntries.get(i) == JSONObject.NULL)
			{
				Rover rover = new Rover(id);
				this.roverEntries.add(i, rover);
				return true;
			}
		}
		return false;
	}
	
	public boolean exitRover(int id)
	{
		for(int i = 0; i < this.roverEntries.size(); i++)
		{
			if (this.roverEntries.get(i) != null)
			{
				if (this.roverEntries.get(i).GetId() == id)
				{
					this.roverEntries.set(i, null);
					return true;
				}
			}
		}
		return false;
	}

//	public boolean moveRover
	
	
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
	                    for(int i = 0; i < rs.roverEntries.size(); i++)
	            		{
	            			if (rs.roverEntries.get(i) != null)
	            			{
	            				rs.SendRoverAnswer(rs.roverEntries.get(i));
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