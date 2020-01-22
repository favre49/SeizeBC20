package redbullbot;
import battlecode.common.*;

/**  
 * Produced By: HQ
 * Cost: 70
 * Sensor Radius: 35
 * Base Cooldown: 1
 *
 * Mines 7 soup a turn, can hold a maximum of 100.
 */

public strictfp class CommsMinerBot extends Globals
{
	//declarations
	
	private static int HELDSOUPCUTOFF=50;
	private static int CLOSESOUPCUTOFF=2;
	private static int VALIDSOUPCUTOFF = 30;
	private static int RFCUTOFF=70;
	private static int MAXTURNS=10;
	private static int STEPSIZE=5;
	private static int MAPDIVISION=8;

	private static MapLocation[] nearbySoup;
	private static MapLocation soupTarget;

	private static MapLocation[] soupQueue = new MapLocation[9];
	private static int soupQueuePointer=0;

	private static MapLocation[] validLocalSoup;
	private static int validLocalSoupPointer=0;

	private static MapLocation[] refineryList = new MapLocation[9];
	private static int refineryListPointer=1;

	private static MapLocation closestRefinery;
	private static MapLocation localClosestRefinery;
	private static boolean seenLocalRefinery=false;

	// private static int broadCastFrequency=1;
	private static MapLocation notGoingTo = null;

	private static boolean inPath=false;
	private static int pathTurns=0;
	private static MapLocation currentTarget=null;
	
	private static boolean firstTimeSQ=true;
	private static boolean firstTimeRL=true;

	private static boolean miningMode=false;

	private static boolean reachedQ0 = false;

	private static boolean[][] alreadyExplored = new boolean[mapWidth/MAPDIVISION + 2][mapHeight/MAPDIVISION + 2];

	//NOTGOINGTOs are unreachable soups for this bot

    // public static void run(RobotController rc) throws GameActionException
    public static void run(RobotController rc) throws GameActionException
    {


        // Seed random number generator.
		// If we don't have the base location, let's find out.
		if (baseLoc == null)
		{
	        FastMath.initRand(rc);
			getBaseLoc();
			refineryList[0]=baseLoc;
		}

    	updateSQ();//should also remove all NOTGOINGTOs from SQ

    	System.out.println("MYSQSIZE is " + soupQueuePointer);
    	for(int i=0;i<soupQueuePointer;i++){
    		System.out.println("MY SQ: " + i +" "+ soupQueue[i].x + " " + soupQueue[i].y);
    	}


    	if(getValidLocalSoup() && rc.getTeamSoup()>50){
    		pushToSQ();
    		return;
    	}

    	if(inPath){
    		pathTo();
    	}

		if(rc.getSoupCarrying()==RobotType.MINER.soupLimit){
			doRefine();

		}
		else{

			if(soupQueuePointer==0){

				if(rc.getSoupCarrying()>=HELDSOUPCUTOFF){
					doRefine();
				}
				else{
					currentTarget=currentPos;
					pickNewExploreDest();
					newPath(currentTarget);
				}
			}
			else{
				if(reachedQ0 && rc.senseNearbySoup(soupQueue[0],-1).length==0 /*|| !rc.canSenseLocation(soupQueue[0])*/ || (rc.canSenseLocation(soupQueue[0]) && rc.senseFlooding(soupQueue[0]))){
					System.out.println("NOSOUPNOSOUP");
					reachedQ0=false;
					declareNoSoup(soupQueue[0]);
				}

				if(!miningMode){
					if(currentPos.distanceSquaredTo(soupQueue[0]) <= CLOSESOUPCUTOFF){
						reachedQ0=true;
						if(rc.senseNearbySoup(soupQueue[0],-1).length>0){
							miningMode=true;
							mineSoup();
						}
					}
					else{
						newPath(soupQueue[0]);
					}
				}
				else{
					if(rc.senseNearbySoup(currentPos,-1).length>0){
						mineSoup();
						miningMode=true;
					}
					else{
						miningMode=false;
					}
				}
			}
		}
    }

    private static void mineSoup() throws GameActionException{
    	System.out.println("IN MINING");
    	if(soupTarget != null && rc.canSenseLocation(soupTarget) && rc.senseSoup(soupTarget)>0){

    	}
    	else{
    		soupTarget=getClosestNearbySoup();
    	}

    	if(currentPos.distanceSquaredTo(soupTarget)<=2){
    		System.out.println("HOLDING:" + rc.getSoupCarrying());
    		rc.mineSoup(currentPos.directionTo(soupTarget));
    	}
    	else{
    		newPath(soupTarget);
    	}

    }

    private static MapLocation getClosestNearbySoup(){
		int closestSoupInd=0;
		int mindist=Integer.MAX_VALUE;
		for(int i=0;i<nearbySoup.length;i++){
			if(currentPos.distanceSquaredTo(nearbySoup[i])<mindist){
				mindist=currentPos.distanceSquaredTo(nearbySoup[i]);
				closestSoupInd=i;
			}
		}
		return nearbySoup[closestSoupInd];
    }

    private static void doRefine() throws GameActionException{
		// if(seenLocalRefinery && !(rc.canSenseLocation(localClosestRefinery))){
		// 	seenLocalRefinery=false;
		// }

		updateRL();
		int refineryDist = getClosestRefinery();

		// if(!seenLocalRefinery){
		// System.out.println(roundNum);
		// System.out.println(Clock.getBytecodeNum());
		checkLocalRefinery();
		// System.out.println(roundNum);
		// System.out.println(Clock.getBytecodeNum());

		// }

		if(seenLocalRefinery && localClosestRefinery.distanceSquaredTo(currentPos)<refineryDist){
			refineryDist=localClosestRefinery.distanceSquaredTo(currentPos);
			closestRefinery=localClosestRefinery;
		}

		if(refineryDist<=RFCUTOFF || rc.getTeamSoup()<200){
			if(refineryDist<=2){
				rc.depositSoup(currentPos.directionTo(closestRefinery), rc.getSoupCarrying());
			}
			else{
				newPath(closestRefinery);
			}
		}
		else{
			for (int i = 0; i < 8; i++)
			{
				if (rc.canBuildRobot(RobotType.REFINERY, directions[i]) && rc.getTeamSoup()>=201){
					announceRefinery(directions[i]);
					buildRefinery(directions[i]);
				}
			}
		}
	}

	private static void checkLocalRefinery(){
		RobotInfo[] nearbyRef=rc.senseNearbyRobots(-1);
		int nearDist = Integer.MAX_VALUE;
		for(int i=0;i<nearbyRef.length;i++){
			if(nearbyRef[i].type == RobotType.REFINERY){
				seenLocalRefinery=true;
				if(currentPos.distanceSquaredTo(nearbyRef[i].location)<nearDist){
					localClosestRefinery=nearbyRef[i].location;
					nearDist=currentPos.distanceSquaredTo(nearbyRef[i].location);
				}
			}
		}
	}

	private static void declareNoSoup(MapLocation zz) throws GameActionException{
		System.out.println("SENDING NO SOUP: " + zz.x + " " + zz.y);
		int broadCastArr[] = new int[9];
		broadCastArr[0] = Communications.getCommsNum(ObjectType.NO_SOUP, zz);
		Communications.sendComs(broadCastArr,1);
	}


	private static void announceRefinery(Direction x) throws GameActionException{
		MapLocation newRefineryLocation=currentPos.add(x);

		seenLocalRefinery=true;
		localClosestRefinery=newRefineryLocation;

		int broadCastArr[] = new int[9];
		broadCastArr[0] = Communications.getCommsNum(ObjectType.REFINERY, newRefineryLocation);
		Communications.sendComs(broadCastArr,1);
	}


	private static int getClosestRefinery() throws GameActionException{
        MapLocation minLoc = null;
        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < refineryListPointer; i++)
        {
            if (currentPos.distanceSquaredTo(refineryList[i]) < minDist)
            {
                minDist = currentPos.distanceSquaredTo(refineryList[i]);
                minLoc = refineryList[i];
            }
        }
        closestRefinery=minLoc;
        // System.out.println(minDist);

        return minDist;
	}

    private static void getBaseLoc() throws GameActionException{
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
				else if (objectHQLocation.rt == ObjectType.COW)
					break;
            }
        }    	
    }


    private static void updateSQ() throws GameActionException{
    	// soupQueuePointer=0;
		if (roundNum%broadCastFrequency == 1 || firstTimeSQ)
		{
			int toQuery = roundNum-(roundNum%broadCastFrequency);
			if(toQuery==roundNum)toQuery-=broadCastFrequency;
			if(toQuery<0)return;
			int commsArr[][]=Communications.getComms(toQuery);
			firstTimeSQ=false;
			soupQueuePointer=0;
			for(int i = 0; i < commsArr.length; i++)
			{
				innerloop:
				for (int j = 0; j < commsArr[i].length; j++)
                {
					ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);
					switch(currLocation.rt)
					{
						case COW: 
							break innerloop;

						case SOUP:
							if(currLocation.loc!=notGoingTo && soupQueuePointer<9)
								soupQueue[soupQueuePointer++]=currLocation.loc;
						break;
					}
                }
			}
		}    	
    }

    private static void updateRL() throws GameActionException{
    	// refineryListPointer=1;
		if (roundNum%broadCastFrequency == 1 || firstTimeRL)
		{
			int toQuery = roundNum-(roundNum%broadCastFrequency);
			if(toQuery==roundNum)toQuery-=broadCastFrequency;
			if(toQuery<0)return;

			firstTimeRL=false;
			refineryListPointer=1;
			int commsArr[][]=Communications.getComms(toQuery);
			for(int i = 0; i < commsArr.length; i++)
			{
				innerloop:
				for (int j = 0; j < commsArr[i].length; j++)
                {
					ObjectLocation currLocation = Communications.getLocationFromInt(commsArr[i][j]);
					switch(currLocation.rt)
					{
						case COW: 
							break innerloop;

						case REFINERY:
							if(currLocation.loc!=notGoingTo && refineryListPointer<9)
								refineryList[refineryListPointer++]=currLocation.loc;
						break;
					}
                }
			}
		}    	
    }


    private static boolean getValidLocalSoup() throws GameActionException{
    	nearbySoup=rc.senseNearbySoup(currentPos,-1);
    	validLocalSoup = new MapLocation[nearbySoup.length];
    	validLocalSoupPointer=0;
    	for(int i=0;i<nearbySoup.length;i++){
    		if(rc.senseFlooding(nearbySoup[i])){
    			continue;
    		}
    		boolean works=true;
    		for(int j=0;j<soupQueuePointer;j++){
    			if(nearbySoup[i].distanceSquaredTo(soupQueue[j])<=VALIDSOUPCUTOFF){
    				works=false;
    				break;
    			}
    		}

    		if(works){
    			validLocalSoup[validLocalSoupPointer++]=nearbySoup[i];
    			break;
    		}
    	}
    	return (validLocalSoupPointer>0);
    }

    public static void pushToSQ() throws GameActionException{
		int broadCastArr[] = new int[9];
		for(int i=0;i<Math.min(9,validLocalSoupPointer);i++){
			broadCastArr[i] = Communications.getCommsNum(ObjectType.SOUP, validLocalSoup[i]);
		}
		Communications.sendComs(broadCastArr,1);
    }

    private static void newPath(MapLocation x) throws GameActionException{
    	inPath=true;
    	pathTurns=0;
    	currentTarget=x;
    	pathTo();
    }

	private static void pathTo() throws GameActionException
	{
		// System.out.println("X:" + currentTarget.x);
		// System.out.println("Y:" + currentTarget.y);
		// System.out.println("pathTurns" + pathTurns);

		if(currentTarget == null)
		{
			currentTarget = currentPos;
			pickNewExploreDest();
		}

		if(pathTurns >= MAXTURNS || rc.canSenseLocation(currentTarget) && rc.senseFlooding(currentTarget)||(currentPos.distanceSquaredTo(currentTarget)<=2))
		{
			alreadyExplored[currentPos.x/MAPDIVISION][currentPos.y/MAPDIVISION]=true;
			inPath=false;
			pathTurns = 0;
			currentTarget = currentPos;
		}
						
		pathTurns++;
		navigate(currentTarget);
	}

	private static void pickNewExploreDest() throws GameActionException 
	{
		// Check if do while is a bad way to do this.
		boolean firsttime = true;
		do
		{
			Direction dir = directions[FastMath.rand256()%8];
			currentTarget = currentTarget.translate(dir.dx*STEPSIZE, dir.dy*STEPSIZE);
			if(!firsttime && !inBounds(currentTarget))
			{
				//this is the quick fix.
				currentTarget = currentTarget.translate(-1*dir.dx*STEPSIZE, -1*dir.dy*STEPSIZE);                
			}
			firsttime=false;
		}
		while((!inBounds(currentTarget)) || alreadyExplored[currentTarget.x/MAPDIVISION][currentTarget.y/MAPDIVISION]);

		pathTurns = 0;
	}	



    //****NAVIGATION****//
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



	

	/** Functions for building buildings. Separated in case we need different behavior for some reason. **/
	// private static MapLocation buildRefinery(Direction dir) throws GameActionException
private static MapLocation buildRefinery(Direction dir) throws GameActionException
	{
		rc.buildRobot(RobotType.REFINERY, dir);
		closestRefinery = currentPos.add(dir);
		return currentPos.add(dir);
	}

	// private static void buildDesignSchool(Direction dir) throws GameActionException
private static void buildDesignSchool(Direction dir) throws GameActionException
	{
		// builtDesignSchool = true;
		rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
	}


	// private static void buildNetGun(Direction dir) throws GameActionException
	private static void buildNetGun(Direction dir) throws GameActionException
	{
		// buildNetGun = true;
		rc.buildRobot(RobotType.NET_GUN, dir);
	}

	// private static void buildFulfilmentCenter(Direction dir) throws GameActionException
	private static void buildFulfilmentCenter(Direction dir) throws GameActionException
	{
		// builtFulfilmentCenter = true;
		rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
	}

	// private static void buildVaporator(Direction dir) throws GameActionException
	private static void buildVaporator(Direction dir) throws GameActionException
	{
		// numVapes++;
		rc.buildRobot(RobotType.VAPORATOR, dir);
	}

}