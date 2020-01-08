package firstbot;
import battlecode.common.*;

public strictfp class Communication extends Globals{

	public static boolean sendComs(int[] data, int bidamount/*message, bid amount*/) throws GameActionException {
		int[] message = new int[7];

		/*
		The XOR values for encryption/decryption
		-903849746
		-172817894
		293009196
		25721274
		205865773
		-1561017189
		2092647876
		*/

		//first 8 bits for key
		//the 8 bit key is 38

		message[0]|=(38<<24);

		//first 8 bits of message is reserved

		//224-8=216 bits remaining
		//each message stores 12 packets of 18 bits each and one packet of 12 bits

		//one bit for whether actual data or key data stored

		for(int i=0;i<216;i++){
			int whichdata = i/18;
			int whichbit = i%18;

			int sourcebit = (data[whichdata]&(1<<(17-whichbit)))?1:0;

			int targetdata = (i+9)/32;
			int targetbit = (i+9)%32;

			message[targetdata]|=sourcebit<<(31-targetbit);
		}

		message[0]^=-903849746;
		message[1]^=-172817894;
		message[2]^=293009196;
		message[3]^=25721274;
		message[4]^=205865773;
		message[5]^=-1561017189;
		message[6]^=2092647876;

		if (rc.canSubmitTransaction(message, bidamount))
			rc.submitTransaction(message, bidamount);
	}


	public static int[] decode(Transaction messageTransaction) throws GameActionException{
		int[] decoded = new int[12];

		int[] message = messageTransaction.getMessage();

		message[0]^=-903849746;
		message[1]^=-172817894;
		message[2]^=293009196;
		message[3]^=25721274;
		message[4]^=205865773;
		message[5]^=-1561017189;
		message[6]^=2092647876;

		if((message[0]>>24)&255 != 38){
			//it was not our message
			return decoded;
		}

		for(int i=0;i<216;i++){
			int whichdata = i/18;
			int whichbit = i%18;

			int messagedata = (i+9)/32;
			int messagebit = (i+9)%32;

			int sourcebit = (message[messagedata]&(1<<31-messagebit))?1:0;
			decoded[whichdata]|=sourcebit<<(17-whichbit);
		}

		return decoded;
	}

	public static int[][] getComs(int roundno) throws GameActionException{


		Transaction[] theBlock = rc.getBlock(roundno);

		int[][] interpreted = new int[theBlock.length][12];

		for(int i=0;i<theBlock.length;i++){
			interpreted[i]=decode(theBlock[i]);
		}

		return interpreted;

	}

	//our HQ
	//opponent HQ
	//soup
	//refinery

	public static int getCommsNum(RobotType theType, MapLocation theloc){
		int theint = 0;
		int theinttype;
		switch(theType){
			case COW:
				theinttype=1;
				break;
			case DELIVERY_DRONE:
				theinttype=2;
				break;
			case DESIGN_SCHOOL:
				theinttype=3;
				break;
			case FULFILMENT_CENTER:
				theinttype=4;
				break;
			case HQ:
				theinttype=5;
				break;
			case LANDSCAPER:
				theinttype=6;
				break;
			case MINER:
				theinttype=7;
				break;
			case NET_GUN:
				theinttype=8;
				break;
			case REFINERY:
				theinttype=9;
				break;
			case VAPORATOR:
				theinttype=10;
				break;
			default:
				theinttype=0;
				break;
		}

		theint|=theinttype<<11;
		theint|=theloc.x<<5;
		theint|=theloc.y;
		return theint;
	}


	public static RobotLocation getLocationFromInt(int x){
		RobotType theRobot;
		MapLocation theLocation;

		theLocation.y=x&&63;
		x=x>>5;
		theLocation.x=x&&63;
		x=x>>5;
		x&=63;
		switch(x){
			case 1:
				theRobot=RobotType.COW;
				break;
			case 2:
				theRobot=RobotType.DELIVERY_DRONE;
				break;
			case 3://DESIGN_SCHOOL:
				theinttype=RobotType.DESIGN_SCHOOL;
				break;
			case 4://FULFILMENT_CENTER:
				theinttype=RobotType.FULFILMENT_CENTER;
				break;
			case 5://HQ:
				theinttype=RobotType.HQ;
				break;
			case 6://LANDSCAPER:
				theinttype=RobotType.LANDSCAPER;
				break;
			case 7://MINER:
				theinttype=RobotType.MINER;
				break;
			case 8://NET_GUN:
				theinttype=RobotType.NET_GUN;
				break;
			case 9://REFINERY:
				theinttype=RobotType.REFINERY;
				break;
			case 10://VAPORATOR:
				theinttype=RobotType.VAPORATOR;
				break;
			default:
				theinttype=0;
				break;			
		}

		RobotLocation theRobotLoc = new RobotLocation(theRobot,theLocation);
		return theRobotLoc;
	}
}

