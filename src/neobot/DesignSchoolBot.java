package neobot;

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
    public static int numLandscapers = 0;
    public static int lastRoundActive = 0;
    public static int scaperIdx = 0;

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

            if (baseLoc == null)
            {
                RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, team);
                for (int i = 0; i < nearbyOpps.length; i++)
                {
                    if (nearbyOpps[i].type == RobotType.HQ)
                    {
                        baseLoc = nearbyOpps[i].location;
                        break;
                    }
                }
            }
        }

		// Defend!!!
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
        int vapeno =0;
		for (int i = 0; i < nearbyTeam.length; i++)
		{
			if (nearbyTeam[i].type == RobotType.LANDSCAPER)
				drno++;
            if(nearbyTeam[i].type == RobotType.VAPORATOR)
                vapeno++;
		}

		if (drno < oppno)
			buildLandscaper();

		// Don't let us get flooded.
        if (findWaterAroundBase() != null)
        {
            for (int i = 0; i < 8; i++)
            {
                int idx = (scaperIdx+i)%8;
                if (rc.canBuildRobot(RobotType.LANDSCAPER, directions[idx]))
                {
                    scaperIdx = (idx+1)%8;
                    buildLandscaper(directions[idx]);
                }
            }
        }
        
        if (roundNum > CRUNCH_PREP_ROUND)
        {
            RobotInfo[] nearbyScapers = rc.senseNearbyRobots(baseLoc, 8, team);
            int scapeno = 0;

            for (int i = 0; i < nearbyScapers.length; i++)
            {
                if (nearbyScapers[i].type == RobotType.LANDSCAPER)
                {
                    scapeno++;
                }
            }

            if(scapeno == 0)
            {
                for (int i = 0; i < 8; i++)
                {
                     int idx = (scaperIdx+i)%8;
                     if (rc.canBuildRobot(RobotType.LANDSCAPER, directions[idx]))
                     {
                        scaperIdx = (idx+1)%8;
                        buildLandscaper(directions[idx]);
                     }
                }
            }
            else
                return;
        }

        if (roundNum > MIN_EXP_ROUND && roundNum < MAX_EXP_ROUND)
        {
            if (roundNum - lastRoundActive > 75)
            {
                System.out.println(scaperIdx);
                for (int i = 0; i < 8; i++)
                {
                     int idx = (scaperIdx+i)%8;
                     if (rc.canBuildRobot(RobotType.LANDSCAPER, directions[idx]))
                     {
                        scaperIdx = (idx+1)%8;
                        buildLandscaper(directions[idx]);
                     }
                }
            }
            else
                return;
        }
		// Every 20 turns make a new scaper.
        if (roundNum - lastRoundActive > LANDSCAPERFRQ && rc.isReady() && (vapeno >= 2 || roundNum >= LANDSCAPERMIN)){
			System.out.println(scaperIdx);
            for (int i = 0; i < 8; i++)
            {
                 int idx = (scaperIdx+i)%8;
                 if (rc.canBuildRobot(RobotType.LANDSCAPER, directions[idx]))
                 {
                    scaperIdx = (idx+1)%8;
                    buildLandscaper(directions[idx]);
                 }
            }
        }
    }

    static void buildLandscaper(Direction dir) throws GameActionException
    {
		numLandscapers++;
        lastRoundActive = roundNum;
    	rc.buildRobot(RobotType.LANDSCAPER, dir);
    }

	static Boolean buildLandscaper() throws GameActionException
    {
    	for(int i = 0; i < 8; i++)
        {
    		if(rc.canBuildRobot(RobotType.LANDSCAPER, directions[i]))
            {
    			rc.buildRobot(RobotType.LANDSCAPER, directions[i]);
                lastRoundActive = roundNum;
    			return true;
    		}
        }
        System.out.println("BOOOHOOO COULDNT NSAF:DLUIJHA:Float");
    	//communicate that HQ is boxed in; 
    	return false;
    }
	
}