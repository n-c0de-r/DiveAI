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
import lenz.htw.ai4g.ai.ShoppingAction;
import lenz.htw.ai4g.ai.ShoppingItem;

public class GameAI_Ex4_Diver1 extends AI {
	private final int CELL_SIZE = 15;
	
	//Complex Types
	private ArrayList<Node> pathToFollow;
	private ArrayList<ShoppingItem> boughtItems;
	private Path2D[] obstacleArray;
	private Point nextAim;
	private Point shipPosition;
	private Point[] pearlArray;
	private Point[] bottleArray;
	
	//Primitive Types
	private boolean isDiving;
	private int currentScore;
	private int currentMoney;
	private int sceneHeight;
	private int sceneWidth;
	private float playerDirection;
	
	public GameAI_Ex4_Diver1 (Info info) {
		super(info);
		
		enlistForTournament(577683, 577423);
		
		//Get initial values 
		init();

		makeDecision();
	}

	@Override
	public String getName() {
		return "Kinilau-A-Mano";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.GRAY;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.CYAN;
	}

	@Override
	public PlayerAction update() {
		// Something got picked up
		updateNumbers();
		
		// If you have money and are close to ship, buy something
		if (isCloseToShip() && currentMoney >= 2 && boughtItems.size() <4){
			return new ShoppingAction(buyItem());
		}
		
//		makeDecision();
//		if (isDiving) {
//			
//			// IF you are following a path, continue it and do this
			if (!pathToFollow.isEmpty()) {
				if (nextAim.distance(info.getX(), info.getY()) < CELL_SIZE) {
					Node node = pathToFollow.remove(0);
					nextAim.x = node.getX() * CELL_SIZE + CELL_SIZE/2;
					nextAim.y = node.getY() * CELL_SIZE + CELL_SIZE/2;
					playerDirection = calculateDirectionToPoint(new Point((int)info.getX(),(int)info.getY()), nextAim);
				}
			} else {
				makeDecision();
			}
//		} else {
//			makeDecision();
//		}
			
		return new DivingAction(info.getMaxAcceleration(), playerDirection);
	}
	
	
	
	//---------------------Helper Methods-----------------------------
	
	// Returning void
	/**
	 * Decide if you swim to an aim directly or with pathfinding
	 * @param from	Point where to start swimming.
	 * @param to	Point to swim to.
	 */
	private void directSwimOrPath(Point from, Point to) {
		// Check for collision
		Point collisionPoint = calculateCollision(from,to);
		// If there's IS one, get the path to the pearl
		if (collisionPoint != null) {
			pathToFollow = calculateDijkstraPath(from, collisionPoint);
			pathToFollow.add(new Node(to.x/CELL_SIZE, to.y / CELL_SIZE));
//			pathToFollow = smoothPath(pathToFollow);
			Node node = pathToFollow.remove(0);
			nextAim.x = node.getX() * CELL_SIZE + CELL_SIZE/2;
			nextAim.y = node.getY() * CELL_SIZE + CELL_SIZE/2;
			playerDirection = calculateDirectionToPoint(new Point((int)info.getX(),(int)info.getY()), nextAim);
		} else {
			// If there's no collision dive straight through
			playerDirection = calculateDirectionToPoint(from, to);
		}
	}
	
	
	
	/**
	 * Initialize starting variables.
	 */
	private void init() {
		currentScore = 0;
		currentMoney = 0;
		isDiving = true;
		nextAim = new Point();
		boughtItems = new ArrayList<>();
		pathToFollow = new ArrayList<>();
		bottleArray = info.getScene().getRecyclingProducts();
		obstacleArray = info.getScene().getObstacles();
		pearlArray = info.getScene().getPearl();
		sceneHeight = info.getScene().getHeight();
		sceneWidth = info.getScene().getWidth();
		shipPosition = new Point(info.getScene().getShopPosition(), 0);
	}
	
	
	
	/**
	 * Some crappy decision making function.
	 * @return 
	 */
	private void makeDecision() {
		// If no items yet buy supplies first
		if (boughtItems.size() < 4) {
			// Not enough money yet
			if (currentMoney < 2) {
				nextAim = findNearestPoint(bottleArray);
			} else {
				nextAim = shipPosition;
			}
		} else {
			nextAim = findNearestPoint(pearlArray);
		}
		
		// If you don't have enough air or just resurfaces, change your state
		if ((info.getAir() < info.getMaxAir() * 1/3) && isDiving) {
			isDiving = false;
			nextAim = new Point((int) info.getX(), 0);
		}
		
		if (info.getAir() == info.getMaxAir() && !isDiving){
			isDiving = true;
			nextAim = findNearestPoint(pearlArray);
		}
		
		directSwimOrPath(new Point((int)info.getX(),(int)info.getY()), nextAim);
	}
	
	
	
	/**
	 * Finds the point closest to the player and removes it.
	 * @param points	The array of points to look over.
	 */
	private void removeNearestPoint(Point[] points) {
		
		double minimumDistance = Double.MAX_VALUE;
		int nearestIndex = 0;
		int index = 0;
		// Check all points
		for (Point point : points) {
			
			//If it is already collected, ignore it and get next
			if (point == null) {
				++index;
				continue;
			}
			
			double currentPearlDistance = point.distance(info.getX(), info.getY());
			
			// A closer point is found, update
			if (currentPearlDistance < minimumDistance) {
				minimumDistance = currentPearlDistance;
				nearestIndex = index;
			}
			++index;
		}
		
		points[nearestIndex] = null;
	}
	
	
	
	private void updateNumbers() {
		if (info.getScore() != currentScore) {
			currentScore = info.getScore();
			removeNearestPoint(pearlArray);
			
		}
		
		if (info.getMoney() > currentMoney) {
			currentMoney = info.getMoney();
			if(!isCloseToShip()) {
				removeNearestPoint(bottleArray);
			}
		}
	}
	
	
	
	// Returning primitives
	/**
	 * Calculates distance to ship, relevant to buying items
	 * @return		true, if you are very close
	 */
	private boolean isCloseToShip() {
		return Math.abs(info.getX() - shipPosition.x) < CELL_SIZE
		&& Math.abs(info.getY() - shipPosition.y) < CELL_SIZE;
	}
	
	
	
	/**
	 * Calculates a direction to a certain Point, from player position.
	 * @param from	Point to calculate direction from.
	 * @param to	Point to calculate the direction to.
	 * @return
	 */
	private float calculateDirectionToPoint(Point from, Point to) {
		
		double distanceX = to.x - from.x;
		double distanceY = to.y - from.y;
		
		return (float) -Math.atan2(distanceY, distanceX);
	}
	
	
	
	// Returning Complex types
	/**
	 * Calculates a path to follow, with an adjacency matrix and the Dijkstra algorithm
	 * @param from	Point where we start the pathfinding
	 * @param to	Point where we want to go to
	 * @return		ArrayList of Nodes to follow step by step
	 */
	private ArrayList<Node> calculateDijkstraPath(Point from, Point to) {
		Node[][] nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
		ArrayList<Node> visitNext = new ArrayList<>();

		Node begin = nodesMatrix[from.x / CELL_SIZE][from.y / CELL_SIZE];
		Node end = nodesMatrix[(to.x / CELL_SIZE)-1][(to.y / CELL_SIZE)];

		begin.setDistance(0);
		visitNext.add(begin);
		end.setVisited(false);
		
		Node visiting = visitNext.remove(0);
		
		int x = visiting.getX();
		int y = visiting.getY();
		
		while(!end.isVisited()) {
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
			nodesMatrix[x][y].setVisited(true);
			
			// Get the next node to check
			if (!visitNext.isEmpty()) {
				visiting=visitNext.remove(0);
				x = visiting.getX();
				y = visiting.getY();
			}
		}
		
		ArrayList<Node> temp = new ArrayList<>();
		temp.add(end);
		while(temp.get(0).getPrevious() != null) {
			temp.add(0, temp.get(0).getPrevious());
		}
		
		return temp;
	}
	
	
	
//	private ArrayList<Node> smoothPath(ArrayList<Node> originalPath) {
//		ArrayList<Node> temp = new ArrayList<>();
//		// Keep the last element, it's the target
//		Node lastNode = originalPath.remove(originalPath.size()-1);
//		Point last = new Point(lastNode.getX() * CELL_SIZE, lastNode.getY() * CELL_SIZE);
//		temp.add(0, lastNode);
//		for (int i = originalPath.size()-2; i > 1 ; i--) {
//			Node beforeNode = originalPath.get(i);
//			Point before = new Point(beforeNode.getX() * CELL_SIZE, beforeNode.getY() * CELL_SIZE);
//			Point collision = calculateCollision(last, before);
//			if (collision != null) {
//				lastNode = originalPath.get(i+1);
//				last = new Point(lastNode.getX() * CELL_SIZE, lastNode.getY() * CELL_SIZE);
//				if (temp.contains(lastNode)) {
//					continue;
//				} else {
//					i++;
//				}
//				temp.add(0, lastNode);
//			}
//		}
////		temp.add(0, originalPath.get(1));
//		return temp;
//	}
	
	
	
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
	 * Calculates the coordinates of the first collision Point
	 * @param from	The point we are starting from
	 * @param to	The Aim towards we are swimming
	 * @return		Point of collision in sigh line
	 */
	private Point calculateCollision(Point from, Point to) {
		Point p = new Point();
		float direction = calculateDirectionToPoint(from, to);
		int n = (int) to.distance(from.x, from.y);
		double sin = Math.sin(direction);
		double cos = Math.cos(direction);
		for (int i = 0; i < n; i+=10) {
			p.x = (int) (to.x - cos*i);
			p.y = (int) (to.y + sin*i);
			for (Path2D obstacle : obstacleArray) {
				if (obstacle.contains(p)) {
					// Offset the last find
					p.x = (int) (to.x - cos*(i-20));
					p.y = (int) (to.y + sin*(i-20));
					return p;
				}
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * Finds the point closest to the player.
	 * @param points	The array of points to look over.
	 * @return			A Point object of the closest point
	 */
	private Point findNearestPoint(Point[] points) {
		
		Point closest = null;
		double minimumDistance = Double.MAX_VALUE;
		
		// Check all points
		for (Point point : points) {
			
			//If it is already collected, ignore it and get next
			if (point == null) {
				continue;
			}
			
			double currentPearlDistance = point.distance(info.getX(), info.getY());
			
			// A closer point is found, update all
			if (currentPearlDistance < minimumDistance) {
				minimumDistance = currentPearlDistance;
				closest = point;
			}
		}
		
		// Returns the closest Point
		return closest;
	}
	
	
	
	/**
	 * Decide which item to buy
	 * @return	The ShoppingItem to buy
	 */
	private ShoppingItem buyItem() {
		ShoppingItem Item = null;
		
		if (!boughtItems.contains(ShoppingItem.MOTORIZED_FLIPPERS)) {
			Item = ShoppingItem.MOTORIZED_FLIPPERS;
		} else if (!boughtItems.contains(ShoppingItem.BALLOON_SET)) {
			Item = ShoppingItem.BALLOON_SET;
		} else  if (!boughtItems.contains(ShoppingItem.STREAMLINED_WIG)){
			Item = ShoppingItem.STREAMLINED_WIG;
		} else {
			Item = ShoppingItem.CORNER_CUTTER;
		}
		// Add the Item to list of bought items & reduce money
		boughtItems.add(Item);
		currentMoney -=2;
		
		return Item;
	}
	
	/**
	 * Finds the points closest to the Ocean surface.
	 * 
	 * @param pearls	The array of points to look over.
	 * @return			A Point object of the topmost pearl.
	 */
	
	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		gfx.setColor(Color.MAGENTA);
		gfx.drawLine((int)info.getX(), (int)info.getY(), nextAim.x, nextAim.y);
		
		if (pathToFollow.size() >0) {
			
			Node n1;
			Node n2;
			for (int i = 0; i < pathToFollow.size()-1; i++) {
				if(i%2 == 0) {
					gfx.setColor(Color.GREEN);
				} else {
					gfx.setColor(Color.RED);
				}
				n1 = pathToFollow.get(i);
				n2 = pathToFollow.get(i+1);
				
				gfx.drawLine(n1.getX()*CELL_SIZE, n1.getY()*CELL_SIZE, n2.getX()*CELL_SIZE, n2.getY()*CELL_SIZE);
			}
		}
	}
}