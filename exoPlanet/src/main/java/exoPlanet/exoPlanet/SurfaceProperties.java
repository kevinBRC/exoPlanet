public class SurfaceProperties 
{
	private Surfaces surface;
    private final int[] position = new int[2];
    private int temperature;
    private int scanScore;
    private int rechargeScore;
    private int distance;

    public SurfaceProperties(Surfaces surface, int x, int y, int temperature)
	{
        this.surface = surface;
        this.position[0] = x;
        this.position[1] = y;
        this.temperature = temperature;
    }

    public JSONObject wrapInfo() 
	{
        JSONObject json = new JSONObject();
        json.put("surface", surface.toString());
        json.put("position", position);
        json.put("temperature", temperature);
        return json;
    }

 private void updateScanScore(roverCoords) 
	{
        int unknownCount = 0;
        if (isUnknown(position[0] + 1, position[1])) unknownCount++;
        if (isUnknown(position[0] - 1, position[1])) unknownCount++;
        if (isUnknown(position[0], position[1] + 1)) unknownCount++;
        if (isUnknown(position[0], position[1] - 1)) unknownCount++;

        switch (unknownCount) {
            case 0 -> scanScore = 0;
            case 1 -> scanScore = 1 ;
            case 2 -> scanScore = 3;
            case 3, 4 -> scanScore = 5;
        }
    }
	
	private boolean isUnknown(int x, int y) 
	{
    return getSurfaceAt(x, y) == Surfaces.UNKNOWN;
	}

    public void setSurface(Surfaces surface) 
	{
        this.surface = surface;
    }

    private Surfaces getSurfaceAt(int x, int y)
    {
    	Surfaces here = 
    }
    
    public int[] getPosition() 
	{
        return position;
    }

    public void setPosition(int x, int y) 
	{
        this.position[0] = x;
        this.position[1] = y;
		updateDistance(roverCoords);
    }

    public void setTemperature(int temperature) 
	{
        this.temperature = temperature;
    }

    public int getScanScore() 
	{
        return scanScore;
    }

    public void setScanScore(int scanScore) 
	{
        this.scanScore = scanScore;
    }

    public int getRechargeScore() 
	{
        return rechargeScore;
    }

    public void setRechargeScore(int rechargeScore) 
	{
        this.rechargeScore = rechargeScore;
    }


    public void setDistance(DistanceFunction distance) 
	{
        this.distance = distance;
    }

	private int CalcDistance(int[] roverCoords) 
	{
        distanceValue = Math.abs(position[0] - roverCoords[0]) + Math.abs(position[1] - roverCoords[1]);
	}
	
	public enum Surfaces
	{
		NICHTS,
		SAND,
		GEROELL,
		FELS,
		WASSER,
		PFANZEN,
		MORAST,
		LAVA,
		BLOCKED,
		UNKNOWN
	}

}
