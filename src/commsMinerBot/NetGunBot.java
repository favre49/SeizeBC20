package commsMinerBot;
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
	// public static void run(RobotController rc) throws GameActionException
	public static void run(RobotController rc) throws GameActionException
	{
		RobotInfo nearbyUnits[] = rc.senseNearbyRobots(currentPos, 15, opponent);
		int minDist = Integer.MAX_VALUE;
		int minID = -1;
		for (int i = 0; i < nearbyUnits.length; i++)
		{
			if (nearbyUnits[i].type == RobotType.DELIVERY_DRONE)
			{
				if (currentPos.distanceSquaredTo(nearbyUnits[i].location) < minDist)
				{
					minDist = currentPos.distanceSquaredTo(nearbyUnits[i].location);
					minID = nearbyUnits[i].ID;
				}
			}
		}
		if (rc.canShootUnit(minID))
		{
			rc.shootUnit(minID);
		}
	}

}