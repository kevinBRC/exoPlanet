package main.java.exoPlanet.exoPlanet;

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
    private float scanScore;
    
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

    public void setSurface(Surfaces surface) 
	{
        this.surface = surface;
    }
    
    public int[] getPosition() 
	{
        return new int[] {xPosition,yPosition};
    }

    public double getTemperature()
    {
        return temperature;
    }

    public Surfaces getSurface()
    {
        return surface;
    }
    
    public float getScanScore()
    {
    	return scanScore;
    }
    
    public void setScanScore(float newScore)
    {
    	scanScore = newScore;
    }
}
