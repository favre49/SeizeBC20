package neobot;

import battlecode.common.*;

public class Globals
{

    public static short x[] = {0, 1, 1, 1, 0, -1, -1, -1};
    public static short y[] = {1, 1, 0, -1, -1, -1, 0, 1};
    public static Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.CENTER};
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

    public static void updateObjectType(){
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

    // public static RobotLocation aRobotLocation;

    public static int baseID;
    public static MapLocation baseLoc;

    public static int opponentHQID;
    public static MapLocation opponentHQLoc;
    //TODO add base location and base id

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

    public static MapLocation findWaterAroundBase() throws GameActionException
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

		searchPos = baseLoc.translate(-2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		
		searchPos = baseLoc.translate(2,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(3,-3);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		searchPos = baseLoc.translate(-3,-3);
        if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
                return searchPos;
		searchPos = baseLoc.translate(3,-2);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(-3,-2);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(3,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-3,-1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;

		searchPos = baseLoc.translate(3,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-3,0);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;

		searchPos = baseLoc.translate(3,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-3,1);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		searchPos = baseLoc.translate(3,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;

		searchPos = baseLoc.translate(-3,2);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;
		searchPos = baseLoc.translate(3,3);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		searchPos = baseLoc.translate(-3,3);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
				return searchPos;

		searchPos = baseLoc.translate(-3,3);
		if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
			return searchPos;
		searchPos = baseLoc.translate(-3,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(-2,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(-2,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(-1,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(-1,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(0,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(0,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(1,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(1,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(2,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(2,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;
		searchPos = baseLoc.translate(3,3);
				if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
					return searchPos;
		searchPos = baseLoc.translate(3,-3);
						if (rc.canSenseLocation(searchPos) && rc.senseFlooding(searchPos))
								return searchPos;


		return null;

	}

}