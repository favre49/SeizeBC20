package neobot;
import battlecode.common.*;

/**
 * BUilt by: Miners
 * Cost: 250
 * Health: 15
 * Sensor Radius: 24
 *
 * Destroys drones in a 15 attack radius!
 */
public strictfp class NetGunBot extends Globals
{
	public static void run(RobotController rc) throws GameActionException
	{
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(currentPos, 15, opponent);
		int minDist = Integer.MAX_VALUE;
		int minID = -1;
		for (int i = 0; i < nearbyRobots.length; i++)
		{
			if (nearbyRobots[i].type == RobotType.DELIVERY_DRONE && nearbyRobots[i].location.distanceSquaredTo(currentPos) < minDist)
			{
				minDist = nearbyRobots[i].location.distanceSquaredTo(currentPos);
				minID = nearbyRobots[i].ID;
			}
		}
		if (minID != -1)
		{
			rc.shootUnit(minID);
		}
	}
}