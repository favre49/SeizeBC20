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
	static MapLocation waterLoc ;

	static boolean shouldWall = true;
	static int wallIdx = 0;

	static int latticeDigDirsIdx = 0;
	static MapLocation waterDest;
	static boolean underAttack = false;
	
	static int wallElevation = 8;
	static int baseElevation = 5;

	static int turnsTrapped = 0;

    public static void run(RobotController rc)  throws GameActionException
    {
		FastMath.initRand(rc);

		System.out.println(opponentHQLoc);

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

		if (roundNum%broadCastFrequency == 1 && opponentHQLoc == null)
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

		// Check if we are trapped. If we have been trapped for too long let's just kill ourselves.
		turnsTrapped++;
		for (int i = 0; i < 8; i++)
		{
			if (rc.canMove(directions[i]))
			{
				turnsTrapped = 0;
				break;
			}
		}
		if (turnsTrapped > 20 && (opponentHQLoc == null))
			rc.disintegrate();
		

		// Are we being attacked???
		goToLoc = null;
		int numAttacking = 0;
		int numDefending = 0;
		if (rc.canSenseLocation(baseLoc))
		{
			// baseElevation = rc.senseElevation(baseLoc);
			wallElevation = 8;
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
		
		/*if (roundNum > 1000)
		{
			System.out.println(roundNum + " " + wallElevation);
			wallElevation = baseElevation + (roundNum-1000)/100 + 3;
		}
*/
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

		// Fill in water near the base.
		// First check if there is any water that could ruin our day.
		waterLoc = findWaterAroundBase();
		System.out.println(waterLoc);

		// If yes, fill!
		if (waterLoc != null)
		{
			if (currentPos.distanceSquaredTo(waterLoc) > 2)
			{
				navigate(waterLoc);
			}
			else
			{
				Direction fillDir = currentPos.directionTo(waterLoc);
				if (rc.getDirtCarrying() > 0)
				{
					if (rc.canDepositDirt(fillDir))
						rc.depositDirt(fillDir);
					else
						return;
				}
				else
				{
					for (int i = 0; i < 8; i++)
					{
						Direction digDir = directions[i];
						MapLocation digPos=  currentPos.add(digDir);
						if (directions[i] == fillDir)
							continue;

						// If it is flooded we can dig from it without issue (MAYBE???)
						if (rc.senseFlooding(digPos))
						{
							if (rc.canDigDirt(digDir))
							{
								rc.digDirt(digDir);
							}
							else
							{
								return;
							}
						}

						boolean canDig =!(rc.senseFlooding(digPos.add(Direction.NORTH)) ||
										rc.senseFlooding(digPos.add(Direction.NORTHWEST)) ||
										rc.senseFlooding(digPos.add(Direction.WEST)) ||
										rc.senseFlooding(digPos.add(Direction.SOUTHWEST)) ||
										rc.senseFlooding(digPos.add(Direction.SOUTH)) ||
										rc.senseFlooding(digPos.add(Direction.SOUTHEAST)) ||
										rc.senseFlooding(digPos.add(Direction.EAST)) ||
										rc.senseFlooding(digPos.add(Direction.NORTHEAST)));

						System.out.println("Can i dig at" + digPos + " " + canDig);

						if (canDig)
						{
							if (rc.canDigDirt(digDir))
							{
								rc.digDirt(digDir);
							}
						}

					}
				}

			}
			return;
		}


		if(currentPos.distanceSquaredTo(baseLoc)<=8)
			navigate(currentPos.add(latticeExpandDirs[rc.getID()%4]));
		
		/*
		Direction currentDirectionFromTheBase = baseLoc.directionTo(currentPos);
		currentDirectionFromTheBase = currentDirectionFromTheBase.rotateLeft().rotateLeft();
		while(currentDirectionFromTheBase == Direction.NORTHWEST || currentDirectionFromTheBase == Direction.NORTHEAST || currentDirectionFromTheBase == Direction.SOUTHWEST || currentDirectionFromTheBase == Direction.SOUTHEAST)
		{
			currentDirectionFromTheBase = currentDirectionFromTheBase.rotateLeft();
		}

		System.out.println(rc.getRoundNum());
		System.out.println(currentDirectionFromTheBase);
		System.out.println(currentPos);
		System.out.println(rc.getLocation());
		System.out.println(currentPos.add(currentDirectionFromTheBase));
		System.out.println(rc.senseElevation(currentPos.add(currentDirectionFromTheBase)));
		System.out.println(wallElevation);	
		if(currentPos.add(currentDirectionFromTheBase).add(currentDirectionFromTheBase).distanceSquaredTo(baseLoc) <= 18 && rc.senseElevation(currentPos.add(currentDirectionFromTheBase).add(currentDirectionFromTheBase)) < wallElevation && rc.senseElevation(currentPos.add(currentDirectionFromTheBase)) >= wallElevation && rc.senseElevation(currentPos) >= wallElevation)
		{
			System.out.println("asdfasdfasdfasdf");
			navigate(currentPos.add(currentDirectionFromTheBase));
		}
		else if(currentPos.add(currentDirectionFromTheBase).add(currentDirectionFromTheBase.rotateLeft().rotateLeft()).distanceSquaredTo(baseLoc) > 8 && rc.senseElevation(currentPos.add(currentDirectionFromTheBase).add(currentDirectionFromTheBase.rotateLeft().rotateLeft())) < wallElevation)
		{
			System.out.println("sdfgsdfgsdfgsdfg");
			navigate(currentPos.add(currentDirectionFromTheBase).add(currentDirectionFromTheBase.rotateLeft().rotateLeft()));
		}*/

		


		// Lattice time
		if (currentPos.x % 2 == (baseLoc.x) % 2 && currentPos.y % 2 == (baseLoc.y) % 2)
		{
			System.out.println("In lattice pos!");
			// If the places you dig from are waterlogged, fill them in!

			for (int i = 0; i < 5; i++)
			{
				MapLocation nextCheck = currentPos.add(latticeExpandDirs[i]);
				if (!rc.onTheMap(nextCheck))
					continue;

				if (nextCheck.distanceSquaredTo(baseLoc)<=8)
					continue;

				RobotInfo robotOccupying = rc.senseRobotAtLocation(nextCheck);
				if (!nextCheck.equals(currentPos) && robotOccupying != null && robotOccupying.team == team)
					continue;


				if (rc.senseElevation(nextCheck) < wallElevation && rc.senseElevation(nextCheck) > -100)
				{
					if (rc.getDirtCarrying() == 25)
					{
						if (rc.canDepositDirt(latticeExpandDirs[i]))
							rc.depositDirt(latticeExpandDirs[i]);
						else
							return;
					}
					else
					{
						if (!inBounds(currentPos.add(latticeDigDirs[latticeDigDirsIdx])) || currentPos.add(latticeDigDirs[latticeDigDirsIdx]).distanceSquaredTo(baseLoc) <= 8)
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
		}
		else if (currentPos.x % 2 == (baseLoc.x) % 2)
		{
			System.out.println("In lattice pos2!");
			
			int temp = 0;
			for (int i = 0; i < 9; i++)
			{
				if (directions[i] == Direction.NORTH || directions[i] == Direction.SOUTH)
					continue;
				
				MapLocation nextCheck = currentPos.add(directions[i]);
				if (!rc.onTheMap(nextCheck))
					continue;

				if (nextCheck.distanceSquaredTo(baseLoc)<=8)
					continue;

				RobotInfo robotOccupying = rc.senseRobotAtLocation(nextCheck);
				if (!nextCheck.equals(currentPos) && robotOccupying != null && robotOccupying.team == team)
					continue;
				
				if (rc.senseElevation(nextCheck) < wallElevation && rc.senseElevation(nextCheck) > -100)
				{
					if (rc.getDirtCarrying() == 25)
					{
						if (rc.canDepositDirt(directions[i]))
							rc.depositDirt(directions[i]);
						else
							return;
					}
					else
					{
						switch(temp)
						{
							case 0:
							if (rc.canDigDirt(Direction.NORTH) && currentPos.add(Direction.NORTH).distanceSquaredTo(baseLoc) > 8)
							{
								rc.digDirt(Direction.NORTH);
								temp = (temp+1)%2;
								break;
							}

							case 1:
							if (rc.canDigDirt(Direction.SOUTH) && currentPos.add(Direction.SOUTH).distanceSquaredTo(baseLoc) > 8)
							{
								rc.digDirt(Direction.SOUTH);
								temp = (temp+1)%2;
								break;
							}
						}
					}
				}
			}
		}
		else if (currentPos.y % 2 == (baseLoc.y) % 2)
		{
			System.out.println("In lattice pos3!");

			int temp = 0;
			for (int i = 0; i < 9; i++)
			{
				if (directions[i] == Direction.EAST || directions[i] == Direction.WEST)
					continue;
				
				MapLocation nextCheck = currentPos.add(directions[i]);
				if (!rc.onTheMap(nextCheck))
					continue;

				if (nextCheck.distanceSquaredTo(baseLoc)<=8)
					continue;

				RobotInfo robotOccupying = rc.senseRobotAtLocation(nextCheck);
				if (!nextCheck.equals(currentPos) && robotOccupying != null && robotOccupying.team == team)
					continue;
				
				if (rc.senseElevation(nextCheck) < wallElevation && rc.senseElevation(nextCheck) > -100)
				{
					if (rc.getDirtCarrying() ==25)
					{
						if (rc.canDepositDirt(directions[i]))
							rc.depositDirt(directions[i]);
						else
							return;
					}
					else
					{
						switch(temp)
						{
							case 0:
							if (rc.canDigDirt(Direction.EAST) && currentPos.add(Direction.EAST).distanceSquaredTo(baseLoc) > 8)
							{
								System.out.println("Digging east");
									System.out.println("really Digging east")	;
									rc.digDirt(Direction.EAST);
								temp = (temp+1)%2;
								break;
							}

							case 1:
							if (rc.canDigDirt(Direction.WEST) && currentPos.add(Direction.WEST).distanceSquaredTo(baseLoc) > 8)
							{
								System.out.println("Digging west");
									System.out.println("really Digging west")	;
									rc.digDirt(Direction.WEST);
								temp = (temp+1)%2;
								break;
							}
						}
						return;
					}
				}
			}
		}

		for(int i = 0; i < directions.length; i++)
			if(inBounds(currentPos.add(directions[i])) && rc.senseFlooding(currentPos.add(directions[i])) && rc.canSenseLocation(currentPos.add(directions[i])) && rc.senseElevation(currentPos.add(directions[i])) < wallElevation && currentPos.add(directions[i]).distanceSquaredTo(baseLoc) <= 18)
			{
				System.out.println("avocado");
				navigate(currentPos.add(directions[i]));
			}	
		
		// Choose the next place to move to.
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
		System.out.println("Finding the next place at" + exploreDest);
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
		if (opponentHQLoc != null)
		{
			exploreDest = opponentHQLoc;
		}
		else
		{	
			exploreDest = null;
			MapLocation possibleDest = null;
			// Pick the next location in the box.
			int boxWidth = Math.max(Math.abs(currentPos.x - baseLoc.x), Math.abs(currentPos.y - baseLoc.y));
			for (int i = 0; i < 4; i++)
			{
				MapLocation nextPos = currentPos.add(latticeExpandDirs[i]).add(latticeExpandDirs[i]);
				System.out.println("Checking " + nextPos);
				if (Math.abs(nextPos.x - baseLoc.x)!=boxWidth && Math.abs(nextPos.y - baseLoc.y) != boxWidth)
				{
					continue;
				}
				if (inBounds(nextPos))
				{
					possibleDest = nextPos;
				}

				if (inBounds(nextPos) && rc.senseElevation(nextPos) < wallElevation)
				{
					exploreDest = nextPos;
					break;
				}
			}
			System.out.println("Exploration destination is" + exploreDest);

			if (exploreDest == null)
			{
				exploreDest = possibleDest;
			}

			System.out.println("In the end we chose " + exploreDest);
		}
		exploredTurns = 0;

	}

	private static MapLocation findWaterAroundBase() throws GameActionException
	{
		// Search over every viable location.
		MapLocation searchPos = baseLoc.translate(0,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(0,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(0,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(1,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(1,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		
		searchPos = baseLoc.translate(-1,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		
		searchPos = baseLoc.translate(-1,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-1,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(2,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,-2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		
		searchPos = baseLoc.translate(2,-1);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		
		searchPos = baseLoc.translate(-2,-1);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		
		searchPos = baseLoc.translate(2,0);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		
		searchPos = baseLoc.translate(-2,0);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		
		searchPos = baseLoc.translate(2,1);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		
		searchPos = baseLoc.translate(-2,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(-2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		return null;

	}
}