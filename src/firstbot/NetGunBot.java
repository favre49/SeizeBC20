package firstbot;
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
		RobotInfo nearbyUnits[] = rc.senseNearbyRobots();
		if(nearbyUnits != null)
		{
			//shoot nearest drone carrying something or else shoot the nearest bot
			RobotInfo nearbyEnemyUnits[] = rc.senseNearbyRobots(-1, team.opponent());

			if(nearbyUnits != null)
			{
				int nearestDroneID = nearbyEnemyUnits[0].getID();
				int nearestDroneDist = 64*64;
				for(int i = 0; i < nearbyEnemyUnits.length; i++){
					int distance = (nearbyEnemyUnits[i].getLocation()).distanceSquaredTo(rc.getLocation());
					if(nearbyEnemyUnits[i].getType() == RobotType.DELIVERY_DRONE 
						&& distance < nearestDroneDist)
					{
						nearestDroneID = nearbyEnemyUnits[i].getID();
						nearestDroneDist = distance;
					}
				}
				
				if(rc.canShootUnit(nearestDroneID))
					rc.shootUnit(nearestDroneID);
			}

			//communicate cows?
		}
	}
}