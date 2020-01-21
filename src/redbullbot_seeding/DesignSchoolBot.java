package redbullbot_seeding;
import battlecode.common.*;

/**
 * Built by: Miners
 * Cost: 150
 * Health: 15
 * Sensor radius: 24
 * Produces: Landscapers
 */
public strictfp class DesignSchoolBot extends Globals
{	
    public static int landscapersbuilt = 0;
    public static boolean attacker = false;
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

        RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, -1, opponent);
        for (int i = 0; i < nearbyOpps.length; i++)
        {
            if (nearbyOpps[i].type == RobotType.HQ)
            {
                opponentHQLoc = nearbyOpps[i].location;
                attacker = true;
            }
        }

        if (attacker)
        {
            RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
    		RobotInfo[] nearbyTeam = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, team);

            int oppno = 0;
            if(nearbyBots.length != 0)
            {
                for (int i = 0; i < nearbyBots.length; i++)
                {
                    if (nearbyBots[i].type.isBuilding() || nearbyBots[i].type == RobotType.LANDSCAPER)
                        oppno++;
                }
            }

            int drno = 0;
            for (int i = 0; i < nearbyTeam.length; i++)
            {
                if (nearbyTeam[i].type == RobotType.LANDSCAPER)
                    drno++;
            }

            if (drno < oppno || drno < 2)
                buildLandscaper();
        }
        else
        {
    		RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
    		RobotInfo[] nearbyTeam = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, team);

            int oppno = 0;
            if(nearbyBots.length != 0)
            {
                for (int i = 0; i < nearbyBots.length; i++)
                {
                    if (nearbyBots[i].type.isBuilding() || nearbyBots[i].type == RobotType.LANDSCAPER)
                        oppno++;
                }
            }

            int drno = 0;
            for (int i = 0; i < nearbyTeam.length; i++)
            {
                if (nearbyTeam[i].type == RobotType.LANDSCAPER)
                    drno++;
            }

            if (drno < oppno)
                buildLandscaper();

            int scapeNo = 0;
            drno = 0;
            RobotInfo[] nearbyScapers = rc.senseNearbyRobots(baseLoc, 2, team);
            for (int i = 0; i < nearbyScapers.length; i++)
            {
                if (nearbyScapers[i].type == RobotType.LANDSCAPER)
                    scapeNo++;
                else if (nearbyScapers[i].type == RobotType.DELIVERY_DRONE)
                    drno++;
            }

            if (scapeNo == 0 && roundNum - lastRoundActive > 20)
            {
                buildLandscaper();
            }
        }
    }


    static Boolean buildLandscaper() throws GameActionException
    {
    	for(int i = 0; i < 8; i++)
        {
    		if(rc.canBuildRobot(RobotType.LANDSCAPER, directions[i]))
            {
    			rc.buildRobot(RobotType.LANDSCAPER, directions[i]);
                lastRoundActive = roundNum;
                landscapersbuilt++;
    			return true;
    		}
        }
        System.out.println("BOOOHOOO COULDNT NSAF:DLUIJHA:Float");
    	//communicate that HQ is boxed in; 
    	return false;
    }
}