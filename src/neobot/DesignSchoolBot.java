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

        if (roundNum - lastRoundActive > 40 && rc.isReady() && numLandscapers < 15)
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

	
}