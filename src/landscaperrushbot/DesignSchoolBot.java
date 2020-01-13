package landscaperrushbot;

import battlecode.common.*;

/**
 * Built by: Miners
 * Cost: 150
 * Health: 15
 * Sensor radius: 24
 * Produces: Landscapers
 */
public strictfp class DesignSchoolBot extends Globals
{	
	public static int numLandscapers = 0;

	public static void run(RobotController rc) throws GameActionException
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


		if (currentPos.distanceSquaredTo(baseLoc) > 2) // Attacking landscapers
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

			if (opponentHQLoc != null && numLandscapers < 2)
			{
				for (int i = 0; i < 8; i++)
				{
					Direction dir = directions[i];
					if (currentPos.add(dir).distanceSquaredTo(opponentHQLoc) <=2 )
					{
						if (rc.canBuildRobot(RobotType.LANDSCAPER, dir))
						{
							buildLandscaper(dir);
						}
					}
				}
			}
		}
		else // Protecting landscapers
		{
			if (numLandscapers < 6)
			{
				for (int i = 0; i < 8; i++)
				{
					Direction dir = directions[i];
					if (currentPos.add(dir).distanceSquaredTo(baseLoc) <=2 )
					{
						if (rc.canBuildRobot(RobotType.LANDSCAPER, dir))
						{
							System.out.println("I'm not building tho I should");
							buildLandscaper(dir);
						}
					}
				}
			}
			else if (roundNum > 800)
			{
				RobotInfo bot = rc.senseRobotAtLocation(currentPos.add(Direction.SOUTHWEST));
				if (bot == null)
				{
					if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.SOUTHWEST))
					{
						buildLandscaper(Direction.SOUTHWEST);
					}
				}
			}
			else
			{
				RobotInfo bot = rc.senseRobotAtLocation(currentPos.add(Direction.SOUTH));
				if (bot == null)
				{
					if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.SOUTH))
					{
						buildLandscaper(Direction.SOUTH);
					}
				}
				else if (bot.team == opponent)
				{
					// Time to panic!!!
				}
			}
		}
    }

    static void buildLandscaper(Direction dir) throws GameActionException
    {
		numLandscapers++;
    	rc.buildRobot(RobotType.LANDSCAPER, dir);
    }

	
}