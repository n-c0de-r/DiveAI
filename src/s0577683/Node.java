package s0577683;

/**
 * Class creating Nodes. Each storing their position
 * the previous node in the path and distance to it.
 * 
 * @author	n-c0de-r
 * @version	17.05.2022
 */
public class Node {

	private Node previous;
	private int distance;
	private boolean visited;
	private int x;
	private int y;
	
	/**
	 * Full constructor of the Node class.
	 * 
	 * @param prev	Contains the previous neighboring Node.
	 * @param dist	The minimum distance to this Node.
	 * @param visit	Boolean if the Node is visited.
	 */
	public Node (Node prev, int dist, boolean visit, int posX, int posY) {
		previous = prev;
		distance = dist;
		visited = visit;
		x = posX;
		y = posY;
	}
	
	/**
	 * Get the node's previous neighbor.
	 * @return Node which came before.
	 */
	public Node getPrevious() {
		return previous;
	}
	
	/**
	 * Get the distance to this node.
	 * @return Integer value of distance.
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * Get the X coordinate to this node.
	 * @return Integer value of X coordinate.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Get the Y coordinate to this node.
	 * @return Integer value of Y coordinate.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Get the visited status of this node.
	 * @return Visited status of the Node.
	 */
	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * Set the neighboring node to this one.
	 * @param prev	Node that came before.
	 */
	public void setPrevious(Node prev) {
		previous = prev;
	}
	
	/**
	 * Set the shortest distance to this node.
	 * @param dist	Integer value of shortest distance.
	 */
	public void setDistance(int dist) {
		distance = dist;
	}
	
	/**
	 * Set the visited status of this node.
	 * @param visit	Set if this Node was visited.
	 */
	public void setVisited(boolean visit) {
		visited = visit;
	}
}
