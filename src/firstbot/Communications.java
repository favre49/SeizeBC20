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


	public static int[] decode(Transaction messageTransaction){
		int[] decoded = new int[12];

		int[] message = messageTransaction.getMessage();

		message[0]^=-903849746;
		message[1]^=-172817894;
		message[2]^=293009196;
		message[3]^=25721274;
		message[4]^=205865773;
		message[5]^=-1561017189;
		message[6]^=2092647876;

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

	public static int[][] getComs(int roundno){
		

		Transaction[] theBlock = rc.getBlock(roundno);

		int[][] interpreted = new int[theBlock.length][12];

		for(int i=0;i<theBlock.length;i++){
			interpreted[i]=decode(theBlock[i]);
		}

		return interpreted;

	}

}

