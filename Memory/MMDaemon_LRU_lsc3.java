package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
*	This class runs in background and is intended to periodically release 
*	memory space
*/
class MMDaemon implements DaemonInterface{
	/**
	*	This method will be repeatedly called, and it will clean memory
	*	space where exists a 'zombie' frame that will never be remove by 
	*	its associated thread.
	*/
	public void unleash(ThreadCB thread){
		MyOut.print(thread, "Periodically clean memory frames.");
		MMU.cleanFrames(thread);
	}
}