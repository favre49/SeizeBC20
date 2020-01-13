package landscaperrushbot;
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

	public static int offset = 0;
	public static MapLocation exploreDest;

	public static boolean isProtector = false;
	public static boolean firstTurn = true;

	// Variables for protecting
	public static MapLocation[] wallArr = new MapLocation[16];
	public static int idx = -1;

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

		if (currentPos.distanceSquaredTo(baseLoc) <= 8 && firstTurn)
		{
			firstTurn = false;
			isProtector = true;
		}
		else
		{
			firstTurn = false;
		}

		if (isProtector) // Protecting landscaper
		{
			if (roundNum > 1000)
			{
				if (opponentHQLoc == null)
				{
					RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
					for (int i = 0; i < nearbyBots.length; i++)
					{
						if (nearbyBots[i].type == RobotType.HQ)
						{
							opponentHQLoc = nearbyBots[i].location;
							break;
						}
					}
				}
				else
				{
					Direction tryDir = currentPos.directionTo(opponentHQLoc);
					if (rc.canMove(tryDir))
					{
						rc.move(tryDir);
					}
					else if (rc.senseFlooding(currentPos.add(tryDir)) || rc.senseRobotAtLocation(currentPos.add(tryDir)) != null || rc.senseElevation(currentPos) > rc.senseElevation(currentPos.add(tryDir)) + 3)
					{
						if (rc.getDirtCarrying() > 0)
						{
							rc.depositDirt(tryDir);
						}
						for (int i = 0; i < 8; i++)
						{
							if (directions[i] != tryDir && rc.canDigDirt(directions[i]))
							{
								rc.digDirt(directions[i]);
							}
						}
					}
					else if (rc.senseElevation(currentPos) + 3 < rc.senseElevation(currentPos.add(tryDir)))
					{
						if (rc.getDirtCarrying() < 25)
						{
							rc.digDirt(tryDir);
						}
						for (int i = 0; i < 8; i++)
						{
							if (directions[i] != tryDir && rc.canDigDirt(directions[i]))
							{
								rc.depositDirt(directions[i]);
							}
						}
					}
				}
			}

			// Dig low wall
			if (wallArr[0] == null)
			{
				wallArr = new MapLocation[]{baseLoc.translate(2,0),
						   baseLoc.translate(2,1),
						   baseLoc.translate(2,2),
						   baseLoc.translate(2,3),
						   baseLoc.translate(1,3),
						   baseLoc.translate(0,3),
						   baseLoc.translate(-1,3),
						   baseLoc.translate(-2,3),
						   baseLoc.translate(-2,2),
						   baseLoc.translate(-2,1),
						   baseLoc.translate(-2,0),
						   baseLoc.translate(-2,-1),
						   baseLoc.translate(-2,-2),
						   baseLoc.translate(-1,-2),
						   baseLoc.translate(0,-2),
						   baseLoc.translate(1,-2),
						   baseLoc.translate(2,-2),
						   baseLoc.translate(2,-1)};
			}

			if (idx == -1)
			{
				int closest = 0;
				int closestDistance = Integer.MAX_VALUE;
				for (int i = 0; i < 18; i++)
				{
					if (currentPos.distanceSquaredTo(wallArr[i]) < closestDistance)
					{
						closest = i;
						closestDistance = currentPos.distanceSquaredTo(wallArr[i]);
					}
				}
				idx = closest;
			}

			if (currentPos.distanceSquaredTo(baseLoc) <= 2)
			{
				// First, check if the base is under attack.
				if (rc.canDigDirt(currentPos.directionTo(baseLoc)))
				{
					if (rc.getDirtCarrying() < 25)
						rc.digDirt(currentPos.directionTo(baseLoc));
					else
					{
						for (int i = 0; i < 8; i++)
						{
							if (directions[i] != currentPos.directionTo(baseLoc) && rc.canDigDirt(directions[i]))
							{
								rc.depositDirt(directions[i]);
							}
						}
					}
				}

				// Next, check whether you should be building or protecting
				RobotInfo bot = rc.senseRobotAtLocation(currentPos.add(Direction.NORTH));
				if (bot != null && bot.type == RobotType.DESIGN_SCHOOL)
				{
					idx = -1;
				}
			}


			if (idx != -1 && currentPos.equals(wallArr[idx]))
			{
				RobotInfo blockingBot = rc.senseRobotAtLocation(wallArr[(idx+1)%18]);
				if (blockingBot != null)
				{
					System.out.println("Get out of the way!!! " + blockingBot.type + "at" + blockingBot.location) ;
				}
				else if (rc.senseElevation(wallArr[(idx+1)%18]) > rc.senseElevation(currentPos)+3)
				{
					if (rc.canDigDirt(currentPos.directionTo(wallArr[(idx+1)%18])))
					{
						rc.digDirt(currentPos.directionTo(wallArr[(idx+1)%18]));
					}
				}
				else if (rc.getDirtCarrying() == 0)
				{
					System.out.println("Dig dig dig!!");
					Direction digDirection = currentPos.directionTo(baseLoc).opposite();
					if (rc.canDigDirt(digDirection))
					{
						rc.digDirt(digDirection);
					}
					else if (rc.canDigDirt(digDirection.rotateLeft()))
					{
						rc.digDirt(digDirection.rotateLeft());
					}
					else if (rc.canDigDirt(digDirection.rotateRight()))
					{
						rc.digDirt(digDirection.rotateRight());
					}
				}
				else
				{
					System.out.println("Put em in!!!!");
					if (rc.isReady())
					{
						if (rc.senseElevation(wallArr[(idx+1)%18]) < rc.senseElevation(currentPos))
						{
							rc.depositDirt(currentPos.directionTo(wallArr[(idx+1)%18]));
						}
						else if (rc.senseElevation(wallArr[(idx+1)%18]) - rc.senseElevation(currentPos) == 3)
						{
							idx = (idx+1)%18;
						}
						else
						{
							idx = (idx+1)%18;
							rc.depositDirt(currentPos.directionTo(wallArr[idx]));
						}
					}
				}
			}
			else if (idx != -1 && !currentPos.equals(wallArr[idx]))
			{
				if (rc.canMove(currentPos.directionTo(wallArr[idx])) && !rc.isLocationOccupied(wallArr[(idx+1)%18]))
					rc.move(currentPos.directionTo(wallArr[idx]));
			}
		}
		else // Attacking landscaper
		{
			if (opponentHQLoc == null)
			{
				RobotInfo[] nearbyBots = rc.senseNearbyRobots(sensorRadiusSquared, opponent);
				for (int i = 0; i < nearbyBots.length; i++)
				{
					if (nearbyBots[i].type == RobotType.HQ)
					{
						opponentHQLoc = nearbyBots[i].location;
						break;
					}
				}
			}		
			buryHQ();
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
				System.out.println("I am digging");
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
				else
				{
					System.out.println("I am unable to dig dirt");
				}
			}
			else
			{
				System.out.println("I should be dumping"+rc.getDirtCarrying());
				if (rc.isReady())
					rc.depositDirt(currentPos.directionTo(opponentHQLoc));
			}
		}
		else
		{
			navigate(opponentHQLoc);
		}
	}
}