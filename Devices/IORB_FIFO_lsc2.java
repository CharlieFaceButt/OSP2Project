package osp.Devices;
import osp.IFLModules.*;
import osp.FileSys.OpenFile;
import osp.Threads.ThreadCB;
import osp.Memory.PageTableEntry;
import osp.Utilities.*;
import osp.Hardware.*;

/** 
*   This class contains all the information necessary to carry out
*   an I/O request.
*
*    @OSPProject Devices
*/
public class IORB extends IflIORB
{
    public Long createTime;
    public Long enqueueTime;
    public Long dequeueTime;
    public Long handleTime;
    public Long finishTime;
    /**
    *       The IORB constructor.
    *       Must have
    *
    *	   super(thread,page,blockNumber,deviceID,ioType,openFile);
    *
    *       as its first statement.
    *
    *       @OSPProject Devices
    */
    public IORB(ThreadCB thread, PageTableEntry page, 
    int blockNumber, int deviceID, 
    int ioType, OpenFile openFile) {
        super(thread, page, blockNumber, deviceID, ioType, openFile);
        MyOut.print(this, "Create IORB object " + this);
        createTime = HClock.get();
    }

}