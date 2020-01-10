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

	public static ObjectLocation[] objectArray = new ObjectLocation[12];
    public static int objectArraySize = 0;

	public static MapLocation soupLocation;
	public static MapLocation toBeRefineryLocation;
	public static MapLocation refineryLocation;

    public static void run(RobotController rc) throws GameActionException
    {
		/*The following code makes the HQ broadcast it's own location*/
		System.out.println("HQ:");
		System.out.println(roundNum);
		System.out.println(objectArraySize);
		for(int i=0;i<objectArraySize;i++){
			System.out.println("i:");
			System.out.println(i);
			// System.out.println(objectArray[i].rt);
			// System.out.println(objectArray[i].loc.x);
			// System.out.println(objectArray[i].loc.y);

			System.out.println("Soup location is:" + soupLocation);
			System.out.println("Refinery location is:" + refineryLocation);
			System.out.println("To be refinery location is:" + toBeRefineryLocation);
		}

		if(roundNum==1){
			int initialArr[] = new int[12];
			initialArr[0] = Communications.getCommsNum(Globals.myObjectType,Globals.currentPos);
			System.out.print(Communications.sendComs(initialArr,0));
   		}
   		else if (roundNum>1){
   			//first, read last message pool and update the ObjectArray
			int commsArr[][]=Communications.getComms(roundNum-1);

			// Set this up to be a switch case?
			for(int i=0;i<commsArr.length;i++)
			{
				innerloop:
				for(int j=0;j<commsArr[i].length;j++)
				{
	                ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);
					boolean exists = false;

					switch(currLocation.rt)
					{
						case COW:
						break innerloop;

						case REFINERY:
						if (refineryLocation != null && currLocation.loc.distanceSquaredTo(toBeRefineryLocation) <= 5)
						{
							refineryLocation = currLocation.loc;
							toBeRefineryLocation = null;
						}
						else if (refineryLocation == null)
						{
							refineryLocation = currLocation.loc;
							toBeRefineryLocation = null;
						}
						break;

						case TO_BE_REFINERY:
						if (refineryLocation != null && currLocation.loc.distanceSquaredTo(refineryLocation) <= 5)
						{
							toBeRefineryLocation = null;
						}
						else if (refineryLocation == null)
						{
							toBeRefineryLocation = currLocation.loc;
						}
						break;

						case SOUP:
	                	if (soupLocation == null)
							soupLocation = currLocation.loc;
						break;

					}
				}
			}

			

			//Now, if we're on our turn, broadcast our entire array
			if(roundNum%broadCastFrequency==0){
				int broadCastArr[] = new int[12];
				// for(int i=0;i<Math.min(objectArraySize,12);i++){
				// 	broadCastArr[i] = Communications.getCommsNum(objectArray[i].rt,objectArray[i].loc);
				// }
				if (soupLocation != null)
					broadCastArr[0] = Communications.getCommsNum(ObjectType.SOUP,soupLocation);
				if (refineryLocation != null)
					broadCastArr[1] = Communications.getCommsNum(ObjectType.REFINERY,refineryLocation);
				if (toBeRefineryLocation != null)
					broadCastArr[2] = Communications.getCommsNum(ObjectType.TO_BE_REFINERY,toBeRefineryLocation);
				System.out.print(Communications.sendComs(broadCastArr,0));
			}

			if(minerCount<5){
				buildMiner();
			}

		}
    }

    static Boolean buildMiner() throws GameActionException
    {
    	for(int i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.MINER, directions[i])){
    			rc.buildRobot(RobotType.MINER, directions[i]);
    			minerCount++;
    			return true;
    		}
    	}
    	//communicate that HQ is boxed in; 
    	return false;
    }
}