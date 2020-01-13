package landscaperrushbot;

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
	public static int landscapersBuilt = 0;
	public static void run(RobotController rc) throws GameActionException
    {	
		if(buildLandscaper())
		{
			landscapersBuilt++;
		}
    }

    static Boolean buildLandscaper() throws GameActionException
    {
    	for(int i = 0; i < 8; i++)
        {
    		if(rc.canBuildRobot(RobotType.LANDSCAPER, directions[i]))
            {
    			rc.buildRobot(RobotType.LANDSCAPER, directions[i]);
    			return true;
    		}
    	}
    	return false;
    }

	
}