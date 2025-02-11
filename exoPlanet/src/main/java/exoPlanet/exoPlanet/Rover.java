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
	private Socket Exo = new Socket();
	private JSONObject CommandBuffer;
	private boolean infoFlag = false;
	public PrintWriter output = new PrintWriter(new Writer(Exo));
	public BufferedReader input = new BufferedReader(new InputStreamReader(Exo.getInputStream()));
	private RoverServer roverManager;
	private int charge;
	
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

	    RoverState(int stateId) 
	    {
	        this.stateId = stateId;
	    }

	    public int getStateId() 
	    {
	        return this.stateId;
	    }

	    public static RoverState GetState(int id) 
	    {
	        for (RoverState rs : RoverState.values()) 
	        {
	            if (rs.getStateId() == id) 
	            {  
	                return rs;  
	            }
	        }
	        throw new IllegalArgumentException("Ungültige ID für RoverState: " + id); 
	    }
	}
	
	public boolean GetInfoFlag(); //RoverServer can read new info from Buffer, if this returns true
	{
		return infoFlag;
	}
	
	public void SetInfoFlagTo0(); //if InternalBuffer is read by RoverServer, execute this method
	{
		infoFlag = false;
	}
		
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
				position = command.optString("Coords") //rewrite everywhere
				Random RANDOM = new Random();
				direction = direction[RANDOM.nextInt(Directons.length)];
				output.Exo("land:POSITION|" + position[0] + "|" + position[1] + "|" + direction); //put to JSON
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "rotate":
				String rotation = command.optString("rotation");
				Rotate(rotation);
				}

				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "move":
				Move(); //ErrorHandling
				//will send {"type":"response"\n, "success":"bool"\n, "text":"string_if_something_happened"}
				break;
				
			case "scan":
				Scan();
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
		
	private void SendCommandToExo(JSONObject json)
	{
		output.println(json.toString());	
	}
	
	private void Move()
		{
	    try 
	    	{
	        	JSONObject json = new JSONObject();
	       		json.put("CMD", "move");
	       		writer.println(json.toString());
	       		 
	       		StringBuilder jsonBuilder = new StringBuilder();
	       		String line;
	       		while ((line = reader.readLine()) != null) 
	       		{
	       		     jsonBuilder.append(line);
	       		}
	       		 
	       		JSONObject response = new JSONObject(jsonBuilder.toString());
	       		if (response.has("POSITION")) 
	       		{
	       		    JSONObject position = response.getJSONObject("POSITION");
	       		    direction = position.getString("DIRECTION");
	       		    position = {position.getInt("X"), position.getInt("Y")};
	       		}
	    	} 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
	
	private void Scan() 
	{ 
	    try 
	    { 
	        JSONObject json = new JSONObject();
	        json.put("CMD", "scan");
	        writer.println(json.toString());

	        StringBuilder jsonBuilder = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) 
	        { 
	            jsonBuilder.append(line);
	        }

	        JSONObject response = new JSONObject(jsonBuilder.toString());
	        if (response.has("MEASURE")) 
	        { 
	            JSONObject measure = response.getJSONObject("MEASURE");
	            Surfaces surface = Surfaces.valueOf(measure.getString("GROUND").toUpperCase());
	            float temperature = (float) measure.getDouble("TEMP");
	            int[] scanPos = CalcPosition();
	            SurfaceProperty scannedProperty = new SurfaceProperty(surface, scanPos[0], scanPos[1], temperature);
	            StoreScanResult(scannedProperty);
	        } 
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
	}
	
	private void StoreScanResult(SurfaceProperty property) 
	{ 
	    try 
	    { 
	        JSONObject json = new JSONObject();
	        json.put("type", "scan");

	        JSONObject scanResponse = new JSONObject();
	        scanResponse.put("Coords", Arrays.toString(property.getCoordinates()));
	        scanResponse.put("surface", property.getSurface().toString());
	        scanResponse.put("temperature", property.getTemperature());

	        json.put("scanResponse", scanResponse.toString());
	        internalBuffer = json.toString();
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
	}

	private int[] CalcPosition ()
	{
		String dir = direction
		int[] nextPos = new int[2];
		switch (dir) 
		{
               case "EAST":
                   nextPos[0] = Position[0] + 1;
				   nextPos[1] = Position[1];
                   break;
               case "SOUTH":
					nextPos[1] = Position[1] + 1;
				   nextPos[0] = Position[0];
                   break;
               case "WEST":
					nextPos[0] = Position[0] - 1;
				   nextPos[1] = Position[1];
                   break;
               case "NORTH":
					nextPos[1] = Position[1] - 1;
				   nextPos[0] = Position[0];
                   break;
		}
		return nextPos;
	}
	
	private void Rotate(String rotation) 
	{ 
	    try 
	    { 
	        JSONObject json = new JSONObject();
	        json.put("CMD", "rotate");
	        json.put("ROTATION", rotation);
	        writer.println(json.toString());

	        StringBuilder jsonBuilder = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) 
	        { 
	            jsonBuilder.append(line);
	        }

	        JSONObject response = new JSONObject(jsonBuilder.toString());
	        if (response.has("DIRECTION")) 
	        { 
	            direction = response.getString("DIRECTION");
	        } 
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
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
	
	private void SetIncommingCommandBuffer()
	{
		new Thread(() -> 
	    { 
	        try 
	        { 
	            String line;
	            while ((line = reader.readLine()) != null) 
	            { 
	                internalCommandBuffer = line;
	            } 
	        } 
	        catch (Exception e) 
	        { 
	            e.printStackTrace();
	        } 
	    }).start();
	}
	
	private void AutoPilot()
	{
		throws Exception not implemented;
	}
	
	public JSONObject GetInternalBuffer()
	{
		return internalBuffer;
	}
	
	public int GetId();
	{
		return id;
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
	
	
		
}   
	