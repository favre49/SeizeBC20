package firstbot;
import battlecode.common.*;

/**
 * Health: 50
 * Sensor Radius: 48
 * Produces: Miners
 * Has a built in gun and refinery.
 */
public strictfp class HQBot
{
    private static Direction[] directions = Direction.allDirections();
    private static RobotController rc = null;

    public static void run(RobotController rc) throws GameActionException
    {
        HQBot.rc = rc;

        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
    }

    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) 
        {
            rc.buildRobot(type, dir);
            return true;
        } 
        else return false;
    }
}