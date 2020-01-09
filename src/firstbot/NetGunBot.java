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
    public static void run(RobotController rc) throws GameActionException
    {
    	RobotInfo nearbyUnits[] = rc.senseNearbyRobots();
    	if(nearbyUnits != null){
	    	    //shoot nearest drone
    			RobotInfo nearbyEnemyUnits[] = null;
    			if(rc.getTeam() == Team.A){
    				nearbyEnemyUnits = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.B);
    			}
    			else{
    				nearbyEnemyUnits = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), Team.A);
    			}
    			if(nearbyUnits != null){
    				int nearestDroneID = nearbyEnemyUnits[0].getID();
    				int nearestDroneDist = 64*64;
    				for(int i = 0; i < nearbyEnemyUnits.length; i++){
    					int distance = (nearbyEnemyUnits[i].getLocation()).distanceSquaredTo(rc.getLocation());
    					if(nearbyEnemyUnits[i].getType() == RobotType.DELIVERY_DRONE && distance < nearestDroneDist){
    						nearestDroneID = nearbyEnemyUnits[i].getID();
    						nearestDroneDist = distance;
    					}
    				}
    				if(rc.canShootUnit(nearestDroneID)){
    					rc.shootUnit(nearestDroneID);
    				}
    			}

	    		//communicate cows?

	    }
	}

}