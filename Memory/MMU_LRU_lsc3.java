package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.FileSys.OpenFile;

/**
*    The MMU class contains the student code that performs the work of
*    handling a memory reference.  It is responsible for calling the
*    interrupt handler if a page fault is required.
*
*    @OSPProject Memory
*/
public class MMU extends IflMMU
{
    private static GenericList LRUlist;
    private static int PFAmount;
    private static int successfulPFAmount;
    private static int referencedPageNum;

    public static void addPFstats(boolean successful){
      if (successful) {
        successfulPFAmount ++;
      }
      PFAmount ++;
    }

    public static int getPFAmount() {
      return PFAmount;
    }

    public static int getSuccussfulPFAmount() {
      return successfulPFAmount;
    }

    public static void addReferencedPageNum() {
      referencedPageNum ++;
    }

    public static int getReferencedPageNum() {
      return referencedPageNum;
    }


    /** 
    *        This method is called once before the simulation starts. 
    *	       Can be used to initialize the frame table and other static variables.
    *
    *        @OSPProject Memory
    */
    public static void init()
    {
      PFAmount = 0;
      successfulPFAmount = 0;
      referencedPageNum = 0;
      LRUlist = new GenericList();
      int frameTableSize = MMU.getFrameTableSize();
      for (int i = 0; i < frameTableSize; i++) {
        MMU.setFrame(i, new FrameTableEntry(i));
      }
      Daemon.create("Memory management daemon", new MMDaemon(), 10000);
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
      //Calculate the parameters

      //The number of bits of the address
      int spaceSizeBits = getVirtualAddressBits();
      //The number of bits of the page number
      int pageNumberBits = getPageAddressBits();
      //The number of bits of the page size
      int offsetBits = spaceSizeBits - pageNumberBits;
      //Page offset of the address
      int offset = memoryAddress % (int)(Math.pow(2, offsetBits));
      //Page number of the address
      int pageNumber = (memoryAddress >> offsetBits) % (int)(Math.pow(2, pageNumberBits));
      //Log
      MyOut.print(thread, "Refer memory(" + 
        pageNumberBits + "|" + offsetBits + "): " +
        Integer.toBinaryString(memoryAddress) + "(" + pageNumber + "|" + offset + ")");
      
      //Several checks before reference

      //Page table of the current task
      PageTable taskPageTable = MMU.getPTBR();
      //Current running task in CPU
      TaskCB task = taskPageTable.getTask();
      //The referencing thread must be currently running
      if (task.getID() != thread.getTask().getID()) {
        MyOut.error(thread, "\t The thread that making reference is not currently running.");
        return null;
      }
      //Available area for the thread to reference.
      if (pageNumber >= taskPageTable.pages.length) {
        MyOut.print(thread, "\t " + thread + ": Reference exceeds the allocated page table length " +
          taskPageTable.pages.length);
        return null;
      }

      //Make sure the page is valid to reference

      //Get the page
      PageTableEntry page = taskPageTable.pages[pageNumber];
      //When the page is not valid, initiate a pagefault
      if (!page.isValid()) {
        MyOut.print(thread, "\t Page is not valid.");
        //If other pagefault on this page, wait for it.
        if (page.getValidatingThread() != null) {
          MyOut.print(thread, "\t Wait for other validating thread.");
          thread.suspend(page);
        } 
        //If no page fault pending or the previous pagefault failed,
        //initiate a page fault interrupt
        while (!page.isValid()) {
          if (thread.getStatus() == ThreadKill || 
            thread.getTask().getStatus() == TaskTerm) {
            MyOut.print(thread, "\t requesting thread is killed during page fault of " + page);
            addReferencedPageNum();
            return page;
          }
          MyOut.print(thread, "\t Initiate page fault for page " + page);
          InterruptVector.setPage(page);
          InterruptVector.setReferenceType(referenceType);
          InterruptVector.setThread(thread);
          CPU.interrupt(PageFault);
        }
      }
      // //When page fault fails, error occurs;
      // //If page fault succeed the page becomes available.
      // if (!page.isValid()) {
      //   MyOut.print(thread, "<<Error>> Page" + page + "is not loaded successfully.");
      //   return page;
      // }

      //At this point, the page should be valid, then it is safe to
      //set reference bit and dirty bit
      FrameTableEntry validFrame = page.getFrame();
      setReferencedAndDirty(validFrame, referenceType, thread);

      //Each reference may cause changes in the LRU count
      do_LRUAlignment(validFrame);
      addReferencedPageNum();
      return page;
    }

    /**
    *   This method will set a frame the reference bit and dirty bit
    */
    private static void setReferencedAndDirty(
      FrameTableEntry frame, int referenceType, ThreadCB thread){
      MyOut.print(thread, "Set reference bit and dirty bit");
      //It is possible that the refencing thread is killed at this 
      //time, then no changes should be made
      if (thread.getStatus() == ThreadKill || 
            thread.getTask().getStatus() == TaskTerm) {
        return;
      }
      //Set reference bit
      frame.setReferenced(true);
      //Set dirty bit
      if (referenceType == MemoryWrite) {
        frame.setDirty(true);
      }
    }

    /**
    *   This method keep LRU count of frame in order. The frame
    *   referenced will be set to the end of the LRU queue, 
    *   indicating that it is the most recently used frame.
    */
    private static void do_LRUAlignment(FrameTableEntry frame){
      MyOut.print(frame, "LRU is align");
      LRUlist.remove(frame);
      LRUlist.append(frame);
    }
    public static void newLRU(FrameTableEntry frame){
      MyOut.print(frame, "Insert frame into LRU list.");
      if (LRUlist.contains(frame)) {
        return;
      }
      LRUlist.append(frame);
    }
    public static FrameTableEntry getLRUframe(){
      MyOut.print(MMU.getPTBR().getTask(), "Get LRU frame.");
      FrameTableEntry frame = null;
      Enumeration iterator = LRUlist.forwardIterator();
      while(iterator.hasMoreElements()){
        frame = (FrameTableEntry)(iterator.nextElement());
        if (!frame.isOccupied()) {
          return frame;
        }
      }
      return null;
    }

    /**
    *   This method free a frame from a page
    */
    synchronized public static void free(FrameTableEntry frame){
      MyOut.print(frame, "Free " + frame);
      frame.setReferenced(false);
      frame.setPage(null);
      frame.setDirty(false);
      LRUlist.remove(frame);
      PageTableEntry originalPage = frame.getPage();
      if (originalPage != null && 
        originalPage.getFrame().getID() == frame.getID()) {
        originalPage.setValid(false);
        originalPage.setFrame(null);
      }
    }

    /**
    *   This method is called periodically by memory management
    *   daomon thread. It cleans memory frames whose assigned 
    *   task is no longer active.
    */
    public static void cleanFrames(ThreadCB thread){
      MyOut.print(thread, "Clean memory frames of " + thread);
      FrameTableEntry frame = null;
      int frameTableSize = MMU.getFrameTableSize();
      for (int i = 0; i < frameTableSize; i++) {
        frame = MMU.getFrame(i);
        if (!frame.isOccupied()) {
          if (frame.getPage() != null){
            PageTableEntry page = frame.getPage();
            if (page.getTask().getStatus() == TaskTerm){
              //Frame must not be occupied, must have a page linked,
              //and the corresponding thread must already be killed,
              //then the frame is ready to be free

              //If the frame is dirty, it has to be written back
              if (frame.isDirty()) {
                OpenFile swapFile = page.getTask().getSwapFile();
                //Swap out the original page
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


}

