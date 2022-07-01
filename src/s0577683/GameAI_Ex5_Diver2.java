package s0577683;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import lenz.htw.ai4g.ai.ShoppingAction;
import lenz.htw.ai4g.ai.ShoppingItem;

public class GameAI_Ex5_Diver2 extends AI {
	private final int CELL_SIZE = 15;
	
	//Complex Types
	private ArrayList<Point> pathToFollow;
	private ArrayList<ShoppingItem> boughtItems;
	private Path2D[] obstacleArray;
	private Point nextAim;
	private Point shipPosition;
	private Point[] pearlArray;
	private Point[] bottleArray;
	private Rectangle[] streams;
	
	//Primitive Types
	private int airFraction;
	private int currentScore;
	private int currentMoney;
	private int sceneHeight;
	private int sceneWidth;
	private float playerDirection;
	
	public GameAI_Ex5_Diver2 (Info info) {
		super(info);
		
		enlistForTournament(577683, 577423);
		
		//Get initial values 
		init();

		makeDecision();
	}

	@Override
	public String getName() {
		return "Tiamat";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.ORANGE;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.GREEN;
	}

	@Override
	public PlayerAction update() {
		// Something got picked up
		updateNumbers();
		
		// If you have money and are close to ship, buy something
		if (isCloseToShip() && currentMoney >= 2 && boughtItems.size() < 4){
			return new ShoppingAction(buyItem());
		}
		
		// IF you are following a path, continue it and do this
		if (!pathToFollow.isEmpty()) {
			if (nextAim.distance(info.getX(), info.getY()) < CELL_SIZE) {
				nextAim = pathToFollow.remove(0);
			}
		} else {
			makeDecision();
		}
		
		playerDirection = calculateDirectionToPoint(new Point((int)info.getX(),(int)info.getY()), nextAim);
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
		pathToFollow.clear();
		Point collisionPoint = calculateCollision(from, to);
		// If there's IS one, get the path to the pearl
		if (collisionPoint != null) {
			pathToFollow = calculateDijkstraPath(from, collisionPoint);
			pathToFollow.add(new Point(to.x, to.y));
			// pathToFollow = smoothPath(pathToFollow);
			nextAim = pathToFollow.remove(0);
		}
	}
	
	
	
	/**
	 * Initialize starting variables.
	 */
	private void init() {
		airFraction = 2;
		currentScore = 0;
		currentMoney = 0;
		nextAim = new Point();
		boughtItems = new ArrayList<>();
		pathToFollow = new ArrayList<>();
		bottleArray = info.getScene().getRecyclingProducts();
		obstacleArray = info.getScene().getObstacles();
		pearlArray = info.getScene().getPearl();
		sceneHeight = info.getScene().getHeight();
		sceneWidth = info.getScene().getWidth();
		shipPosition = new Point(info.getScene().getShopPosition(), 0);
		Rectangle[] rectsToL = info.getScene().getStreamsToTheLeft();
		Rectangle[] rectsToR = info.getScene().getStreamsToTheRight();
		streams = new Rectangle[rectsToL.length + rectsToR.length];
		int pos= 0;
		for (Rectangle rectangle : rectsToL) {
			streams[pos] = rectangle;
			pos++;
		}
		for (Rectangle rectangle : rectsToR) {
			streams[pos] = rectangle;
			pos++;
		}
	}
	
	
	
	/**
	 * Some crappy decision making function.
	 */
	private void makeDecision() {
		// If no items yet buy supplies first
		if (boughtItems.size() < 4 && bottleArray.length != 0) {
			// Not enough money yet
			if (currentMoney < 2) {
				nextAim = findNearestPoint(bottleArray);
			} else {
				nextAim = new Point(shipPosition);
			}
		} else {
			nextAim = findNearestPoint(pearlArray);
		}
		// If you don't have enough air resurfaces
		if (info.getAir() < info.getMaxAir() * 1/airFraction) {
			nextAim = new Point((int) info.getX(), 0);
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
			makeDecision();
		}
		
		if (info.getMoney() != currentMoney) {
			currentMoney = info.getMoney();
			if(!isCloseToShip()) {
				removeNearestPoint(bottleArray);
			}
			makeDecision();
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
	
	
	
	private boolean isInStream(Point point) {
		// You don't have the items to get this point right now!
		for (Rectangle rect : streams) {
			if (rect.contains(point) && !boughtItems.contains(ShoppingItem.CORNER_CUTTER)) {
				return true;
			}
		}
		return false;
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
	private ArrayList<Point> calculateDijkstraPath(Point from, Point to) {
		Node[][] nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
		ArrayList<Node> visitNext = new ArrayList<>();

		int fromX = Math.max(0, Math.min((from.x / CELL_SIZE), nodesMatrix.length-1));
		int fromY = from.y / CELL_SIZE;
		int toX = Math.max(0, Math.min((to.x / CELL_SIZE), nodesMatrix.length-1));
		int toY = to.y / CELL_SIZE;
		Node begin = nodesMatrix[fromX][fromY];
		Node end = nodesMatrix[toX][toY];
		
		// Dirty fix for pearls that are within widely intersecting areas
//		if (toX >= 1) {
//			nodesMatrix[toX-1][toY].setVisited(false);
//		}
//		if (toX < nodesMatrix.length-1) {
//			nodesMatrix[toX+1][toY].setVisited(false);
//		}
		if (toY >= 1 && nodesMatrix[toX][toY-1].isVisited()) {
			nodesMatrix[toX][toY-1].setVisited(false);
		}
//		if (toY < nodesMatrix[toX].length-1) {
//			nodesMatrix[toX][toY+1].setVisited(false);
//		}
		
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
				Point p = new Point(nodesMatrix[x + i][y].getX() * CELL_SIZE + CELL_SIZE/2,
						nodesMatrix[x + i][y].getY() * CELL_SIZE + CELL_SIZE/2);
				if (isInStream(p)) {
					i++;
					continue;
				}
				for (int j = -1; j <= 1; j++) {
					if (y + j < 0 || y + j >= nodesMatrix[0].length || (i == 0 && j == 0)) {
						continue;
					}
					
					p = new Point(nodesMatrix[x + i][y + j].getX() * CELL_SIZE + CELL_SIZE/2,
							nodesMatrix[x + i][y + j].getY() * CELL_SIZE + CELL_SIZE/2);
					if (isInStream(p)) {
						j++;
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
		
		ArrayList<Point> temp = new ArrayList<>();
		Point p = new Point();
		p.x = end.getX() * CELL_SIZE + CELL_SIZE/2;
		p.y = end.getY() * CELL_SIZE + CELL_SIZE/2;
		temp.add(p);
		while(end.getPrevious() != null) {
			Point p2 = new Point();
			p2.x = end.getPrevious().getX() * CELL_SIZE + CELL_SIZE/2;
			p2.y = end.getPrevious().getY() * CELL_SIZE + CELL_SIZE/2;
			
			temp.add(0, p2);
			end = end.getPrevious();
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
		Rectangle rect = new Rectangle();
		Point p = new Point();
		float direction = calculateDirectionToPoint(from, to);
		int n = (int) to.distance(from.x, from.y)-CELL_SIZE;
		double sin = Math.sin(direction);
		double cos = Math.cos(direction);
		for (int i = 0; i < n; i+=CELL_SIZE) {
			int x = (int) (to.x - cos*i);
			int y = (int) (to.y + sin*i);
			rect.setBounds(x-2, y-2, 4, 4);
			for (Path2D obstacle : obstacleArray) {
//				if (obstacle.contains(p)) {
				if(obstacle.intersects(rect)) {
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
			if (point == null || isInStream(point)) {
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
		
		if (!boughtItems.contains(ShoppingItem.CORNER_CUTTER)) {
			Item = ShoppingItem.CORNER_CUTTER;
		} else if (!boughtItems.contains(ShoppingItem.BALLOON_SET)) {
			airFraction = 3;
			Item = ShoppingItem.BALLOON_SET;
		} else  if (!boughtItems.contains(ShoppingItem.STREAMLINED_WIG)){
			Item = ShoppingItem.STREAMLINED_WIG;
		} else {
			Item = ShoppingItem.MOTORIZED_FLIPPERS;
		}
		// Add the Item to list of bought items & reduce money
		boughtItems.add(Item);
		
		return Item;
	}
	
	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		gfx.setColor(Color.MAGENTA);
		gfx.drawLine((int)info.getX(), (int)info.getY(), nextAim.x, nextAim.y);
		
		if (pathToFollow.size() >0) {
			
			Point  n1;
			Point  n2;
			for (int i = 0; i < pathToFollow.size()-1; i++) {
				if(i%2 == 0) {
					gfx.setColor(Color.GREEN);
				} else {
					gfx.setColor(Color.RED);
				}
				n1 = pathToFollow.get(i);
				n2 = pathToFollow.get(i+1);
				
				gfx.drawLine(n1.x, n1.y, n2.x, n2.y);
			}
		}
	}
}