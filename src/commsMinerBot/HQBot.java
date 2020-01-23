package commsMinerBot;
import battlecode.common.*;

/**
 * Health: 50
 * Sensor Radius: 48
 * Produces: Miners
 * Has a built in gun and refinery.
 */
public strictfp class HQBot extends Globals
{
	// declarations
	public static int minerCount = 0;

	public static ObjectLocation[] objectArray = new ObjectLocation[12];
	public static int objectArraySize = 0;

	public static MapLocation soupLocation;
	public static MapLocation toBeRefineryLocation;
	public static MapLocation refineryLocation;
	public static boolean builtFulfilmentCenter = false; 
	public static boolean builtDesignSchool = false;
	public static boolean panic = false;

    // public static void run(RobotController rc) throws GameActionException
    public static void run(RobotController rc) throws GameActionException
    {
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
			// Set this up to be a switch case?
			for(int i = 0; i < commsArr.length; i++)
			{
				ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][0]);
				switch(currLocation.rt)
				{
					case COW: continue;

					case HQ: opponentHQLoc = currLocation.loc;
				}
			}
		}

		if (roundNum%broadCastFrequency == 0 && opponentHQLoc != null)
		{
			int broadCastArr[] = new int[9];
			broadCastArr[0] = Communications.getCommsNum(ObjectType.HQ, opponentHQLoc);
			Communications.sendComs(broadCastArr,1);
		}

		if (minerCount == 0)
		{
			if(rc.canBuildRobot(RobotType.MINER, Direction.NORTH))
			{
				rc.buildRobot(RobotType.MINER, Direction.NORTH);
				minerCount++;
			}
		}

		soupLocation = senseNearbySoup();

		if (soupLocation != null)
		{
			if (minerCount < 5)
			{
				Direction tryDir = currentPos.directionTo(soupLocation);
				if(rc.canBuildRobot(RobotType.MINER, tryDir))
				{
					rc.buildRobot(RobotType.MINER, tryDir);
					minerCount++;
				}
				else if(rc.canBuildRobot(RobotType.MINER, tryDir.rotateLeft()))
				{
					rc.buildRobot(RobotType.MINER, tryDir.rotateLeft());
					minerCount++;
				}
				else if(rc.canBuildRobot(RobotType.MINER, tryDir.rotateRight()))
				{
					rc.buildRobot(RobotType.MINER, tryDir.rotateRight());
					minerCount++;
				}
				else
				{
					buildMiner();
				}
			}
		}
		
		if (roundNum > 50 && minerCount != 5)
		{
			buildMiner();
		}

		RobotInfo[] assets = rc.senseNearbyRobots(currentPos, 2, team);
		boolean stillhaveDS = false, stillhaveFC = false;
		for (int i = 0; i < assets.length; i++)
		{
			if (assets[i].type == RobotType.FULFILLMENT_CENTER)
			{
				stillhaveFC = true;
			}
			else if (assets[i].type == RobotType.DESIGN_SCHOOL)
			{
				stillhaveDS = true;
			}
		}

		if (!panic)
		{
			panic = (!stillhaveFC && builtFulfilmentCenter) || (!stillhaveDS && builtDesignSchool);
		}
		builtFulfilmentCenter = stillhaveFC;
		builtDesignSchool = stillhaveDS;
		System.out.println(panic);

		if (panic)
		{
			int minerNum = 0;
			RobotInfo[] stuff = rc.senseNearbyRobots(currentPos, 2, team);
			for (int i = 0; i < stuff.length; i++)
			{
				if (stuff[i].type == RobotType.MINER)
				{
					minerNum++;
				}
			}
			if (minerNum == 0)
				buildMiner();
			else
				panic=  false;
		}


		int nearbyDroneID = senseDrones();
		if(nearbyDroneID != -1)
			rc.shootUnit(nearbyDroneID);
	}
	// static Boolean buildMiner() throws GameActionException
	static Boolean buildMiner() throws GameActionException
	{
		for(int i = 0; i < 8; i++)
		{
			if(rc.canBuildRobot(RobotType.MINER, directions[i]))
			{
				rc.buildRobot(RobotType.MINER, directions[i]);
				minerCount++;
				panic = false;
				return true;
			}
		}
		//communicate that HQ is boxed in; 
		return false;
	}

	// static int senseDrones() throws GameActionException
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