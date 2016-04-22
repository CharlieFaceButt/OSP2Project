package osp.Devices;

import osp.Utilities.*;
import java.io.*;

/**
*	Statistics in these model. Only device head record included.
*/
public class Stats{
	/**
	*	Head movement record for each device
	*/
	private GenericList diskHList = new GenericList();
	private Device device;

	public Stats(Device d){
		this.device = d;
	}

	/**
	*	Head record, tuple (head position, record time)
	*/
	public class HeadStat{
		public int srcPos;
		public int destPos;
		public long time;
		public HeadStat(int s, int d, long t){
			this.srcPos = s;
			this.destPos = d;
			this.time = t;
		}
	}

	// /**
	// *	Include a device in the record list
	// */
	// public static void registerDevice(Device device){
	// 	if (!diskHList.contains(device)) {
	// 		diskHList.put(device, new GenericList());
	// 	}
	// }

	private Boolean isSaving = false;
	/**
	*	Insert head movement record
	*/
	public void inputHeadStat(
		final int s, final int d, 
		final long c, final long t){
		// GenericList headList = diskHList.get(device);
		// if (headList == null) {
		// 	registerDevice(device);
		// 	headList = diskHList.get(device);
		// }
		// headList.append(hstat);
		diskHList.append(new HeadStat(s,d,t));

		new Thread(new Runnable(){
			public void run(){
				synchronized (isSaving){
					if (!isSaving) {
					if (!isSaving) {
						isSaving = true;
						try {
							FileWriter fw = new FileWriter("headRecord" + device + ".txt", true);
							fw.write("[" + t + "]: " + s + "-" + d + 
								"\tIORB created: " + c + "\r\n");
							fw.close();
						} catch (IOException ioe){
							System.err.println("IOException: " + ioe.getMessage());
						}
						isSaving = false;
					}}
				}
			}
		}).start();
	}
}