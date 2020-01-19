package neobot;
import battlecode.common.*;

/**
 * Produced by: Design School
 * Cost: 150
 * Sensor Radius: 24
 * Base cooldown: 1
 * 
 * Can store a maximum of 25 dirt.
 */
public strictfp class LandscaperBot extends Globals
{
	static MapLocation goToLoc = null;
	static Direction[] latticeExpandDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.CENTER};
	static Direction[] latticeDigDirs = {Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST};
	static MapLocation nextPos;

	static boolean shouldWall = true;
	static int wallIdx = 0;

	static int latticeDigDirsIdx = 0;
	static MapLocation waterDest;
	static boolean underAttack = false;
	
	static int wallElevation = 6;

    public static void run(RobotController rc)  throws GameActionException
    {
		FastMath.initRand(rc);

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
                        break outerloop;
                    }
                }
            }
		}

		// Are we being attacked???
		goToLoc = null;
		int numAttacking = 0;
		int numDefending = 0;
		if (rc.canSenseLocation(baseLoc))
		{
			wallElevation = rc.senseElevation(baseLoc) + 3;
			RobotInfo[] nearbyOpps = rc.senseNearbyRobots(baseLoc, -1, opponent);
			if (nearbyOpps.length != 0)
			{
				for (int i = 0; i < nearbyOpps.length; i++)
				{
					if (nearbyOpps[i].type == RobotType.LANDSCAPER && nearbyOpps[i].location.distanceSquaredTo(baseLoc) <= 2)
					{
						underAttack = true;
						numAttacking++;
					}
					else if (goToLoc == null && nearbyOpps[i].type.isBuilding())
					{
						goToLoc = nearbyOpps[i].location;
					}
				}
			}

			RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(baseLoc,2,team);
			for (int i = 0; i < nearbyTeammates.length; i++)
			{
				if (nearbyTeammates[i].type == RobotType.LANDSCAPER)
				{
					numDefending++;	
				}
			}
			if (numDefending >= numAttacking)
				underAttack = false;
		}

		System.out.println("Are we under attack?" + underAttack);
		System.out.println("We need to kill" + goToLoc);
		// We are!!! Defend!!
		if (goToLoc != null || underAttack)
		{
			if (underAttack)
			{
				if (currentPos.distanceSquaredTo(baseLoc) <= 2)
				{
					Direction tryDir = currentPos.directionTo(baseLoc);
					if (rc.canDigDirt(tryDir))
					{
						rc.digDirt(tryDir);
					}

					if (rc.getDirtCarrying() == 25)
					{
						for (int i = 0; i < 8; i++)
						{
							if (directions[i]!=tryDir && rc.canDepositDirt(tryDir))
							{
								rc.depositDirt(tryDir);
							}
						}
					}
				}
				else
					navigate(baseLoc);
			}

			if(goToLoc != null)
			{
				if (currentPos.distanceSquaredTo(goToLoc) <= 2)
				{
					Direction tryDir = currentPos.directionTo(goToLoc);
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(tryDir);
					}
					else
					{
						for (int i = 0; i < 8; i++)
						{
							if (directions[i] != tryDir && rc.canDigDirt(directions[i]))
							{
								rc.digDirt(directions[i]);
							}
						}
					}
				}
				else
					navigate(goToLoc);
			}
		}

		// Dig the lattice!
		if (currentPos.x % 2 == baseLoc.x % 2 && currentPos.y % 2 == baseLoc.y % 2)
		{
			// Dig lattice formation.
			for (int i = 0; i < 5; i++)
			{
				MapLocation nextCheck = currentPos.add(latticeExpandDirs[i]);
				if (!rc.onTheMap(nextCheck))
					continue;

				RobotInfo robotOccupying = rc.senseRobotAtLocation(nextCheck);
				if (!nextCheck.equals(currentPos) && robotOccupying != null && robotOccupying.team == team)
					continue;

				if (rc.senseElevation(nextCheck) < wallElevation && rc.senseElevation(nextCheck) > -100)
				{
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(latticeExpandDirs[i]);
					}
					else
					{
						if (!rc.onTheMap(currentPos.add(latticeDigDirs[latticeDigDirsIdx])))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}

						if (rc.canDigDirt(latticeDigDirs[latticeDigDirsIdx]))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							rc.digDirt(latticeDigDirs[latticeDigDirsIdx]);
						}
						else
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}
					}
				}
			}

			nextPos = null;
			Direction idealDir = latticeExpandDirs[myID%4];
			for (int i = 0; i < 4; i++)
			{
				MapLocation possibleNextPos = currentPos.translate(idealDir.dx*2, idealDir.dy*2);
				idealDir = idealDir.rotateLeft().rotateLeft();
				if (!rc.onTheMap(possibleNextPos) || possibleNextPos.distanceSquaredTo(baseLoc) <= 5)
					continue;
				
				if (rc.senseElevation(possibleNextPos) < wallElevation)
				{
					nextPos = possibleNextPos;
					break;
				}
			}

			if (nextPos == null)
			{
				explore();
			}
			else
			{
				Direction nextDir = currentPos.directionTo(nextPos);
				RobotInfo blockingBot = rc.senseRobotAtLocation(currentPos.add(nextDir));
				if (blockingBot != null && blockingBot.type.isBuilding() && blockingBot.team != team) // Building blocking our path. Level it!!!
				{
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(nextDir);
					}
					else
					{
						if (!rc.onTheMap(currentPos.add(latticeDigDirs[latticeDigDirsIdx])))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}

						if (rc.canDigDirt(latticeDigDirs[latticeDigDirsIdx]))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							rc.digDirt(latticeDigDirs[latticeDigDirsIdx]);
						}
						else
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}
					}
				}

				if (rc.senseElevation(currentPos.add(nextDir)) < wallElevation && rc.senseElevation(currentPos.add(nextDir)) > -10) // Fill it up.
				{
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(nextDir);
					}
					else
					{
						if (!rc.onTheMap(currentPos.add(latticeDigDirs[latticeDigDirsIdx])))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}

						if (rc.canDigDirt(latticeDigDirs[latticeDigDirsIdx]))
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							rc.digDirt(latticeDigDirs[latticeDigDirsIdx]);
						}
						else
						{
							latticeDigDirsIdx = (latticeDigDirsIdx+1)%4;
							return;
						}
					}
				}

				if (rc.senseElevation(currentPos.add(nextDir)) > wallElevation && rc.senseElevation(currentPos.add(nextDir)) < 100) // Dig it up!
				{
					if (rc.getDirtCarrying() == 25)
					{
						for (int i = 0; i < 4; i++)
						{
							if(rc.canDepositDirt(latticeDigDirs[i]))
								rc.depositDirt(latticeDigDirs[i]);
						}
					}
					else
					{
						if (rc.canDigDirt(nextDir))
							rc.digDirt(nextDir);
					}
				}

				if (blockingBot != null && (!blockingBot.type.isBuilding() || blockingBot.type == RobotType.VAPORATOR))
				{
					nextPos = null;
					explore();
				}

				if (rc.canMove(nextDir))
					rc.move(nextDir);
			}
		}
		else
		{
			if (nextPos == null)
			{
				explore();
			}
			else
			{
				Direction nextDir = currentPos.directionTo(nextPos);
				RobotInfo blockingBot = rc.senseRobotAtLocation(currentPos.add(nextDir));
				if (blockingBot != null && blockingBot.type.isBuilding() && blockingBot.team != team) // Building blocking our path. Level it!!!
				{
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(nextDir);
					}
					else
					{
						for (int i = 0; i < 4; i++)
						{
							if (latticeExpandDirs[i] != nextDir && latticeExpandDirs[i] != nextDir.opposite())
							{
								if (rc.canDigDirt(latticeExpandDirs[i]))
									rc.digDirt(latticeExpandDirs[i]);
							}
						}
					}
				}

				if (rc.senseElevation(currentPos.add(nextDir)) < wallElevation && rc.senseElevation(currentPos.add(nextDir)) > -10) // Fill it up.
				{
					if (rc.getDirtCarrying() > 0)
					{
						rc.depositDirt(nextDir);
					}
					else
					{
						for (int i = 0; i < 4; i++)
						{
							if (latticeExpandDirs[i] != nextDir && latticeExpandDirs[i] != nextDir.opposite())
							{
								if (rc.canDigDirt(latticeExpandDirs[i]))
									rc.digDirt(latticeExpandDirs[i]);
							}
						}
					}
				}

				if (rc.senseElevation(currentPos.add(nextDir)) > wallElevation) // Dig it up!
				{
					if (rc.getDirtCarrying() == 25)
					{
						for (int i = 0; i < 4; i++)
						{
							if(rc.canDepositDirt(latticeDigDirs[i]))
								rc.depositDirt(latticeDigDirs[i]);
						}
					}
					else
					{
						if (rc.canDigDirt(nextDir))
							rc.digDirt(nextDir);
					}
				}

				if (blockingBot != null && (!blockingBot.type.isBuilding() || blockingBot.type == RobotType.VAPORATOR))
				{
					nextPos = null;
					explore();
				}

				if (rc.canMove(nextDir))
					rc.move(nextDir);
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
        if(!dest.equals(bugDest))
        {
			bugDest = dest;
			bugTracing = false;
		}

        if(dest.equals(currentPos))
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
            for (int i = 0; i < 8; ++i)
            {
                leftDir = leftDir.rotateLeft();
                if (rc.canMove(leftDir) 
                	&& !rc.senseFlooding(currentPos.add(leftDir)))
                {
                    leftDistSq = currentPos.add(leftDir).distanceSquaredTo(bugDest);
                    break;
                }
            }

            Direction rightDir = dirToDest;
            int rightDistSq = Integer.MAX_VALUE;
            for (int i = 0; i < 8; ++i)
            {
                rightDir = rightDir.rotateRight();
                if (rc.canMove(rightDir) 
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

        if (bugNumTurnsWithNoWall >= 2)
        {
	    	bugTracing = false;
	    }

    }

    static void bugTraceMove(boolean recursed) throws GameActionException
    {
		Direction tryDir = currentPos.directionTo(bugLastWall);
		bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight] = true;
		if (rc.canMove(tryDir) && !rc.senseFlooding(currentPos.add(tryDir)))
			bugNumTurnsWithNoWall += 1;
        else
			bugNumTurnsWithNoWall = 0;

		for (int i = 0; i < 8; ++i)
        {
			if (bugWallOnLeft)
				tryDir = tryDir.rotateRight();
            else
				tryDir = tryDir.rotateLeft();

			MapLocation dirLoc = currentPos.add(tryDir);
			if(!rc.onTheMap(dirLoc) 
				&& !recursed)
            {
				// If we hit the edge of the map, reverse direction and recurse
				bugWallOnLeft = !bugWallOnLeft;
				bugTraceMove(true);
				return;
			}
			if (rc.canMove(tryDir) 
				&& !rc.senseFlooding(currentPos.add(tryDir)))
            {
				rc.move(tryDir);
				currentPos = rc.getLocation(); // we just moved
				if(bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight])
				{
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

    /******* END NAVIGATION *******/

	public static void buryHQ() throws GameActionException
	{
		if (currentPos.distanceSquaredTo(opponentHQLoc)<=2)
		{
			if (rc.getDirtCarrying() == 0)
			{
				int i;
				for (i = 0; i < 8; i++)
				{
					if (directions[i]!=currentPos.directionTo(opponentHQLoc) && rc.canDigDirt(directions[i]))
						break;
				}
				if (i!=8)
				{
					rc.digDirt(directions[i]);
				}
			}
			else
			{
				if (rc.isReady())
					rc.depositDirt(currentPos.directionTo(opponentHQLoc));
			}
		}
		else
		{
			navigate(opponentHQLoc);
		}
	}

	public static MapLocation exploreDest;
	public static int stepSize = 5;	
	public static int exploredTurns = 0;
	public static int maxNumTurns = 10;

	public static void explore() throws GameActionException
	{
		if (exploreDest == null || maxNumTurns == exploredTurns)
		{
			exploreDest = currentPos;
		}

		if (currentPos.equals(exploreDest))
		{
			pickNewExploreDest();
		}
		exploredTurns++;
		navigate(exploreDest);
	}

	private static void pickNewExploreDest() throws GameActionException 
	{
		if (opponentHQLoc == null)
		{
			// Direction towardsCentre = baseLoc.directionTo(new MapLocation(mapWidth/2, mapHeight/2));
			// Direction towardsLeft = baseLoc.directionTo(new MapLocation(mapWidth-baseLoc.x, baseLoc.y));
			// Direction towardsRight = baseLoc.directionTo(new MapLocation(baseLoc.x, mapHeight-baseLoc.y));

			// int idx = FastMath.rand256()%3;
			// switch(idx)
			// {
			// 	case 0:
			// 	exploreDest = currentPos.translate(towardsCentre.dx*2, towardsCentre.dy*2);
			// 	break;
			// 	case 1:
			// 	exploreDest = currentPos.translate(towardsLeft.dx*2, towardsLeft.dy*2);
			// 	break;
			// 	case 2:
			// 	exploreDest = currentPos.translate(towardsRight.dx*2, towardsRight.dy*2);
			// 	break;
			// }
			// exploredTurns = 0;

			int idx = FastMath.rand256()%8;
			switch(idx)
			{
				case 0:
				exploreDest = currentPos.translate(2,0);
				break;
				case 1:
				exploreDest = currentPos.translate(2,-2);
				break;
				case 2:
				exploreDest = currentPos.translate(0,-2);
				break;
				case 3:
				exploreDest = currentPos.translate(-2,-2);
				break;
				case 4:
				exploreDest = currentPos.translate(-2,0);
				break;
				case 5:
				exploreDest = currentPos.translate(-2,2);
				break;
				case 6:
				exploreDest = currentPos.translate(0,2);
				break;
				case 7:
				exploreDest = currentPos.translate(2,2);
				break;
			}
			exploredTurns = 0;

		}
		else
		{
			exploreDest = opponentHQLoc;
			exploredTurns = 0;
		}
	}
}