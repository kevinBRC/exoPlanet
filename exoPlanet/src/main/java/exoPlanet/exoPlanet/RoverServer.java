package exoPlanet.exoPlanet;

import java.io.*;
import org.json.*;
import java.net.*;
import java.util.Queue;

public class RoverServer {
	private Queue<JSONObject> sharedBuffer;
	private PrintWriter out;
	private ConnectionListener cl;
	
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleClientMessage() {
		JSONObject entry = null;
		
		while(entry == null) 
		{
			entry = this.sharedBuffer.poll();
		}
		
		switch(entry.getString("type"))
		{
			case "DEPLOY":
				break;
			case "MOVE":
				break;
			case "LAND":
				break;
			case "SCAN":
				break;
			case "MOVE_SCAN":
				break;
			case "ROTATE":
				break;
			case "EXIT":
				break;
			case "GETPOS":
				break;
			case "CHARGE":
				break;
			case "GET_CHARGE":
				break;
			case "SWITCH_AUTOPILOT":
				break;
			default:
				break;
		}
	}
	
	public void sendToClient(JSONObject message) {
		this.out.print(message.toString());
	}
	
	
    public static void main(String[] args) {
        int port = 12345;  // Port, auf dem der Server horcht
        
        // Erstellen des Serversockets innerhalb eines try-with-resources-Blocks
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server gestartet. Warte auf Verbindungen...");

            // Endlosschleife, um immer wieder auf neue Verbindungen zu warten
            while (true) {
                // Warten auf eine eingehende Client-Verbindung
                try (Socket client = serverSocket.accept()) {
                    System.out.println("Client verbunden: " + client.getInetAddress());
                    RoverServer rs = new RoverServer(client);
                    
                    
                    
                    System.out.println("Client-Verbindung geschlossen.");
                } catch (IOException e) {
                    System.err.println("Fehler bei der Bearbeitung der Client-Verbindung: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen des Serversockets: " + e.getMessage());
        }
    }
}