package s0577683;

import java.awt.Point;
import java.util.*;

public class Graph {
	// Store edges in a graph
	private Map<Point, List<Point>> map = new HashMap<>();
	
	// Add a new node to the graph
	public void addNode(Point node) {
		map.put(node, new LinkedList<Point>());
	}
	
	// Add a new edge between two nodes
	public void addEdge(Point sourceNode, Point destinationNode,
					    boolean twoDirectional) {
		// Checks for the availability of the source node
		// and creates one, if not available
		if (!map.containsKey(sourceNode)) {
			addNode(sourceNode);
		}
		
		// Checks for the availability of the destination node
		// and creates one, if not available
		if (!map.containsKey(destinationNode))
		{
			addNode(destinationNode);
		}
		
		// Add a destination point to the source point
		map.get(sourceNode).add(destinationNode);
		
		// Should the graph go in both directions
		// add the source point to the destination point
		if (twoDirectional)
		{
			map.get(destinationNode).add(sourceNode);
		}
	}
	
	// Get the amount of nodes available in the graph
	public int getNodeAmount()
	{
		return map.keySet().size();
	}
	
	// Get the amount of edges available in the graph
	public int getEdgeAmount(boolean twoDirectional)
	{
		int edgeAmount = 0;
		
		// For each node get the amount of edges
		for (Point node : map.keySet()) {
			edgeAmount += map.get(node).size();
		}
		
		// For two directional graphs divide the amount by 2
		// since each node pair only have one edge
		if (twoDirectional) {
			edgeAmount /= 2;
		}
		
		return edgeAmount;
	}
	
	// Check a node for its availability
	public boolean nodeAvailable(Point node) {
		return (map.containsKey(node))? true : false;
	}
	
	// Check an edge for its availability
	public boolean edgeAvailable(Point sourceNode, Point destinationNode)
	{
		return (map.get(sourceNode).contains(destinationNode))? true : false; 
	}
}
