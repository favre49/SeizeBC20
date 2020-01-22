package neobot;
import battlecode.common.*;

public strictfp class Communications extends Globals
{
	public static int hamming18to24(int data)
	{
		/*https://www.xilinx.com/support/documentation/application_notes/xapp383.pdf*/

		//data is now an integer where only the last 18 bits are relevant
		int newData = 0;

		//putting data in newData
		newData |= ((data&(1<<0))<<2);
		newData |= ((data&(1<<1))<<3);
		newData |= ((data&(1<<2))<<3);
		newData |= ((data&(1<<3))<<3);
		newData |= ((data&(1<<4))<<4);
		newData |= ((data&(1<<5))<<4);
		newData |= ((data&(1<<6))<<4);
		newData |= ((data&(1<<7))<<4);
		newData |= ((data&(1<<8))<<4);
		newData |= ((data&(1<<9))<<4);
		newData |= ((data&(1<<10))<<4);
		newData |= ((data&(1<<11))<<5);
		newData |= ((data&(1<<12))<<5);
		newData |= ((data&(1<<13))<<5);
		newData |= ((data&(1<<14))<<5);
		newData |= ((data&(1<<15))<<5);
		newData |= ((data&(1<<16))<<5);
		newData |= ((data&(1<<17))<<5);

		//building parity bits
		int r1 = ((newData>>2)&1)^((newData>>4)&1)^((newData>>6)&1)^((newData>>8)&1)^((newData>>10)&1)^((newData>>12)&1)^((newData>>14)&1)^((newData>>16)&1)^((newData>>18)&1)^((newData>>20)&1)^((newData>>22)&1);
		int r2 = ((newData>>2)&1)^((newData>>5)&1)^((newData>>6)&1)^((newData>>9)&1)^((newData>>10)&1)^((newData>>13)&1)^((newData>>14)&1)^((newData>>17)&1)^((newData>>18)&1)^((newData>>21)&1)^((newData>>22)&1);
		int r3 = ((newData>>4)&1)^((newData>>5)&1)^((newData>>6)&1)^((newData>>11)&1)^((newData>>12)&1)^((newData>>13)&1)^((newData>>14)&1)^((newData>>19)&1)^((newData>>20)&1)^((newData>>21)&1)^((newData>>22)&1);
		int r4 = ((newData>>8)&1)^((newData>>9)&1)^((newData>>10)&1)^((newData>>11)&1)^((newData>>12)&1)^((newData>>13)&1)^((newData>>14)&1);
		int r5 = ((newData>>16)&1)^((newData>>17)&1)^((newData>>18)&1)^((newData>>19)&1)^((newData>>20)&1)^((newData>>21)&1)^((newData>>22)&1);

		newData |= r1;
		newData |= r2<<1;
		newData |= r3<<3;
		newData |= r4<<7;
		newData |= r5<<15;

		int finalparity = ((newData>>0)&1)^((newData>>1)&1)^((newData>>2)&1)^((newData>>3)&1)^((newData>>4)&1)^((newData>>5)&1)^((newData>>6)&1)^((newData>>7)&1)^((newData>>8)&1)^((newData>>9)&1)^((newData>>10)&1)^((newData>>11)&1)^((newData>>12)&1)^((newData>>13)&1)^((newData>>14)&1)^((newData>>15)&1)^((newData>>16)&1)^((newData>>17)&1)^((newData>>18)&1)^((newData>>19)&1)^((newData>>20)&1)^((newData>>21)&1)^((newData>>22)&1);

		//adding parity bits to newData

		newData|=finalparity<<23;
		return newData;
	}

	// ERROR IS SOMETHWERE HERE

	public static boolean isCorrect(int data)
	{
		int r1 = ((data>>2)&1)^((data>>4)&1)^((data>>6)&1)^((data>>8)&1)^((data>>10)&1)^((data>>12)&1)^((data>>14)&1)^((data>>16)&1)^((data>>18)&1)^((data>>20)&1)^((data>>22)&1);
		int r2 = ((data>>2)&1)^((data>>5)&1)^((data>>6)&1)^((data>>9)&1)^((data>>10)&1)^((data>>13)&1)^((data>>14)&1)^((data>>17)&1)^((data>>18)&1)^((data>>21)&1)^((data>>22)&1);
		int r3 = ((data>>4)&1)^((data>>5)&1)^((data>>6)&1)^((data>>11)&1)^((data>>12)&1)^((data>>13)&1)^((data>>14)&1)^((data>>19)&1)^((data>>20)&1)^((data>>21)&1)^((data>>22)&1);
		int r4 = ((data>>8)&1)^((data>>9)&1)^((data>>10)&1)^((data>>11)&1)^((data>>12)&1)^((data>>13)&1)^((data>>14)&1);
		int r5 = ((data>>16)&1)^((data>>17)&1)^((data>>18)&1)^((data>>19)&1)^((data>>20)&1)^((data>>21)&1)^((data>>22)&1);

		int gp = ((data>>0)&1)^((data>>1)&1)^((data>>2)&1)^((data>>3)&1)^((data>>4)&1)^((data>>5)&1)^((data>>6)&1)^((data>>7)&1)^((data>>8)&1)^((data>>9)&1)^((data>>10)&1)^((data>>11)&1)^((data>>12)&1)^((data>>13)&1)^((data>>14)&1)^((data>>15)&1)^((data>>16)&1)^((data>>17)&1)^((data>>18)&1)^((data>>19)&1)^((data>>20)&1)^((data>>21)&1)^((data>>22)&1);

		int syndrome = (r1<<0)^(r2<<1)^(r3<<2)^(r4<<3)^(r5<<4)^(data&1)^(((data>>1)&1)<<1)^(((data>>3)&1)<<2)^(((data>>7)&1)<<3)^(((data>>15)&1)<<4);

		if(syndrome == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static int unhamming24to18(int data)
	{
		int newData=0;
		newData |= ((data&(1<<2))>>2);
		newData |= ((data&(1<<4))>>3);
		newData |= ((data&(1<<5))>>3);
		newData |= ((data&(1<<6))>>3);
		newData |= ((data&(1<<8))>>4);
		newData |= ((data&(1<<9))>>4);
		newData |= ((data&(1<<10))>>4);
		newData |= ((data&(1<<11))>>4);
		newData |= ((data&(1<<12))>>4);
		newData |= ((data&(1<<13))>>4);
		newData |= ((data&(1<<14))>>4);
		newData |= ((data&(1<<16))>>5);
		newData |= ((data&(1<<17))>>5);
		newData |= ((data&(1<<18))>>5);
		newData |= ((data&(1<<19))>>5);
		newData |= ((data&(1<<20))>>5);
		newData |= ((data&(1<<21))>>5);
		newData |= ((data&(1<<22))>>5);

		return newData;
	}

	public static int[] encodeHamming(int[] data)
	{
		//takes int[9], adds hamming to each, then given int
		int[] hammingData = new int[9];
		for(int i = 0; i < 9; i++)
		{
			hammingData[i] = hamming18to24(data[i]);
		}

		int[] message = new int[7];
		message[0] |= (32 << 24);
		
		for(int i = 0; i < 216; i++)
		{
			int whichdata = i/24;
			int whichbit = i%24;

			int sourcebit = ((hammingData[whichdata] & (1 << (23-whichbit))) > 0) ? 1 : 0;

			int targetdata = (i+8)/32;
			int targetbit = (i+8)%32;

			message[targetdata] |= sourcebit << (31-targetbit);
		}

		message[0] ^= -903849746;
		message[1] ^= -172817894;
		message[2] ^= 293009196;
		message[3] ^= 25721274;
		message[4] ^= 205865773;
		message[5] ^= -1561017189;
		message[6] ^= 2092647876;
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
		return message;
	}

	public static int[] decodeHamming(Transaction messageTransaction) throws GameActionException
	{
		int[] decoded = new int[9];

		int[] message = messageTransaction.getMessage();

		if (message.length != 7)
		{
			System.out.println("NOT OUR MESSAGE");
			return decoded;
		}

		message[0] ^= -903849746;
		message[1] ^= -172817894;
		message[2] ^= 293009196;
		message[3] ^= 25721274;
		message[4] ^= 205865773;
		message[5] ^= -1561017189;
		message[6] ^= 2092647876;

		// System.out.println(message[0]);

		if(((message[0]>>24) & 255) != 32)
		{
			System.out.println("NOT OUR MESSAGE");
			return decoded;
		}

		for(int i = 0; i < 216; i++)
		{
			int whichdata = i/24;
			int whichbit = i%24;

			int messagedata = (i+8)/32;
			int messagebit = (i+8)%32;

			int sourcebit = ((message[messagedata] & (1 << 31-messagebit)) > 0) ? 1 : 0;
			decoded[whichdata] |= sourcebit << (23-whichbit);
		}

		int[] hammingChecked = new int[9];
		for(int i = 0; i < 9; i++)
		{
			if(isCorrect(decoded[i]))
				hammingChecked[i]=unhamming24to18(decoded[i]);
		}

		return hammingChecked;
	}

	static int getHashSimple(int[] data, int n)
	{
		int hash = 0;
		for(int i = 0; i < n; ++i)
		{
			hash += 31 * data[i];
		}
		return hash;
	}

	static int getHash2(int[] data, int n)
	{
		int hash = 0;
		for (int i = 0; i < n; ++i)
		{
			hash += data[i];
			hash += (hash << 10);
			hash ^= (hash >> 6);
		}
		hash += (hash << 3);
		hash ^= (hash >> 11);
		hash += (hash << 15);
		
		return hash;
	}

	static int getHash(int[] data, int n)
	{
		return getHash2(data, n);
	}

	public static int[] encodeHash(int[] data)
	{
		int[] message = new int[7];
		for(int i = 0; i < 6; ++i)
		{
			message[i] = data[i];
		}

		message[0] |= (data[6] << 14) & 0xFFFC0000; //14 bits
		message[1] |= (data[6] << 28) & 0xF0000000;	//4 bits

		message[2] |= (data[7] << 14) & 0xFFFC0000; //10 bits
		message[3] |= (data[7] << 28) & 0xF0000000; //4 bits

		message[4] |= (data[8] << 14) & 0xFFFC0000; //10 bits
		message[5] |= (data[8] << 28) & 0xF0000000; //4 bits

		message[6] |= getHash(message, 6);

		return message;
	}

	public static int[] decodeHash(Transaction messageTransaction) throws GameActionException
	{
		int[] decoded = new int[9];

		int[] message = messageTransaction.getMessage();

		if (message.length != 7 || message[6] != getHash(message,6))
		{
			System.out.println("NOT OUR MESSAGE");
			return decoded;
		}

		//TODO
		for(int i = 0; i < 7; ++i)
		{
			decoded[i] = message[i] & 0x0003FFFF;
		}

		decoded[6] = ((message[0] >> 14) & 0x0003FFF0) | (message[1] >> 28);
		decoded[7] = ((message[2] >> 14) & 0x0003FFF0) | (message[3] >> 28);
		decoded[8] = ((message[4] >> 14) & 0x0003FFF0) | (message[5] >> 28);

		return decoded;
	}

	public static int[] encode(int[] data)
	{
		return encodeHash(data);
	}

	public static int[] decode(Transaction messageTransaction) throws GameActionException
	{
		return decodeHash(messageTransaction);
	}

	public static boolean sendComs(int[] data, int bidamount/*message, bid amount*/) throws GameActionException
	{
		int message[] = encode(data);

		if(rc.canSubmitTransaction(message, bidamount))
		{
			rc.submitTransaction(message, bidamount);
			return true;
		}
		else
			return false;

	}


	public static int[][] getComms(int roundno) throws GameActionException
	{
		Transaction[] theBlock = rc.getBlock(roundno);
		int[][] interpreted = new int[theBlock.length][9];

		System.out.println("Pre decoding" + Clock.getBytecodeNum());
		for(int i = 0; i < theBlock.length; i++)
			interpreted[i] = decode(theBlock[i]);
		System.out.println("Post decoding" + Clock.getBytecodeNum());

		return interpreted;
	}

	public static int[][] getLastIntervalComms() throws GameActionException
	{
		int roundToQuery = roundNum - roundNum%broadCastFrequency;
		if(roundToQuery == roundNum) roundToQuery = roundNum - broadCastFrequency;
		if(roundToQuery == 0) roundToQuery = 1;

		Transaction[] theBlock = rc.getBlock(roundToQuery);
		int[][] interpreted = new int[theBlock.length][9];

		for(int i = 0; i < theBlock.length; i++)
			interpreted[i] = decode(theBlock[i]);

		return interpreted;
	}


	public static int getCommsNum(ObjectType theType, MapLocation theloc)
	{
		int theint = 0;
		int theinttype;
		switch(theType)
		{
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
			case TO_BE_REFINERY:
				theinttype=13;
				break;
			case NO_SOUP:
				theinttype=14;
				break;
			default:
				theinttype=0;
				break;
		}

		theint |= theinttype << 12;
		theint |= theloc.x << 6;
		theint |= theloc.y;
		return theint;
	}


	public static ObjectLocation getLocationFromInt(int x)
	{
		ObjectType theRobot = ObjectType.COW;

		int a,b;
		a = x & 63;
		x = x >> 6;
		b = x & 63;
		x = x >> 6;
		x = x & 63;
		MapLocation theLocation = new MapLocation(b,a);
		switch(x)
		{
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
			case 13:
				theRobot=ObjectType.TO_BE_REFINERY;
				break;	
			case 14:
				theRobot=ObjectType.NO_SOUP;
				break;			
			default:
				// theRobot=0;
				break;
		}

		return new ObjectLocation(theRobot,theLocation);
	}
}
