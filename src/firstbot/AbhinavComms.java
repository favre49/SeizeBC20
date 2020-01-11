public class AbhinavComms
{
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

	public static int[] encode(int[] data)
	{
		int[] hammingData = new int[9];
		for(int i = 0; i < 9; i++)
		{
			hammingData[i] = hamming18to24(data[i]);
		}
		int[] message = new int[7];
		message[0] |= (38 << 24);

		//first 8 bits for key
		//the 8 bit key is 38
		
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

		return message;
	}

	public static int[] decode(int[] message)
	{
		int[] decoded = new int[9];

		message[0] ^= -903849746;
		message[1] ^= -172817894;
		message[2] ^= 293009196;
		message[3] ^= 25721274;
		message[4] ^= 205865773;
		message[5] ^= -1561017189;
		message[6] ^= 2092647876;

		// System.out.println(message[0]);

		if(((message[0]>>24) & 255) != 38)
		{
			//it was not our message
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
			hammingChecked[i]=unhamming24to18(decoded[i]);
		}


		return hammingChecked;
	}

	public static void main (String args[])
	{
		int[] arr = new int[9];
		arr[0] = 54158;
		System.out.println(Integer.toBinaryString(arr[0])+"\t"+Integer.toBinaryString(arr[1]));
		arr = encode(arr);
		System.out.println(Integer.toBinaryString(arr[0])+"\t"+Integer.toBinaryString(arr[1]));
		arr = decode(arr);
		System.out.println(Integer.toBinaryString(arr[0])+"\t"+Integer.toBinaryString(arr[1]));
	}

}