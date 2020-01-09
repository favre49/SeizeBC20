package firstbot;
import battlecode.common.*;

/**
 * Health: 50
 * Sensor Radius: 48
 * Produces: Miners
 * Has a built in gun and refinery.
 */
public strictfp class HQBot extends Globals
{	

    public static void run(RobotController rc) throws GameActionException
    {
		/*The following code makes the HQ broadcast it's own location*/
		if(Globals.roundNum==1){
			int initialArr[] = new int[12];
			initialArr[0] = getCommsNum(Globals.myType,Globals.currentPos);
			sendComs(arr,0);

			/*To read this, the code is as follows:
				int newarr[][]=getComms(2);
				for(int i=0;i<newarr.length;i++){
					//this loop iterates over all messages of round 2 (since we don't know which one is ours)
					//if it's an enemy message, the output will be COW at (0,0)
					getLocationFromInt(newarr[i][0]).rt //RobotType HQ
					getLocationFromInt(newarr[i][0]).loc.x //integer HQ x position
					getLocationFromInt(newarr[i][0]).loc.y //integer HQ y position
				}
			*/

   		}
    }

    static Boolean buildMiner() throws GameActionException
    {
    	for(int i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.MINER, directions[i])){
    			rc.buildRobot(RobotType.MINER, directions[i]);
    			return true;
    		}
    	}
    	//communicate that HQ is boxed in; 
    	return false;
    }


}