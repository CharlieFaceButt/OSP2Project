package osp.Devices;
import java.util.*;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Tasks.*;
import osp.Memory.*;
import osp.FileSys.*;

/**
*    The disk interrupt handler.  When a disk I/O interrupt occurs,
*    this class is called upon the handle the interrupt.
*
*    @OSPProject Devices
*/
public class DiskInterruptHandler extends IflDiskInterruptHandler
{
    /** 
    *        Handles disk interrupts. 
    *
    *        This method obtains the interrupt parameters from the 
    *        interrupt vector. The parameters are IORB that caused the 
    *        interrupt: (IORB)InterruptVector.getEvent(), 
    *        and thread that initiated the I/O operation: 
    *        InterruptVector.getThread().
    *        The IORB object contains references to the memory page 
    *        and open file object that participated in the I/O.
    *
    *        The method must unlock the page, set its IORB field to null,
    *        and decrement the file's IORB count.
    *
    *        The method must set the frame as dirty if it was memory write 
    *        (but not, if it was a swap-in, check whether the device was 
    *        SwapDevice)
    *
    *        As the last thing, all threads that were waiting for this 
    *        event to finish, must be resumed.
    *
    *        @OSPProject Devices 
    */
    public void do_handleInterrupt()
    {
        MyOut.print(this, "Handle disk interrupt");
        //Obtain information from the the interrupt vector
        IORB iorb = (IORB)(InterruptVector.getEvent());
        //Decrement the IORB count of the associated openfile
        OpenFile swapFile = iorb.getOpenFile();
        synchronized (swapFile){
            swapFile.decrementIORBCount();   
            MyOut.print(swapFile, "decrement IORB count");
        }
        //Close the file when closePending flag is true and IORB count
        //is 0
        if (swapFile.closePending && swapFile.getIORBCount() == 0) {
            swapFile.close();
        }
        //Unlock the associated page
        PageTableEntry page = iorb.getPage();
        page.unlock();
        //Set reference bit and dirty bit of frame
        TaskCB task = iorb.getThread().getTask();
        FrameTableEntry frame = page.getFrame();
        if (iorb.getDeviceID() != SwapDeviceID) {
            //When it is not swaping, set reference bit
            if (task.getStatus() == TaskLive) {
                frame.setReferenced(true);
                //When read, set dirty
                if (iorb.getIOType() == FileRead) {
                    frame.setDirty(true);
                }
            }
        } else{
            //If it is swapping operation, set frame clean
            if (task.getStatus() == TaskLive) {
                frame.setDirty(false);
            }
        }
        //Unreserve the frame if the task is terminated
        if (task.getStatus() == TaskTerm && frame.isReserved()) {
            frame.setUnreserved(task);
        }
        //Notify all threads waiting for this IORB
        iorb.notifyThreads();
        //Set the device idle
        Device device = Device.get(iorb.getDeviceID());
        device.setBusy(false);
        //The device restart a new I/O request if there is
        IORB newRequest = device.dequeueIORB();
        if (newRequest != null) {
            device.startIO(newRequest);
        }
        //A chance to dispatch new thread
        ThreadCB.dispatch();
    }


}

