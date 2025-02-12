package exoPlanet.exoPlanet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONObject;

public class EchoMultiServer {
    private static ServerSocket serverSocket;
//    private PrintWriter out;
    private Rover roverOne = null;
	private Rover roverTwo = null;
	private Rover roverThree = null;
	private Rover roverFour = null;
	private Rover roverFive = null;
	private ArrayList<Rover> roverEntries = new ArrayList<Rover>();	
	private static Queue<JSONObject> sharedBuffer = new LinkedList<JSONObject>();
	private static EchoMultiServer serv;
	
	
    
//    public static void main(String[] args) {
//        int port = 12345;  // Port, auf dem der Server horcht
//        
//        // Erstellen des Serversockets innerhalb eines try-with-resources-Blocks
////        try (ServerSocket serverSocket = new ServerSocket(port)) {   
//        serv = new EchoMultiServer();
//        serv.start(port);
//    }
    
    public EchoMultiServer() {
    	this.roverEntries.add(this.roverOne);
		this.roverEntries.add(this.roverTwo);
		this.roverEntries.add(this.roverThree);
		this.roverEntries.add(this.roverFour);
		this.roverEntries.add(this.roverFive);
		int port = 12345;  // Port, auf dem der Server horcht
        
        // Erstellen des Serversockets innerhalb eines try-with-resources-Blocks
//        try (ServerSocket serverSocket = new ServerSocket(port)) {   
//        serv = new EchoMultiServer();
        start(port);
    }

    public void start(int port) {
        try {
			serverSocket = new ServerSocket(port);
			while (true) {
				Socket client = serverSocket.accept();
				new EchoClientHandler(client).start();				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void stop() {
        try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedReader in;
		private Socket client;
		private PrintWriter writeInput;
		private BufferedReader readInput;
		private PrintWriter out;   
        

        public EchoClientHandler(Socket socket) {
//        	try {
////				this.out = new PrintWriter(socket.getOutputStream(), true);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
            this.clientSocket = socket;    
           
        }

        public void run() {
            try {
            	this.client = new Socket("localhost", 8150);
     			this.writeInput = new PrintWriter(client.getOutputStream(), true);
     			this.readInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
     			
				in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					if (".".equals(inputLine)) {
//						writeInput.println("bye");
						break;
					}
					System.out.println(inputLine);
//					out.println(inputLine);
					JSONObject input = new JSONObject(inputLine);
				    sharedBuffer.add(input);
				    handleClientMessage();
				}
				
				in.close();
				writeInput.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
     // For easier handling -> detect if one rover is unused (null)
        public JSONObject deployRover(int id){
//    		for(int i = 0; i < this.roverEntries.size(); i++)
//    		{
//    			if(this.roverEntries.get(i) == null)
//    			{
//    				Rover rover = new Rover(id);
//    				this.roverEntries.set(i, rover);
        	
        	
        	JSONObject message = new JSONObject();
        	message.put("CMD", "orbit");
        	message.put("NAME", "" + id);
        	this.writeInput.println(message);
        	this.writeInput.flush();	
        	try {
        		return new JSONObject(this.readInput.readLine());
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	
//    				return true;
//    			}
//    		}
//    		return false;
        	return null;
        }
        public JSONObject moveRover(int id){
        	JSONObject message = new JSONObject();
        	message.put("CMD", "move");
        	this.writeInput.println(message);
        	this.writeInput.flush();	
        	try {
        		return new JSONObject(this.readInput.readLine());
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	return null;
        }
        
        public JSONObject scan(int id){
        	JSONObject message = new JSONObject();
        	message.put("CMD", "scan");
        	this.writeInput.println(message);
        	this.writeInput.flush();	
        	try {
        		return new JSONObject(this.readInput.readLine());
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	return null;
        }
        
        
        public JSONObject mvscan(int id){
        	JSONObject message = new JSONObject();
        	message.put("CMD", "mvscan");
        	this.writeInput.println(message);
        	this.writeInput.flush();	
        	try {
        		return new JSONObject(this.readInput.readLine());
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	return null;
        }
        
    	public JSONObject rotateRover(int id, String rotation){
			JSONObject message = new JSONObject();
			message.put("CMD", "rotate");
			message.put("ROTATION", rotation);
			this.writeInput.println(message);
			this.writeInput.flush();	
			try {
				return new JSONObject(this.readInput.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return null;
    	}
    	
    	public JSONObject landRover(int id, int x, int y){
//    		for(int i = 0; i < this.roverEntries.size(); i++)
//    		{
//    			if(this.roverEntries.get(i) != null)
//    			{
//    				this.roverEntries.get(i).xPosition = x;
//    				this.roverEntries.get(i).yPosition = y;
//    				
    				
    				JSONObject message = new JSONObject();
    				message.put("CMD", "land");
    				message.put("POSITION", new JSONObject().put("X", x).put("Y", y).put("DIRECTION", "EAST"));
    				this.writeInput.println(message);
    				this.writeInput.flush();	
    				try {
    					return new JSONObject(this.readInput.readLine());
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				
//    				return true;
//    			}
//    		}
//    		return false;
    		return null;
    	}
    	
    	public JSONObject exitRover(int id)
    	{
//    		for(int i = 0; i < this.roverEntries.size(); i++)
//    		{
//    			if (this.roverEntries.get(i) != null)
//    			{
//    				if (this.roverEntries.get(i).GetId() == id)
//    				{
//    					this.roverEntries.set(i, null);
    					
    					JSONObject message = new JSONObject();
    					message.put("CMD", "exit");
    					
    					this.writeInput.println(message);
    					this.writeInput.flush();	
    					try {
    						return new JSONObject(this.readInput.readLine());
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					} 
//    				}
//    			}
//    		}
    		return null;
    	}
    	
    	public void handleClientMessage() {
    		JSONObject returnMsg;
    		JSONObject entry = new JSONObject();
    		
    		while(entry == null || entry.isEmpty()) 
    		{
    			entry = sharedBuffer.poll();
    		}
    		switch(entry.getString("type"))
    		{
    			case "DEPLOY":
    				returnMsg = deployRover(entry.getInt("id"));
//    				answer.put("type", "DEPLOY");
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "MOVE":
    				returnMsg = moveRover(entry.getInt("id"));
    				returnMsg.put("id", entry.getInt("id"));
//    				answer.put("type", "MOVE");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				sendToClient(returnMsg);
    				break;
    			case "LAND":
    				returnMsg = landRover(entry.getInt("id"), entry.getJSONArray("Coords").getInt(0),entry.getJSONArray("Coords").getInt(1));
//    				answer.put("type", "LAND");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("Coords", entry.optString("Coords"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "SCAN":
    				returnMsg = scan(entry.getInt("id"));
//    				answer.put("type", "SCAN");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "MOVE_SCAN":
    				returnMsg = mvscan(entry.getInt("id"));
//    				answer.put("type", "MOVE-AND-SCAN");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "ROTATE":
    				returnMsg = rotateRover(entry.getInt("id"), entry.optString("rotation"));
//    				answer.put("type", "ROTATE");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("rotation", entry.optString("rotation"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "EXIT":
    				returnMsg = exitRover(entry.getInt("id"));
//    				answer.put("type", "EXIT");
//    				answer.put("id", entry.getInt("id"));
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "GETPOS":
    				returnMsg = deployRover(entry.getInt("id"));
//    				answer.put("type", "GETPOS");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "CHARGE":
    				returnMsg = deployRover(entry.getInt("id"));
//    				answer.put("type", "CHARGE");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "GET_CHARGE":
    				returnMsg = deployRover(entry.getInt("id"));
//    				answer.put("type", "GET_CHARGE");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			case "SWITCH_AUTOPILOT":
    				returnMsg = deployRover(entry.getInt("id"));
//    				answer.put("type", "ENABLE-AUTO");
//    				answer.put("id", entry.getInt("id"));
//    				answer.put("success", success);
    				returnMsg.put("id", entry.getInt("id"));
    				sendToClient(returnMsg);
    				break;
    			default:
    				break;
    		}
    	}
    	
    	public JSONObject SendRoverAnswer(Rover roverInstance)
    	{
    		
//    		PrintWriter toBS = new PrintWriter(client.getOutputStream(), true);
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
    		this.out.print(message.toString() + "\n");
    		this.out.flush();
    	}
    }
    
    
	
	
}