package landscaperrushbot;
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
		if (baseLoc == null)
		{
			int[][] commsarr=Communications.getComms(1);
            outerloop:
            for(int i=0;i<commsarr.length;i++){
                for (int j = 0; j < commsarr[i].length; j++)
                {
                    ObjectLocation objectHQLocation = Communications.getLocationFromInt(commsarr[i][j]); 
                    System.out.println(objectHQLocation.rt);
                    if(objectHQLocation.rt==ObjectType.HQ)
                    {
                        System.out.println("I should be understanding shit rn" + objectHQLocation.loc);
                        baseLoc = new MapLocation(objectHQLocation.loc.x,objectHQLocation.loc.y);
                        break outerloop;
                    }
                }
            }
		}

		if (currentPos.distanceSquaredTo(baseLoc) <= 2) // Protecting.
		{
			if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, Direction.SOUTH))
			{
				rc.buildRobot(RobotType.DELIVERY_DRONE, Direction.SOUTH);
			}
		}
		else // Helper.
		{
			if (dronesBuilt != 1)
			{
				System.out.println("I should be building drones");
				buildDrone();
			}
		}

	}

	static Boolean buildDrone() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(directions[i] != Direction.CENTER && rc.canBuildRobot(RobotType.DELIVERY_DRONE, directions[i]))
			{
				dronesBuilt++;
				System.out.println("I should have built a drone");
				rc.buildRobot(RobotType.DELIVERY_DRONE, directions[i]);
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
	}
}