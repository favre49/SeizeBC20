package landscaperrushbot;
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
	public static boolean foundWater = false;
	public static boolean foundHQ = false;
	public static boolean isExploring = true;
	public static MapLocation exploreDest;
	public static int stepSize = 5;

	public static MapLocation closestPos;
	public static boolean isProtector = false;

	// Helper drone variables.
	public static boolean finishedHQ1 = false;
	public static boolean finishedHQ2 = false;
	public static boolean finishedHQ3 = false;

	// Rush variables.
	public static int offset = 0;

	public static void run(RobotController rc) throws GameActionException
	{
		FastMath.initRand(rc);

		if (baseLoc == null)
		{
			int[][] commsarr=Communications.getComms(1);
            outerloop:
            for(int i = 0; i<commsarr.length;i++){
                for (int j = 0; j < commsarr[i].length; j++)
                {
                    ObjectLocation objectHQLocation = Communications.getLocationFromInt(commsarr[i][j]); 
                    System.out.println(objectHQLocation.rt);
                    if(objectHQLocation.rt==ObjectType.HQ)
                    {
                        baseLoc = new MapLocation(objectHQLocation.loc.x,objectHQLocation.loc.y);
                        break outerloop;
                    }
                }
            }
		}

		if (roundNum > 800)
		{
			System.out.println("I got here!!");

			// Begin drone rush!
			if (rc.getRobotCount() < 30 && opponentHQLoc == null)
			{
				findHQ();
			}
			else if (rc.getRobotCount() < 50 && opponentHQLoc == null)
			{
				if (currentPos.distanceSquaredTo(baseLoc) <= 2)
				{
					if (!rc.isCurrentlyHoldingUnit())
					{
						RobotInfo bot = rc.senseRobotAtLocation(baseLoc.add(Direction.SOUTH));
						if (bot != null && bot.type == RobotType.LANDSCAPER)
						{
							if (rc.canPickUpUnit(bot.ID))
								rc.pickUpUnit(bot.ID);
						}
					}
					else
					{
						findHQ();
					}
				}
				else
				{
					findHQ();
				}
			}
			else
			{
				System.out.println(opponentHQLoc);
				if (opponentHQLoc == null)
				{
					System.out.println("I should be looking for the opponent rn");
					findHQ();
				}
				else
				{
					// int nearbyTeamNum = rc.senseNearbyRobots(currentPos,sensorRadiusSquared, team).length;
					// System.out.println(nearbyTeamNum);
					if (roundNum > 1500)
					{
						// Attack!!!!
						if (!rc.isCurrentlyHoldingUnit())
						{
							RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
							for (int i = 0; i < nearbyBots.length; i++)
							{
								if (nearbyBots[i].type == RobotType.LANDSCAPER || nearbyBots[i].type == RobotType.MINER)
								{
									if (currentPos.distanceSquaredTo(nearbyBots[i].location) <= 2)
									{
										if (rc.canPickUpUnit(nearbyBots[i].ID))
											rc.pickUpUnit(nearbyBots[i].ID);
									}
									else
										navigate(nearbyBots[i].location);
								}
							}
						}
						else
						{
							if (currentPos.distanceSquaredTo(opponentHQLoc) < 8)
							{
								for (int i = 0; i < 8; i++)
								{
									if (!rc.senseFlooding(currentPos.add(directions[i])) && rc.canDropUnit(directions[i]))
									{
										rc.dropUnit(directions[i]);
									}
								}
							}
							else
							{
								navigate(opponentHQLoc);
							}
						}
					}
				}
			}
		}
		else
		{
			if (currentPos.distanceSquaredTo(baseLoc) <= 5 || isProtector) // Protecting drone.
			{
				isProtector = true;

				if (rc.isCurrentlyHoldingUnit())
				{
					dropInWater();
				}
				else
				{
					if (currentPos.distanceSquaredTo(baseLoc) < 15)
					{
						RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
						for (int i = 0; i < nearbyBots.length; i++)
						{
							if (nearbyBots[i].type == RobotType.LANDSCAPER || nearbyBots[i].type == RobotType.MINER)
							{
								if (currentPos.distanceSquaredTo(nearbyBots[i].location) <= 2)
								{
									if (rc.canPickUpUnit(nearbyBots[i].ID))
										rc.pickUpUnit(nearbyBots[i].ID);
								}
								else
									navigate(nearbyBots[i].location);
							}
						}
					}
					else
					{
						navigate(baseLoc);
					}
				}
			}
			else // Helper drone
			{
				System.out.println("I should be going for the miner!!");
				if (!rc.isCurrentlyHoldingUnit())
				{
					// Sense for nearby HQ.
					RobotInfo[] nearbyOpps = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
					for (int i = 0; i < nearbyOpps.length; i++)
					{
						if (nearbyOpps[i].type == RobotType.HQ)
						{
							foundHQ = true;
							break;
						}
					}

					if (!foundHQ)
					{
						RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, 5, team);
						for (int i = 0; i < nearbyBots.length; i++)
						{
							if (nearbyBots[i].type == RobotType.MINER)
							{
								if (rc.canPickUpUnit(nearbyBots[i].ID))
									rc.pickUpUnit(nearbyBots[i].ID);
								else
								{
									navigate(nearbyBots[i].location);
								}
							}
						}
					}
				}
				else
				{
					if (closestPos == null) // Find out where to carry miner.
					{
						MapLocation HQPos1 = new MapLocation(mapWidth-baseLoc.x-1, baseLoc.y);
						MapLocation HQPos2 = new MapLocation(mapWidth-baseLoc.x-1, mapHeight-baseLoc.y-1);
						MapLocation HQPos3 = new MapLocation(baseLoc.x, mapHeight-baseLoc.y-1);

						int min = Integer.MAX_VALUE;
						if (!finishedHQ1 && min > currentPos.distanceSquaredTo(HQPos2))
						{
							min = currentPos.distanceSquaredTo(HQPos1);
							closestPos = HQPos1;
						}

						if (!finishedHQ2 && min > currentPos.distanceSquaredTo(HQPos2))
						{
							min  = currentPos.distanceSquaredTo(HQPos2);
							closestPos = HQPos2;
						}

						if (!finishedHQ3 && min > currentPos.distanceSquaredTo(HQPos3))
						{
							min  = currentPos.distanceSquaredTo(HQPos3);
							closestPos = HQPos3;
						}

						if (closestPos.equals(HQPos1))
						{
							finishedHQ1 = true;
						}
						else if (closestPos.equals(HQPos2))
						{
							finishedHQ2 = true;
						}
						else if (closestPos.equals(HQPos3))
						{
							finishedHQ3 = true;
						}
					}
					else // Go where you should.
					{
						if (currentPos.distanceSquaredTo(closestPos) <= 5)
						{
							Direction dir = currentPos.directionTo(closestPos);
							do
							{
								if (rc.canDropUnit(dir) && !rc.senseFlooding(currentPos.add(dir)))
								{
									rc.dropUnit(dir);
								}
								else
								{
									dir = dir.rotateLeft();
								}
							}
							while (dir != currentPos.directionTo(closestPos));
						}
						else
							navigate(closestPos);
					}
				}
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
		if (!dest.equals(bugDest))
		{
			bugDest = dest;
			bugTracing = false;
		}

		if (dest.equals(currentPos))
			return;

		Direction nextDir = currentPos.directionTo(dest);
		// If we can move in the best direction, let's not bother bugging.
		if(!bugTracing && rc.canMove(nextDir))
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
			for(int i = 0; i < 8; ++i)
			{
				leftDir = leftDir.rotateLeft();
				if(rc.canMove(leftDir))
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
				if(rc.canMove(rightDir))
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
		if (rc.canMove(tryDir))
			bugNumTurnsWithNoWall += 1;
		else
			bugNumTurnsWithNoWall = 0;

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
			if (!inBounds(dirLoc) && !recursed)
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


	public static void dropInWater() throws GameActionException
	{
		System.out.println("Looking to drop my opponent");
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
						foundWater = true;
						break outerloop;
					}
				}
			}
			if (!foundWater)
				pickNewExploreDest();
			navigate(exploreDest);
		}
		else
		{
			if (currentPos.distanceSquaredTo(exploreDest) <= 2)
			{
				rc.dropUnit(currentPos.directionTo(exploreDest));
				foundWater = false;
			}
			else
				navigate(exploreDest);
		}
	}

	private static void pickNewExploreDest() throws GameActionException 
	{
		// Check if do while is a bad way to do this.
		boolean firsttime = true;
		do
		{
			Direction dir = directions[FastMath.rand256()%8];
			exploreDest = exploreDest.translate(dir.dx*stepSize, dir.dy*stepSize);
			if(!firsttime && !inBounds(exploreDest))
			{
				//this is the quick fix.
				exploreDest = exploreDest.translate(-1*dir.dx*stepSize, -1*dir.dy*stepSize);                
			}
			firsttime=false;
		}
		while(!inBounds(exploreDest));
	}

	private static void findHQ() throws GameActionException
	{
		System.out.println("I am looking for HQ!!!");
		switch(offset)
		{
			case 0: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, baseLoc.y);
			break;

			case 1: exploreDest = new MapLocation(mapWidth-baseLoc.x-1, mapHeight-baseLoc.y-1);
			break;

			case 2: exploreDest = new MapLocation(baseLoc.x, mapHeight-baseLoc.y-1);
			break;
		}

		if (rc.canSenseLocation(exploreDest))
		{
			System.out.println("I can sense it!");
			RobotInfo bot = rc.senseRobotAtLocation(exploreDest);
			if (bot != null && bot.type == RobotType.HQ)
				opponentHQLoc = bot.location;
			else
				offset++;
		}
		else
		{
			MapLocation netGunLoc = null;

			RobotInfo[] nearbyBots = rc.senseNearbyRobots(currentPos, sensorRadiusSquared, opponent);
			for (int i = 0; i < nearbyBots.length; i++)
			{
				if (nearbyBots[i].type == RobotType.NET_GUN)
				{
					netGunLoc = nearbyBots[i].location;
					break;
				}
			}

			if (netGunLoc != null && currentPos.distanceSquaredTo(netGunLoc) == 25)
			{
				if (rc.canMove(currentPos.directionTo(netGunLoc).rotateLeft()))
				{
					rc.move(currentPos.directionTo(netGunLoc).rotateLeft());
				}
				else if (rc.canMove(currentPos.directionTo(netGunLoc).rotateLeft()))
				{
					rc.move(currentPos.directionTo(netGunLoc).rotateLeft());
				}
				else
				{
					rc.move(currentPos.directionTo(netGunLoc).rotateLeft().rotateLeft());
				}
			}

			if (currentPos.distanceSquaredTo(exploreDest) == 25)
			{
				if (rc.canMove(currentPos.directionTo(exploreDest).rotateLeft()))
				{
					rc.move(currentPos.directionTo(exploreDest).rotateLeft());
				}
				else if (rc.canMove(currentPos.directionTo(exploreDest).rotateLeft()))
				{
					rc.move(currentPos.directionTo(exploreDest).rotateLeft());
				}
				else
				{
					rc.move(currentPos.directionTo(exploreDest).rotateLeft().rotateLeft());
				}
			}
			else
			{
				navigate(exploreDest);
			}
		}
	}
}