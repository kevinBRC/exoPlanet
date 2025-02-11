package main.java.exoPlanet.exoPlanet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.json.*;

public class Rover
{
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
	
	public int id;
	public int xPosition;
	public int yPosition;
	public Directions direction;
	public JSONObject internalBuffer;
	private Socket Exo = new Socket();
	private String internalCommandBuffer;
	private boolean infoFlag = false;
	public PrintWriter output;
	public BufferedReader input;
	private int charge;
	private boolean autopilotActive = false;
    private Auto_and_Pathfinding pathfinder;

    public void setPathfinder(Auto_and_Pathfinding pathfinder) 
    {
        this.pathfinder = pathfinder;
    }
	
	public boolean getInfoFlag() //RoverServer can read new info from Buffer, if this returns true
	{
		return infoFlag;
	}
	
	public void setInfoFlag(boolean flag) //if InternalBuffer is read by RoverServer, execute this method
	{
		this.infoFlag = flag;
	}
		
	public Rover(int id)
	{
		this.id = id;
	}
	
	public String getDirection()
	{
		String dir = this.direction.toString();
		return dir;
	}
	
	public void ConnectToExoplanet(String addressExoplanet, String portExoplanet)
	{
		try {
			this.Exo = new Socket("", 0);
			this.output = new PrintWriter(Exo.getOutputStream(), true);
			this.input = new BufferedReader(new InputStreamReader(Exo.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Disconnect()
	{
		try {
			this.output.close();
			this.input.close();
			this.Exo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ExecuteCommand (String command)
	{
		JSONObject action = new JSONObject(command);
		String type = action.getString("type");
		JSONObject jToExo = new JSONObject();
		switch (type)
		{
			case "deploy":
				jToExo.put("CMD", "orbit");
				SendCommandToExo(jToExo);
				JSONObject response = WaitForExoResponse();
				if (response.getString("CMD") == "init")
				{
					JSONObject toBuffer = new JSONObject();
					toBuffer.put("type", "DEPLOY")
							.put("id", this.id)
							.put("planet", response.get("SIZE"));
					this.internalBuffer = toBuffer;
					setInfoFlag(true);
				}
				else
				{
					this.internalBuffer = response;
				}
				
				//will send {"type":"DEPLOY"\n, "id":int, "planet":{"WIDTH":int,"HEIGHT":int}}
				break;
				
			case "land":		
				this.xPosition = action.getInt("xPosition");
				this.yPosition = action.getInt("yPosition");
				Random RANDOM = new Random();
				Directions[] values = Directions.values();
			    this.direction= values[new Random().nextInt(values.length)];
				jToExo.put("CMD","land")
						.put("POSITION", new JSONObject()
					    .put("X", this.xPosition)
					    .put("Y", this.yPosition)
					    .put("DIRECTION", this.direction));

				SendCommandToExo(jToExo);
				JSONObject landResponse = WaitForExoResponse();
				if(landResponse.getString("CMD") == "landed")
				{
					    JSONObject measure = landResponse.getJSONObject("MEASURE");
			            measure.put("X", this.xPosition)
			            	   .put("Y", this.yPosition)
			            	   .put("id", this.id)
			            	   .put("direction", this.direction)
			            	   .put("success", true)
			            	   .put("crashed", false)
			            	   .put("type", "LAND");
			            StoreScanResult(measure);
				}
				else
				{
					this.internalBuffer = landResponse;
				}
				setInfoFlag(true);
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "rotate":
				String rotation = action.getString("rotation");
				Rotate(rotation);
				
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "move":
				Move();
				//will send {"type":"response"\n, "success":"bool"\n, "text":"string_if_something_happened"}
				break;
				
			case "scan":
				Scan();
				//will send {"type":"scan"\n, "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]",\n "direction": "{String}", "crashed": "{boolean}"}
				break;
				
			case "move-and-scan":
				Move();
				Scan();
				//ErrorHandler(input.Exo());
				//will send {"type":"scan"\n, "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]",\n "direction": "{String}", "crashed": "{boolean}"}
				break;
				
			case "exit":
				JSONObject json = new JSONObject();
				json.put("CMD", "exit");
				output.println(json.toString());
				Disconnect();
				UpdateState(9);
				//will send {"type":"response"\n, "success":"bool}
				break;
				
			case "enable-auto":
				autopilotActive = !autopilotActive;
				JSONObject jsonAuto = new JSONObject();
				jsonAuto.put("type", "SWITCH_AUTOPILOT")
						.put("id", this.id)
						.put("autopilot", autopilotActive);
				Autopilot();
				//will send {"type":"SWITCH_AUTOPILOT"\n, "id":int, "autopilot":bool}
				// and continue with scans
				//finally {"type":"response"\n,"status":"RoverState(mostly idle,crashed,decommissioned)"}
				break;
				
			case "getpos":
				JSONObject getPosResponse = new JSONObject();
				getPosResponse.put("type", "GETPOS")
						.put("id", this.id)
						.put("xPosition", this.xPosition)
						.put("yPosition", this.yPosition);
				this.internalBuffer = getPosResponse;
				setInfoFlag(true);
				// will send {"type":"GETPOS", "id":int, "xPosition":int, "yPosition":int}
				break;
				
			case "charge":
				//will send {"type":"response"\n, "success": "bool"}
				break;
				
			case "get_charge":
				JSONObject exoStatus = new JSONObject();
				exoStatus.put("CMD", "status");
				//not implemented
				//will send {"type":"response"\n, "charge": "{charge as int}"}
				break;
				
		}
	}
		
	private void SendCommandToExo(JSONObject json)
	{
		output.println(json.toString());	
	}
	
	private JSONObject WaitForExoResponse()
	{
		StringBuilder jsonBuilder = new StringBuilder();
   		String line;
   		try {
			while ((line = input.readLine()) != null) 
			{
			     jsonBuilder.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   		JSONObject response = new JSONObject(jsonBuilder.toString());
   		return response;
	}
	
	public void Move()
	{
	    try 
	    	{
	        	JSONObject json = new JSONObject();
	       		json.put("CMD", "move");
	       		output.println(json.toString());
	       		JSONObject response = WaitForExoResponse();
	       		
	       		if (response.has("POSITION")) 
	       		{
	       		    JSONObject responsePosition = response.getJSONObject("POSITION");
	       		    this.direction = Directions.valueOf(responsePosition.getString("DIRECTION"));
	       		    this.xPosition = responsePosition.getInt("X");
	       		    this.yPosition = responsePosition.getInt("Y");
	       		    responsePosition.put("type", "MOVE")
	       		    				.put("id", this.id)
	       		    				.put("crashed", false);
	       		    this.internalBuffer = responsePosition;
	       		    setInfoFlag(true);  
	       		}
	    	} 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
	
	public void Scan() 
	{ 
	    try 
	    { 
	        JSONObject json = new JSONObject();
	        json.put("CMD", "scan");
	        output.println(json.toString());
	        JSONObject response = WaitForExoResponse();
	        if (response.has("MEASURE")) 
	        { 
	            JSONObject measure = response.getJSONObject("MEASURE");
	            measure.put("X", CalcPosition()[0])
	                   .put("Y", CalcPosition()[1]);
	            /*SurfaceProperties.Surfaces surface = SurfaceProperties.Surfaces.valueOf(measure.getString("GROUND").toUpperCase());
	            float temperature = (float) measure.getDouble("TEMP");
	            int[] scanPos = CalcPosition();
	            SurfaceProperties scannedProperty = new SurfaceProperties(surface, scanPos[0], scanPos[1], temperature);*/
	            StoreScanResult(measure);
	        } 
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
	}
	
	public void StoreScanResult(JSONObject response) 
	{ 
	    try 
	    { 
	    	int[] pos = CalcPosition();
	    	response.put("X", pos[0])
	    			.put("Y", pos[1]);
	    	JSONObject toBuffer = new JSONObject();
	    	toBuffer.put("scanResponse", response.getString("MEASURE"));
	        toBuffer.put("type", "SCAN")
	        		.put("id", this.id);
	    	this.internalBuffer = toBuffer;
	        setInfoFlag(true);
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
	}

	private int[] CalcPosition ()
	{
		String dir = direction.toString();
		int[] nextPos = new int[2];
		switch (dir) 
		{
               case "EAST":
                   nextPos[0] = this.xPosition + 1;
				   nextPos[1] = this.yPosition;
                   break;
               case "SOUTH":
					nextPos[1] = this.yPosition + 1;
				   nextPos[0] = this.xPosition;
                   break;
               case "WEST":
					nextPos[0] = this.xPosition - 1;
				   nextPos[1] = this.yPosition;
                   break;
               case "NORTH":
					nextPos[1] = this.yPosition - 1;
				   nextPos[0] = this.xPosition;
                   break;
		}
		return nextPos;
	}
	
	public void Rotate(String rotation) 
	{ 
	    try 
	    { 
	        JSONObject json = new JSONObject();
	        json.put("CMD", "rotate");
	        json.put("ROTATION", rotation);
	        output.println(json.toString());

	        StringBuilder jsonBuilder = new StringBuilder();
	        String line;
	        while ((line = input.readLine()) != null) 
	        { 
	            jsonBuilder.append(line);
	        }

	        JSONObject response = new JSONObject(jsonBuilder.toString());
	        if (response.has("DIRECTION")) 
	        { 
	            String dir = response.getString("DIRECTION");
	            direction = Directions.valueOf(dir);
	        }
	        else
	        {
	        	this.internalBuffer = response;
	        }
	    } 
	    catch (Exception e) 
	    { 
	        e.printStackTrace();
	    } 
	}

	
/*	public void RunStateMachine()
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
				
	}*/
	/*
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
				//throws Exception ex;
			}
		}
	}
	*/
	private void SetIncommingCommandBuffer()
	{
		new Thread(() -> 
	    { 
	        try 
	        { 
	            String line;
	            while ((line = input.readLine()) != null) 
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
	
	public void Autopilot() 
    {
        if (pathfinder == null) 
        {
            throw new IllegalStateException("Something went wrong :(");
        }

        autopilotActive = true;

        while (autopilotActive) 
        {
            pathfinder.executePathfinding();
        }
    }

    public void stopAutopilot() 
    {
        autopilotActive = false;
    }
	
	public int[] GetPos()
	{
		int[] pos = new int[2];
		pos[0] = xPosition;
		pos[1] = yPosition;
		return pos;
	}
	
	public JSONObject GetInternalBuffer()
	{
		return this.internalBuffer;
	}
	
	public int GetId()
	{
		return this.id;
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
		//throws Exception not implemented
	}
}   
	