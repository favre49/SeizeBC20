package firstbot;
import battlecode.common.*;

public class Globals
{
    public static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    public static int mapWidth;
    public static int mapHeight;

    public static int roundNum;

    public static RobotController rc;
    public static MapLocation currentPos;
    public static Team team;
    public static Team opponent;
    public static int myID;
    public static RobotType myType;
    public static int cost;
    public static int sensorRadiusSquared;
    public static float baseCooldown;
    public static int health;

    public static void init(RobotController givenrc)
    {
        rc = givenrc;
        roundNum = rc.getRoundNum();
        currentPos = rc.getLocation();
        team = rc.getTeam();
        opponent = team.opponent();
        myID = rc.getID();
        myType = rc.getType();
        cost = myType.cost;
        sensorRadiusSquared = myType.sensorRadiusSquared;
        baseCooldown = myType.actionCooldown;

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
    }

    public static void update()
    {
        currentPos = rc.getLocation();
        roundNum = rc.getRoundNum();
        sensorRadiusSquared = rc.getCurrentSensorRadiusSquared();
    }

    public static boolean inBounds(MapLocation loc)
    {
        return loc.x >= 0 && loc.y >= 0 && loc.x < mapWidth && loc.y < mapHeight; 
    }
}