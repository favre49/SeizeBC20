package landscaperrushbot;
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
		RobotInfo nearbyUnits[] = rc.senseNearbyRobots(currentPos, 15, opponent);
		for (int i = 0; i < nearbyUnits.length; i++)
		{
			if (nearbyUnits[i].type == RobotType.DELIVERY_DRONE)
			{
				rc.shootUnit(nearbyUnits[i].ID);
			}
		}
	}
}