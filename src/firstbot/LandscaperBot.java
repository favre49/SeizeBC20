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
    public static Direction[] path = {Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH};
    public static MapLocation detectedDesignSchoolLoc = null;
	public static MapLocation goToLoc;
	public static boolean underAttack = false;

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
		
		// Are we being attacked???
		goToLoc = null;
		int numAttacking = 0;
		int numDefending = 0;
		if (rc.canSenseLocation(baseLoc))
		{
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

		/*if(currentPos.distanceSquaredTo(baseLoc) <= 8)
		{
			fortifyBase2();
		} */
		fortifyBase3();   	
    }
    

    //if a design school is built away from the base then we automatically build A base, I think if we use mother
    //design school locations for determing which beahvior the miner will exhibit, it will be best.
    /*static void buildSubBase() throws GameActionException
    {
    	if(detectedDesignSchoolLoc == null){
    		RobotInfo nearbyUnits[] = rc.senseNearbyRobots(2);
	    	for(int i = 0; i < nearbyUnits.length; i++)
	    	{
	    		if(nearbyUnits[i].getType() == RobotType.DESIGN_SCHOOL && currentPos.distanceSquaredTo(nearbyUnits[i].getLocation()) <= 2)
	    		{
	    			detectedDesignSchoolLoc = nearbyUnits[i].getLocation();
	    		}
	    	}
    	}

    	if(detectedDesignSchoolLoc.distanceSquaredTo(baseLoc) >)
    }*/


    static void fortifyBase3() throws GameActionException
    {
    	//detect your own locations of the Mother Design School
    	if(detectedDesignSchoolLoc == null)
    	{
	    	RobotInfo nearbyUnits[] = rc.senseNearbyRobots(2);
	    	for(int i = 0; i < nearbyUnits.length; i++)
	    	{
	    		if(nearbyUnits[i].getType() == RobotType.DESIGN_SCHOOL && currentPos.distanceSquaredTo(nearbyUnits[i].getLocation()) <= 2)
	    		{
	    			detectedDesignSchoolLoc = nearbyUnits[i].getLocation();
	    		}
	    	}
	    }
		
		if(detectedDesignSchoolLoc.distanceSquaredTo(baseLoc) <= 8)
	    {
	    	if(currentPos.distanceSquaredTo(baseLoc) <= 2)
	    	{
	    		System.out.println("in position, diggin");
				dig3();
	    	}
	    	else if(currentPos.distanceSquaredTo(baseLoc) > 8)
	    	{
	    		System.out.println("Away from Base...");
	    		navigate(baseLoc);
	    	}
	    	else{
				for(int i = 0; i < 8; i++){
					if(rc.canSenseLocation(baseLoc.add(directions[i]))){
						if(!rc.isLocationOccupied(baseLoc.add(directions[i]))){
							navigate(baseLoc.add(directions[i]));
						}
					}
					else{
						navigate(baseLoc.add(directions[i]).add(directions[i]));
					}
				}
	    	}
	    }

    }

    static void dig3() throws GameActionException
    {
    	//dig from highest elevation the outer layer and the inner layer if unnoccupoied;
    	//encapsulate this in another condition
	    RobotInfo nearbyRobots[] = rc.senseNearbyRobots(baseLoc, 2, team);
		    if(rc.canSenseRadiusSquared(8) && nearbyRobots.length < 8)
		    {
		    	if(rc.getDirtCarrying() == 0)
		    	{
			    	System.out.println("Formation not ready Yet");
			    	int highestElevation = 1000;
			    	Direction highestElevationdirection = Direction.CENTER;
			    	for(int i = 0; i < 8; i++){
			    		if(currentPos.add(directions[i]).distanceSquaredTo(baseLoc) > 2){
			    			if(rc.senseElevation(currentPos.add(directions[i])) < highestElevation && rc.canDigDirt(directions[i])){
			    				highestElevation = rc.senseElevation(currentPos.add(directions[i]));
			    				highestElevationdirection = directions[i];
			    			}
			    		}
			    		else{
			    			if(rc.senseElevation(currentPos.add(directions[i]))-3 < highestElevation && rc.canDigDirt(directions[i]) && !rc.isLocationOccupied(currentPos.add(directions[i]))){
			    				highestElevation = rc.senseElevation(currentPos.add(directions[i])) - 3;
			    				highestElevationdirection = directions[i];
			    			}
			    		}
			    	}
		    		if(rc.canDigDirt(currentPos.directionTo(baseLoc))){
		    			highestElevationdirection = currentPos.directionTo(baseLoc);
		    		}
			    	rc.digDirt(highestElevationdirection);
			    }
			    else{
			    	RobotInfo nearbyEnemyBuildings[] = rc.senseNearbyRobots(2, team.opponent());
			    	if(nearbyEnemyBuildings != null){
			    		for(int i = 0; i < nearbyEnemyBuildings.length; i++){
			    			if(nearbyEnemyBuildings[i].getType() == RobotType.DESIGN_SCHOOL || nearbyEnemyBuildings[i].getType() == RobotType.NET_GUN)
			    				rc.depositDirt(currentPos.directionTo(nearbyEnemyBuildings[i].getLocation()));
			    		}
			    	}


			    	
			    }
		    }
		    else{
		    	System.out.println("Formation Ready");
		    	if(rc.getDirtCarrying() == 0){
		    		int highestElevation = 1000;
		    		Direction highestElevationdirection = Direction.CENTER;
		    		for(int i =0; i < 8; i++){
		    			if(rc.senseElevation(currentPos.add(directions[i])) < highestElevation && rc.canDigDirt(directions[i]) && !rc.isLocationOccupied(currentPos.add(directions[i]))){
		    				highestElevation = rc.senseElevation(currentPos.add(directions[i]));
		    				highestElevationdirection = directions[i];
		    			}
		    		}
		    		if(rc.canDigDirt(currentPos.directionTo(baseLoc))){
		    			highestElevationdirection = currentPos.directionTo(baseLoc);
		    		}
		    		rc.digDirt(highestElevationdirection);		
		    	}
		    	else{
		    		//dposit on enemy buildings/lowest location
		    		int lowestElevation = rc.senseElevation(currentPos);
		    		Direction lowestElevationDirection = Direction.CENTER;

		    		for(int i = 0; i < 8; i++){
		    			if(lowestElevation > rc.senseElevation(currentPos.add(directions[i])) && baseLoc.distanceSquaredTo(currentPos.add(directions[i])) > 2){
		    				lowestElevation = rc.senseElevation(currentPos.add(directions[i]));
		    			}

		    			if(isEnemyBuildingAtLocation(currentPos.add(directions[i]))){
		    				lowestElevation = -5;
		    				lowestElevationDirection = directions[i];
		    			}
		    		}
		    		System.out.println("Dumping");
					rc.depositDirt(lowestElevationDirection);
		    	}
		    }
    }



    static void fortifyBase2() throws GameActionException
    {
    	System.out.println("BaseLOC   " + baseLoc);
		if(currentPos.distanceSquaredTo(baseLoc) <= 2)
		{
 			//get into position
 			RobotInfo nearBase[] = rc.senseNearbyRobots(baseLoc, 8, rc.getTeam());
 			Direction path[] = {Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH};
    
 			if(nearBase.length < 24)
 			{
 				System.out.println("Flaksdf");
				for(int i = 0; i < 8; i++)
				{
	 				if(!rc.isLocationOccupied(new MapLocation(baseLoc.x + x[(i+1)%8], baseLoc.y + y[(i+1)%8])) && currentPos.equals(new MapLocation(baseLoc.x + x[i], baseLoc.y + y[i])))
	 				{
	 					if(rc.canMove(path[(i+1)%8]))
	 						rc.move(path[(i+1)%8]);

	 				}
	 			}
	 		}
			else{
				System.out.println("in POSIUSdekl;jnfla;sjknbLKUJGHLJKUHLIUHUIKLH");
				if(rc.getDirtCarrying() <= 0)
				{
					System.out.println("DIGGING");
					if(rc.canDigDirt(Direction.CENTER))
	    				rc.digDirt(Direction.CENTER);
	    		}
	    		else
	    		{	
	    			Direction lowestSquareDirectionInOuterLayer = Direction.CENTER;
	    			int lowestElevation = 1000;
	    			//lay dirt down in the lowest location of the outer layer
	    			for(int i = 0; i < 8; i++)
	    			{	
	    				if(rc.senseElevation(new MapLocation(currentPos.x + x[i], currentPos.y + y[i])) < lowestElevation 
	    					&& rc.canDepositDirt(directions[i]) 
	    					&& baseLoc.distanceSquaredTo(new MapLocation(currentPos.x + x[i], currentPos.y + y[i])) > 2
	    					&& baseLoc.distanceSquaredTo(new MapLocation(currentPos.x + x[i], currentPos.y + y[i])) <= 8)
	    				{
	    					lowestElevation = rc.senseElevation(new MapLocation(currentPos.x + x[i], currentPos.y + y[i]));
	    					lowestSquareDirectionInOuterLayer = directions[i];
	    				}
	    			}
    				if(rc.canDepositDirt(lowestSquareDirectionInOuterLayer))
    					rc.depositDirt(lowestSquareDirectionInOuterLayer);
    				System.out.println("dumping dirt");
	    		}	
    		}
		}
		else{
			//get into formation;
			int xx[] = {-2, -1,  0,  1,  2,  2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2};
			int yy[] = {-2, -2, -2, -2, -2, -1, 0, 1, 2, 2, 2,  2,  2,  1,  0, -1};
			Direction path2[] = {Direction.SOUTH, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.WEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH};
			RobotInfo nearBase[] = rc.senseNearbyRobots(baseLoc, 8, rc.getTeam());
 			if(nearBase.length < 23)
 			{
				for(int i = 0; i < xx.length; i++)
				{
	 				if(currentPos.equals(new MapLocation(baseLoc.x + xx[i], baseLoc.y + yy[i])) && !rc.isLocationOccupied(new MapLocation(baseLoc.x + xx[(i+1)%16], baseLoc.y + yy[(i + 1)%16])) && i != 5)
	 					if(rc.canMove(path2[(i+1)%16]))
	 						rc.move(path2[(i+1)%16]);
	 					else{
	 						if(rc.canMove(path2[(i+1)%16].rotateLeft()))
	 							rc.move(path2[(i+1)%16].rotateLeft());
	 					}
	 			}
	 		}
	 		else{
				Direction digsFrom1 = currentPos.directionTo(baseLoc);
				Direction digsFrom2 = baseLoc.directionTo(currentPos);
				if(rc.getDirtCarrying() <= 0)
				{
					//should I check if availble from other sources, this should cover it
					if(rc.canDigDirt(digsFrom1))
						rc.digDirt(digsFrom1);
					else
						rc.digDirt(digsFrom2);
				}
				else
				{
					if(rc.canDepositDirt(Direction.CENTER))
					rc.depositDirt(Direction.CENTER);
				}
	 		}
		}
    }

    static void fortifyBase1() throws GameActionException //not using this anymore
    {
    	if(dumping)
    	{
    		if(rc.getDirtCarrying() > 0)
    		{
    			Globals.rc.depositDirt(dumpingTo);
    		}
    		else
    		{
    			dumping = false;
    		}
    	}
    	else
    	{
	    	if(rc.getDirtCarrying() < 3)
	    	{
	    		for(int t = 0; t < 8; t++)
	    		{
	    			RobotInfo baseInfo = rc.senseRobotAtLocation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]));
	    			if(baseInfo.getTeam() == rc.getTeam() 
	    				&& baseInfo.getType() == RobotType.HQ)
	    			{
	    				t += 3;
	    				t %= 8;
	    				for(int i = 0; i < 3; i++)
	    				{
	    					if(rc.canDigDirt(directions[t]))
	    					{
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
	    		for(int t = 0; t < 8; t++)
	    		{
	    			RobotInfo baseInfo = rc.senseRobotAtLocation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]));
	    			if(baseID == baseInfo.ID)
	    			{
	    				t++;
	    				t %= 8; 
	    				if(rc.senseElevation(new MapLocation(currentPos.x, currentPos.y)) >= rc.senseElevation(new MapLocation(currentPos.x + x[t], currentPos.y + y[t])))
	    				{
	    					dumping = true;
	    					dumpingTo = directions[t];
	    					rc.depositDirt(dumpingTo);
	    				}
	    				else
	    				{
	    					if(rc.canMove(directions[t]))
	    						rc.move(directions[t]);
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