package landscaperrushbot;
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

	private static boolean sensedOnFirstRound = false;

    private static boolean[][] exploredGrid = new boolean[mapWidth][mapHeight];
    private static MapLocation refineryLocation;
    private static MapLocation toBeRefineryLocation;
    private static MapLocation soupLocation;
	private static boolean isBomber = false;
	private static int offset = 0;
	private static boolean buildNetGun = false;
	private static boolean builtDesignSchool = false;
	private static boolean builtFulfilmentCenter = false;
	private static int numVapes = 0;
	private static boolean firstTurn  = true;
	private static boolean isBuilder = false;

    public static void run(RobotController rc) throws GameActionException
    {
        // Seed random number generator.
        FastMath.initRand(rc);

		// CHeck if you are the bomber.
		// if (roundNum <= 5)
		// {
		// 	RobotInfo hq = rc.senseRobotAtLocation(currentPos.add(Direction.SOUTH));
		// 	if (hq != null && hq.type == RobotType.HQ)
		// 		isBomber = true;
		// }

		// CHeck if you are the builder.
		if (roundNum > 80 && firstTurn)
		{
			firstTurn = false;
			RobotInfo bot = rc.senseRobotAtLocation(currentPos.add(Direction.SOUTH));
			if (bot!=null && bot.type == RobotType.HQ)
				isBuilder = true;
		}
		else
		{
			firstTurn = false;
		}

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

		if (!sensedOnFirstRound)
		{
			soupLocation = senseNearbySoup();
			sensedOnFirstRound = true;
		}

		if (isBomber)
		{
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
		}
		else if (isBuilder) // My job is to build build build!!!
		{
			System.out.println("I am a builder!!");
			if (rc.getTeamSoup() > 300 && !builtDesignSchool)
			{
				MapLocation designSchoolLoc = baseLoc.add(Direction.EAST);
				if (currentPos.distanceSquaredTo(designSchoolLoc) <= 2 && rc.canBuildRobot(RobotType.DESIGN_SCHOOL, currentPos.directionTo(designSchoolLoc)))
				{
					buildDesignSchool(currentPos.directionTo(designSchoolLoc));
				}
			}
			else if (rc.getTeamSoup() > 300 && builtDesignSchool && !builtFulfilmentCenter)
			{
				MapLocation fulfillmentCenterLoc = baseLoc.add(Direction.WEST);
				if (currentPos.distanceSquaredTo(fulfillmentCenterLoc) <= 2 && rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, currentPos.directionTo(fulfillmentCenterLoc)))
				{
					buildFulfilmentCenter(currentPos.directionTo(fulfillmentCenterLoc));
				}
			}
			else if (rc.getTeamSoup() > 1000 && builtDesignSchool && builtFulfilmentCenter && numVapes < 4)
			{
				Direction[] vapeDirs = {Direction.NORTHEAST, Direction.EAST, Direction.WEST, Direction.NORTHWEST};
				for (int i = 0; i < 4; i++)
				{
					if (rc.canBuildRobot(RobotType.VAPORATOR, vapeDirs[i]))
						buildVaporator(vapeDirs[i]);
				}
			}
			else if (rc.getTeamSoup() > 250 && builtDesignSchool && builtFulfilmentCenter && numVapes >= 4)
			{
				for (int i = 0; i < 8; i++)
				{
					if (rc.canBuildRobot(RobotType.NET_GUN, directions[i]))
						buildNetGun(directions[i]);
				}
			}
		}
		else
		{
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
	private static int stepSize = 3; // Picking a hardcoded step size for now.
	private static int maxTurns = 10; // If you don't reach your destination in 10 turns, take lite.
	private static int currentNumberOfTurns = 0;
	private static int exploredTurns = 0;

	private static void explore() throws GameActionException
	{
		if(!isExploring) return;

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
		
		if (currentPos.equals(exploreDest))
		{
			exploredGrid[exploreDest.x][exploreDest.y] = true;
			int r = (int)Math.sqrt(sensorRadiusSquared);

			// Uses tons of bytecodes i think.
			// System.out.println(Clock.getBytecodeNum());
			outerloop:
			for(int x = -r; x <= r; x++)
			{
				int maxY = (int)Math.sqrt(r*r-x*x);
				for(int y = -maxY; y <= maxY; y++)
				{
					MapLocation checkingPos = currentPos.translate(x,y);
					if(inBounds(checkingPos) 
						&& !exploredGrid[checkingPos.x][checkingPos.y] 
						&& !rc.senseFlooding(checkingPos))
					{
						exploredGrid[checkingPos.x][checkingPos.y] = true;
						if(rc.senseSoup(checkingPos) > 0)
						{
							soupLocation = checkingPos;
							isExploring = false;
							toBeRefineryLocation = currentPos;
							break outerloop;
						}
					}
				}
			}
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

		if (rc.canSenseLocation(exploreDest))
		{
			System.out.println(exploreDest);
			RobotInfo bot = rc.senseRobotAtLocation(exploreDest);
			if (bot != null && bot.type == RobotType.HQ)
				opponentHQLoc = bot.location;
			else
			{
				turnsWithoutGettingCloser = 0;
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
				
				if (turnsWithoutGettingCloser >= 8)
				{
					if (rc.getTeamSoup() > 300)
					{
						for (int i = 0; i < 8; i++)
						{
							buildFulfilmentCenter(directions[i]);
						}
					}
				}
				else
				{
					navigate(exploreDest);
				}
			}
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
}