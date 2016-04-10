package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

class MMDaemon implements DaemonInterface{
	public void unleash(ThreadCB thread){
		MMU.cleanFrames(thread);
	}
}