package firstbot;
import battlecode.common.*;

public strictfp class RobotPlayer extends Globals{

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Globals.init(rc);

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                Globals.update();
                
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 HQBot.run(rc);                break;
                    case MINER:              MinerBot.run(rc);             break;
                    case REFINERY:           RefineryBot.run(rc);          break;
                    case VAPORATOR:          VaporatorBot.run(rc);         break;
                    case DESIGN_SCHOOL:      DesignSchoolBot.run(rc);      break;
                    case FULFILLMENT_CENTER: FulfillmentCenterBot.run(rc); break;
                    case LANDSCAPER:         LandscaperBot.run(rc);        break;
                    case DELIVERY_DRONE:     DroneBot.run(rc);             break;
                    case NET_GUN:            NetGunBot.run(rc);            break;
                }


                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    
}
