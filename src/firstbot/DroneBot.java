package firstbot;
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
    public static boolean carryingCow = false;
    public static boolean carryingOpponent = false;
    public static boolean foundCow = false;
    public static boolean foundLandscaper = false;
    public static boolean foundMiner = false;
    public static boolean foundWater = false;
    public static boolean foundHQ = false;
    public static boolean isExploring = true;
    public static MapLocation exploreDest;
    public static int stepSize = 5;

    public static void run(RobotController rc) throws GameActionException
    {
        FastMath.initRand(rc);

        if (carryingCow)
            dropCow();
        else if(carryingOpponent)
            dropOpponent();

        // Explore
        if (isExploring)
            explore();
        else if (foundLandscaper)
            getLandscaper();
        else if (foundCow)
            getCow();
        else if (foundMiner)
            getMiner();

        // If you are carrying an opponent, try and find water

        // If you are carrying a cow, drop it in their
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
        if (!dest.equals(bugDest)) {
			bugDest = dest;
			bugTracing = false;
		}

        if (dest.equals(currentPos))
            return;

        Direction nextDir = currentPos.directionTo(dest);
        // If we can move in the best direction, let's not bother bugging.
        if (!bugTracing && rc.canMove(nextDir))
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
            for (int i = 0; i < 8; ++i) {
                leftDir = leftDir.rotateLeft();
                if (rc.canMove(leftDir)) {
                    leftDistSq = currentPos.add(leftDir).distanceSquaredTo(bugDest);
                    break;
                }
            }
            Direction rightDir = dirToDest;
            int rightDistSq = Integer.MAX_VALUE;
            for (int i = 0; i < 8; ++i) {
                rightDir = rightDir.rotateRight();
                if (rc.canMove(rightDir)) {
                    rightDistSq = currentPos.add(rightDir).distanceSquaredTo(bugDest);
                    break;
                }
            }
            if (rightDistSq < leftDistSq)
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
			if (currentPos.distanceSquaredTo(bugDest) < closestDistWhileBugging)
            {
				if (rc.canMove(currentPos.directionTo(bugDest)))
                {
					bugTracing = false;
					return;
				}
			}
		}
        
        bugTraceMove(false);

        if (bugNumTurnsWithNoWall >= 2) {
	    	bugTracing = false;
	    }

    }

    static void bugTraceMove(boolean recursed) throws GameActionException
    {
		Direction tryDir = currentPos.directionTo(bugLastWall);
		bugVisitedLocations[currentPos.x % mapWidth][currentPos.y % mapHeight] = true;
		if (rc.canMove(tryDir))
        {
			bugNumTurnsWithNoWall += 1;
		}
        else 
        {
			bugNumTurnsWithNoWall = 0;
		}
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
			if (!rc.onTheMap(dirLoc) && !recursed)
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

    /******* END NAVIGATION *******/

    public static void explore() throws GameActionException
    {
        if (exploreDest == null)
        {
            exploreDest = currentPos;
            pickNewExploreDest();
        }

        if (currentPos.equals(exploreDest))
        {
            RobotInfo[] nearbyBots = rc.senseNearbyRobots();
            for (int i = 0; i < nearbyBots.length; i++)
            {
                if (nearbyBots[i].team == team)
                    continue;

                switch(nearbyBots[i].type)
                {
                    case LANDSCAPER:
                    exploreDest = nearbyBots[i].location;
                    foundLandscaper = true;
                    foundCow = false;
                    foundMiner = false;
                    isExploring = false;
                    break;

                    case COW:
                    exploreDest = nearbyBots[i].location;
                    foundCow = true;
                    foundMiner = false;
                    isExploring = false;
                    break;

                    case MINER:
                    exploreDest = nearbyBots[i].location;
                    foundMiner = true;
                    isExploring = false;
                    break;

                    case HQ:
                    foundHQ = true;
                    opponentHQLoc = nearbyBots[i].location;
                    // Broadcast that we found it.
                }

                foundCow = !foundLandscaper && foundCow;
                foundMiner = !foundLandscaper && !foundCow && foundMiner;
            }
            navigate(exploreDest);
        }
        else
        {
            navigate(exploreDest);
        }
        
    }

    // Picks a new destination for exploration.
    private static void pickNewExploreDest() throws GameActionException 
    {
        // Check if do while is a bad way to do this.
        do
        {
            Direction dir = directions[FastMath.rand256()%8];
            exploreDest = exploreDest.translate(dir.dx*stepSize, dir.dy*stepSize);
        }
        while(!inBounds(exploreDest));
    }

    // Find and pick up cow.
    private static void getCow() throws GameActionException
    {
        RobotInfo[] nearbyBots = rc.senseNearbyRobots();
        boolean found = false;
        for (int i = 0; i < nearbyBots.length; i++)
        {
            if (nearbyBots[i].type == RobotType.COW)
            {
                if (nearbyBots[i].location.distanceSquaredTo(currentPos) <= 2)
                {
                    rc.pickUpUnit(nearbyBots[i].ID);
                    carryingCow = true;
                    foundCow = false;
                }
                else
                    exploreDest = nearbyBots[i].location;
                found = true;
            }
        }
        if (!found) // We lost it.
            isExploring = true;
        navigate(exploreDest);
    }

    // Find and pick up Miner
    private static void getMiner() throws GameActionException
    {
        RobotInfo[] nearbyBots = rc.senseNearbyRobots();
        boolean found = false;
        for (int i = 0; i < nearbyBots.length; i++)
        {
            if (nearbyBots[i].type == RobotType.MINER)
            {
                if (nearbyBots[i].location.distanceSquaredTo(currentPos) <= 2)
                {
                    rc.pickUpUnit(nearbyBots[i].ID);
                    carryingOpponent = true;
                    foundMiner = false;
                }
                else
                    exploreDest = nearbyBots[i].location;
                found = true;
            }
        }
        if (!found) // We lost it.
            isExploring = true;
        navigate(exploreDest);
    }

    // Find and pick up Landscaper
    private static void getLandscaper() throws GameActionException
    {
        RobotInfo[] nearbyBots = rc.senseNearbyRobots();
        boolean found = false;
        for (int i = 0; i < nearbyBots.length; i++)
        {
            if (nearbyBots[i].type == RobotType.LANDSCAPER)
            {
                if (nearbyBots[i].location.distanceSquaredTo(currentPos) <= 2)
                {
                    rc.pickUpUnit(nearbyBots[i].ID);
                    carryingOpponent = true;
                    foundLandscaper = false;
                }
                else
                    exploreDest = nearbyBots[i].location;
                found = true;
            }
        }
        if (!found) // We lost it.
            isExploring = true;
        navigate(exploreDest);
    }

    // Look for water and drop it there
    // TODO: Make it easier to look for water?
    private static void dropOpponent() throws GameActionException
    {
        int r = (int)Math.sqrt(sensorRadiusSquared);
        if (!foundWater)
        {
            outerloop:
            for (int x = -r; x <= r; x++)
            {
                int maxY = (int)Math.sqrt(r*r-x*x);
                for (int y = -maxY; y <= maxY; y++)
                {
                    MapLocation checkingPos = currentPos.translate(x,y);
                    if (rc.senseFlooding(checkingPos))
                    {
                        exploreDest = checkingPos;
                        break outerloop;
                    }
                }
            }
        }
        else
        {
            if (currentPos.distanceSquaredTo(exploreDest) <= 2)
            {
                rc.dropUnit(currentPos.directionTo(exploreDest));
                foundWater = false;
                carryingOpponent = false;
                isExploring = true;
            }
            else
                navigate(exploreDest);
        }
    }

    // Look for HQ (?)and drop it there
    // TODO: Make it easier to look for water?
    private static void dropCow() throws GameActionException
    {
        // A bit complicated.
    }


}