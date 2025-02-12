package exoPlanet.exoPlanet;

import org.json.JSONObject;


public class SurfaceProperties 
{
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
	
	private Surfaces surface;
    private int xPosition;
    private int yPosition;
    private double temperature;
    private int scanScore;
    private int rechargeScore;
    private int distance;
    
    public SurfaceProperties(Surfaces surface, int x, int y, double temperature)
	{
        this.surface = surface;
        this.xPosition = x;
        this.yPosition = y;
        this.temperature = temperature;
    }

    public JSONObject wrapInfo() 
	{
        JSONObject json = new JSONObject();
        json.put("surface", surface.toString());
        json.put("X", xPosition);
        json.put("Y", yPosition);
        json.put("temperature", temperature);
        return json;
    }

//    private void updateScanScore() 
//	{
//        int unknownCount = 0;
//        if (isUnknown(xPosition + 1, yPosition)) unknownCount++;
//        if (isUnknown(xPosition - 1, yPosition)) unknownCount++;
//        if (isUnknown(xPosition, yPosition + 1)) unknownCount++;
//        if (isUnknown(xPosition, yPosition - 1)) unknownCount++;
//
//        switch (unknownCount) 
//        {
//            case 0: scanScore = 0; break;
//            case 1: scanScore = 1; break;
//            case 2: scanScore = 3; break;
//            case 3: scanScore = 5; break;
//            default: scanScore = 0; break;
//        }
//        ScoreDeduction();
//    }
	
    private void ScoreDeduction()
    {
        if (this.surface == Surfaces.LAVA || 
            this.surface == Surfaces.UNKNOWN || 
            this.surface == Surfaces.BLOCKED) 
        {
            this.scanScore -= 5;
        }
    }

    
//	private boolean isUnknown(int x, int y) 
//	{
//    return getSurfaceAt(x, y) == Surfaces.UNKNOWN;
//	}

    public void setSurface(Surfaces surface) 
	{
        this.surface = surface;
    }

//    private Surfaces getSurfaceAt(int x, int y)
//    {
//    	//Surfaces here = 
//    	return this.surface;
//    }
    
    public int[] getPosition() 
	{
        return new int[] {xPosition,yPosition};
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

    private int CalcDistance(int roverX, int roverY) 
    {
        int distance = Math.abs(xPosition - roverX) + Math.abs(yPosition - roverY);
        int disScore = (int) (5 * (Math.log10(distance + 1) / Math.log10(11))); 
        return disScore;
    }

}
