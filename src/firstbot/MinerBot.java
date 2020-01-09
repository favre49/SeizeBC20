package firstbot;
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
    private static MapLocation soupLocation;
    private static int numRefineries = 0;

    public static void run(RobotController rc) throws GameActionException
    {
        // System.out.println("HELLO");
        // Seed random number generator.
        FastMath.initRand(rc);

        // LISTEN FOR SOUPLOCATION AND REFINERY LOCATIONS. IF WE FIND THEM OUT UPDATE STATES AND ACT ACCORDINGLY
        if(roundNum%broadCastFrequency==1){
            System.out.println(Clock.getBytecodeNum());
            int[][] commsarr=Communications.getLastIntervalComms();
            for(int i=0;i<commsarr.length;i++){
                for(int j=0;j<commsarr[i].length;j++){
                    ObjectLocation currObjectLocation = Communications.getLocationFromInt(commsarr[i][0]);
                    if(currObjectLocation.rt==ObjectType.REFINERY){
                        refineryLocation = new MapLocation(currObjectLocation.loc.x,currObjectLocation.loc.y);
                    }
                    else if(currObjectLocation.rt==ObjectType.SOUP){
                        soupLocation = new MapLocation(currObjectLocation.loc.x,currObjectLocation.loc.y);
                    }
                    else if(currObjectLocation.rt==ObjectType.COW){
                        break;
                    }
                }
            }
            System.out.println(Clock.getBytecodeNum());
            // System.out.println("I AM THE MINER");
            // System.out.println(Clock.getBytecodeNum());
        }

        // TODO: ALSO CONSIDER RACE CONDITION WHERE TWO BOTS COMMUNICATE INFO ON THE SAME TURN. GO TO NEAREST ONE.

        if (isExploring){
            // System.out.println("HELLO");
            explore();
        }
        else if(rc.getTeamSoup() >= 200 && numRefineries == 0) // Build a refinery if we have enough.
        {
            if (currentPos.distanceSquaredTo(refineryLocation) <= 2)
                refineryLocation = buildRefinery();
            else
                navigate(refineryLocation);
        }
        else if (rc.getSoupCarrying() == 100) // Refine if we have enough.
        {
            if (numRefineries == 0)
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
        else  // Now we found the soup location. Let's go there and mine!
        {
            System.out.println(soupLocation);
            // If we are nearby, we should start looking for soup.
            if (currentPos.distanceSquaredTo(soupLocation) < NEAR_SOUP)
            {
                MapLocation loc = senseNearbySoup();
                if (loc == null)
                {
                    isExploring = true;
                    explore();
                }
                else if(currentPos.distanceSquaredTo(loc) <= 2)
                {
                    soupLocation = loc;
                    rc.mineSoup(currentPos.directionTo(loc));
                }
                else
                    navigate(soupLocation);
            }
            else
                navigate(soupLocation);
        }
        // System.out.println("HELLO");
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
    private static int stepSize = 5; // Picking a hardcoded step size for now.

    private static void explore() throws GameActionException
    {
        if (!isExploring) return;
        // System.out.println("HELLO2");

        if (exploreDest == null)
        {
            exploreDest = currentPos;
            pickNewExploreDest();
        }


        
        if (rc.canSenseLocation(exploreDest) && rc.senseFlooding(exploreDest))
        {
            exploredGrid[exploreDest.x][exploreDest.y] = true;
            exploreDest = currentPos;
        }
        
        if (currentPos.equals(exploreDest))
        {
            exploredGrid[exploreDest.x][exploreDest.y] = true;
            int r = (int)Math.sqrt(sensorRadiusSquared);

            if (rc.senseSoup(currentPos) > 0)
            {
                soupLocation = currentPos;
                isExploring = false;
                return;
            }

            // Uses tons of bytecodes i think.
            // System.out.println(Clock.getBytecodeNum());
            outerloop:
            for (int x = -r; x <= r; x++)
            {
                int maxY = (int)Math.sqrt(r*r-x*x);
                for (int y = -maxY; y <= maxY; y++)
                {
                    MapLocation checkingPos = currentPos.translate(x,y);
                    if (inBounds(checkingPos) && !exploredGrid[checkingPos.x][checkingPos.y])
                    {
                        exploredGrid[checkingPos.x][checkingPos.y] = true;
                        if (rc.senseSoup(checkingPos) > 0)
                        {
                            soupLocation = checkingPos;
                            isExploring = false;
                            refineryLocation = currentPos;
                            // SEND REFINERY AND SOUP LOCATION

                            int[] toSendArr = new int[12];

                            toSendArr[0] = Communications.getCommsNum(ObjectType.SOUP,soupLocation);
                            toSendArr[1] = Communications.getCommsNum(ObjectType.TO_BE_REFINERY,refineryLocation);
                            Communications.sendComs(toSendArr,0);

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
            navigate(exploreDest);
    }

    // Picks a new destination for exploration.
    private static void pickNewExploreDest() throws GameActionException 
    {
        // Check if do while is a bad way to do this.
        boolean firsttime = true;
        do
        {
            Direction dir = directions[FastMath.rand256()%8];
            // System.out.println("HELLO3");
            exploreDest = exploreDest.translate(dir.dx*stepSize, dir.dy*stepSize);
            if(!firsttime && !inBounds(exploreDest)){
                //this is the quick fix.
                exploreDest = exploreDest.translate(-1*dir.dx*stepSize, -1*dir.dy*stepSize);                
            }
            firsttime=false;
        }
        while(!inBounds(exploreDest) ||exploredGrid[exploreDest.x][exploreDest.y]);
    }

    /** Functions for building buildings. Separated in case we need different behavior for some reason. **/

    private static MapLocation buildRefinery() throws GameActionException
    {
        int i;
        for(i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.REFINERY, directions[i])){
    			rc.buildRobot(RobotType.REFINERY, directions[i]);

    			break;
    		}
    	}
        //IF WE DO BUILD REFINERIES, SEND LOCATION.
        if (i!=8){
            numRefineries++;
            int[] sendArr = new int[12];
            sendArr[0] = Communications.getCommsNum(ObjectType.REFINERY,currentPos.add(directions[i]));
            Communications.sendComs(sendArr,0);
        }
        return currentPos.add(directions[i]);
    }

    private static MapLocation buildVaporator() throws GameActionException
    {
        int i;
        for(i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.VAPORATOR, directions[i])){
    			rc.buildRobot(RobotType.VAPORATOR, directions[i]);
    			break;
    		}
    	}
        return currentPos.add(directions[i]);

    }

    private static MapLocation buildFulfillmentCenter() throws GameActionException
    {
        int i;
        for(i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, directions[i])){
    			rc.buildRobot(RobotType.FULFILLMENT_CENTER, directions[i]);
    			break;
    		}
    	}
        return currentPos.add(directions[i]);

    }

    private static MapLocation buildDesignSchool() throws GameActionException
    {
        int i;
        for(i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, directions[i])){
    			rc.buildRobot(RobotType.MINER, directions[i]);
    			break;
    		}
    	}
        return currentPos.add(directions[i]);

    }

    private static MapLocation buildNetGun() throws GameActionException
    {
        int i;
        for(i = 0; i < 8; i++){
    		if(rc.canBuildRobot(RobotType.NET_GUN, directions[i])){
    			rc.buildRobot(RobotType.NET_GUN, directions[i]);
    			break;
    		}
    	}
        return currentPos.add(directions[i]);

    }
}