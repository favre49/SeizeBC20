package firstbot;
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

    public static Direction dumpingTo = Direction.NORTH;
    public static boolean dumping = false;

    public static void run(RobotController rc)
    {
    	//move to base
    	navigate(new MapLocation(Globals.baseLoc.x+1, Globals.baseLoc.y+1));
 		if(currentPos == new MapLocation(Globals.baseLoc.x+1, Globals.baseLoc.y+1)){
    		fortifyBase();
 		}
    }

    static void fortifyBase(){
		short x[] = {-1, 0, 1, 1, 1, 0, -1, -1};
	    short y[] = {-1, -1, -1, 0, 1, 1, 1, 0};
	    Direction directions[] = {Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST};
    	if(dumping){
    		if(rc.getDirtCarrying() > 0){
    			Globals.rc.depositDirt(dumpingTo);
    		}
    		else{
    			dumping = false;
    		}
    	}
    	else{
	    	if(rc.getDirtCarrying() < 3){
	    		for(int t = 0; t < 8; t++){
	    			RobotInfo baseInfo = rc.senseRobotAtLocation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]));
	    			if(baseID == baseInfo.ID){
	    				t += 3;
	    				t %= 8;
	    				for(int i = 0; i < 3; i++){
	    					if(rc.canDigDirt(directions[t])){
	    						rc.digDirt(directions[t]);
	    						break;
	    					}
	    					t++;
	    					t%= 8;
						}	
					}
	    		}
	    	}
	    	else{
	    		for(int t = 0; t < 8; t++){
	    			RobotInfo baseInfo = rc.senseRobotAtLocation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]));
	    			if(baseID == baseInfo.ID){
	    				t++;
	    				t %= 8; 
	    				if(rc.senseElevation(new MapLocation(currentPos.x, currentPos.y)) >= rc.senseElevation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]))){
	    					dumping = true;
	    					dumpingTo = directions[t];
	    					rc.depositDirt(dumpingTo);
	    				}
	    				else{
	    					if(rc.canMove(directions[t])){
	    						rc.move(directions[t]);
	    					}
	    				}
	    				break;
	    			}
	    		}
    		}
    	}
    	return;
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


}