package main.java.exoPlanet.exoPlanet;
import java.util.*;

public class Auto_and_Pathfinding 
{
    private SurfaceManager surfaceManager;
    private Rover rover;

    public Auto_and_Pathfinding(SurfaceManager surfaceManager, Rover rover) 
    {
        this.surfaceManager = surfaceManager;
        this.rover = rover;
    }

    public void executePathfinding() 
    {
        while (true) 
        {
            SurfaceProperties target = findBestScanField();
            if (target == null) 
            {
                break;
            }

            List<SurfaceProperties> path = aStarPathfinding(target);
            if (path.isEmpty()) 
            {
                break;
            }

            followPath(path);
            rover.Scan();
        }
    }

    private SurfaceProperties findBestScanField() 
    {
        SurfaceProperties bestField = null;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (SurfaceProperties field : surfaceManager.getAllFields()) 
        {
            if (field.getScanScore() > 0) 
            {
                float score = surfaceManager.grandScore(rover.GetPos()[0], rover.GetPos()[1]);
                if (score > bestScore) 
                {
                    bestScore = score;
                    bestField = field;
                }
            }
        }

        return bestField;
    }

    private List<SurfaceProperties> aStarPathfinding(SurfaceProperties target) 
    {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<String, Node> allNodes = new HashMap<>();

        SurfaceProperties startField = surfaceManager.getField(rover.GetPos()[0], rover.GetPos()[1]);
        Node startNode = new Node(startField, null, 0, heuristic(startField, target));
        openSet.add(startNode);
        allNodes.put(getKey(startField), startNode);

        while (!openSet.isEmpty()) 
        {
            Node current = openSet.poll();
            if (current.field.equals(target)) 
            {
                return reconstructPath(current);
            }

            for (SurfaceProperties neighbor : getNeighbors(current.field)) 
            {
                if (isUndesired(neighbor)) 
                {
                    continue;
                }

                double gCost = current.gCost + 1;
                Node neighborNode = allNodes.get(getKey(neighbor));

                if (neighborNode == null || gCost < neighborNode.gCost) 
                {
                    neighborNode = new Node(neighbor, current, gCost, heuristic(neighbor, target));
                    openSet.add(neighborNode);
                    allNodes.put(getKey(neighbor), neighborNode);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<SurfaceProperties> reconstructPath(Node node) 
    {
        List<SurfaceProperties> path = new ArrayList<>();
        while (node != null) 
        {
            path.add(0, node.field);
            node = node.parent;
        }
        return path;
    }

    private List<SurfaceProperties> getNeighbors(SurfaceProperties field) 
    {
        List<SurfaceProperties> neighbors = new ArrayList<>();
        int x = field.getPosition()[0];
        int y = field.getPosition()[1];

        addNeighbor(neighbors, x + 1, y);
        addNeighbor(neighbors, x - 1, y);
        addNeighbor(neighbors, x, y + 1);
        addNeighbor(neighbors, x, y - 1);

        return neighbors;
    }

    private void addNeighbor(List<SurfaceProperties> neighbors, int x, int y) 
    {
        SurfaceProperties neighbor = surfaceManager.getField(x, y);
        if (neighbor != null) 
        {
            neighbors.add(neighbor);
        }
    }

    private boolean isUndesired(SurfaceProperties field) 
    {
        return field.getSurface() == SurfaceProperties.Surfaces.LAVA 
        	 ||field.getSurface() == SurfaceProperties.Surfaces.UNKNOWN
             ||field.getSurface() == SurfaceProperties.Surfaces.BLOCKED;
    }

    private double heuristic(SurfaceProperties a, SurfaceProperties b) 
    {
        return Math.abs(a.getPosition()[0] - b.getPosition()[0]) + Math.abs(a.getPosition()[1] - b.getPosition()[1]);
    }

    private void followPath(List<SurfaceProperties> path) 
    {
        for (SurfaceProperties step : path) 
        {
            rover.Move();
        }
    }

    private String getKey(SurfaceProperties field) 
    {
        return field.getPosition()[0] + "," + field.getPosition()[1];
    }

    private static class Node 
    {
        SurfaceProperties field;
        Node parent;
        double gCost;
        double fCost;

        Node(SurfaceProperties field, Node parent, double gCost, double hCost) 
        {
            this.field = field;
            this.parent = parent;
            this.gCost = gCost;
            this.fCost = gCost + hCost;
        }
    }
}
