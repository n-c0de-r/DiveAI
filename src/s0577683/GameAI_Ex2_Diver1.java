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

public class GameAI_Ex2_Diver1 extends AI {
	private final int CELL_SIZE = 25;
	
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
	private int currentScore;
	private int nearestPearlIndex;
	private int sceneHeight;
	private int sceneWidth;
	private float playerDirection;
	
	public GameAI_Ex2_Diver1 (Info info) {
		super(info);
		
		enlistForTournament(577683, 577423);
		
		//Get initial values 
		currentScore = 0;
		obstacleArray = info.getScene().getObstacles();
		pearlArray = info.getScene().getPearl();
		sceneHeight = info.getScene().getHeight();
		sceneWidth = info.getScene().getWidth();
		//Get calculated values
//		nearestPearl = findLeftmostPearl(pearlArray); // Get left to right
		nearestPearl = findNearestPearl(pearlArray); // closest always
		nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
		begin = nodesMatrix[(int) (info.getX() / CELL_SIZE)][(int) (info.getY() / CELL_SIZE)];
		end = nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)];
		begin.setDistance(0);
		visitNext.add(begin);
		end.setVisited(false);

//		playerDirection = calculateDirectionToPoint(nearestPearl);
		pathToFollow = calculateDijkstraPath();
		nextAim = new Point();
		nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
		nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
		pathToFollow.remove(0);
		playerDirection = calculateDirectionToPoint(nextAim);
	}

	@Override
	public String getName() {
		return "DrownedOne";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.BLUE;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.GREEN;
	}

	@Override
	public PlayerAction update() {
		// Detects if a pearl is collected
		if (info.getScore() != currentScore) {
			currentScore = info.getScore();
			pearlArray[nearestPearlIndex] = null;
			nearestPearl = findNearestPearl(pearlArray);
			playerDirection = calculateDirectionToPoint(nearestPearl);
			
			// Reset Matrix
			nodesMatrix = calculateIntersections(sceneWidth, sceneHeight);
			begin = nodesMatrix[(int) (info.getX() / CELL_SIZE)][(int) (info.getY() / CELL_SIZE)];
			end = nodesMatrix[(nearestPearl.x / CELL_SIZE)][(nearestPearl.y / CELL_SIZE)];
			begin.setDistance(0);
			visitNext.add(begin);
			end.setVisited(false);
			//nearestPearl = findNearestPearl(pearlArray); // closest always
//			playerDirection = calculateDirectionToPoint(nearestPearl);
			pathToFollow = calculateDijkstraPath();
			nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
			nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
			pathToFollow.remove(0);
			playerDirection = calculateDirectionToPoint(nextAim);
		}
		
		
		// Finds the closest pearl
//		
//		if (detectCollision(playerDirection)) {
//			playerDirection = avoidObstacle(obstacles[obstacleIndex], playerDirection);
//		} else {
//		}
		if (!pathToFollow.isEmpty()) {
			if (nextAim.distanceSq(info.getX(), info.getY()) < CELL_SIZE) {
				nextAim.x = pathToFollow.get(0).getX() * CELL_SIZE + CELL_SIZE/2;
				nextAim.y = pathToFollow.get(0).getY() * CELL_SIZE + CELL_SIZE/2;
				pathToFollow.remove(0);
				playerDirection = calculateDirectionToPoint(nextAim);
			}
		} else {
			playerDirection = calculateDirectionToPoint(nearestPearl);
		}
			
		return new DivingAction(info.getMaxAcceleration(), playerDirection);
	}
	
	/*@Override
	public void drawDebugStuff(Graphics2D gfx) {
		// Draw red oval at aim
		gfx.setColor(Color.yellow);
		gfx.drawOval(nearestPearl.x-4, nearestPearl.y-4, 8, 8);
		
		// Draw green ovals in freespace
		gfx.setColor(Color.green);
		
		double cos = Math.cos(playerDirection);
		double sin = Math.sin(playerDirection);
		
		double cosL = Math.cos(playerDirection+Math.PI/4);
		double sinL = Math.sin(playerDirection+Math.PI/4);
		
		double cosR = Math.cos(playerDirection-Math.PI/4);
		double sinR = Math.sin(playerDirection-Math.PI/4);
		
		Point collisionPoint = new Point();
		collisionPoint.x = (int) (info.getX() + cos * CELL_SIZE);
		collisionPoint.y = (int) (info.getY() - sin * CELL_SIZE);
		
		Point collisionPointL = new Point();
		collisionPointL.x = (int) (info.getX() + cosL * CELL_SIZE);
		collisionPointL.y = (int) (info.getY() - sinL * CELL_SIZE);
		
		Point collisionPointR = new Point();
		collisionPointR.x = (int) (info.getX() + cosR * CELL_SIZE);
		collisionPointR.y = (int) (info.getY() - sinR * CELL_SIZE);
		
		gfx.drawOval(collisionPoint.x-5, collisionPoint.y-5, 10, 10);
		
		gfx.drawOval(collisionPointL.x-5, collisionPointL.y-5, 10, 10);
		gfx.drawOval(collisionPointR.x-5, collisionPointR.y-5, 10, 10);
		
		gfx.setColor(Color.white);
		gfx.drawOval((int) (nextAim.x / CELL_SIZE) * CELL_SIZE+ 20, (int) (nextAim.y / CELL_SIZE) *CELL_SIZE + 20, 10, 10);
		
		for (int i = 0; i < nodesMatrix.length; i++) {
			for (int j = 0; j < nodesMatrix[i].length; j++) {
				// Test overlay
				if (!nodesMatrix[i][j].isVisited()) {
					gfx.setColor(Color.green);
//					gfx.drawOval(i*CELL_SIZE+CELL_SIZE*2/5, j*CELL_SIZE+CELL_SIZE*2/5, CELL_SIZE/5, CELL_SIZE/5);
					gfx.drawOval(i*CELL_SIZE, j*CELL_SIZE, CELL_SIZE, CELL_SIZE);
				} else {
					gfx.setColor(Color.red);
//					gfx.drawOval(i*CELL_SIZE+CELL_SIZE*2/5, j*CELL_SIZE+CELL_SIZE*2/5, CELL_SIZE/5, CELL_SIZE/5);
					gfx.drawRect(i*CELL_SIZE+5, j*CELL_SIZE+5, CELL_SIZE-10, CELL_SIZE-10);

				}
			}
		}
	}*/
	
	
	
	//---------------------Helper Methods-----------------------------
	
	
	
	/**
	 * Casts additional rays to find the better direction
	 * to avoid a detected obstacle. Needs some fixes and is too long!
	 * 
	 * @param obstacle	The object player is bound to collide.
	 * @param playerDirection	Player's current direction
	 * @return		An updated player direction.
	 */
	private float avoidObstacle(Path2D obstacle, float playerDirection) {
		// 50 Degrees in Radians to add or subtract
		double directionRayAngle = Math.PI / 180 * 15;
		
		float rayDirection1 = 0;
		float rayDirection2 = 0;
		
		rayDirection1 = (float) (playerDirection + directionRayAngle);
		rayDirection2 = (float) (playerDirection - directionRayAngle);
		
		int collisionCount1 = 0;
		int collisionCount2 = 0;
		
		for (int i = 1; i < 30; i++) {
			Point helperCollisionPoint1 = calculateCollisionPoint(i, rayDirection1);
			if (obstacle.contains(helperCollisionPoint1)) collisionCount1++;
		}
		
		for (int i = 1; i < 30; i++) {
			Point helperCollisionPoint2 = calculateCollisionPoint(i, rayDirection2);
			if (obstacle.contains(helperCollisionPoint2)) collisionCount2++;
		}
		
		if (collisionCount1 > collisionCount2) {
			return (float) (rayDirection2);
		} else {
			return (float) (rayDirection1);
		}
	}
	
	
	
	/**
	 * Calculculates the coordinates of a predicted collision point
	 * along a certain virtual ray of variable length.
	 * 
	 * @param lookahead		The length of the ray in pixels(?)
	 * @param playerDirection	The player's current direction
	 * @return		a Point object of collision to check against.
	 */
	private Point calculateCollisionPoint(int lookahead, float playerDirection) {
		float ray = info.getVelocity() / info.getMaxVelocity();
		
		ray *= lookahead;

		double cos = Math.cos(playerDirection);
		double sin = Math.sin(playerDirection);
		
		Point collisionPoint = new Point();
		collisionPoint.x = (int) (info.getX() + cos * ray);
		collisionPoint.y = (int) (info.getY() - sin * ray);
		
		return collisionPoint;
	}
	
	
	
	private ArrayList<Node> calculateDijkstraPath() {
//		System.a
		Node visiting = visitNext.get(0);
		visitNext.remove(0);
		int x = visiting.getX();
		int y = visiting.getY();
		int currentDirX = Integer.signum(end.getX() - begin.getX());
		int currentDirY = Integer.signum(end.getY() - begin.getY());
		
		while(!end.isVisited()) {
			nodesMatrix[x][y].setVisited(true);
			outerLoop: for (int i = -1; i <= 1; i++) {
				if (x + i < 0 || x + i >= nodesMatrix.length) {
					continue outerLoop;
				}
				innerLoop: for (int j = -1; j <= 1; j++) {
					if (y + j < 0 || y + j >= nodesMatrix[0].length || (i == 0 && j == 0)) {
						continue innerLoop;
					}
					if (!nodesMatrix[x + i][y + j].isVisited()) {
						// Calculate check priority according to direction and node relative position
						if (currentDirX == Integer.signum(nodesMatrix[x + i][y + j].getX() - visiting.getX())) {
							if (currentDirY == Integer.signum(nodesMatrix[x + i][y + j].getY() - visiting.getY())) {
								// Favor nodes if they are in the same direction both ways
								visitNext.add(0, nodesMatrix[x + i][y + j]);
							} else {
								// partly favor one direction
								visitNext.add(visitNext.size()/4, nodesMatrix[x + i][y + j]);
							}
						} else {
							if (currentDirY == Integer.signum(nodesMatrix[x + i][y + j].getY() - visiting.getY())) {
								// partly favor one direction
								visitNext.add(visitNext.size()/2, nodesMatrix[x + i][y + j]);
							} else {
								// Check opposite directed nodes last, if at all
								visitNext.add(visitNext.size(), nodesMatrix[x + i][y + j]);
							}
						}

						if (nodesMatrix[x + i][y + j].getDistance() > visiting.getDistance()+1) {
							nodesMatrix[x + i][y + j].setDistance(visiting.getDistance()+1);
							nodesMatrix[x + i][y + j].setPrevious(visiting);
						}
					}
				}
			}
			if (!visitNext.isEmpty()) {
				visiting=visitNext.get(0);
				visitNext.remove(0);
				x = visiting.getX();
				y = visiting.getY();
			}
		}
		visitNext.clear();
		ArrayList<Node> temp = new ArrayList<>();
		temp.add(end); // Skip last, same as pearl, obsolete
		
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
	 * Finds the peals closest to the left edge of the screen.
	 * Also sets the index of that pearl for later removal.
	 * 
	 * @param pearls	The array of pearls to look over.
	 * @return			A Point object of the leftmost pearl.
	 */
	private Point findLeftmostPearl(Point[] pearls) {
		
		Point closest = null;
		int minimumDistance = Integer.MAX_VALUE;
		int index = 0;
		
		// Check all pearls
		for (Point pearl : pearls) {
			
			// A closer pearl is found, update all
			if (pearl.x < minimumDistance) {
				minimumDistance = pearl.x;
				closest = pearl;
				nearestPearlIndex = index;
			}
			++index;
		}
		
		// Updates index for removal and returns the closest Point
		return closest;
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
	 * Detects if player will collide with an obstacle.
	 * @param playerDirection	The radians directions of the player.
	 * @return		A Bollean value, if a collision is detected.
	 */
	private boolean detectCollision(float playerDirection) {
		
		Point mainCollisionPoint = calculateCollisionPoint(10, playerDirection);
		
		for (Path2D obstacle : obstacleArray) {
			if (obstacle.contains(mainCollisionPoint)) {
				return true;
			}
		}
		
		return false;
	}

}
