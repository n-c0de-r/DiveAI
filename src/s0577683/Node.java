package s0577683;

/**
 * Class creating Vertices. Each storing their name
 * the previous vertex in the path and distance to it.
 * 
 * @author	n-c0de-r
 * @author	AlexanderStae
 * @version	11.07.2021
 */
public class Node {

	private Node previous;
	private int distance;
	private boolean visited;
	private int x;
	private int y;
	
	/**
	 * Full constructor of the Vertex class.
	 * 
	 * @param prev	Contains the previous neighboring Vertex.
	 * @param dist	The minimum distance to this Vertex.
	 * @param visit	Boolean if the Vertex is visited.
	 */
	public Node (Node prev, int dist, boolean visit, int posX, int posY) {
		previous = prev;
		distance = dist;
		visited = visit;
		x = posX;
		y = posY;
	}
	
	/**
	 * Get the vertex's previous neighbor.
	 * @return Vertex which came before.
	 */
	public Node getPrevious() {
		return previous;
	}
	
	/**
	 * Get the distance to this vertex.
	 * @return Integer value of distance.
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * Get the X coordinate to this vertex.
	 * @return Integer value of X coordinate.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Get the Y coordinate to this vertex.
	 * @return Integer value of Y coordinate.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Get the visited status of this vertex.
	 * @return Visited status of the Vertex.
	 */
	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * Set the neighboring vertex to this one.
	 * @param prev	Vertex that came before.
	 */
	public void setPrevious(Node prev) {
		previous = prev;
	}
	
	/**
	 * Set the shortest distance to this vertex.
	 * @param dist	Integer value of shortest distance.
	 */
	public void setDistance(int dist) {
		distance = dist;
	}
	
	/**
	 * Set the visited status of this vertex.
	 * @param visit	Set if this Vertex was visited.
	 */
	public void setVisited(boolean visit) {
		visited = visit;
	}
}
