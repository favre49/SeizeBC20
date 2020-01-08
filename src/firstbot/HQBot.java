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
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    static Transaction transaction[];
    static int numberOfMinerCreated = 0;

    static RobotController rc;

    public static void run(RobotController rc) throws GameActionException
    {
     	//strategy 
     	//1. build miners .. 
     	//2. miners build refineries and communicate for more miners
     	//3. wall ourselves in.
		HQBot.rc = rc;     	
     	//get messages
     	transaction = rc.getBlock(rc.getRoundNum());
     	if(numberOfMinerCreated < 3){
     		if(buildUnit()){
     			numberOfMinerCreated++;
     		}
     	}
     	if 


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