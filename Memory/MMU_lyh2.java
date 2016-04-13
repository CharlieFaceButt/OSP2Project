package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.FileSys.OpenFile;

// /**
//     The MMU class contains the student code that performs the work of
//     handling a memory reference.  It is responsible for calling the
//     interrupt handler if a page fault is required.

//     @OSPProject Memory
// */
public class MMU extends IflMMU
{
    private static GenericList FIFOlist;
    private static int PFAmount;
    private static float successfulPFAmount;
    private static int referencedPageNum;

    public static void addPFstats (boolean successful) {
      if(successful) {
        successfulPFAmount ++;
      }
      PFAmount ++;
    }

    public static int getPFAmount() {
      return PFAmount;
    }

    public static float getSuccessfulPFAmount() {
      return successfulPFAmount;
    }

    public static void addReferencedPageNum() {
      referencedPageNum ++;
    }

    public static int getReferencedPageNum() {
      return referencedPageNum;
    }
 //    /** 
 //        This method is called once before the simulation starts. 
	// Can be used to initialize the frame table and other static variables.

 //        @OSPProject Memory
 //    */
    public static void init()
    {
        // your code goes here
      PFAmount = 0;
      successfulPFAmount = 0;
      referencedPageNum = 0;
      FIFOlist = new GenericList();
      int num = MMU.getFrameTableSize();
      FrameTableEntry frame;
      for (int i = 0; i < num; i ++) {
        frame = new FrameTableEntry(i);
        MMU.setFrame(i, frame);
        Daemon.create("Memory manegement daemon", new MMDaemon(), 10000);
      }
    }

    // /**
    //    This method handlies memory references. The method must 
    //    calculate, which memory page contains the memoryAddress,
    //    determine, whether the page is valid, start page fault 
    //    by making an interrupt if the page is invalid, finally, 
    //    if the page is still valid, i.e., not swapped out by another 
    //    thread while this thread was suspended, set its frame
    //    as referenced and then set it as dirty if necessary.
    //    (After pagefault, the thread will be placed on the ready queue, 
    //    and it is possible that some other thread will take away the frame.)
       
    //    @param memoryAddress A virtual memory address
    //    @param referenceType The type of memory reference to perform 
    //    @param thread that does the memory access
    //    (e.g., MemoryRead or MemoryWrite).
    //    @return The referenced page.

    //    @OSPProject Memory
    // */
    static public PageTableEntry do_refer(int memoryAddress,
					  int referenceType, ThreadCB thread)
    {
        // your code goes here
      int pageNumBits = MMU.getVirtualAddressBits() - MMU.getPageAddressBits();
      int pageNum = (int)Math.pow(2, pageNumBits);
      PageTable pageTable = MMU.getPTBR();

      int end = memoryAddress / pageNum;

      if(!pageTable.pages[end].isValid()) {
        if (pageTable.pages[end].getValidatingThread() != null) {
          thread.suspend(pageTable.pages[end]);
          if(pageTable.pages[end].isValid()) {
            if(thread.getStatus() != ThreadKill) {
              if(referenceType == MemoryWrite)
                pageTable.pages[end].getFrame().setDirty(true);
              pageTable.pages[end].getFrame().setReferenced(true);
            }
          }
        }
        else {
          InterruptVector.setPage(pageTable.pages[end]);
          InterruptVector.setReferenceType(referenceType);
          InterruptVector.setThread(thread);
          CPU.interrupt(PageFault);
          pageTable.pages[end].notifyThreads();
          if(pageTable.pages[end].isValid()) {
            if(thread.getStatus() != ThreadKill) {
              if(referenceType == MemoryWrite)
                pageTable.pages[end].getFrame().setDirty(true);
              pageTable.pages[end].getFrame().setReferenced(true);
            }
          }
        }
      }
      else {
        if(referenceType == MemoryWrite)
          pageTable.pages[end].getFrame().setDirty(true);
        pageTable.pages[end].getFrame().setReferenced(true);
      }
      addReferencedPageNum();
      return pageTable.pages[end];
    }

    public static void newFIFO(FrameTableEntry frame) {
      MyOut.print(frame, "Insert frame into FIFO list.");
      if(FIFOlist.contains(frame)) {
        return;
      }
      FIFOlist.insert(frame);
    }

    public static FrameTableEntry getFIFOFrame() {
      MyOut.print(MMU.getPTBR().getTask(), "Get FIFO frame.");
      FrameTableEntry frame = null;
      Enumeration iterator = FIFOlist.forwardIterator();
      while(iterator.hasMoreElements()) {
        frame = (FrameTableEntry)(iterator.nextElement());
        if(!frame.isOccupied()) {
          return frame;
        }
      }
      return null;
    }

    public static void free(FrameTableEntry frame) {
      MyOut.print(frame, "Free " + frame);
      frame.setReferenced(false);
      frame.setPage(null);
      frame.setDirty(false);
      FIFOlist.remove(frame);
      PageTableEntry originalPage = frame.getPage();
      if(originalPage != null && originalPage.getFrame().getID() == frame.getID()) {
        originalPage.setValid(false);
        originalPage.setFrame(null);
      }
    }

    public static void cleanFrames(ThreadCB thread) {
      MyOut.print(thread, "Clean memory frames of " + thread);
      FrameTableEntry frame = null;
      int frameTableSize = MMU.getFrameTableSize();
      for(int i = 0; i < frameTableSize; i++) {
        frame = MMU.getFrame(i);
        if(!frame.isOccupied()) {
          if(frame.getPage() != null) {
            PageTableEntry page = frame.getPage();
            if(page.getTask().getStatus() == TaskTerm) {
              if(frame.isDirty()) {
                OpenFile swapFile = page.getTask().getSwapFile();
                swapFile.write(page.getID(), page, thread);
                MyOut.print(thread, "\tswap out page " + page);
              }
              MMU.free(frame);
            }
          }
        }
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
        // your code goes here

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
