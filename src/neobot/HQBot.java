package neobot;
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
	public static boolean madeBuilder = false;
	public static MapLocation soupLocation;
	public static MapLocation toBeRefineryLocation;
	public static MapLocation refineryLocation;
	public static boolean builtFulfilmentCenter = false; 
	public static int lastRoundActive = 0;

	private static MapLocation[] soupQueue = new MapLocation[9];
	private static int soupLocationPointer=0;

	private static MapLocation[] refineryList = new MapLocation[9];
	private static int refineryLocationPointer=0;

	private static int lastTurnTo9Soup=0;
	private static int lastTurnTo9Ref=0;

	private static int VALIDSOUPCUTOFF = 30;
	private static int NINELIMIT = 50;


    public static void run(RobotController rc) throws GameActionException
    {
		System.out.println("THe tbrf is " + toBeRefineryLocation);
		System.out.println("THe sl is " + soupLocation);
		System.out.println("THe rf is " + refineryLocation);

		if(roundNum == 1){
			System.out.println("I entered where I am supposed to");
			// soupLocation = senseNearbySoup();
			int initialArr[] = new int[9];
			initialArr[0] = Communications.getCommsNum(ObjectType.HQ,currentPos);
			// if(soupLocation != null)
			// 	initialArr[1] = Communications.getCommsNum(ObjectType.SOUP, soupLocation);
			System.out.print(Communications.sendComs(initialArr,1));
		}
		else if (roundNum > 2)
		{
			int commsArr[][]=Communications.getComms(roundNum-1);

			if(soupLocationPointer==9 && roundNum-lastTurnTo9Soup>NINELIMIT){
				soupLocationPointer=0;
			}

			if(refineryLocationPointer==9 && roundNum-lastTurnTo9Ref>NINELIMIT){
				refineryLocationPointer=0;
			}


			// Set this up to be a switch case?
			for(int i = 0; i < commsArr.length; i++)
			{
				ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][0]);
				switch(currLocation.rt)
				{
					case COW: 
						continue;
					case HQ: 
						opponentHQLoc = currLocation.loc;
						break;
					case SOUP:
						//check
						System.out.println("SOUPSOUP");
						System.out.println(currLocation.loc.x + " " + currLocation.loc.y);

						boolean works=true;
						for(int j=0;j<soupLocationPointer;j++){
							if(currLocation.loc.distanceSquaredTo(soupQueue[j])<=VALIDSOUPCUTOFF){
								works=false;
								break;
							}
						}

						if(works && soupLocationPointer<9){
							soupQueue[soupLocationPointer++]=currLocation.loc;
							if(soupLocationPointer==9){
								lastTurnTo9Soup=roundNum;
							}
						}
						break;
					case REFINERY:
						boolean works2=true;
						for(int j=0;j<refineryLocationPointer;j++){
							if(currLocation.loc.equals(refineryList[j])){
								works2=false;
								break;
							}
						}


						if(works2 && refineryLocationPointer<9){
							if(refineryLocationPointer==9){
								lastTurnTo9Ref=roundNum;
							}
							refineryList[refineryLocationPointer++]=currLocation.loc;
						}
						break;


					case NO_SOUP:
						System.out.println("NOSOUPNOSOUP");
						System.out.println(currLocation.loc.x + " " + currLocation.loc.y);

						int j=0;
						while(true){
							j=0;
							while(j<soupLocationPointer && (currLocation.loc.distanceSquaredTo(soupQueue[j]) > VALIDSOUPCUTOFF)){
								j++;
							}
							if(j!=soupLocationPointer){
								soupQueue[j]=soupQueue[soupLocationPointer-1];
								soupLocationPointer--;
							}
							else{
								break;
							}
						}
						break;
				}			
			}
		}

		if (roundNum%broadCastFrequency == 0)
		{
			//reorder soupQueue
			int closestSoupInd=-1;
			int mindist=Integer.MAX_VALUE;
			for(int i=0;i<soupLocationPointer;i++){
				if(currentPos.distanceSquaredTo(soupQueue[i])<mindist){
					mindist=currentPos.distanceSquaredTo(soupQueue[i]);
					closestSoupInd=i;
				}
			}

			if(closestSoupInd>-1){
				MapLocation temp = soupQueue[0];
				soupQueue[0]=soupQueue[closestSoupInd];
				soupQueue[closestSoupInd]=temp;
			}

			System.out.println("soupQueueSize"+soupLocationPointer);
			int[] broadCastArr;
			if(soupLocationPointer>0){
				broadCastArr = new int[9];
				for(int i=0;i<Math.min(9,soupLocationPointer);i++){
					System.out.println(soupQueue[i].x+","+soupQueue[i].y);
					broadCastArr[i]=Communications.getCommsNum(ObjectType.SOUP,soupQueue[i]);
				}
				Communications.sendComs(broadCastArr,1);
			}

			System.out.println("refineryListSize"+refineryLocationPointer);
			broadCastArr = new int[9];
			for(int i=0;i<Math.min(9,refineryLocationPointer);i++){
				System.out.println(refineryList[i].x+","+refineryList[i].y);
				broadCastArr[i]=Communications.getCommsNum(ObjectType.REFINERY,refineryList[i]);
			}
			Communications.sendComs(broadCastArr,1);

			broadCastArr = new int[9];
			int idx = 0;
			if (opponentHQLoc != null)
			{
				broadCastArr[idx++] = Communications.getCommsNum(ObjectType.HQ, opponentHQLoc);
			}
			Communications.sendComs(broadCastArr,1);
		}

		soupLocation = senseNearbySoup();

		if (soupLocation != null)
		{
			if (minerCount < 4)
			{
				Direction tryDir = currentPos.directionTo(soupLocation);
				if(rc.canBuildRobot(RobotType.MINER, tryDir))
				{
					rc.buildRobot(RobotType.MINER, tryDir);
					lastRoundActive = roundNum;
					minerCount++;
				}
				else if(rc.canBuildRobot(RobotType.MINER, tryDir.rotateLeft()))
				{
					rc.buildRobot(RobotType.MINER, tryDir.rotateLeft());
					lastRoundActive = roundNum;	
					minerCount++;
				}
				else if(rc.canBuildRobot(RobotType.MINER, tryDir.rotateRight()))
				{
					rc.buildRobot(RobotType.MINER, tryDir.rotateRight());
					lastRoundActive = roundNum;
					minerCount++;
				}
				else
				{
					buildMiner();
				}
			}
		}

		int nearbyDroneID = senseDrones();
		if (nearbyDroneID != -1)
			rc.shootUnit(nearbyDroneID);

		if (minerCount < 4)
			buildMiner();

		if (roundNum > 200 && minerCount < 10)
		{
			if (roundNum-lastRoundActive > 30)
			{
				int maxEleIdx = -1;
				int maxEleHeight = -1;
				for (int i = 0; i < 8; i++)
				{
					if (rc.canBuildRobot(RobotType.MINER, directions[i]) && rc.senseElevation(currentPos.add(directions[i])) > maxEleHeight)
					{
						maxEleHeight = rc.senseElevation(currentPos.add(directions[i]));
						maxEleIdx = i;
					}
				}
				if (maxEleIdx != -1)
				{
					buildMiner(directions[maxEleIdx]);
				}
			}
		}
		
		if (roundNum > 500 && minerCount < 11)
		{
			buildMiner();
		}
	}

	static boolean buildMiner() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(rc.canBuildRobot(RobotType.MINER, directions[i]))
			{
				rc.buildRobot(RobotType.MINER, directions[i]);
				lastRoundActive = roundNum;
				minerCount++;
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
	}

	static void buildMiner(Direction dir) throws GameActionException
	{
		minerCount++;
		lastRoundActive = roundNum;
		rc.buildRobot(RobotType.MINER, dir);
	}

	static int senseDrones() throws GameActionException
	{
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		for (int i = 0; i < nearbyRobots.length; i++)
		{
			if(nearbyRobots[i].type == RobotType.DELIVERY_DRONE 
				&& nearbyRobots[i].team == opponent 
				&& nearbyRobots[i].location.distanceSquaredTo(currentPos) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)
				return nearbyRobots[i].ID;
		}
		return -1;
	}
}