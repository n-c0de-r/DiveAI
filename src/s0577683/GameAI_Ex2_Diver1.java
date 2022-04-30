package s0577683;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class GameAI_Ex2_Diver1 extends AI {
	private Point[] pearls;
	private Point nearestPearl;
	private int nearestPearlIndex;
	private float playerDirection;
	private int currentScore = 0;
	private Path2D[] obstacles;
	private int obstacleIndex;
	
	public GameAI_Ex2_Diver1 (Info info) {
		super(info);
		
		enlistForTournament(577683, 577423);
		
		obstacles = info.getScene().getObstacles();
		pearls = info.getScene().getPearl();
		nearestPearl = findNearestPearl(pearls);
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
			pearls[nearestPearlIndex] = null;
			nearestPearl = findNearestPearl(pearls);
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
	
	
	
	//---------------------Helper Methods-----------------------------
	
	
	
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
		for (int i = 0; i<pearls.length; i++) {
			//If it is already collected, ignore it
			if (pearls[i] == null) {
				continue;
			}
			
			double currentPearlDistance =  Math.sqrt(Math.pow(pearls[i].x - info.getX(), 2) + Math.pow(pearls[i].y - info.getY(), 2));
			// A closer pearl is found, update all
			if (currentPearlDistance < minimumDistance) {
				minimumDistance = currentPearlDistance;
				index = i;
			}
		}
		// Updates index for removal and returns the closest Point
		nearestPearlIndex = index;
		return pearls[index];
	}
	
	
	
	/**
	 * Detects if player will collide with an obstacle.
	 * @param playerDirection	The radians directions of the player.
	 * @return		A Bollean value, if a collision is detected.
	 */
	private boolean detectCollision(float playerDirection) {
		
		int index = 0;
		Point mainCollisionPoint = calculateCollisionPoint(10, playerDirection);
		
		for (Path2D obstacle : obstacles) {
			if (obstacle.contains(mainCollisionPoint)) {
				obstacleIndex = index;
				return true;
			}
			index++;
		}
		
		return false;
	}
	
	
	
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

}
