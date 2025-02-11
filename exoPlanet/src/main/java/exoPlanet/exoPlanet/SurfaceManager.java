package main.java.exoPlanet.exoPlanet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import main.java.exoPlanet.exoPlanet.SurfaceProperties.Surfaces;

public class SurfaceManager
{
    private Map<String, SurfaceProperties> surfaces = new HashMap<>();
    private SurfaceProperties currentField;
    int xPos = currentField.getPosition()[0];
    int yPos = currentField.getPosition()[1];
    SurfaceProperties.Surfaces surface = currentField.getSurface();
    double temp = currentField.getTemperature();
    

    public SurfaceProperties getSurfaceAt(int x, int y)
    {
        String key = x + "," + y;
        return surfaces.computeIfAbsent(key, k -> new SurfaceProperties(Surfaces.UNKNOWN, x, y, 0.0));
    }
    
    public void addField(int x, int y, double temperature, SurfaceProperties.Surfaces surface)
    {
        String key = x + "," + y;
        SurfaceProperties field = new SurfaceProperties(surface, x, y, temperature);
        surfaces.put(key, field);

        if (currentField != null && isAdjacent(x, y))
        {
            updateScanScore();
        }
    }

    private boolean isUnknown(int x, int y)
    {
        String key = x + "," + y;
        return !surfaces.containsKey(key) || surfaces.get(key).getSurface() == SurfaceProperties.Surfaces.UNKNOWN;
    }

    private boolean isAdjacent(int x, int y)
    {
        return (Math.abs(xPos - x) == 1 && yPos == y) ||
               (Math.abs(yPos - y) == 1 && xPos == x);
    }

    private void updateScanScore()
    {
        int unknownCount = 0;
        if (isUnknown(xPos + 1, yPos)) unknownCount++;
        if (isUnknown(xPos - 1, yPos)) unknownCount++;
        if (isUnknown(xPos, yPos + 1)) unknownCount++;
        if (isUnknown(xPos, yPos - 1)) unknownCount++;

        int scanScoreBase = 0;
        
        if (!CheckForUndesiredSurface())
        {	
        	switch (unknownCount)
        	{
            	case 0: scanScoreBase = 0; break;
            	case 1: scanScoreBase = 1; break;
            	case 2: scanScoreBase = 3; break;
            	case 3: scanScoreBase = 5; break;
            	default: scanScoreBase = 0; break;
        	}
        }
        currentField.setScanScore(scanScoreBase);
    }
    
    private boolean CheckForUndesiredSurface()
    {
        if (currentField.getSurface() == Surfaces.LAVA 
        	|| currentField.getSurface() == Surfaces.UNKNOWN 
            || currentField.getSurface() == Surfaces.NICHTS
            || currentField.getSurface() == Surfaces.BLOCKED) 
        {
            return true;
        }
        else { return false; }
    }

    public float grandScore(int x, int y)
    {
    	float finalScore = currentField.getScanScore() - CalcDistance(x,y);
    	return finalScore;
    }
    
    private float CalcDistance(int roverX, int roverY)
    {
        int distance = Math.abs(xPos - roverX) + Math.abs(yPos - roverY);
        return (float) (5 * (Math.log10(distance + 1) / Math.log10(11)));
    }

	public SurfaceProperties getCurrentField() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<SurfaceProperties> getAllFields() 
	{
	    return surfaces.values();
	}
	
    public SurfaceProperties getField(int x, int y) 
    {
        return surfaces.getOrDefault(getKey(x, y), null);
    }
    
    private String getKey(int x, int y) 
    {
        return x + "," + y;
    }
}

