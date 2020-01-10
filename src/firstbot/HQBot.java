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
	public static boolean builtFulfilmentCenter = false; 

<<<<<<< HEAD
    public static void run(RobotController rc) throws GameActionException
    {
		System.out.println("THe tbrf is " + toBeRefineryLocation);
		if(roundNum==1){
=======
	public static void run(RobotController rc) throws GameActionException
	{
		if(roundNum == 1)
		{
>>>>>>> aeccac758035be480a78e83d02ef090ed7b85ae9
			soupLocation = senseNearbySoup();
			int initialArr[] = new int[9];
			initialArr[0] = Communications.getCommsNum(ObjectType.HQ,currentPos);
			if(soupLocation != null)
				initialArr[1] = Communications.getCommsNum(ObjectType.SOUP, soupLocation);
			System.out.print(Communications.sendComs(initialArr,0));
		}
		else if(roundNum > 1)
		{
			//first, read last message pool and update the ObjectArray
			int commsArr[][]=Communications.getComms(roundNum-1);

			// Set this up to be a switch case?
			for(int i = 0; i < commsArr.length; i++)
			{
				innerloop:
				for(int j = 0; j < commsArr[i].length; j++)
				{
					ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);
					System.out.println(currLocation.rt + " " + currLocation.loc);

					switch(currLocation.rt)
					{
						case COW:
						break;

						case REFINERY:
						if(refineryLocation == null)
						{
							refineryLocation = currLocation.loc;
							toBeRefineryLocation = null;
						}
						break;

						case TO_BE_REFINERY:
						if(refineryLocation != null 
							&& currLocation.loc.distanceSquaredTo(refineryLocation) <= 5)
						{
							toBeRefineryLocation = null;
						}
						else if(refineryLocation == null)
						{
							toBeRefineryLocation = currLocation.loc;
						}
						break;

						case SOUP:
						if(soupLocation == null)
							soupLocation = currLocation.loc;
						break;

						case HQ:
						if(currLocation.loc != currentPos)
							opponentHQLoc = currLocation.loc;
						break;

						case FULFILLMENT_CENTER:
						builtFulfilmentCenter = true;
						break;

						case NO_SOUP:
						soupLocation = null;
						refineryLocation = null;
						break;

					}
				}
			}

			//Now, if we're on our turn, broadcast our entire array
			if(roundNum%broadCastFrequency == 0)
			{
				int broadCastArr[] = new int[9];
				int numBroadCasts = 0;
				// for(int i=0;i<Math.min(objectArraySize,12);i++){
				// 	broadCastArr[i] = Communications.getCommsNum(objectArray[i].rt,objectArray[i].loc);
				// }
				if(soupLocation != null)
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.SOUP, soupLocation);
				else
<<<<<<< HEAD
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.NO_SOUP,new MapLocation(0,0));
				if (refineryLocation != null)
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.REFINERY,refineryLocation);
				if (toBeRefineryLocation != null)
				{
					System.out.println("I should be broadcasting rn");
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.TO_BE_REFINERY,toBeRefineryLocation);
				}
				if (opponentHQLoc != null)
=======
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.NO_SOUP, new MapLocation(0,0));
				
				if(refineryLocation != null)
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.REFINERY, refineryLocation);
				
				if(toBeRefineryLocation != null)
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.TO_BE_REFINERY, toBeRefineryLocation);
				
				if(opponentHQLoc != null)
>>>>>>> aeccac758035be480a78e83d02ef090ed7b85ae9
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.HQ, opponentHQLoc);
				
				if(builtFulfilmentCenter)
					broadCastArr[numBroadCasts++] = Communications.getCommsNum(ObjectType.FULFILLMENT_CENTER, new MapLocation(0,0));

				System.out.print(Communications.sendComs(broadCastArr,0));
			}

			int nearbyDroneID = senseDrones();
			if(nearbyDroneID != -1)
				rc.shootUnit(nearbyDroneID);

			if(minerCount < 3)
			{
				buildMiner();
			}

			if(roundNum >= 150 && roundNum <= 160)
			{
				if(refineryLocation == null 
					&& soupLocation != null 
					&& toBeRefineryLocation == null)
				{
					Direction dirToCenter = currentPos.directionTo(new MapLocation(mapWidth/2,mapHeight/2));
					toBeRefineryLocation = currentPos.translate(dirToCenter.dx*4, dirToCenter.dy*4);
					while(!inBounds(toBeRefineryLocation) 
						&& rc.senseFlooding(toBeRefineryLocation))
					{
						dirToCenter = dirToCenter.rotateLeft();
						toBeRefineryLocation = currentPos.translate(dirToCenter.dx*4, dirToCenter.dy*4);
					}
					System.out.println(toBeRefineryLocation);
				}
<<<<<<< HEAD
			}
			if (roundNum >= 150)
			{
				if (minerCount != 4)
=======

				if(minerCount != 4)
>>>>>>> aeccac758035be480a78e83d02ef090ed7b85ae9
					buildMiner();
			}

		}
	}

	static Boolean buildMiner() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(rc.canBuildRobot(RobotType.MINER, directions[i]))
			{
				rc.buildRobot(RobotType.MINER, directions[i]);
				minerCount++;
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
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