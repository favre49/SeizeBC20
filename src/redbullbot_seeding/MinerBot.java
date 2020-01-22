package redbullbot_seeding;
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
    private static int NEAR_SOUP = 5;
    private static int REFINERY_DISTANCE = 6;

    private static boolean[][] exploredGrid = new boolean[mapWidth][mapHeight];
    private static MapLocation refineryLocation;
    private static MapLocation toBeRefineryLocation;
    private static MapLocation soupLocation;
	private static boolean buildNetGun = false;
	private static boolean builtDesignSchool = false;
	private static boolean builtFulfilmentCenter = false;
	private static int numVapes = 0;
	private static boolean firstTurn  = true;
	private static boolean isBomber = false;
	private static boolean isBuilder = false;
	private static boolean HQBlocked = false;

	private static int offset = 0;
	private static MapLocation idealDSLoc;
	private static MapLocation idealFCLoc;
	private static boolean shouldBuildDS = false;
	private static boolean shouldBuildFC = false;

    public static void run(RobotController rc) throws GameActionException
    {
        // System.out.println("HELLO");
        // Seed random number generator.
        FastMath.initRand(rc);
        currentNumberOfTurns++;

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
                        System.out.println("I should be understanding shit rn" + objectHQLocation.loc);
                        baseLoc = new MapLocation(objectHQLocation.loc.x,objectHQLocation.loc.y);
						isExploring = true;
                        break outerloop;
                    }
					else if (objectHQLocation.rt == ObjectType.COW)
						break;
                }
            }
		}

        if (roundNum%broadCastFrequency == 1 && opponentHQLoc == null)
		{
			System.out.println(roundNum);
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

		// CHeck if you are the bomber.
		if (roundNum <= 5)
		{
			RobotInfo hq = rc.senseRobotAtLocation(currentPos.add(Direction.SOUTH));
			if (hq != null && hq.type == RobotType.HQ)
				isBomber = true;
		}

		if (roundNum > 50 && roundNum < 700 && firstTurn)
		{
			firstTurn = false;
			System.out.println(currentPos.isAdjacentTo(baseLoc));
			if (currentPos.isAdjacentTo(baseLoc))
				isBuilder = true;
		}
		else
		{
			firstTurn = false;
		}

		System.out.println("SP" + soupLocation);
		System.out.println("RF" + refineryLocation);
		System.out.println("TBRF" + toBeRefineryLocation);

		if (isBomber)
		{
			System.out.println("I am bomber");
			System.out.println(baseLoc);
			System.out.println(opponentHQLoc);
			if (opponentHQLoc == null)
			{
				System.out.println("Finding the HQ");
				findHQ();
			}
			else
			{
				if (currentPos.distanceSquaredTo(opponentHQLoc) <= 2)
				{
					Direction dir = currentPos.directionTo(opponentHQLoc);
					if (!builtDesignSchool && rc.canBuildRobot(RobotType.DESIGN_SCHOOL,dir.rotateLeft()))
					{
						buildDesignSchool(dir.rotateLeft());
					}
					else if (!builtDesignSchool && rc.canBuildRobot(RobotType.DESIGN_SCHOOL,dir.rotateRight()))
					{
						buildDesignSchool(dir.rotateRight());
					}
					else if (!builtDesignSchool)
					{
						for (int i = 0; i < 8; i++)
						{
							if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, directions[i]))
							{
								buildDesignSchool(directions[i]);
							}
						}
					}
					else if (!buildNetGun && builtDesignSchool)
					{
						if (rc.getTeamSoup() > 250)
						{
							for (int i = 0; i < 8; i++)
							{
								buildNetGun(directions[i]);
							}
						}
					}
				}
				else
				{
					navigate(opponentHQLoc);
				}
			}
			return;
		}

		if (isBuilder)
		{
			System.out.println("I am the builder!!!");
			
			Direction tryDir = Direction.NORTHWEST;
			for (int i = 0; i < 8; i++)
			{
				idealDSLoc = baseLoc.add(tryDir);
				if (!(rc.onTheMap(idealDSLoc) && !rc.senseFlooding(idealDSLoc) && Math.abs(rc.senseElevation(currentPos) - rc.senseElevation(idealDSLoc)) < 4 && !rc.isLocationOccupied(idealDSLoc)))
				{
					tryDir = tryDir.rotateLeft();
					continue;
				}
				break;
			}

			Direction FCDir = tryDir.opposite();
			for (int i = 0; i < 8; i++)
			{
				idealFCLoc = baseLoc.add(FCDir);
				if (idealFCLoc.isAdjacentTo(idealDSLoc))
					continue;
				if (!(rc.onTheMap(idealFCLoc) && !rc.senseFlooding(idealFCLoc) && Math.abs(rc.senseElevation(currentPos) - rc.senseElevation(idealFCLoc)) < 4 && !rc.isLocationOccupied(idealFCLoc)))
				{
					FCDir = FCDir.rotateLeft();
					continue;
				}
				break;
			}

			System.out.println(idealDSLoc + " " + idealFCLoc);
			
			if (shouldBuildDS || (!builtDesignSchool && roundNum > 150))
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.DESIGN_SCHOOL)
					{
						shouldBuildDS = false;
						builtDesignSchool = true;
						break;
					}
				}

				if (shouldBuildDS || !builtDesignSchool)
				{
					if (currentPos.distanceSquaredTo(idealDSLoc) <= 2)
					{
						if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, currentPos.directionTo(idealDSLoc)))
							buildDesignSchool(currentPos.directionTo(idealDSLoc));
						else
							return;
					}
					else
					{
						navigate(idealDSLoc);
					}
				}
			}

			if (shouldBuildFC || (!builtFulfilmentCenter && roundNum > 180))
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.FULFILLMENT_CENTER)
					{
						shouldBuildFC = false;
						builtFulfilmentCenter = true;
						break;
					}
				}

				if (shouldBuildFC || !builtFulfilmentCenter)
				{
					if (currentPos.distanceSquaredTo(idealFCLoc) <= 2)
					{
						if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, currentPos.directionTo(idealFCLoc)))
							buildFulfilmentCenter(currentPos.directionTo(idealFCLoc));
						else
							return;
					}
					else
					{
						navigate(idealFCLoc);
					}
				}
			}


			if (!buildNetGun && roundNum > 200)
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, team);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.NET_GUN)
					{
						buildNetGun = true;
						break;
					}
				}

				if (!buildNetGun)
				{
					for (int i = 0; i < 8; i++)
					{
						MapLocation idealNGLoc = currentPos.add(directions[i]);
						if (baseLoc.distanceSquaredTo(idealNGLoc) <= 2)
						{
							if (rc.canBuildRobot(RobotType.NET_GUN, directions[i]))
							{
								buildNetGun(directions[i]);
							}
						}
					}
				}
			}

			isBuilder = !(builtFulfilmentCenter && builtDesignSchool && buildNetGun);
			return;
		}

		// Check if we are out building up the base
		if (rc.canSenseLocation(baseLoc))
		{
			RobotInfo[] checkingBots = rc.senseNearbyRobots(baseLoc, 2, team);
			for (int i = 0; i < checkingBots.length; i++)
			{
				if (checkingBots[i].type == RobotType.FULFILLMENT_CENTER || checkingBots[i].type == RobotType.DESIGN_SCHOOL)
				{
					soupLocation = null;
					HQBlocked = true;
				}
			}
		}

		System.out.println("I am carrying " + rc.getSoupCarrying());

        if (soupLocation == null)
		{
			explore();
		}
		else // We found soup, we must mine it.
		{
			// We found soup, but we are too far from the HQ. Let's make a refinery!
			if (refineryLocation == null && currentPos.distanceSquaredTo(baseLoc) > 35 && rc.getTeamSoup() > 200)
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
					}
					else if(currentPos.distanceSquaredTo(loc) <= 2)
					{
						soupLocation = loc;
						rc.mineSoup(currentPos.directionTo(loc));
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
	private static int stepSize = 10; // Picking a hardcoded step size for now.
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
		
		if (currentPos.equals(exploreDest))
		{
			// System.out.println(Clock.getBytecodeNum());            
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
		while(!inBounds(exploreDest) ||exploredGrid[exploreDest.x][exploreDest.y]);
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

	//! Constants I'm using for this function.
	public static int closestDist = Integer.MAX_VALUE;
	public static int turnsWithoutGettingCloser = 0;

	private static void findHQ() throws GameActionException
	{
		switch(offset)
		{
			case 0: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, baseLoc.y);
			break;

			case 1: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, mapHeight-baseLoc.y-1);
			break;

			case 2: exploreDest = new MapLocation(baseLoc.x, mapHeight-baseLoc.y-1);
			break;
		}
		
		if (exploreDest.equals(baseLoc))
		{
			offset++;
			return;
		}

		if (rc.canSenseLocation(exploreDest))
		{
			System.out.println(exploreDest);
			RobotInfo bot = rc.senseRobotAtLocation(exploreDest);
			if (bot != null && bot.type == RobotType.HQ)
			{
				opponentHQLoc = bot.location;
				int initialArr[] = new int[9];
				initialArr[0] = Communications.getCommsNum(ObjectType.HQ,opponentHQLoc);
				System.out.print(Communications.sendComs(initialArr,1));
			}
			else
			{
				closestDist = Integer.MAX_VALUE;
				offset++;
			}
		}
		else
		{
			if (rc.isReady())
			{
				// If i'm taking too long I must be stuck. Stop and use drones.
				if (currentPos.distanceSquaredTo(exploreDest) < closestDist)
				{
					closestDist = currentPos.distanceSquaredTo(exploreDest);
					turnsWithoutGettingCloser = 0;
				}
				else
					turnsWithoutGettingCloser++;
				
				if (turnsWithoutGettingCloser >= 20)
				{
					isBomber = false;
				}
				else
				{
					navigate(exploreDest);
				}
			}
		}
	}
}