package firstbot;
import battlecode.common.*;

public strictfp class Communications extends Globals{

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

			int sourcebit = ((data[whichdata]&(1<<(17-whichbit)))>0)?1:0;

			int targetdata = (i+8)/32;
			int targetbit = (i+8)%32;

			message[targetdata]|=sourcebit<<(31-targetbit);
		}

		message[0]^=-903849746;
		message[1]^=-172817894;
		message[2]^=293009196;
		message[3]^=25721274;
		message[4]^=205865773;
		message[5]^=-1561017189;
		message[6]^=2092647876;

		if (rc.canSubmitTransaction(message, bidamount)){
			rc.submitTransaction(message, bidamount);
			return true;
		}
		else{
			return false;
		}

	}


	public static int[] decode(Transaction messageTransaction) throws GameActionException{
		int[] decoded = new int[12];

		int[] message = messageTransaction.getMessage();

		// System.out.println(message[0]);

		message[0]^=-903849746;
		message[1]^=-172817894;
		message[2]^=293009196;
		message[3]^=25721274;
		message[4]^=205865773;
		message[5]^=-1561017189;
		message[6]^=2092647876;

		if(((message[0]>>24)&255) != 38){
			//it was not our message
			// System.out.println("NOT OUR MESSAGE");
			return decoded;
		}

		for(int i=0;i<216;i++){
			int whichdata = i/18;
			int whichbit = i%18;

			int messagedata = (i+8)/32;
			int messagebit = (i+8)%32;

			int sourcebit = ((message[messagedata]&(1<<31-messagebit))>0)?1:0;
			decoded[whichdata]|=sourcebit<<(17-whichbit);
		}


		return decoded;
	}



	public static int[][] getComms(int roundno) throws GameActionException{

		Transaction[] theBlock = rc.getBlock(roundno);
		int[][] interpreted = new int[theBlock.length][12];

		for(int i=0;i<theBlock.length;i++){
			interpreted[i]=decode(theBlock[i]);
		}

		return interpreted;
	}

	public static int[][] getLastIntervalComms() throws GameActionException{

		int roundToQuery = roundNum-roundNum%broadCastFrequency;
		if(roundToQuery==0)roundToQuery=1;

		Transaction[] theBlock = rc.getBlock(roundToQuery);
		int[][] interpreted = new int[theBlock.length][12];

		for(int i=0;i<theBlock.length;i++){
			interpreted[i]=decode(theBlock[i]);
		}

		return interpreted;
	}


	public static int getCommsNum(ObjectType theType, MapLocation theloc){
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
			case FULFILLMENT_CENTER:
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
			case SOUP:
				theinttype=11;
				break;
			case WATER:
				theinttype=12;
				break;
			default:
				theinttype=0;
				break;
		}

 		theint|=theinttype<<12;
		theint|=theloc.x<<6;
		theint|=theloc.y;

		// System.out.println(theinttype);
		// System.out.println(theinttype<<12);
		// System.out.println(theloc.x);
		// System.out.println(theloc.x<<6);
		// System.out.println(theloc.y);
		// System.out.println(theloc.y);


		return theint;
	}


	public static ObjectLocation getLocationFromInt(int x){
		ObjectType theRobot = ObjectType.COW;

		int a,b;
		a=x&63;
		x=x>>6;
		b=x&63;
		x=x>>6;
		x&=63;
		MapLocation theLocation = new MapLocation(a,b);
		switch(x){
			case 1:
				theRobot=ObjectType.COW;
				break;
			case 2:
				theRobot=ObjectType.DELIVERY_DRONE;
				break;
			case 3://DESIGN_SCHOOL:
				theRobot=ObjectType.DESIGN_SCHOOL;
				break;
			case 4://FULFILMENT_CENTER:
				theRobot=ObjectType.FULFILLMENT_CENTER;
				break;
			case 5://HQ:
				theRobot=ObjectType.HQ;
				break;
			case 6://LANDSCAPER:
				theRobot=ObjectType.LANDSCAPER;
				break;
			case 7://MINER:
				theRobot=ObjectType.MINER;
				break;
			case 8://NET_GUN:
				theRobot=ObjectType.NET_GUN;
				break;
			case 9://REFINERY:
				theRobot=ObjectType.REFINERY;
				break;
			case 10://VAPORATOR:
				theRobot=ObjectType.VAPORATOR;
				break;
			case 11:
				theRobot=ObjectType.SOUP;
				break;
			case 12:
				theRobot=ObjectType.WATER;
				break;
			default:
				// theRobot=0;
				break;
		}

		return new ObjectLocation(theRobot,theLocation);

		// /*RobotLocation theRobotLoc =*/return new RobotLocation(theRobot,theLocation);
		// theRobotLoc.rt = theRobot;
		// theRobotLoc.loc = theLocation;
		// return theRobotLoc;
	}
}



/*

		Code for testing comms.

    	if(Globals.roundNum<10){
	    	int arr[] = new int[12];
	    	arr[0] = arr[1] = arr[2] = (getCommsNum(Globals.myType,Globals.currentPos));
	    	System.out.println(sendComs(arr,10));
		}
		else{
			int[][] newarr = getComms(5);
			System.out.println(newarr.length);
			System.out.println(newarr[0].length);
			for(int i=0;i<newarr.length;i++){
				System.out.println(newarr[i][0]);
				System.out.println(getLocationFromInt(newarr[i][0]).rt);
				System.out.println(getLocationFromInt(newarr[i][0]).loc.x);
				System.out.println(getLocationFromInt(newarr[i][0]).loc.y);

				System.out.println(newarr[i][1]);
				System.out.println(getLocationFromInt(newarr[i][1]).rt);
				System.out.println(getLocationFromInt(newarr[i][1]).loc.x);
				System.out.println(getLocationFromInt(newarr[i][1]).loc.y);

				System.out.println(newarr[i][2]);
				System.out.println(getLocationFromInt(newarr[i][2]).rt);
				System.out.println(getLocationFromInt(newarr[i][2]).loc.x);
				System.out.println(getLocationFromInt(newarr[i][2]).loc.y);
			}
		}   
*/
