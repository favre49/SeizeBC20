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

    Direction dumpingTo = Direction.NORTH;
    boolean dumping = false;

    public static void run(RobotController rc)
    {
    	
    	fortifyBase();
    }

    static void fortifyBase(){
		short x[8] = {-1, 0, 1, 1, 1, 0, -1, -1};
	    short y[8] = {-1, -1, -1, 0, 1, 1, 1, 0};
	    Direction directions[8] = {Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST};
    	if(dumping){
    		if(rc.getDirtCarrying()){
    			rc.depositDirection(dumpingTo);
    		}
    		else{
    			dumping = false;
    		}
    	}
    	else{
	    	if(rc.getDirtCarrying() < 3){
	    		for(int t = 0; t < 8; t++){
	    			RobotInfo baseInfo = rc.senseRobot(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]))
	    			if(baseID == baseInfo.ID){
	    				t += 3;
	    				t %= 8;
	    				for(int i = 0; i < 3; i++;){
	    					if(rc.canDigDirt(directions[t])){
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
	    		for(int t = 0; t < 8; t++){
	    			RobotInfo baseInfo = rc.senseRobot(new MapLocation(currentPos.x + x[t], currentPos.y + y[t]))
	    			if(baseID == baseInfo.ID){
	    				t++;
	    				t %/= 8; 
	    				if(rc.senseElevation(new MapLocation(currentPos.x, currentPos.y) >= rc.senseElevation(currentPos.x + x[t], currentPos.y + y[t]))){
	    					dumping = true;
	    					dumpingTo = directions[t];
	    					rc.depositDirection(dumpingTo);
	    				}
	    				else{
	    					if(canMove(directions[t])){
	    						move(direcitons[t]);
	    					}
	    				}
	    				break;
	    			}
	    		}
    		}
    	}
    }

}