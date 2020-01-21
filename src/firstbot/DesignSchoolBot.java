package firstbot;
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
    public static void run(RobotController rc) throws GameActionException
    {   
        RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, -1, opponent);
        for (int i = 0; i < nearbyOpps.length; i++)
        {
            if (nearbyOpps[i].type == RobotType.HQ)
            {
                attacker = true;
            }
        }

        if(landscapersbuilt<8)
            buildLandscaper();
    }


    static Boolean buildLandscaper() throws GameActionException
    {
    	for(int i = 0; i < 8; i++)
        {
    		if(rc.canBuildRobot(RobotType.LANDSCAPER, directions[i]))
            {
    			rc.buildRobot(RobotType.LANDSCAPER, directions[i]);
                landscapersbuilt++;
    			return true;
    		}
        }
        System.out.println("BOOOHOOO COULDNT NSAF:DLUIJHA:Float");
    	//communicate that HQ is boxed in; 
    	return false;
    }
}