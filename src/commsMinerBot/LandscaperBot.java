package commsMinerBot;
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
	// declarations
	public static MapLocation goToLoc;
	public static boolean underAttack = false;
	public static boolean dugged = false;
	public static Direction[] cardinalDirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	public static MapLocation designSchoolLoc;
	public static MapLocation fulfillmentCenterLoc;
	public static MapLocation emptySpace;
	public static int wallIdx = 0;
	public static int numTurns = 0;

    // public static void run(RobotController rc)  throws GameActionException
    public static void run(RobotController rc)  throws GameActionException
    {
		if (baseLoc == null)
		{
			int[][] commsarr=Communications.getComms(1);
            outerloop:
            for(int i=0;i<commsarr.length;i++){
                for (int j = 0; j < commsarr[i].length; j++)
                {
                    ObjectLocation objectHQLocation = Communications.getLocationFromInt(commsarr[i][j]); 
                    System.out.println(objectHQLocation.rt);
                    if(objectHQLocation.rt==ObjectType.HQ)
                    {
                        System.out.println("I should be understanding shit rn" + objectHQLocation.loc);
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
				}
			}
		}

		// Late game drone rush shit.
		if (opponentHQLoc!= null && currentPos.distanceSquaredTo(opponentHQLoc) < 35 && roundNum > 1000)
		{
			if (currentPos.distanceSquaredTo(opponentHQLoc) <= 2)
			{
				System.out.println("Burying HQ");
				Direction tryDir = currentPos.directionTo(opponentHQLoc);
				if (rc.getDirtCarrying() > 0)
				{
					if (rc.canDepositDirt(tryDir))
					{
						dugged = true;
						rc.depositDirt(tryDir);
					}
				}
				else
				{
					Direction digDir = tryDir.opposite(); 
					if (rc.canDigDirt(digDir))
						rc.digDirt(digDir);
					else if (rc.canDigDirt(digDir.rotateRight()))
						rc.digDirt(digDir.rotateRight());
					else if (rc.canDigDirt(digDir.rotateLeft()))
						rc.digDirt(digDir.rotateLeft());
				}
			}


			if (rc.canMove(currentPos.directionTo(opponentHQLoc)))
				rc.move(currentPos.directionTo(opponentHQLoc));

			RobotInfo inTheWay = rc.senseRobotAtLocation(currentPos.add(currentPos.directionTo(opponentHQLoc)));
			if (inTheWay!= null && inTheWay.team != team && inTheWay.type.isBuilding())
			{
				System.out.println("Burying");
				Direction tryDir = currentPos.directionTo(opponentHQLoc);
				if (rc.getDirtCarrying() > 0)
				{
					if (rc.canDepositDirt(tryDir))
					{
						dugged = true;
						rc.depositDirt(tryDir);
					}
				}
				else
				{
					Direction digDir = tryDir.opposite(); 
					if (rc.canDigDirt(digDir))
						rc.digDirt(digDir);
					else if (rc.canDigDirt(digDir.rotateRight()))
						rc.digDirt(digDir.rotateRight());
					else if (rc.canDigDirt(digDir.rotateLeft()))
						rc.digDirt(digDir.rotateLeft());
				}
			}

			if (rc.senseElevation(currentPos) > rc.senseElevation(currentPos.add(currentPos.directionTo(opponentHQLoc))))
			{
				System.out.println("Delevating");
				if (rc.getDirtCarrying() == 25)
				{
					if (rc.canDepositDirt(currentPos.directionTo(opponentHQLoc).opposite()))
						rc.depositDirt(currentPos.directionTo(opponentHQLoc).opposite());
				}
				else
				{
					if (rc.canDigDirt(Direction.CENTER))
					{
						rc.digDirt(Direction.CENTER);
					}
				}
			}

			if (rc.senseElevation(currentPos) < rc.senseElevation(currentPos.add(currentPos.directionTo(opponentHQLoc))))
			{
				System.out.println("elevating");
				Direction tryDir = currentPos.directionTo(opponentHQLoc);
				if (rc.getDirtCarrying() > 0)
				{
					if (rc.canDepositDirt(tryDir))
					{
						dugged = true;
						rc.depositDirt(tryDir);
					}
				}
				else
				{
					Direction digDir = tryDir.opposite(); 
					if (rc.canDigDirt(digDir))
						rc.digDirt(digDir);
					else if (rc.canDigDirt(digDir.rotateRight()))
						rc.digDirt(digDir.rotateRight());
					else if (rc.canDigDirt(digDir.rotateLeft()))
						rc.digDirt(digDir.rotateLeft());
				}
			}

		}
		
		// Are we being attacked???
		goToLoc = null;
		int numAttacking = 0;
		int numDefending = 0;
		if (rc.canSenseLocation(baseLoc))
		{
			RobotInfo[] nearbyOpps = rc.senseNearbyRobots(baseLoc, -1, opponent);
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

			RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(baseLoc,2,team);
			for (int i = 0; i < nearbyTeammates.length; i++)
			{
				if (nearbyTeammates[i].type == RobotType.LANDSCAPER)
				{
					numDefending++;	
				}
			}
			System.out.println(numDefending);
			System.out.println(numAttacking);
			if (numDefending > numAttacking)
				underAttack = false;
		}
		else
		{
			RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, -1, opponent);
			if (nearbyOpps.length != 0)
			{
				for (int i = 0; i < nearbyOpps.length; i++)
				{
					if (nearbyOpps[i].type == RobotType.LANDSCAPER && nearbyOpps[i].location.distanceSquaredTo(baseLoc) <= 2)
					{
						underAttack = true;
						numAttacking++;
					}
					else if (nearbyOpps[i].type == RobotType.HQ)
					{
						goToLoc = nearbyOpps[i].location;
						break;
					}
					else if (goToLoc == null && nearbyOpps[i].type.isBuilding())
					{
						goToLoc = nearbyOpps[i].location;
					}
				}
			}
		}

		// We are! go defend.
		if (goToLoc != null || underAttack)
		{
			if (underAttack)
			{
				System.out.println("Defending HQ");
				if (currentPos.distanceSquaredTo(baseLoc) <= 2)
				{
					Direction tryDir = currentPos.directionTo(baseLoc);
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

					if (rc.canDigDirt(tryDir))
					{
						rc.digDirt(tryDir);
					}
					else
						return;

				}
				else
				{
					for (int i = 0; i < 8; i++)
					{
						if (!rc.isLocationOccupied(baseLoc.add(directions[i])))
							navigate(baseLoc.add(directions[i]));
					}
					navigate(baseLoc);
				}
			}

			if(goToLoc != null)
			{
				System.out.println("Attacking " + goToLoc);
				if (currentPos.distanceSquaredTo(goToLoc) <= 2)
				{
					Direction tryDir = currentPos.directionTo(goToLoc);
					if (rc.getDirtCarrying() > 0)
					{
						System.out.println("Piling it onto " + tryDir);
						if (rc.canDepositDirt(tryDir))
							rc.depositDirt(tryDir);
						else
							return;
					}
					else
					{
						for (int i = 0; i < 8; i++)
						{
							if (directions[i] != tryDir && rc.canDigDirt(directions[i]))
							{
								RobotInfo isMiner = rc.senseRobotAtLocation(currentPos.add(directions[i]));
								if (isMiner == null || isMiner.type != RobotType.MINER || isMiner.type != RobotType.LANDSCAPER)
									rc.digDirt(directions[i]);
							}
						}
					}
				}
				else
					navigate(goToLoc);
			}
		}

		RobotInfo[] structs = rc.senseNearbyRobots(baseLoc,2, team);
		designSchoolLoc = null;
		fulfillmentCenterLoc =  null;
		for (int i = 0; i < structs.length; i++)
		{
			if (structs[i].type == RobotType.DESIGN_SCHOOL)
				designSchoolLoc = structs[i].location;
			else if (structs[i].type == RobotType.FULFILLMENT_CENTER)
				fulfillmentCenterLoc = structs[i].location;
		}

		if (fulfillmentCenterLoc != null)
		{
			if(opponentHQLoc == null)
			{
				Direction dir = baseLoc.directionTo(new MapLocation(mapHeight/2, mapWidth/2));
				emptySpace = baseLoc.add(dir).add(dir);
			}
			else
			{
				Direction dir = baseLoc.directionTo(opponentHQLoc);
				emptySpace = baseLoc.add(dir).add(dir);
			}
		}
		else
		{
			emptySpace = null;
		}
	
		System.out.println(emptySpace);
		
		// WALL TIME!!!
		int distToBase = currentPos.distanceSquaredTo(baseLoc);
		if (distToBase == 8 ||
			distToBase == 5 ||
			distToBase == 4) // we are where we must be!!!
		{
			if (dugged && rc.isReady())
			{
				System.out.println("I am moving in the circle " + wallIdx);
				for (int i = 0; i < 4; i++)
				{
					int currentIdx = (wallIdx+i)%4;
					if (emptySpace != null && currentPos.add(cardinalDirs[currentIdx]).equals(emptySpace))
						continue;

					if(rc.canMove(cardinalDirs[currentIdx]) && currentPos.add(cardinalDirs[currentIdx]).distanceSquaredTo(baseLoc) <= 8 && currentPos.add(cardinalDirs[currentIdx]).distanceSquaredTo(baseLoc) >= 4)
					{
						dugged = false;
						wallIdx = currentIdx;
						rc.move(cardinalDirs[wallIdx]);
					}
				}
				dugged = false;
			}

			if (rc.getDirtCarrying() > 0)
			{
				for (int i = 0; i < 4; i++)
				{
					if (currentPos.add(cardinalDirs[i]).distanceSquaredTo(baseLoc) <= 8 && currentPos.add(cardinalDirs[i]).distanceSquaredTo(baseLoc) >= 4)
					{
						if (rc.senseElevation(currentPos) > rc.senseElevation(currentPos.add(cardinalDirs[i])))
						{
							if (rc.canDepositDirt(cardinalDirs[i]))
								rc.depositDirt(cardinalDirs[i]);
						}
					}
				}

				if (rc.senseFlooding(currentPos.add(currentPos.directionTo(baseLoc))))
				{
					if (rc.canDepositDirt(currentPos.directionTo(baseLoc)))
						rc.depositDirt(currentPos.directionTo(baseLoc));
				}

				if (rc.canDepositDirt(Direction.CENTER))
				{
					dugged = true;
					rc.depositDirt(Direction.CENTER);
				}
			}
			else
			{
				Direction digDir = baseLoc.directionTo(currentPos); 
				if (rc.canDigDirt(digDir))
					rc.digDirt(digDir);
				else if (rc.canDigDirt(digDir.rotateRight()))
					rc.digDirt(digDir.rotateRight());
				else if (rc.canDigDirt(digDir.rotateLeft()))
					rc.digDirt(digDir.rotateLeft());
			}
		}
		else
		{
			Direction digDir = baseLoc.directionTo(currentPos); 
			if (rc.canMove(digDir))
				rc.move(digDir);
			else if (rc.canMove(digDir.rotateRight()))
				rc.move(digDir.rotateRight());
			else if (rc.canMove(digDir.rotateLeft()))
				rc.move(digDir.rotateLeft());
			else
			{
				// Sense if there's someone to carry you around.
				RobotInfo[] nearbyDrones = rc.senseNearbyRobots(baseLoc, 2, team);
				int drno = 0;
				for (int i = 0; i < nearbyDrones.length; i++)
				{
					if (nearbyDrones[i].type == RobotType.DELIVERY_DRONE)
						drno++;
				}
				if (drno == 0)
					navigate(baseLoc.add(currentPos.directionTo(baseLoc)));
				else
					return;
			}
		}
    }

    /******* NAVIGATION *************/
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
				currentPos = currentPos; // we just moved
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
}