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

	public static int minerCount = 0;

    public static void run(RobotController rc) throws GameActionException
    {
		/*The following code makes the HQ broadcast it's own location*/
		System.out.println("HQ:");
		System.out.println(roundNum);
		System.out.println(objectArraySize);
		for(int i=0;i<objectArraySize;i++){
			System.out.println("i:");
			System.out.println(i);
			System.out.println(objectArray[i].rt);
			System.out.println(objectArray[i].loc.x);
			System.out.println(objectArray[i].loc.y);
		}

		if(roundNum==1){
			int initialArr[] = new int[12];
			initialArr[0] = Communications.getCommsNum(Globals.myObjectType,Globals.currentPos);
			System.out.print(Communications.sendComs(initialArr,0));
   		}
   		else if (roundNum>1){
   			//first, read last message pool and update the ObjectArray
			int commsArr[][]=Communications.getComms(roundNum-1);
			for(int i=0;i<commsArr.length;i++){
				for(int j=0;j<commsArr[i].length;j++){
	                ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);

	                if(currLocation.rt == ObjectType.REFINERY || currLocation.rt == ObjectType.SOUP){
	                	boolean alreadyIn = false;
	                	for(int k=0;k<objectArraySize;k++){
	                		if(currLocation.equals(objectArray[k])){
	                			alreadyIn=true;
	                			break;
	                		}
	                	}

	                	if(!alreadyIn){
	                		objectArray[(objectArraySize++)%12]=currLocation;
	                	}
	                } 
				}
			}

			//Now, if we're on our turn, broadcast our entire array
			if(roundNum%broadCastFrequency==0){
				int broadCastArr[] = new int[12];
				for(int i=0;i<Math.min(objectArraySize,12);i++){
					broadCastArr[i] = Communications.getCommsNum(objectArray[i].rt,objectArray[i].loc);
				}
				System.out.print(Communications.sendComs(broadCastArr,0));
			}

			if(minerCount<5){
				minerCount++;
				buildMiner();
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