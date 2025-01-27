package commsMinerBot;
import battlecode.common.*;

public class Globals
{
//declarations
    public static short x[] = {0, 1, 1, 1, 0, -1, -1, -1};
    public static short y[] = {1, 1, 0, -1, -1, -1, 0, 1};
    public static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    //Direction path[] = {DIrection.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH};
    
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

    // public static ObjectLocation[] objectArray = new ObjectLocation[12];
    // public static int objectArraySize = 0;

    public static final int broadCastFrequency = 5;

    enum ObjectType{
        COW,DELIVERY_DRONE,DESIGN_SCHOOL,FULFILLMENT_CENTER,HQ,LANDSCAPER,MINER,NET_GUN,REFINERY,VAPORATOR,SOUP,WATER,TO_BE_REFINERY, NO_SOUP;
    }

    public static ObjectType myObjectType;

    public static int baseID;
    public static MapLocation baseLoc;

    public static int opponentHQID;
    public static MapLocation opponentHQLoc;

//public static void updateObjectType()
    public static void updateObjectType()
    {
        switch(myType){
            case COW:
                myObjectType=ObjectType.COW;
                break;
            case DELIVERY_DRONE:
                myObjectType=ObjectType.DELIVERY_DRONE;
                break;
            case DESIGN_SCHOOL:
                myObjectType=ObjectType.DESIGN_SCHOOL;
                break;
            case FULFILLMENT_CENTER:
                myObjectType=ObjectType.FULFILLMENT_CENTER;
                break;
            case HQ:
                myObjectType=ObjectType.HQ;
                break;
            case LANDSCAPER:
                myObjectType=ObjectType.LANDSCAPER;
                break;
            case MINER:
                myObjectType=ObjectType.MINER;
                break;
            case NET_GUN:
                myObjectType=ObjectType.NET_GUN;
                break;
            case REFINERY:
                myObjectType=ObjectType.REFINERY;
                break;
            case VAPORATOR:
                myObjectType=ObjectType.VAPORATOR;
                break;
            default:
                myObjectType=ObjectType.COW;
                break;
        }

    }

//public static void init(RobotController givenrc) throws GameActionException
    public static void init(RobotController givenrc) throws GameActionException
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

        //add HQ pos update
        if(myType != RobotType.HQ){
        }

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        updateObjectType();
    }
//public static void update()
    public static void update()
    {
        currentPos = rc.getLocation();
        roundNum = rc.getRoundNum();
        sensorRadiusSquared = rc.getCurrentSensorRadiusSquared();
    }
//public static boolean inBounds(MapLocation loc)
    public static boolean inBounds(MapLocation loc)
    {
        return loc.x >= 0 && loc.y >= 0 && loc.x < mapWidth && loc.y < mapHeight; 
    }

//public static MapLocation senseNearbySoup() throws GameActionException
    // Sees in sensor location for nearby soup. Takes up like 200 bytecodes.
    public static MapLocation senseNearbySoup() throws GameActionException
    {
        if (rc.senseSoup(currentPos)>0)
            return currentPos;
        
        MapLocation[] nearbySoup = rc.senseNearbySoup(currentPos, sensorRadiusSquared);
        MapLocation minLoc = null;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < nearbySoup.length; i++)
        {
            if (currentPos.distanceSquaredTo(nearbySoup[i]) < minDist)
            {
                minDist = currentPos.distanceSquaredTo(nearbySoup[i]);
                minLoc = nearbySoup[i];
            }
        }

        return minLoc;
    }
//public static boolean isEnemyBuildingAtLocation(MapLocation jaja) throws GameActionException
    public static boolean isEnemyBuildingAtLocation(MapLocation jaja) throws GameActionException
    {
        RobotInfo temp = rc.senseRobotAtLocation(jaja);
        if(temp != null)    
        {
            RobotType t = temp.getType();
            if(temp.getTeam() != team && (t == RobotType.NET_GUN || t == RobotType.DESIGN_SCHOOL || t == RobotType.VAPORATOR || t == RobotType.FULFILLMENT_CENTER))
                return true;
        }
        return false;        
    }
}