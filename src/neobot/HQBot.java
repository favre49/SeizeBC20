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

		if (roundNum > 400 && minerCount < 25)
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