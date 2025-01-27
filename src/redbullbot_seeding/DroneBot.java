package redbullbot_seeding;
import battlecode.common.*;

/**
 * Produced by: Fulfillment center
 * Cost: 150
 * Sensor Radius: 24
 * Base cooldown: 1.5
 *
 * Can shoot and also pick up units.
 */
public strictfp class DroneBot extends Globals
{
	public static boolean foundWater = false;
	public static boolean foundHQ = false;
	public static MapLocation exploreDest;
	public static MapLocation soupLocation;
	public static MapLocation[] netGunLocations = new MapLocation[10];
	public static int netGunLocationsIdx = 0;
	public static int stepSize = 10;
	public static int carryingTeammate = -1; // -1 if it isn't carrying anything, 0 if it is carrying opp, 1 if it is.
	public static int offset = 0;
	public static boolean shouldCarryTeammate = true;
	public static boolean rush = false;
	public static int scapersReqd = -1;
	public static int teammatesCarried = 0;
	public static int numTurns = 0;

	public static void run(RobotController rc) throws GameActionException
	{
		FastMath.initRand(rc);

		if (baseLoc == null)
		{
			int[][] commsarr=Communications.getComms(1);
            outerloop:
            for(int i = 0; i<commsarr.length;i++){
                for (int j = 0; j < commsarr[i].length; j++)
                {
                    ObjectLocation objectHQLocation = Communications.getLocationFromInt(commsarr[i][j]); 
                    System.out.println(objectHQLocation.rt);
                    if(objectHQLocation.rt==ObjectType.HQ)
                    {
                        baseLoc = new MapLocation(objectHQLocation.loc.x,objectHQLocation.loc.y);
                        break outerloop;
                    }
                }
            }
		}
		numTurns++;

		if ((roundNum%broadCastFrequency == 1||numTurns == 1) && opponentHQLoc == null)
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
					netGunLocations[netGunLocationsIdx++] = opponentHQLoc;
				}
			}
		}

		RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
		for (int i = 0; i < nearbyOpps.length; i++)
		{
			switch(nearbyOpps[i].type)
			{
				case HQ:
				case NET_GUN:

				boolean exists = nearbyOpps[i].location.equals(netGunLocations[0]) || 
								 nearbyOpps[i].location.equals(netGunLocations[1]) || 
								 nearbyOpps[i].location.equals(netGunLocations[2]) || 
								 nearbyOpps[i].location.equals(netGunLocations[3]) || 
								 nearbyOpps[i].location.equals(netGunLocations[4]) || 
								 nearbyOpps[i].location.equals(netGunLocations[5]) || 
								 nearbyOpps[i].location.equals(netGunLocations[6]) || 
								 nearbyOpps[i].location.equals(netGunLocations[7]) || 
								 nearbyOpps[i].location.equals(netGunLocations[8]) || 
								 nearbyOpps[i].location.equals(netGunLocations[9]);

				if (!exists)
					netGunLocations[netGunLocationsIdx++] = nearbyOpps[i].location;
			}
			if (netGunLocationsIdx == 10) // Prevent overflow
				netGunLocationsIdx = 0;
		}

		if (scapersReqd == -1) // How many scapers do we need?
		{
			scapersReqd = 16;
			int xOffset = Math.min(mapWidth - baseLoc.x, baseLoc.x);
			int yOffset = Math.min(mapHeight - baseLoc.y, baseLoc.y);

			switch(xOffset)
			{
				case 0: scapersReqd -= 7;
				case 1: scapersReqd -= 5;
			}
			
			switch(yOffset)
			{
				case 0: scapersReqd -= 7;
				case 1: scapersReqd -= 5;
			}
			if (yOffset <= 1 && xOffset <= 1)
			{
				scapersReqd += 1;
			}

			scapersReqd--;
		}

		System.out.println(scapersReqd);

		// Let's get rid of the real dangers.
		if (!rush)
		{
			pickUpOpponents();
		}

		if (carryingTeammate == 0)
		{
			dropInWater();
		}
		else if (carryingTeammate == 1 && rc.isReady() && !rush)
		{
			for (int i = 0; i < 8; i++)
			{
				if (inBounds(currentPos.add(directions[i])) && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) >= 4 && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) <= 8 && !rc.senseFlooding(currentPos.add(directions[i])))
				{
					if (rc.canDropUnit(directions[i]))
					{
						carryingTeammate = -1;
						teammatesCarried++;
						if (teammatesCarried > 4)
							rush = true;
						rc.dropUnit(directions[i]);
					}
				}
			}

			// I can't place it anywhere. Let's look for the next place.
			navigate(baseLoc.add(currentPos.directionTo(baseLoc)));
		}
		else if (carryingTeammate == 2)
		{
			if (roundNum > 750)
			{
				dropInWater();
			}

			if (soupLocation == null)
			{
				System.out.println("Looking for a spot");
				explore();
			}
			else if (soupLocation != null && rc.isReady())
			{
				if (currentPos.distanceSquaredTo(soupLocation) <= 2)
				{
					Direction tryDir = currentPos.directionTo(soupLocation);
					for (int i = 0; i < 8; i++)
					{
						if (!rc.senseFlooding(currentPos.add(tryDir)) && rc.canDropUnit(tryDir))
						{
							carryingTeammate = -1;
							shouldCarryTeammate = false;
							rc.dropUnit(currentPos.directionTo(soupLocation));
						}
						else
							tryDir = tryDir.rotateLeft();
					}
				}
				else
				{
					navigateAroundNetGuns(soupLocation);
				}
				rush = true;
			}
		}

		// if (currentPos.distanceSquaredTo(baseLoc) >=4 && currentPos.distanceSquaredTo(baseLoc) <= 8)
		// {
		// 	rush = true;
		// }

		RobotInfo[] scapersToPick = rc.senseNearbyRobots(baseLoc, 8, team);
		int scapersInPlace = 0;
		int pickUpID = -1;
		MapLocation pickUpLoc = null;
		for (int i = 0; i < scapersToPick.length; i++)
		{
			if (scapersToPick[i].type == RobotType.LANDSCAPER)
			{
				if (scapersToPick[i].location.distanceSquaredTo(baseLoc) <= 2)
				{
					pickUpID = scapersToPick[i].ID;
					pickUpLoc = scapersToPick[i].location;
				}
				else
				{
					scapersInPlace++;
				}
			}

			if (scapersToPick[i].type == RobotType.MINER && roundNum < 700)
			{
				pickUpMiners();
			}
		}
		
		if (scapersInPlace == scapersReqd) // Rush time!!!
			rush = true;

		if (rush)
		{
			if (opponentHQLoc == null)
			{
				findHQ();
			}
			else
			{
				if (scapersInPlace == scapersReqd && FastMath.rand256()%2 == 0)
				{
					if (pickUpID != -1)
					{
						if (currentPos.distanceSquaredTo(pickUpLoc) <= 2)
						{
							System.out.println("Trying to pick up" + pickUpID);
							System.out.println(rc.isCurrentlyHoldingUnit());
							if (rc.canPickUpUnit(pickUpID))
							{
								carryingTeammate = 1;
								rc.pickUpUnit(pickUpID);
							}
							else
								return;
						}
						else
						{
							navigate(pickUpLoc);
						}
					}
				}

				if (roundNum > 1000)
				{
					if (!rc.isCurrentlyHoldingUnit())
						pickUpOpponents();
					navigate(opponentHQLoc);
				}
				navigateAroundNetGuns(opponentHQLoc);
			}
		}
		else
		{
			if (pickUpID != -1)
			{
				if (currentPos.distanceSquaredTo(pickUpLoc) <= 2)
				{
					System.out.println("Trying to pick up" + pickUpID);
					System.out.println(rc.isCurrentlyHoldingUnit());
					if (rc.canPickUpUnit(pickUpID))
					{
						carryingTeammate = 1;
						rc.pickUpUnit(pickUpID);
					}
					else
						return;
				}
				else
				{
					navigate(pickUpLoc);
				}
			}
		}

	}


	/******* NAVIGATION *************/

	// Bug nav related stuff
	private static MapLocation bugDest = new MapLocation(0,0);
	private static boolean bugTracing = false;
	private static MapLocation bugLastWall = null;
	private static int closestDistWhileBugging = Integer.MAX_VALUE;	
	private static int bugNumTurnsWithNoWall = 0;
	private static boolean bugWallOnLeft = true; // whether the wall is on our left or our right
	private static boolean[][] bugVisitedLocations;

	// Use Bug Navigation to navigate.
	public static void navigate(MapLocation dest) throws GameActionException
	{
		if (!dest.equals(bugDest))
		{
			bugDest = dest;
			bugTracing = false;
		}

		if (dest.equals(currentPos))
			return;

		Direction nextDir = currentPos.directionTo(dest);
		// If we can move in the best direction, let's not bother bugging.
		if(!bugTracing && rc.canMove(nextDir))
		{
			rc.move(nextDir);
			return;
		}
		else if(!bugTracing)
		{
			bugTracing = true;
			bugVisitedLocations = new boolean[mapWidth][mapHeight];
			closestDistWhileBugging = currentPos.distanceSquaredTo(dest);
			
			Direction dirToDest = currentPos.directionTo(bugDest);
			Direction leftDir = dirToDest;
			int leftDistSq = Integer.MAX_VALUE;
			for(int i = 0; i < 8; ++i)
			{
				leftDir = leftDir.rotateLeft();
				if(rc.canMove(leftDir))
				{
					leftDistSq = currentPos.add(leftDir).distanceSquaredTo(bugDest);
					break;
				}
			}
			
			Direction rightDir = dirToDest;
			int rightDistSq = Integer.MAX_VALUE;
			for(int i = 0; i < 8; ++i)
			{
				rightDir = rightDir.rotateRight();
				if(rc.canMove(rightDir))
				{
					rightDistSq = currentPos.add(rightDir).distanceSquaredTo(bugDest);
					break;
				}
			}

			if(rightDistSq < leftDistSq)
			{
				bugWallOnLeft = true;
				bugLastWall = currentPos.add(rightDir.rotateLeft());
			}
			else
			{
				bugWallOnLeft = false;
				bugLastWall = currentPos.add(leftDir.rotateRight());
			}
		}
		else
		{
			if(currentPos.distanceSquaredTo(bugDest) < closestDistWhileBugging)
			{
				if(rc.canMove(currentPos.directionTo(bugDest)))
				{
					bugTracing = false;
					return;
				}
			}
		}
		
		bugTraceMove(false);

		if (bugNumTurnsWithNoWall >= 2)
		{
			bugTracing = false;
		}

	}

	static void bugTraceMove(boolean recursed) throws GameActionException
	{
		Direction tryDir = currentPos.directionTo(bugLastWall);
		bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight] = true;
		if (rc.canMove(tryDir))
			bugNumTurnsWithNoWall += 1;
		else
			bugNumTurnsWithNoWall = 0;

		for (int i = 0; i < 8; ++i)
		{
			if (bugWallOnLeft)
			{
				tryDir = tryDir.rotateRight();
			}
			else
			{
				tryDir = tryDir.rotateLeft();
			}
			MapLocation dirLoc = currentPos.add(tryDir);
			if (!inBounds(dirLoc) && !recursed)
			{
				// If we hit the edge of the map, reverse direction and recurse
				bugWallOnLeft = !bugWallOnLeft;
				bugTraceMove(true);
				return;
			}
			if (rc.canMove(tryDir))
			{
				rc.move(tryDir);
				currentPos = rc.getLocation(); // we just moved
				if (bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight]) {
					bugTracing = false;
				}
				return;
			}
			else
			{
				bugLastWall = currentPos.add(tryDir);
			}
		}
	}

	private static MapLocation bugDestNG = new MapLocation(0,0);
	private static boolean bugTracingNG = false;
	private static MapLocation bugLastWallNG = null;
	private static int closestDistWhileBuggingNG = Integer.MAX_VALUE;	
	private static int bugNumTurnsWithNoWallNG = 0;
	private static boolean bugWallOnLeftNG = true; // whether the wall is on our left or our right
	private static boolean[][] bugVisitedLocationsNG;

	public static void navigateAroundNetGuns(MapLocation dest) throws GameActionException
	{
		if (!dest.equals(bugDestNG))
		{
			bugDestNG = dest;
			bugTracingNG = false;
		}

		if (dest.equals(currentPos))
			return;

		Direction nextDir = currentPos.directionTo(dest);
		// If we can move in the best direction, let's not bother bugging.
		if(!bugTracingNG && rc.canMove(nextDir) && !inNetGunRange(currentPos.add(nextDir)))
		{
			System.out.println("Moving to " + currentPos.add(nextDir));
			rc.move(nextDir);
			return;
		}
		else if(!bugTracingNG)
		{
			bugTracingNG = true;
			bugVisitedLocationsNG = new boolean[mapWidth][mapHeight];
			closestDistWhileBuggingNG = currentPos.distanceSquaredTo(dest);
			
			Direction dirToDest = currentPos.directionTo(bugDestNG);
			Direction leftDir = dirToDest;
			int leftDistSq = Integer.MAX_VALUE;
			for(int i = 0; i < 8; ++i)
			{
				leftDir = leftDir.rotateLeft();
				if(rc.canMove(leftDir) && !inNetGunRange(currentPos.add(leftDir)))
				{
					leftDistSq = currentPos.add(leftDir).distanceSquaredTo(bugDestNG);
					break;
				}
			}
			
			Direction rightDir = dirToDest;
			int rightDistSq = Integer.MAX_VALUE;
			for(int i = 0; i < 8; ++i)
			{
				rightDir = rightDir.rotateRight();
				if(rc.canMove(rightDir) && !inNetGunRange(currentPos.add(rightDir)))
				{
					rightDistSq = currentPos.add(rightDir).distanceSquaredTo(bugDestNG);
					break;
				}
			}

			if(rightDistSq < leftDistSq)
			{
				bugWallOnLeftNG = true;
				bugLastWallNG = currentPos.add(rightDir.rotateLeft());
			}
			else
			{
				bugWallOnLeftNG = false;
				bugLastWallNG = currentPos.add(leftDir.rotateRight());
			}
		}
		else
		{
			if(currentPos.distanceSquaredTo(bugDestNG) < closestDistWhileBuggingNG)
			{
				if(rc.canMove(currentPos.directionTo(bugDestNG)) && !inNetGunRange(currentPos.add(currentPos.directionTo(bugDestNG))))
				{
					bugTracingNG = false;
					return;
				}
			}
		}
		
		bugTraceMoveAroundNetGuns(false);

		if (bugNumTurnsWithNoWallNG >= 2)
		{
			bugTracingNG = false;
		}
	}

	static void bugTraceMoveAroundNetGuns(boolean recursed) throws GameActionException
	{
		Direction tryDir = currentPos.directionTo(bugLastWallNG);
		bugVisitedLocationsNG[currentPos.x % mapWidth][currentPos.y % mapHeight] = true;
		if (rc.canMove(tryDir) && !inNetGunRange(currentPos.add(tryDir)))
			bugNumTurnsWithNoWallNG += 1;
		else
			bugNumTurnsWithNoWallNG = 0;

		for (int i = 0; i < 8; ++i)
		{
			if (bugWallOnLeftNG)
			{
				tryDir = tryDir.rotateRight();
			}
			else
			{
				tryDir = tryDir.rotateLeft();
			}
			MapLocation dirLoc = currentPos.add(tryDir);
			if (!inBounds(dirLoc) && !recursed)
			{
				// If we hit the edge of the map, reverse direction and recurse
				bugWallOnLeftNG = !bugWallOnLeftNG;
				bugTraceMoveAroundNetGuns(true);
				return;
			}
			if (rc.canMove(tryDir) && !inNetGunRange(dirLoc))
			{
				for (int j = 0; j < netGunLocationsIdx; j++)
					System.out.println(netGunLocations[j] + " " + dirLoc.distanceSquaredTo(netGunLocations[j]));
				System.out.println("Bugnaving to" + dirLoc);
				rc.move(tryDir);
				currentPos = rc.getLocation(); // we just moved
				if (bugVisitedLocationsNG[currentPos.x % mapWidth][currentPos.y % mapHeight]) {
					bugTracingNG = false;
				}
				return;
			}
			else
			{
				bugLastWallNG = currentPos.add(tryDir);
			}
		}
	}

	/******* END NAVIGATION *******/


	public static void dropInWater() throws GameActionException
	{
		System.out.println("Looking to drop my opponent");
		int r = (int)Math.sqrt(sensorRadiusSquared);

		if (exploreDest == null)
			exploreDest = currentPos;

		if (!foundWater)
		{
			outerloop:
			for (int x = -r; x <= r; x++)
			{
				int maxY = (int)Math.sqrt(r*r-x*x);
				for (int y = -maxY; y <= maxY; y++)
				{
					MapLocation checkingPos = currentPos.translate(x,y);
					if (inBounds(checkingPos) && rc.senseFlooding(checkingPos))
					{
						exploreDest = checkingPos;
						foundWater = true;
						break outerloop;
					}
				}
			}
			if (!foundWater)
				pickNewExploreDest();
			navigateAroundNetGuns(exploreDest);
		}
		else
		{
			if (currentPos.distanceSquaredTo(exploreDest) <= 2 && rc.canDropUnit(currentPos.directionTo(exploreDest)))
			{
				carryingTeammate = -1;
				foundWater = false;
				rc.dropUnit(currentPos.directionTo(exploreDest));
			}
			else
				navigateAroundNetGuns(exploreDest);
		}
	}

	private static void explore() throws GameActionException
	{
		System.out.println("Exploring" + exploreDest);
		if(exploreDest == null)
		{
			exploreDest = currentPos;
			pickNewExploreDest();
		}
		
		if (currentPos.equals(exploreDest))
		{
			MapLocation[] nearbySoup = rc.senseNearbySoup(currentPos, sensorRadiusSquared);
			int minDist = Integer.MAX_VALUE;
			for (int i = 0; i < nearbySoup.length; i++)
			{
				if (currentPos.distanceSquaredTo(nearbySoup[i]) < minDist)
				{
					minDist = currentPos.distanceSquaredTo(nearbySoup[i]);
					soupLocation = nearbySoup[i];
				}
			}

			pickNewExploreDest();
			navigateAroundNetGuns(exploreDest);
		}
		else
		{
			navigateAroundNetGuns(exploreDest);
		}
	}

	private static void pickNewExploreDest() throws GameActionException 
	{
		// Check if do while is a bad way to do this.
		boolean firsttime = true;
		do
		{
			Direction dir = directions[FastMath.rand256()%8];
			exploreDest = exploreDest.translate(dir.dx*stepSize, dir.dy*stepSize);
			if(!firsttime && !inBounds(exploreDest))
			{
				//this is the quick fix.
				exploreDest = exploreDest.translate(-1*dir.dx*stepSize, -1*dir.dy*stepSize);                
			}
			firsttime=false;
		}
		while(!inBounds(exploreDest));
	}

	private static void pickUpOpponents() throws GameActionException
	{
		RobotInfo[] nearbyopps = rc.senseNearbyRobots(currentPos,sensorRadiusSquared,opponent);
		if (nearbyopps.length != 0)
		{
			MapLocation attackingLoc = null;
			int closestDist = Integer.MAX_VALUE;
			int attackingID = -1;
			for (int i = 0; i < nearbyopps.length; i++)
			{
				if (nearbyopps[i].type == RobotType.LANDSCAPER && currentPos.distanceSquaredTo(nearbyopps[i].location) < closestDist)
				{
					closestDist = currentPos.distanceSquaredTo(nearbyopps[i].location);
					attackingLoc = nearbyopps[i].location;
					attackingID = nearbyopps[i].ID;
				}
				else if (nearbyopps[i].type == RobotType.MINER && currentPos.distanceSquaredTo(nearbyopps[i].location) < closestDist)
				{
					closestDist = currentPos.distanceSquaredTo(nearbyopps[i].location);
					attackingLoc = nearbyopps[i].location;
					attackingID = nearbyopps[i].ID;
				}
			}
			if (attackingLoc != null)
			{
				System.out.println("Getting");
				System.out.println(attackingLoc);
				if (currentPos.distanceSquaredTo(attackingLoc) <= 2)
				{
					if (rc.canPickUpUnit(attackingID))
					{
						carryingTeammate = 0;
						rc.pickUpUnit(attackingID);
					}
					else
					{
						return;
					}
				}
				else
				{
					navigate(attackingLoc);
				}
			}
		}
	}

	private static void pickUpCows() throws GameActionException
	{
		// Get rid of those damn cows.
		RobotInfo[] nearbyCows = rc.senseNearbyRobots(currentPos,sensorRadiusSquared,Team.NEUTRAL);
		if (nearbyCows.length != 0)
		{
			if (currentPos.distanceSquaredTo(nearbyCows[0].location) <= 2 && rc.canPickUpUnit(nearbyCows[0].ID))
			{
				carryingTeammate  = 0;
				rc.pickUpUnit(nearbyCows[0].ID);
			}
			else
			{
				navigateAroundNetGuns(nearbyCows[0].location);
			}
		}
	}

	private static void pickUpMiners() throws GameActionException
	{
		RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, team);
		if (nearbyTeammates.length != 0) 
		{
			int minerID = -1;
			MapLocation minerLoc = null;
			for (int i = 0; i < nearbyTeammates.length; i++)
			{
				if (nearbyTeammates[i].type == RobotType.MINER)
				{
					minerID = nearbyTeammates[i].ID;
					minerLoc = nearbyTeammates[i].location;
				}
			}
			if (minerLoc != null)
			{
				if (currentPos.distanceSquaredTo(minerLoc) <= 2 && rc.canPickUpUnit(minerID))
				{
					carryingTeammate = 2;
					rc.pickUpUnit(minerID);
				}
				else
				{
					navigateAroundNetGuns(minerLoc);
				}
			}
		}
	}

	private static void findHQ() throws GameActionException
	{
		System.out.println("Finding HQ");
		switch(offset)
		{
			case 0: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, baseLoc.y);
			break;

			case 1: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, mapHeight-baseLoc.y-1);
			break;

			case 2: exploreDest = new MapLocation(baseLoc.x, mapHeight-baseLoc.y-1);
			break;
		}

		System.out.println(currentPos.distanceSquaredTo(exploreDest));
		System.out.println(sensorRadiusSquared);

		if (baseLoc.equals(exploreDest))
		{
			offset++;
			return;
		}

		if (rc.canSenseLocation(exploreDest))
		{
			RobotInfo HQbot = rc.senseRobotAtLocation(exploreDest);
			System.out.println("Sensing " + exploreDest);
			if (HQbot != null && HQbot.type == RobotType.HQ)
			{
				opponentHQLoc = exploreDest;
				int initialArr[] = new int[9];
				initialArr[0] = Communications.getCommsNum(ObjectType.HQ,opponentHQLoc);
				System.out.print(Communications.sendComs(initialArr,1));
			}
			else
			{
				offset++;
			}
		}
		else
		{
			navigateAroundNetGuns(exploreDest);
		}
	}

	// Takes up bytecode, maybe put it inside navigateion instead.
	private static boolean inNetGunRange(MapLocation loc) throws GameActionException
	{
		for (int i = 0; i < netGunLocationsIdx; i++)
		{
			if (loc.distanceSquaredTo(netGunLocations[i]) <= 15)
			{
				System.out.println(loc + "is close enough to" + netGunLocations[i]);
				return true;
			}
		}
		return false;
	}
}