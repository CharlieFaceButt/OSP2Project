package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
*    The MMU class contains the student code that performs the work of
*    handling a memory reference.  It is responsible for calling the
*    interrupt handler if a page fault is required.
*
*    @OSPProject Memory
*/
public class MMU extends IflMMU
{
    /** 
    *        This method is called once before the simulation starts. 
    *	       Can be used to initialize the frame table and other static variables.
    *
    *        @OSPProject Memory
    */
    public static void init()
    {
      int frameTableSize = MMU.getFrameTableSize();
      for (int i = 0; i < frameTableSize; i++) {
        MMU.setFrame(i, new FrameTableEntry(i));
      }
    }

    /**
    *       This method handlies memory references. The method must 
    *       calculate, which memory page contains the memoryAddress,
    *       determine, whether the page is valid, start page fault 
    *       by making an interrupt if the page is invalid, finally, 
    *       if the page is still valid, i.e., not swapped out by another 
    *       thread while this thread was suspended, set its frame
    *       as referenced and then set it as dirty if necessary.
    *       (After pagefault, the thread will be placed on the ready queue, 
    *       and it is possible that some other thread will take away the frame.)
    *
    *       @param memoryAddress A virtual memory address
    *       @param referenceType The type of memory reference to perform 
    *       @param thread that does the memory access
    *       (e.g., MemoryRead or MemoryWrite).
    *       @return The referenced page.
    *
    *       @OSPProject Memory
    */
    static public PageTableEntry do_refer(int memoryAddress,
					  int referenceType, ThreadCB thread)
    {
      //Calculate offset and page number
      int spaceSizeBits = getVirtualAddressBits();
      int pageSizeBits = getPageAddressBits();
      int pageNumberBits = spaceSizeBits - pageSizeBits;
      int offset = memoryAddress % Math.pow(2, pageSizeBits);
      int pageNumber = (memoryAddress >> pageSizeBits) % Math.pow(2, pageNumberBits);

      PageTable taskPageTable = MMU.getPTBR();
      TaskCB task = taskPageTable.getTask();
      //The thread must be currently running
      if (task.getID() != thread.getTask().getID()) {
        MyOut.error(thread, "The thread that making reference is not currently running.");
      }
      //Available area for the thread to reference.
      if (pageNumber >= taskPageTable.pages.length) {
        MyOut.warning(thread, "Reference exceeds the allocated page table.");
        return null;
      }
      //Get the page
      PageTableEntry page = taskPageTable.pages[pageNumber];

      //When the page is not valid
      if (!page.isValid()) {
        //If other pagefault on this page, wait for it.
        if (page.getValidatingThread() != null) {
          thread.suspend(page);
        } else{
          //If no page fault pending, initiate a page fault interrupt
          InterruptVector.setPage(page);
          InterruptVector.setReferenceType(referenceType);
          InterruptVector.setThread(thread);
          InterruptVector.setInterruptType(PageFault);
          CPU.interrupt();
        }
      }
      
      //When page fault fails, error occurs;
      //If page fault succeed the page becomes available.
      if (!page.isValid()) {
        MyOut.error(thread, "Page is not loaded successfully.");
        return page;
      }
      //At this point, the page should be valid, then it is safe to
      //set reference bit and dirty bit
      FrameTableEntry validFrame = page.getFrame();
      setReferencedAndDirty(validFrame, referenceType, thread);
      return page;      
    }

    private static void setReferencedAndDirty(FrameTableEntry frame, int referenceType, ThreadCB thread){
        //It is possible that the refencing thread is killed at this 
        //time, then no changes should be made
        if (thread.getStatus() == ThreadKill) {
          return;
        }
        //Set reference bit
        validFrame.setReferenced(TRUE);
        //Set dirty bit
        if (referenceType == GlobalViriables.MemoryWrite) {
          validFrame.setDirty(TRUE);
        }
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        
    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
