package exoplanet;

import exoplanet.DatabaseManager;
import org.json.*;
import java.io.*;

public class Bodenstation {
	private DatabaseManager dm;
	private RoverManager rm;
	private String serverAddress;
	private int port;
	private String databaseAddress;
	private boolean hasNotification;
	private synchronized JSONOnject buffer;
	private synchronized JSONArray notifications;

	class RoverManager extends Thread{

	}

	// TODO: parameters for DatabaseManager and RoverManager
	Bodenstation(String serverAddress, int port, String databaseAddress)
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
		System.out.println("Befehl Eingegeben.");
	}

	// TODO programm this method
	private void handleNotification()
	{

	}


	public String readUserInput() 
	{
		String[] possibleCommands = {"land", "scan", "move", "mvscan", "rotate", "exit", "getpos", "charge"};
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("Die Bodenstation ist bereit für den nächsten Befehl:");
			String input;
			while ((input = reader.readLine()) != null) 
			{
                if ("exit".equalsIgnoreCase(input)) 
				{
                    System.out.println("Programm beendet.");
                    break;
                }
				for (String command : possibleCommands)
				{

					if(command.equals(input) || command.toUpperCase().equals(input))
					{
						handelUserInput(input);
					}
					else
					{
						System.out.println("Ungueltiger Befehl!");
					}
				}
                System.out.println("Die Bodenstation ist bereit für den nächsten Befehl: " + input);
			} 
		
		
		}   
		catch (IOException ex)
		{

		}


	

	
	public static void main(String[] args) {
		
	}

}
