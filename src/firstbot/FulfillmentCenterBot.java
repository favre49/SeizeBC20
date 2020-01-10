package firstbot;
import battlecode.common.*;

/**
 * BUilt by: Miners
 * Cost: 150
 * Health: 15
 * Sensor radius: 24
 * Produces: Drones
 */
public strictfp class FulfillmentCenterBot extends Globals
{
	public static int dronesBuilt = 0;
	public static void run(RobotController rc) throws GameActionException
	{
		while(dronesBuilt < 4)
		{
			if(buildDrone())
				dronesBuilt++;
		}
	}

	static Boolean buildDrone() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(rc.canBuildRobot(RobotType.DELIVERY_DRONE, directions[i]))
			{
				rc.buildRobot(RobotType.DELIVERY_DRONE, directions[i]);
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
	}
}