package firstbot;
import battlecode.common.*;

/**
 * BUilt by: Miners
 * Cost: 250
 * Health: 15
 * Sensor Radius: 24
 *
 * Destroys drones in a 15 attack radius!
 */
public strictfp class NetGunBot extends Globals
{
    public static void run(RobotController rc)
    {
    	RobotInfo nearbyUnits = rc.senseNearbyRobots();
    	if(nearbyDrones.length() > 0){
	    	    //shoot nearest drone
    			if(rc.getTeam() == Team.A){
    				RobotInfo nearbyEnemyUnits = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.B);
    			}
    			else{
    			RobotInfo nearbyEnemyUnits = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.A);
    			}
    			if(nearbyUnits.length() > 0){
    				int nearestDroneID = nearbyEnemyUnits[0].ID;
    				int nearestDroneDist = 64*64;
    				for(int i = 0; i < nearbyEnemyUnits.length()){
    					if(nearbyEnemyUnits[i].RobotType == RobotType.DELIVERY_DRONE && nearbyEnemyUnits[]){
    						nears
    					}
    				}
    			}

	    		//communicate cows?
	    }
	}

}