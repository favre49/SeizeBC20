package neobot;
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
	public static int lastRoundActive = 0;

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

		RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
		RobotInfo[] nearbyTeam = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, team);

		int oppno = 0;
		if(nearbyBots.length != 0)
		{
			for (int i = 0; i < nearbyBots.length; i++)
			{
				if (!nearbyBots[i].type.isBuilding())
					oppno++;
			}
		}

		int drno = 0;
		for (int i = 0; i < nearbyTeam.length; i++)
		{
			if (nearbyTeam[i].type == RobotType.DELIVERY_DRONE)
				drno++;
		}

		if (drno < oppno)
			buildDrone();
		
		if (roundNum > 1200)
		{
			if (roundNum - lastRoundActive > 30)
				buildDrone();
		}

		if (roundNum > 300)
		{
			if (roundNum - lastRoundActive > 30)
				buildDrone();
		}

	}

	static Boolean buildDrone() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(directions[i] != Direction.CENTER && rc.canBuildRobot(RobotType.DELIVERY_DRONE, directions[i]))
			{
				dronesBuilt++;
				lastRoundActive = roundNum;
				rc.buildRobot(RobotType.DELIVERY_DRONE, directions[i]);
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
	}
}