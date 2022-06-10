package s0577683;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.ArrayList;


import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class GameAI_Ex3_Diver3c extends AI {
	private final int CELL_SIZE = 15;
	
	//Complex Types
	private ArrayList<Node> visitNext = new ArrayList<>();
	private ArrayList<Node> pathToFollow = new ArrayList<>();
	private Node begin;
	private Node end;
	private Node[][] nodesMatrix;
	private Path2D[] obstacleArray;
	private Point nearestPearl;
	private Point nextAim;
	private Point[] pearlArray;
	
	//Primitive Types
	private boolean dive;
	private int currentScore;
	private int nearestPearlIndex;
	private int sceneHeight;
	private int sceneWidth;
	private float playerDirection;
	
	public GameAI_Ex3_Diver3c (Info info) {
		super(info);
		
		//Get initial values 
		currentScore = 0;
		dive = true;
		obstacleArray = info.getScene().getObstacles();
		pearlArray = info.getScene().getPearl();
		sceneHeight = info.getScene().getHeight();
		sceneWidth = info.getScene().getWidth();
		//Get calculated values
		nearestPearl = findTopmostPearl(pearlArray); // Get top to bottom
//		nearestPearl = findNearestPearl(pearlArray); // closest always
		nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
		nextAim = new Point();
		Point first = calculateCollisionPoint(nearestPearl.x, nearestPearl.y);
		if (first == null) {
			pathToFollow.add(0, nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)]);
			nextAim.x = nearestPearl.x;
			nextAim.y = 0;
			playerDirection = calculateDirectionToPoint(nextAim);
		} else {
			nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
			begin = nodesMatrix[first.x / CELL_SIZE][first.y / CELL_SIZE];
			end = nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)];
			begin.setDistance(0);
			visitNext.add(begin);
			end.setVisited(false);
			pathToFollow = calculateDijkstraPath();
			nextAim.x = first.x;
			nextAim.y = first.y;
			playerDirection = calculateDirectionToPoint(nextAim);
		}
	}

	@Override
	public String getName() {
		return "Son Of Sobek";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.GREEN;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.BLUE;
	}

	@Override
	public PlayerAction update() {
		// TODO: Lots of code duplication. calls for refactoring!
		
		// IF you are following a path, do this
		if (!pathToFollow.isEmpty()) {
			if (nextAim.distance(info.getX(), info.getY()) < CELL_SIZE) {
				nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
				nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
				pathToFollow.remove(0);
				playerDirection = calculateDirectionToPoint(nextAim);
			}// Arrived at final node
		} else { 
			// Swim to pearl
			playerDirection = calculateDirectionToPoint(nearestPearl);
			
			if (info.getScore() != currentScore) { // Pearl is collected
				currentScore = info.getScore();
				// If pearl is collected by chance, but path is still not done,
				//remove this pearl instead and continue your way
				nearestPearl = findNearestPearl(pearlArray);
				pearlArray[nearestPearlIndex] = null;
				
				nearestPearl = findNearestPearl(pearlArray);
				// If you still have air, and the next one is really close,
				// Use Pathfinding to get it while you are there
				if (nearestPearl.distance(info.getX(), info.getY()) < 100 && info.getAir() > info.getMaxAir()/2) {
					nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
					begin = nodesMatrix[(int) info.getX() / CELL_SIZE][(int) info.getY() / CELL_SIZE];
					end = nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)];
					begin.setDistance(0);
					visitNext.add(begin);
					end.setVisited(false);
					pathToFollow = calculateDijkstraPath();
					nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
					nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
					playerDirection = calculateDirectionToPoint(nextAim);
				} else {
					// Otherwise play safe and resurface
					dive=false;
					
					//Check if there are collisions on the way up
					Point first = calculateCollisionPoint((int) info.getX(), (int) info.getY());
					
					// If not, just swimm straight upwards
					if (first == null) {
						nearestPearl = new Point((int) info.getX(), 0);
						// Otherwise avoid the obstacle with "pathfinding"
					} else {
						// Reset Matrix to find next aim
						nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
						nearestPearl = new Point((int) info.getX(), 0);
						end = nodesMatrix[(int) (info.getX() / CELL_SIZE)][0];
						
						begin = nodesMatrix[first.x / CELL_SIZE][first.y / CELL_SIZE];
						begin.setDistance(0);
						visitNext.add(begin);
						end.setVisited(false);
						pathToFollow = calculateDijkstraPath();
						nextAim.x = first.x;
						nextAim.y = first.y;
						playerDirection = calculateDirectionToPoint(nextAim);
					}
				}
			}
			// We collected a pearl and there's no other nearby,
			// So resurface until you have full air!
			else if (!dive && info.getAir() == info.getMaxAir()) {
				// Find your next aim
				nearestPearl = findTopmostPearl(pearlArray);
				
				// Check if there is a collision on the way to it
				Point first = calculateCollisionPoint(nearestPearl.x, nearestPearl.y);
				
				// If it's a straight line
				if (first == null) {
					// Set it as a goal
					pathToFollow.add(0, nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)]);
					// But before that, swim to the surface point right above it
					nextAim.x = nearestPearl.x;
					nextAim.y = 0;
					// Swim straight down!
					playerDirection = calculateDirectionToPoint(nextAim);
					
				} else {
					// If there are obstacles, use "pathfinding"
					nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
					begin = nodesMatrix[(int) info.getX() / CELL_SIZE][(int) info.getY() / CELL_SIZE];
					end = nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)];
					begin.setDistance(0);
					visitNext.add(begin);
					end.setVisited(false);
					pathToFollow = calculateDijkstraPath();
					nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
					nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
					playerDirection = calculateDirectionToPoint(nextAim);
				}
				// We are done with "surfacing" and want to dive again
				dive=true;
			}
		}
			
		return new DivingAction(info.getMaxAcceleration(), playerDirection);
	}
	
	
	
	//---------------------Helper Methods-----------------------------
	
	
	
	private ArrayList<Node> calculateDijkstraPath() {
		Node visiting = visitNext.get(0);
		visitNext.remove(0);
		int x = visiting.getX();
		int y = visiting.getY();
		
		while(!end.isVisited()) {
			nodesMatrix[x][y].setVisited(true);
			for (int i = -1; i <= 1; i++) {
				if (x + i < 0 || x + i >= nodesMatrix.length) {
					continue;
				}
				for (int j = -1; j <= 1; j++) {
					if (y + j < 0 || y + j >= nodesMatrix[0].length || (i == 0 && j == 0)) {
						continue;
					}
					if (!nodesMatrix[x + i][y + j].isVisited()) {
						if (!visitNext.contains(nodesMatrix[x + i][y + j])) {
							visitNext.add(visitNext.size(), nodesMatrix[x + i][y + j]);
						}
					}
					if (nodesMatrix[x + i][y + j].getDistance() > visiting.getDistance()+1) {
						nodesMatrix[x + i][y + j].setDistance(visiting.getDistance()+1);
						nodesMatrix[x + i][y + j].setPrevious(visiting);
					}
				}
			}
			
			// Get the next node to check
			if (!visitNext.isEmpty()) {
				visiting=visitNext.get(0);
				visitNext.remove(0);
				x = visiting.getX();
				y = visiting.getY();
			}
		}
		
		visitNext.clear();
		ArrayList<Node> temp = new ArrayList<>();
		temp.add(end.getPrevious()); // Skip last, same as pearl, obsolete
		
		while(temp.get(0).getPrevious() != null) {
			temp.add(0, temp.get(0).getPrevious());
		}
		temp.remove(0); // remove first, it's the player position = obsolete
		return temp;
	}
	
	
	
	/**
	 * Calculates a direction to a certain Point, from player position.
	 * 
	 * @param point	Point to calculate the distance to.
	 * @return
	 */
	private float calculateDirectionToPoint(Point point) {
		
		double distanceX = point.x - info.getX();
		double distanceY = point.y - info.getY();
		
		return (float) -Math.atan2(distanceY, distanceX);
	}
	
	
	
	/**
	 * Calculates an adjacency matrix to use with a pathfinding algorithm
	 * 
	 * @param width		Width of part to check
	 * @aparam height	Height of part to check
	 * @return			A boolean adjacency matrix
	 */
	private Node[][] calculateIntersections(int width, int height) {
		Node[][] tempArray = new Node[width/CELL_SIZE][height/CELL_SIZE];
		Rectangle rect = new Rectangle();
		
		for (int x = 0; x < width/CELL_SIZE; x++) {
			innerLoop: for (int y = 0; y < height/CELL_SIZE; y++) {
				rect.setBounds(x*CELL_SIZE, y*CELL_SIZE, CELL_SIZE, CELL_SIZE);
				
				// Check intersections of Rectangles with sand banks
				for(Path2D obstacle : obstacleArray) {
					if(obstacle.intersects(rect)) {
						
						/* If ANY intersection is found, skip this position
						   as it is not passable keep it false */
						tempArray[x][y] = new Node (null, Integer.MAX_VALUE, true, x, y);
						continue innerLoop;
					}
				}
				
				// Only if it doesn't intersect any, path is free
				tempArray[x][y] = new Node (null, Integer.MAX_VALUE, false, x, y);
			}
		}
		return tempArray;
	}
	
	
	
	/**
	 * Finds the peals closest to the player.
	 * Also sets the index of that pearl for later removal.
	 * 
	 * @param pearls	The array of pearls to look over.
	 * @return			A Point object of the closest pearl.
	 */
	private Point findNearestPearl(Point[] pearls) {
		
		Point closest = null;
		double minimumDistance = Double.MAX_VALUE;
		int index = 0;
		// Check all pearls
		for (Point pearl : pearls) {
			
			//If it is already collected, ignore it and get next
			if (pearl == null) {
				++index;
				continue;
			}
			
			double currentPearlDistance = pearl.distance(info.getX(), info.getY());
			
			// A closer pearl is found, update all
			if (currentPearlDistance < minimumDistance) {
				minimumDistance = currentPearlDistance;
				closest = pearl;
				nearestPearlIndex = index;
			}
			++index;
		}
		
		// Updates index for removal and returns the closest Point
		return closest;
	}
	
	
	
	/**
	 * Finds the peals closest to the Ocean surface.
	 * Also sets the index of that pearl for later removal.
	 * 
	 * @param pearls	The array of pearls to look over.
	 * @return			A Point object of the leftmost pearl.
	 */
	private Point findTopmostPearl(Point[] pearls) {
		
		Point closest = null;
		int minimumDistance = Integer.MAX_VALUE;
		int index = 0;
		
		// Check all pearls
		for (Point pearl : pearls) {
			//If it is already collected, ignore it and get next
			if (pearl == null) {
				++index;
				continue;
			}
			
			// A closer pearl is found, update all
			if (pearl.y < minimumDistance) {
				minimumDistance = pearl.y;
				closest = pearl;
				nearestPearlIndex = index;
			}
			++index;
		}
		
		// Updates index for removal and returns the closest Point
		return closest;
	}
	
	
	
	/**
	 * Calculculates the coordinates of a predicted collision point
	 * along a certain virtual ray of variable length.
	 * 
	 * @param lookahead		The length of the ray in pixels(?)
	 * @param playerDirection	The player's current direction
	 * @return		a Point object of collision to check against.
	 */
	private Point calculateCollisionPoint(int x, int y) {
		Point p = new Point();
		p.x = (int) x;
		double sin = Math.sin(Math.PI/2);
		for (int i = 0; i < x; i++) {
			p.y = (int) (y - sin*i);
			for (Path2D obstacle : obstacleArray) {
				if (obstacle.contains(p)) {
					return p;
				}
			}
		}
		
		return null;
	}

}
