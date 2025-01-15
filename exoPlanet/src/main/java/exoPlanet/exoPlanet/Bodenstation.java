package exoPlanet.exoPlanet;
import java.io.*;
import org.json.*;
import java.net.*;

public class Bodenstation {
	private DatabaseManager dm;
	private RoverManager rm;
	private String serverAddress;
	private int port;
	private String databaseAddress;
	private boolean hasNotification;
	private  JSONObject buffer;
	private  JSONArray notifications;

	class RoverManager extends Thread{
		private int latestRoverNumber;
		private PrintWriter writeInput;
		private BufferedReader readInput;
		private Socket client;
		private JSONArray roverAlive;

        public RoverManager() 
		{}

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

		public void run(){}

		//TODO makes a rover via Server but no start yet
		public boolean createRover()
		{
		}

		//TODO sends command to server to deploy a rover
		public void deployRover(JSONObject command)
		{
			// TODO check if rover exists in roverAlive
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
		// TODO: JSON implementation
	}

	// TODO: programm this method
	private void handelUserInput(String input)
	{
		switch (input) {
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
			default:
				/*Should do nothing*/
				break;

		}

	}

	// TODO programm this method
	private void handleNotification()
	{

	}


	public void readUserInput() 
	{
		boolean cmdFound = false;
		String[] possibleCommands = {"land", "scan", "move", "mvscan", "rotate", "exit", "getpos", "charge"};
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("Die Bodenstation ist bereit für den nächsten Befehl:");
			String input;
			while ((input = reader.readLine()) != null) 
			{
                if ("disconnect".equalsIgnoreCase(input)) 
				{
                    System.out.println("Programm beendet.");
                    break;
                }
				for (String command : possibleCommands)
				{

					if(command.equals(input) || command.toUpperCase().equals(input))
					{
						handelUserInput(input);
						cmdFound = true;
						break;
					}
				}
				if(!cmdFound)
				{
					System.out.println("Ungueltiger Befehl!");
				}
                System.out.println("Die Bodenstation ist bereit für den nächsten Befehl: ");
			} 
		
		
		}   
		catch (IOException ex)
		{
			System.out.println("Fehler! Konsole konnte nicht eingelesen werden!");
		}
	}

	public int getCharge(int roverNumber)
	{
		
	}

	

	
	public static void main(String[] args) {
		Bodenstation bs = new Bodenstation(null, 0, null);
		bs.readUserInput();
		
	}

}
