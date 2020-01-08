package firstbot;
import battlecode.common.*;

/**  
 * Produced By: HQ
 * Cost: 70
 * Sensor Radius: 35
 * Base Cooldown: 1
 *
 * Mines 7 soup a turn, can hold a maximum of 10
 */

public strictfp class MinerBot extends Globals
{
    private static boolean[][] exploredGrid = new boolean[mapWidth][mapHeight];

    public static void run(RobotController rc) throws GameActionException
    {
        System.out.println(currentPos);
        FastMath.initRand(rc);

        explore();
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
        if (!bugTracing && rc.canMove(nextDir) && !rc.senseFlooding(currentPos.add(nextDir)))
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
                if (rc.canMove(leftDir) && !rc.senseFlooding(currentPos.add(leftDir))) {
                    leftDistSq = currentPos.add(leftDir).distanceSquaredTo(bugDest);
                    break;
                }
            }
            Direction rightDir = dirToDest;
            int rightDistSq = Integer.MAX_VALUE;
            for (int i = 0; i < 8; ++i) {
                rightDir = rightDir.rotateRight();
                if (rc.canMove(rightDir) && !rc.senseFlooding(currentPos.add(rightDir))) {
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
		if (rc.canMove(tryDir) && !rc.senseFlooding(currentPos.add(tryDir)))
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
			if (rc.canMove(tryDir) && !rc.senseFlooding(currentPos.add(tryDir)))
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

    private static boolean isExploring = true;
    private static MapLocation exploreDest;
    private static int stepSize = 8; // Picking a hardcodes step size for now.

    private static void explore() throws GameActionException
    {
        if (!isExploring) return;

        if (exploreDest == null)
        {
            exploreDest = currentPos;
            pickNewExploreDest();
        }
        System.out.println(exploreDest);
        
        if (rc.canSenseLocation(exploreDest) && rc.senseFlooding(exploreDest))
        {
            exploredGrid[exploreDest.x][exploreDest.y] = true;
            exploreDest = currentPos;
        }
        
        if (currentPos.equals(exploreDest))
        {
            exploredGrid[exploreDest.x][exploreDest.y] = true;
            int r = FastMath.floorSqrt(sensorRadiusSquared);

            if (rc.senseSoup(currentPos)>0)
                System.out.println("I found soup at" + currentPos);
            
            for (int x = 1; x <= r; x++)
            {
                int ySquare = r*r - x*x;
                int y = FastMath.floorSqrt(ySquare);
                if (y*y == ySquare)
                {
                    MapLocation checkingPos = currentPos.translate(x,y);
                    if (rc.onTheMap(checkingPos))
                    {
                        exploredGrid[checkingPos.x][checkingPos.y] = true;
                        if (rc.senseSoup(checkingPos)>0)
                            System.out.println("I found soup at" + checkingPos);
                    }
                    
                    checkingPos = currentPos.translate(x,-y);
                    if (rc.onTheMap(checkingPos))
                    {
                        exploredGrid[checkingPos.x][checkingPos.y] = true;
                        if (rc.senseSoup(checkingPos)>0)
                            System.out.println("I found soup at" + checkingPos);
                    }
                    
                    checkingPos = currentPos.translate(-x,y);
                    if (rc.onTheMap(checkingPos))
                    {
                        exploredGrid[checkingPos.x][checkingPos.y] = true;
                        if (rc.senseSoup(checkingPos)>0)
                            System.out.println("I found soup at" + checkingPos);
                    }
                    
                    checkingPos = currentPos.translate(-x,-y);
                    if (rc.onTheMap(checkingPos))
                    {
                        exploredGrid[checkingPos.x][checkingPos.y] = true;
                        if (rc.senseSoup(checkingPos)>0)
                            System.out.println("I found soup at" + checkingPos);
                    }            
                }
            }
            pickNewExploreDest();
        }
        else
            navigate(exploreDest);
    }

    private static void pickNewExploreDest() throws GameActionException 
    {
        // Check if do whhile is a bad way to do this.
        do
        {
            Direction dir = directions[FastMath.rand256()%8];
            exploreDest = exploreDest.translate(dir.dx*stepSize, dir.dy*stepSize);
        }
        while(!inBounds(exploreDest) ||exploredGrid[exploreDest.x][exploreDest.y]);
    }
}