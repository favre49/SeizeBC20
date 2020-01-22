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
        }

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

        if (roundNum > 1000 && roundNum - lastRoundActive > 10 && rc.isReady())
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

        if (roundNum > 800 && roundNum - lastRoundActive > 30 && rc.isReady())
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

        if (roundNum - lastRoundActive > 40 && rc.isReady())
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
    }

    static void buildLandscaper(Direction dir) throws GameActionException
    {
		numLandscapers++;
        lastRoundActive = roundNum;
    	rc.buildRobot(RobotType.LANDSCAPER, dir);
    }


    private static MapLocation findWaterAroundBase() throws GameActionException
	{
		// Search over every viable location.
		MapLocation searchPos = baseLoc.translate(0,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(2,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		
		searchPos = baseLoc.translate(2,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		
		searchPos = baseLoc.translate(2,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		
		searchPos = baseLoc.translate(2,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		
		searchPos = baseLoc.translate(-1,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		
		searchPos = baseLoc.translate(-1,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(0,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		return null;

	}

	
}