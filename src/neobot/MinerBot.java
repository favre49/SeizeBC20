package neobot;
import battlecode.common.*;

/**  
 * Produced By: HQ
 * Cost: 70
 * Sensor Radius: 35
 * Base Cooldown: 1
 *
 * Mines 7 soup a turn, can hold a maximum of 100.
 */

public strictfp class MinerBot extends Globals
{
    // Hardcoded constants I use.
    private static int NEAR_SOUP = 2;
    private static int REFINERY_DISTANCE = 6;

	private static boolean sensedOnFirstRound = false;

    private static boolean[][] exploredGrid = new boolean[mapWidth][mapHeight];
    private static MapLocation refineryLocation;
    private static MapLocation toBeRefineryLocation;
    private static MapLocation soupLocation;
	private static boolean buildNetGun = false;
	private static boolean builtDesignSchool = false;
	private static boolean builtFulfilmentCenter = false;
	private static int numVapes = 0;
	private static int baseElevation = 3;
	private static int wallElevation = 6;
	private static int buildTurn = 0;

    public static void run(RobotController rc) throws GameActionException
    {
        // Seed random number generator.
        FastMath.initRand(rc);
		System.out.println("soup location" + soupLocation);

		// If we don't have the base location, let's find out.
		if (baseLoc == null)
		{
			int[][] commsarr=Communications.getComms(1);
            outerloop:
            for(int i=0;i<commsarr.length;i++){
                for (int j = 0; j < commsarr[i].length; j++)
                {
                    ObjectLocation objectHQLocation = Communications.getLocationFromInt(commsarr[i][j]); 
                    if(objectHQLocation.rt==ObjectType.HQ)
                    {
                        baseLoc = new MapLocation(objectHQLocation.loc.x,objectHQLocation.loc.y);
						isExploring = true;
                        break outerloop;
                    }
					else if (objectHQLocation.rt == ObjectType.COW)
						break;
                }
            }
		}

		if (roundNum%broadCastFrequency == 1)
		{
			int commsArr[][]=Communications.getComms(roundNum-1);
			// Set this up to be a switch case?
			for(int i = 0; i < commsArr.length; i++)
			{
				innerloop:
				for (int j = 0; j < commsArr[i].length; j++)
                {
					ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);
					switch(currLocation.rt)
					{
						case COW: break innerloop;

						case HQ: opponentHQLoc = currLocation.loc;
						break;

						case SOUP:
						if (soupLocation == null)
						{
							soupLocation = currLocation.loc;
							toBeRefineryLocation = currLocation.loc;
							refineryLocation = null;
						}
						break;
					}

                }
			}
		}

		RobotInfo[] nearbyOpps= rc.senseNearbyRobots(currentPos, -1, opponent);
		boolean shouldBuild = false;
		for (int i = 0; i < nearbyOpps.length; i++)
		{
			if (!nearbyOpps[i].type.isBuilding())
			{
				shouldBuild = true;
				break;
			}
		}

		if (rc.canSenseLocation(baseLoc))
		{
			RobotInfo[] nearbyStructs = rc.senseNearbyRobots(baseLoc, 8, team);
			for (int i = 0; i < nearbyStructs.length; i++)
			{
				if (nearbyStructs[i].type == RobotType.FULFILLMENT_CENTER)
					builtFulfilmentCenter = true;
				else if (nearbyStructs[i].type == RobotType.DESIGN_SCHOOL)
					builtDesignSchool = true;
			}
		}

		System.out.println("I am carrying " + rc.getSoupCarrying() + " soup");

		// if (opponentHQLoc != null)
		// {
		// 	if (currentPos.distanceSquaredTo(opponentHQLoc) > 20)
		// 		navigate(opponentHQLoc);
		// 	else
		// 	{
		// 		RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
		// 		for (int i = 0; i < nearbyBots.length; i++)
		// 		{
		// 			if (nearbyBots[i].type == RobotType.NET_GUN)
		// 			{
		// 				buildNetGun = false;
		// 				break;
		// 			}
		// 		}

		// 		if (buildNetGun)
		// 		{
		// 			for (int i = 0; i < 8; i++)
		// 			{
		// 				if (rc.canBuildRobot(RobotType.NET_GUN, directions[i]))
		// 					buildNetGun(directions[i]);
		// 			}
		// 		}
		// 	}
		// }

		if (shouldBuild && !builtFulfilmentCenter)
		{
			if (baseLoc.distanceSquaredTo(currentPos) > 5)
			{
				navigate(baseLoc);
			}
			else
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.FULFILLMENT_CENTER)
					{
						shouldBuild = false;
						builtFulfilmentCenter = true;
						break;
					}
				}

				if (!builtFulfilmentCenter)
				{
					for (int i = 0; i < 8; i++)
					{
						if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, directions[i]) && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) <= 8)
							buildFulfilmentCenter(directions[i]);
					}
					navigate(baseLoc);
				}
			}
		}

		if (roundNum > 100 && !builtDesignSchool)
		{
			if (baseLoc.distanceSquaredTo(currentPos) > 8)
			{
				navigate(baseLoc);
			}
			else
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.DESIGN_SCHOOL)
					{
						builtDesignSchool = true;
						break;
					}
				}

				if (!builtDesignSchool)
				{
					for (int i = 0; i < 8; i++)
					{
						if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, directions[i]) && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) <= 8)
							buildDesignSchool(directions[i]);
					}
					navigate(baseLoc);
				}
			}
		}

		if (roundNum > 150 && !builtFulfilmentCenter)
		{
			if (baseLoc.distanceSquaredTo(currentPos) > 5)
			{
				navigate(baseLoc);
			}
			else
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.FULFILLMENT_CENTER)
					{
						builtFulfilmentCenter = true;
						break;
					}
				}

				if (!builtFulfilmentCenter)
				{
					for (int i = 0; i < 8; i++)
					{
						if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, directions[i]) && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) <= 8)
							buildFulfilmentCenter(directions[i]);
					}
					navigate(baseLoc);
				}
			}
		}


		if (rc.getTeamSoup() > 500 && (rc.senseElevation(currentPos) >= 8||roundNum < 200))
		{
			int vapenums = 0;
			RobotInfo[] nearbyVapes = rc.senseNearbyRobots(currentPos, 8, team);
			for (int i = 0; i < nearbyVapes.length; i++)
			{
				if (nearbyVapes[i].type == RobotType.VAPORATOR)
				{
					vapenums++;
				}
			}
			if (vapenums < 3)
			{
				for (int i = 0; i < 8; i++)
				{
					MapLocation buildPos = currentPos.add(directions[i]);
					if (rc.canBuildRobot(RobotType.VAPORATOR, directions[i]) && buildPos.x % 2 == (baseLoc.x+1) % 2 && buildPos.y % 2 == (baseLoc.y+1) % 2 && (baseLoc.distanceSquaredTo(buildPos) < 9 || baseLoc.distanceSquaredTo(buildPos) > 18))
					{
						buildTurn++;
						buildVaporator(directions[i]);
					}
				}
			}
		}

		if (rc.getTeamSoup() > 250 && rc.senseElevation(currentPos) >= 8 && roundNum > 500 && buildTurn%4 == 0)
		{
			int numGuns = 0;
			RobotInfo[] nearbyVapes = rc.senseNearbyRobots(currentPos, 8, team);
			for (int i = 0; i < nearbyVapes.length; i++)
			{
				if (nearbyVapes[i].type == RobotType.NET_GUN)
				{
					numGuns++;
				}
			}
			if (numGuns < 1)
			{
				for (int i = 0; i < 8; i++)
				{
					MapLocation buildPos = currentPos.add(directions[i]);
					if (rc.canBuildRobot(RobotType.NET_GUN, directions[i]) && buildPos.x % 2 == (baseLoc.x+1) % 2 && buildPos.y % 2 == (baseLoc.y+1) % 2 && (baseLoc.distanceSquaredTo(buildPos) < 9 || baseLoc.distanceSquaredTo(buildPos) > 18))
					{
						buildTurn++;
						buildNetGun(directions[i]);
					}
				}
			}
		}

		if (soupLocation == null)
		{
			explore();
		}
		else // We found soup, we must mine it.
		{
			// We found soup, but we are too far from the HQ. Let's make a refinery!
			if (refineryLocation == null && currentPos.distanceSquaredTo(baseLoc) > 50 && rc.getTeamSoup() > 200)
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.REFINERY)
					{
						refineryLocation = nearbyBots[i].location;
						toBeRefineryLocation = null;
					}
				}

				if (refineryLocation == null && toBeRefineryLocation != null)
				{
					if (currentPos.distanceSquaredTo(toBeRefineryLocation) <= 2)
					{
						for (int i = 0; i < 8; i++)
						{
							if (rc.canBuildRobot(RobotType.REFINERY, directions[i]))
								buildRefinery(directions[i]);
						}
					}
				}
			}
			if (rc.getSoupCarrying() >= 100)
			{
				if (refineryLocation == null)
				{
					if (currentPos.distanceSquaredTo(baseLoc) <= 2)
						rc.depositSoup(currentPos.directionTo(baseLoc), rc.getSoupCarrying());
					else
						navigate(baseLoc);
				}
				else
				{
					if (currentPos.distanceSquaredTo(refineryLocation) <= 2)
						rc.depositSoup(currentPos.directionTo(refineryLocation), rc.getSoupCarrying());
					else
						navigate(refineryLocation);
				} 
			}
			else
			{
				if (currentPos.distanceSquaredTo(soupLocation) < NEAR_SOUP)
				{
					MapLocation loc = senseNearbySoup();
					if (loc == null)  // No soup nearby? Must be no soup left!!
					{
						soupLocation = null;
						refineryLocation = null;
						isExploring = true;

						int broadCastArr[] = new int[9];
						broadCastArr[0] = Communications.getCommsNum(ObjectType.NO_SOUP, new MapLocation(0,0));
						Communications.sendComs(broadCastArr,1);
					}
					else if(currentPos.distanceSquaredTo(loc) <= 2)
					{
						soupLocation = loc;
						if (rc.isReady())
							rc.mineSoup(currentPos.directionTo(loc));
						else
							return;
					}
					else
					{
						soupLocation = loc;
						navigate(soupLocation);
					}
				}
				else
				{
					navigate(soupLocation);
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
		if(!bugTracing && rc.canMove(nextDir) 
			&& !rc.senseFlooding(currentPos.add(nextDir)))
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
			for(int i = 0; i < 8; ++i) {
				leftDir = leftDir.rotateLeft();
				if(rc.canMove(leftDir) 
					&& !rc.senseFlooding(currentPos.add(leftDir)))
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
				if(rc.canMove(rightDir) 
					&& !rc.senseFlooding(currentPos.add(rightDir)))
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

		if(bugNumTurnsWithNoWall >= 2)
		{
			bugTracing = false;
		}

	}

	static void bugTraceMove(boolean recursed) throws GameActionException
	{
		Direction tryDir = currentPos.directionTo(bugLastWall);
		bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight] = true;
		
		if(rc.canMove(tryDir) && !rc.senseFlooding(currentPos.add(tryDir)))
			bugNumTurnsWithNoWall += 1;
		else 
			bugNumTurnsWithNoWall = 0;

		for (int i = 0; i < 8; ++i)
		{
			if(bugWallOnLeft)
				tryDir = tryDir.rotateRight();
			else
				tryDir = tryDir.rotateLeft();

			MapLocation dirLoc = currentPos.add(tryDir);
			if(!inBounds(dirLoc) && !recursed)
			{
				// If we hit the edge of the map, reverse direction and recurse
				bugWallOnLeft = !bugWallOnLeft;
				bugTraceMove(true);
				return;
			}

			if(rc.canMove(tryDir) && !rc.senseFlooding(currentPos.add(tryDir)))
			{
				rc.move(tryDir);
				currentPos = rc.getLocation(); // we just moved
				if (bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight])
					bugTracing = false;
				return;
			}
			else
			{
				bugLastWall = currentPos.add(tryDir);
			}
		}
	}

	/******* END NAVIGATION *******/

	private static boolean isExploring = false;
	private static MapLocation exploreDest;
	private static int stepSize = 5; // Picking a hardcoded step size for now.
	private static int maxTurns = 10; // If you don't reach your destination in 10 turns, take lite.
	private static int currentNumberOfTurns = 0;
	private static int exploredTurns = 0;

	private static void explore() throws GameActionException
	{
		if(exploreDest == null)
		{
			exploreDest = currentPos;
			pickNewExploreDest();
		}

		if(exploredTurns >= maxTurns)
		{
			exploredTurns = 0;
			exploredGrid[exploreDest.x][exploreDest.y] = true;
			exploreDest = currentPos;
		}
		
		if(rc.canSenseLocation(exploreDest) && rc.senseFlooding(exploreDest))
		{
			exploredGrid[exploreDest.x][exploreDest.y] = true;
			exploreDest = currentPos;
		}
		
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

		toBeRefineryLocation = soupLocation;

		if (soupLocation != null)
		{
			int broadCastArr[] = new int[9];
			broadCastArr[0] = Communications.getCommsNum(ObjectType.SOUP, soupLocation);
			Communications.sendComs(broadCastArr,1);
		}

		if (currentPos.equals(exploreDest))
		{
			pickNewExploreDest();
			navigate(exploreDest);
		}
		else
		{
			exploredTurns++;
			navigate(exploreDest);
		}
	}

	// Picks a new destination for exploration.
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
		while(!inBounds(exploreDest) || exploredGrid[exploreDest.x][exploreDest.y]);
		exploredTurns = 0;
	}

	/** Functions for building buildings. Separated in case we need different behavior for some reason. **/
	private static MapLocation buildRefinery(Direction dir) throws GameActionException
	{
		rc.buildRobot(RobotType.REFINERY, dir);
		refineryLocation = currentPos.add(dir);
		toBeRefineryLocation = null;
		return currentPos.add(dir);
	}

	private static void buildDesignSchool(Direction dir) throws GameActionException
	{
		builtDesignSchool = true;
		rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
	}

	private static void buildNetGun(Direction dir) throws GameActionException
	{
		buildNetGun = true;
		rc.buildRobot(RobotType.NET_GUN, dir);
	}

	private static void buildFulfilmentCenter(Direction dir) throws GameActionException
	{
		builtFulfilmentCenter = true;
		rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
	}

	private static void buildVaporator(Direction dir) throws GameActionException
	{
		numVapes++;
		rc.buildRobot(RobotType.VAPORATOR, dir);
	}
}