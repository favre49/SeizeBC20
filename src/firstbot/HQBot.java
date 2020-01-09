package firstbot;
import battlecode.common.*;

/**
 * Health: 50
 * Sensor Radius: 48
 * Produces: Miners
 * Has a built in gun and refinery.
 */
public strictfp class HQBot
{	

    static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    
    static int numberOfMinerCreated = 0;

    public static void run(RobotController rc) throws GameActionException
    {
     	//strategy 
     	//1. build miners .. 
     	//2. miners build refineries and communicate for more miners
     	//3. wall ourselves in.
		Globals.rc = rc;     	
     	//get messages
     	transaction = rc.getBlock(rc.getRoundNum());
     	if(numberOfMinerCreated < 3){
     		if(buildUnit()){
     			numberOfMinerCreated++;
     		}
     	}

     	//if not mining any resources , build more miners

     	if(rc.getRoundNum() > 100 && rc.getRoundNum()/70 >= numberOfMinerCreated-3){
     		if(buildMiner()){
     			numberOfMinerCreated++;
     		}
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