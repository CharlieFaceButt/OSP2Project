package osp.Memory;

import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Devices.*;
import osp.Utilities.*;
import osp.IFLModules.*;
/**
*   The PageTableEntry object contains information about a specific virtual
*   page in memory, including the page frame in which it resides.
*
*   @OSPProject Memory
*
*/

public class PageTableEntry extends IflPageTableEntry
{
    /**
    *       The constructor. Must call
    *
    *       	   super(ownerPageTable,pageNumber);
	  *
    *       as its first statement.
    *
    *       @OSPProject Memory
    */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
        super(ownerPageTable, pageNumber);
    }

    /**
    *   This method is called when IO request about this page is called.
    *   This method increases the lock count on the page by one. 
    * 
    * 	The method must FIRST increment lockCount, THEN  
    * 	check if the page is valid, and if it is not and no 
    * 	page validation event is present for the page, start page fault 
    * 	by calling PageFaultHandler.handlePageFault().
    * 
    * 	@return SUCCESS or FAILURE
    * 	FAILURE happens when the pagefault due to locking fails or the 
    * 	thread that created the IORB gets killed.
    * 
    * 	@OSPProject Memory
     */
    public int do_lock(IORB iorb)
    {
      //The requesting thread? using getTask()?
      ThreadCB thread = iorb.getThread();
      MyOut.print(thread, "Lock " + this + "-" + getFrame());
      //Call page fault when the page is not in memory frame
      if (!isValid()) {
        //Check if page is in page fault
        if (getValidatingThread() != null) {
          //If the same thread causes the page fault, return immediately
          if (thread.getID() == getValidatingThread().getID()) {
            return final_check_do_lock(thread);
          }
          //If a different thread caused a page fault previously, this 
          //thread should be suspended by this event
          thread.suspend(this);
          //After the page fault is finished, this thread is unblocked, 
          //But it can only return success if the page becomes valid.
          if (isValid()) {
            return final_check_do_lock(thread);
          } else{
            return FAILURE;
          }
        } else{
          //Though there is no page fault, still it may be reserved by 
          //Other page fault on this page
          if (isReserved()) {
            thread.suspend(this);
            //After the page fault is finished, this thread is unblocked, 
            //But it can only return success if the page becomes valid.
            if (isValid()) {
              return final_check_do_lock(thread);
            }
          }
          //If no page fault and no reservation happening to this page, 
          //call page fault. It is already kernel mode so no need to 
          //cause an interrupt
          int pfresult = PageFaultHandler.handlePageFault(
            thread, MemoryLock, this); 
          //Page fault may fail due to insufficient memory
          if (pfresult == FAILURE) {
            return FAILURE;
          }
        }
      }
      return final_check_do_lock(thread);
    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
      //decrement lock Count
      FrameTableEntry frame = getFrame();
      frame.decrementLockCount();
      if (frame.getLockCount() < 0) {
        MyOut.error(frame, "<XXL>: frame lock count becomes negative");
      }
      MyOut.print(this, "Unlock " + this + 
        ". new lock count: " + frame.getLockCount());
    }

    /**
    *   This method is the final check before return of do_lock
    *
    *   The method check if the thread running do_lock is killed
    *   during locking.
    */
    private int final_check_do_lock(ThreadCB thread){
      //If the locking thread is killed, return failure
      if (thread.getStatus() == ThreadKill || 
        thread.getTask().getStatus() == TaskTerm) {
        return FAILURE;
      } else  {
        getFrame().incrementLockCount();
        MyOut.print(this, "Lock successful " + this + 
          ". new lock count: " + getFrame().getLockCount());
        return SUCCESS;
      }
    }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
