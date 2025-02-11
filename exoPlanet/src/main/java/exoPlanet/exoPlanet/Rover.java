package main.java.exoPlanet.exoPlanet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Arrays;
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
	
	
	
	public void ExecuteCommand (String command) // befehl ist string
	{
		JSONObject action = new JSONObject(command);
		String type = action.getString("type");
		JSONObject jToExo = new JSONObject();
		switch (type)
		{
			case "deploy":
				jToExo.put("CMD", "orbit");
				SendCommandToExo(jToExo);
				this.internalBuffer = WaitForExoResponse();
				setInfoFlag(true);
				//will send {"type":"response"\n, "success":"bool"}
				break;
				
			case "land":
				SurfaceProperties surfaceInstance;
				
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
				JSONObject response = WaitForExoResponse();
				if(response.getString("CMD") == "landed")
				{
					    JSONObject measure = response.getJSONObject("MEASURE");
			            measure.put("X", this.xPosition)
			            	   .put("Y", this.yPosition);
			            StoreScanResult(measure);
					    /*SurfaceProperties.Surfaces surface = SurfaceProperties.Surfaces.valueOf(measure.getString("GROUND").toUpperCase());
			            float temperature = (float) measure.getDouble("TEMP");
			            StoreScanResults(surfaceInstance.SurfaceProperties(response.getString("GROUND"), this.xPosition, this.yPosition, response.getDouble("TEMP"));*/
					
				}
				else
				{
					this.internalBuffer = response;
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
	
	private void Move()
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
	
	private int GetxPosition()
	{
		return this.xPosition;
	}
	
	private void StoreScanResult(JSONObject response) 
	{ 
	    try 
	    { 
	        this.internalBuffer = response;
	        setInfoFlag(true);
	    	/*  JSONObject scanResponse = new JSONObject();
	        int[] pos = property.GetPosition();
	        scanResponse.put("type", "scan");
	        scanResponse.put("xPostion", pos[0]);
	        scanResponse.put("yPostion", pos[1]);
	        scanResponse.put("surface", property.getSurface().toString());
	        scanResponse.put("temperature", property.getTemperature());

	        scanResponse.put("scanResponse", scanResponse.toString());
	        this.internalBuffer.put("", scanResponse);
	        */
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
	
	private void Rotate(String rotation) 
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
	
	private void AutoPilot()
	{
		//throws Exception not implemented;
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
	