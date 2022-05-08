package s0577683;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.Iterator;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class GameAI_Ex2_Diver1 extends AI {
	private final int CELL_SIZE = 10;
	
	//Complex Types
	private Path2D[] obstacleArray;
	private Point nearestPearl;
	private Point[] pearlArray;
	
	//Primitive Types
	private boolean[][] freespaceMatrix;
	private int currentScore;
	private int nearestPearlIndex;
	private int obstacleIndex;
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
		freespaceMatrix = calculateIntersections(sceneWidth, sceneHeight);
		nearestPearl = findNearestPearl(pearlArray);
		playerDirection = calculateDirectionToPoint(nearestPearl);
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
		}
		
		// Finds the closest pearl
//		
//		if (detectCollision(playerDirection)) {
//			playerDirection = avoidObstacle(obstacles[obstacleIndex], playerDirection);
//		} else {
			playerDirection = calculateDirectionToPoint(nearestPearl);
//		}
	
		
		return new DivingAction(info.getMaxAcceleration(), playerDirection);
	}
	
	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		// Draw red oval at aim
		gfx.setColor(Color.red);
		gfx.drawOval(nearestPearl.x-4, nearestPearl.y-4, 8, 8);
		
		// Draw green ovals in freespace
		/*gfx.setColor(Color.green);
		for (int i = 0; i < freespaceMatrix.length; i++) {
			for (int j = 0; j < freespaceMatrix[i].length; j++) {
				if (freespaceMatrix[i][j]) {
					gfx.drawOval(i*10+3, j*10+3, 4, 4);
				}
				
			}
			
		}*/
	}
	
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
	 * @param height	Height of part to check
	 * @return			A boolean adjacency matrix
	 */
	private boolean[][] calculateIntersections(int width, int height) {
		boolean[][] tempArray  = new boolean[width/CELL_SIZE][height/CELL_SIZE];
		Rectangle rect = new Rectangle();
		
		for (int x = 0; x < width/CELL_SIZE; x++) {
			innerLoop: for (int y = 0; y < height/CELL_SIZE; y++) {
				rect.setBounds(x*CELL_SIZE, y*CELL_SIZE, CELL_SIZE, CELL_SIZE);
				
				// Check intersections of Rectangles with sand banks
				for(Path2D obstacle : obstacleArray) {
					if(obstacle.intersects(rect)) {
						
						/* If ANY intersection is found, skip this position
						   as it is not passable keep it false */
						continue innerLoop;
					}
				}
				
				// Only if it doesn't intersect any, path is free
				tempArray[x][y] = true;
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
				nearestPearlIndex = index;
			}
			++index;
		}
		
		// Updates index for removal and returns the closest Point
		return pearls[nearestPearlIndex];
	}
	
	
	
	/**
	 * Detects if player will collide with an obstacle.
	 * @param playerDirection	The radians directions of the player.
	 * @return		A Bollean value, if a collision is detected.
	 */
	private boolean detectCollision(float playerDirection) {
		
		int index = 0;
		Point mainCollisionPoint = calculateCollisionPoint(10, playerDirection);
		
		for (Path2D obstacle : obstacleArray) {
			if (obstacle.contains(mainCollisionPoint)) {
				obstacleIndex = index;
				return true;
			}
			index++;
		}
		
		return false;
	}

}
