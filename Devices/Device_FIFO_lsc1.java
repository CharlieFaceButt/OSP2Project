package osp.Devices;

/**
*    This class stores all pertinent information about a device in
*    the device table.  This class should be sub-classed by all
*    device classes, such as the Disk class.
*
*    @OSPProject Devices
*/

import osp.IFLModules.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Tasks.*;
import java.util.*;

public class Device extends IflDevice
{
    protected IORBQueue iorbQueue;
    /**
    *        This constructor initializes a device with the provided parameters. 
    *        As a first statement it must have the following:
    *        super(id,numberOfBlocks);
    *
    *        @param numberOfBlocks -- number of blocks on device
    *
    *        @OSPProject Devices
    */
    public Device(int id, int numberOfBlocks)
    {
        super(id, numberOfBlocks);
        iorbQueue = new IORBQueue();
    }

    /**
    *    This method is called once at the beginning of the
    *    simulation. Can be used to initialize static variables.
    *
    *    @OSPProject Devices
    */
    public static void init()
    {

    }

    /**
    *   Enqueues the IORB to the IORB queue for this device
    *   according to some kind of scheduling algorithm.
    *          
    *   This method must lock the page (which may trigger a page fault),
    *   check the device's ate and call startIO() if the 
    *   device is idle, otherwise append the IORB to the IORB queue.
    *   
    *   @return SUCCESS or FAILURE.
    *   FAILURE is returned if the IORB wasn't enqueued 
    *   (for instance, locking the page fails or thread is killed).
    *   SUCCESS is returned if the IORB is fine and either the page was 
    *   valid and device started on the IORB immediately or the IORB
    *   was successfully enqueued (possibly after causing pagefault pagefault)
    *          
    *   @OSPProject Devices
    */
    public int do_enqueueIORB(IORB iorb)
    {
        if (iorb == null) {
            MyOut.error(iorb, "IORB is null");
        }
        //Lock the page associated with the iorb, to ensure that the
        //page will not be swapped out till the end of the operation.
        iorb.getPage().lock(iorb);

        //Increment IORB count of the open file handle, to prevent 
        //closing before all I/O operation have finished.
        iorb.getOpenFile().incrementIORBCount();

        //Set the cylinder of the IORB to the device(disk)
        int cylinder = computeCylinder(iorb.getBlockNumber());
        iorb.setCylinder(cylinder);

        //Return FAILURE if the requesting thread is killed.
        ThreadCB thread = iorb.getThread();
        if (thread == null || thread.getStatus() == ThreadKill) {
            return FAILURE;
        } else {
            TaskCB task = thread.getTask();
            if (task != null && task.getStatus() == TaskTerm) {
                return FAILURE;
            }
        }

        if (isBusy()) {
            //If the device is busy, put iorb on the waiting queue.
            iorbQueue.enqueue(iorb);
        } else {
            //If the device is idle, start I/O and return SUCCESS
            startIO(iorb);
        }
        
        return SUCCESS;
    }

    /**
    *       Selects an IORB (according to some scheduling strategy)
    *       and dequeues it from the IORB queue.
    *
    *       No unlock because device has not finished servicing that
    *       IORB.
    *
    *       @OSPProject Devices
    */
    public IORB do_dequeueIORB()
    {
        // if (iorbQueue.isEmpty()) {
        //     return null;
        // }
        return iorbQueue.dequeue();
    }

    /**
    *        Remove all IORBs that belong to the given ThreadCB from 
    *        this device's IORB queue
    *
    *        The method is called when the thread dies and the I/O 
    *        operations it requested are no longer necessary. The memory 
    *        page used by the IORB must be unlocked and the IORB count for 
    *        the IORB's file must be decremented.
    *
    *        @param thread thread whose I/O is being canceled
    *
    *        @OSPProject Devices
    */
    public void do_cancelPendingIO(ThreadCB thread)
    {
        iorbQueue.cancelPendingIO(thread);
    }

    /** 
    *    Called by OSP after printing an error message. The student can
    *    insert code here to print various tables and data structures
    *    in their state just after the error happened.  The body can be
    *    left empty, if this feature is not used.
    *
    *    @OSPProject Devices
    */
    public static void atError()
    {
        // your code goes here

    }

    /** 
    *    Called by OSP after printing a warning message. The student
    *    can insert code here to print various tables and data
    *    structures in their state just after the warning happened.
    *    The body can be left empty, if this feature is not used.
    *
    *    @OSPProject Devices
     */
    public static void atWarning()
    {
        // your code goes here

    }

    /**
    *   Calculate the cylinder number of a block
    */
    private int computeCylinder(int blockNumber){
        Disk disk = (Disk)this;
        int addressBits = MMU.getVirtualAddressBits();
        int pageAddressBits = MMU.getPageAddressBits();
        int blockSizeBits = addressBits - pageAddressBits;
        //The block size is equal to the memory page size
        int blockSize = (int)(Math.pow(2, blockSizeBits));
        //The number of blocks in a track
        int blocksPerTrack = 
            disk.getSectorsPerTrack() * 
            disk.getBytesPerSector() / blockSize;
        //Block number cannot exceed the amount of block a device can hold
        int platterNumber = (blockNumber / 
            (blocksPerTrack * disk.getTracksPerPlatter())) + 1;
        if (platterNumber > disk.getPlatters()) {
            MyOut.error(this, "block number exceeds the device range");
        }
        //The cylinder that holds the block
        int cylinder = (blockNumber % blocksPerTrack) % disk.getTracksPerPlatter();
        MyOut.print(this, "Calculate (cylinder/blockNumber): " + cylinder + 
            "/" + blockNumber);
        return cylinder;
    }

    /**
    *   This class descirbes the iorb queue used in device request
    *   scheduling.
    *   Enqueue and dequeue use the instance of this class to describe 
    *   the scheduling strategy.
    *   Current implementation: FIFO
    */
    public class IORBQueue implements GenericQueueInterface{
        private GenericList queue;
        public IORBQueue(){
            queue = new GenericList();
        }

        /**
        *   FIFO put new request to the end of the queue
        */
        public void enqueue(IORB iorb){
            queue.append(iorb);
        }

        /**
        *   FIFO remove the head of the queue.
        */
        public IORB dequeue(){
            if (isEmpty()) {
                MyOut.print(this, "Cannot remove item from empty IORB queue.");
                return null;
            }
            Object obj = queue.removeHead();
            return (IORB)obj;
        }

        /**
        *   The method go over the device queue and do the cleaning stuff
        *   in terms of a killed thread.
        */
        public void cancelPendingIO(ThreadCB thread){
            if (thread == null) {
                return;
            }
            //Search all pending IO
            Enumeration iterator = queue.forwardIterator();
            while(iterator.hasMoreElements()){
                Object obj = iterator.nextElement();
                IORB request = (IORB)obj;
                if (request.getThread().getID() == thread.getID()) {
                    //Remove IORB of the thread
                    queue.remove(obj);
                    //Unlock corresponding page
                    request.getPage().unlock();
                    //Decrement the IORB count of the open file
                    OpenFile swapFile = request.getOpenFile();
                    swapFile.decrementIORBCount();
                    //Close the open file handle when the close pending 
                    //flag is true and IORB count becomes 0.
                    if (swapFile.closePending && swapFile.getIORBCount() == 0) {
                        swapFile.close();
                    }
                }
            }
        }


        public int length(){
            return queue.length();
        }
        public boolean isEmpty(){
            return queue.isEmpty();
        }
        public boolean contains(Object obj){
            return queue.contains(obj);
        }
    }
}
