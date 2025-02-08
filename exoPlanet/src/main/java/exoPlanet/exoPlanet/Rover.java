import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Random;

public class rover
{
	public int id;
	public int[] position = new int[2];
	public Directions direction;
	public JSONObject internalBuffer;
	public Socket Exo = new Socket();
	public PrintWriter output = new PrintWriter(new Writer(Exo));
	public BufferedReader input = new BufferedReader(new InputStreamReader(Exo));
	private RoverServer roverManager;
	private int charge;
		
	public rover(int id)
	{
		Random RANDOM = new Random();
		direction = direction[RANDOM.nextInt(Directons.length)];
		id = this.id;
	}
	
	
	
	public ConnectToExoplanet(string addressExoplanet, string portExoplanet)
	{
		throws Exception not implemented;
	}
	
	
	
	private void ExecuteCommand (JSONObject command) // befehl ist string
	{
		String action = command.optString("type").toLowerCase();
		switch (action)
		{
			case "deploy":
				output.Exo("orbit");
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "land":
				position = command.optString("Coords")
				Random RANDOM = new Random();
				direction = direction[RANDOM.nextInt(Directons.length)];
				output.Exo("land:POSITION|" + position[0] + "|" + position[1] + "|" + direction);
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "rotate":
				String rotation = command.optString("rotation");
				if (rotation == "right")
				{
					direction = direction[(direction.ordinal() + 1) % DIRECTIONS.length];
				}
				if (rotation == "left")
				{
					direction = direction[(direction.ordinal() + DIRECTIONS.length - 1) % DIRECTIONS.length];
				}
				output.Exo("rotate:" + rotation);
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "move":
				output.Exo("move");
				ErrorHandler(input.Exo());
				//will send {"type":"response"\n, "success":"bool"\n, "text":"string_if_something_happened"}
				break;
				
			case "scan":
				output.Exo("scan");
				SaveToBuffer(input.Exo());
				int[] scannedPos = CalcPosition(direction);
				SurfaceProperty.SurfaceProperty(internalBuffer.optString("Ground"), scannedPos[0], scannedPos[1], internalBuffer.optString("temp"))
				//will send {"type":"scan"\n, "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]",\n "direction": "{String}", "crashed": "{boolean}"}
				break;
				
			case "move-and-scan":
			
				output.Exo("mvscan");
				ErrorHandler(input.Exo());
				//will send {"type":"scan"\n, "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]",\n "direction": "{String}", "crashed": "{boolean}"}
				break;
				
			case "exit":
				output.Exo("exit");
				UpdateState(9);
				//will send {"type":"response"\n, "success":"bool}
				break;
				
			case "enable-auto":
				UpdateState(2);
				//will send {"type":"response"\n, "success":"bool"}
				// and continue with scans
				//finally {"type":"response"\n,"status":"RoverState(mostly idle,crashed,decommissioned)"}
				break;
				
			case "getpos":
				// will send {"type":"response"\n, "pos":"[int,int]"}
				break;
				
			case "charge":
				//will send {"type":"response"\n, "success": "bool"}
				break;
				
			case "get_charge":
				//will send {"type":"response"\n, "charge": "{charge as int}"}
				break;
				
		}
	}
		
	private void SendCommand(JSONObject json)
	{
		output.println(json.toString());	
	}
	
	private void Move()
		{
			StringBuilder jsontranslator = new StringBuilder();
			output.Exo("move");
			ErrorHandler(input.Exo());
			// wait for incomming message
			string response = await command.optString("CMD").toLowerCase();
		}
	
	private int[] CalcPosition (direction)
	{
		int[] nextPos = new int[2];
		switch (direction) 
		{
               case EAST:
                   nextPos[0] = Position[0] + 1;
				   nextPos[1] = Position[1];
                   break;
               case SOUTH:
					nextPos[1] = Position[1] + 1;
				   nextPos[0] = Position[0];
                   break;
               case WEST:
					nextPos[0] = Position[0] - 1;
				   nextPos[1] = Position[1];
                   break;
               case NORTH:
					nextPos[1] = Position[1] - 1;
				   nextPos[0] = Position[0];
                   break;
		}
		return nextPos;
	}
	
	public void RunStateMachine()
	{
		int state;
		switch (state)
		{
			case 0:
				//listening
				break;
				
			case 1:
				//listening
				break;
				
			case 2:
			AutoPilot();
			UpdateState(9);
				break;
				
			case 3:
				//can't exec oders
				break;
				
			case 4:
				//just another 9
				break;
				
			case 5:
				//special kind of 3
				break;
				
			case 9:
				//dead
				break;
				
	}
	
	private void ErrorHandler(JSONObject message)
	{
		if (message.optString("CMD") == "crashed")
		{
			UpdateState(4);
			position.SurfaceProperty.SetSurface(BLOCKED);
		}
		String errormsg = message.optString("Error-text");
		if (message.optString("error") == "ERROR")
		{
			switch (errormsg)
			{
				case "stuck":
				
					break;
				
				default:
				throws Exception ex;
			}
		}
	}
	
	private String ReadFromInternalBuffer()
	{
		throws Exception not implemented;
	}
	
	private void AutoPilot()
	{
		throws Exception not implemented;
	}
	
	private void SaveToBuffer(JSONObject message)
	{
		internalBuffer = message;
	}
	
	public void DecommissionRover()
	{
		UpdateState(9);
	}
	
	public void UpdateState(int i)
	{
		throws Exception not implemented
	}
	
		private enum Directions
	{
		EAST,
		SOUTH,
		WEST,
		NORTH
	}
	
		private enum RoverState
		{
		    NEW(0),
		    IDLE(1),
		    AUTO(2),
		    BUSY(3),
		    CRASHED(4),
		    RECHARGING(5),
		    DECOMMISSIONED(9);

		    private final int stateId;

		    RoverState(int stateId) {
		        this.stateId = stateId;
		    }

		    public int getStateId() {
		        return this.stateId;
		    }

		    public static RoverState GetState(int id) {
		        for (RoverState rs : RoverState.values()) {
		            if (rs.getStateId() == id) 
		            {  
		                return rs;  
		            }
		        }
		        throw new IllegalArgumentException("Ungültige ID für RoverState: " + id); 
		    }
		}
}   
	