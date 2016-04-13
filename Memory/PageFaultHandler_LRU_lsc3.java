package osp.Memory;
import java.util.*;
import osp.Hardware.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.FileSys.FileSys;
import osp.FileSys.OpenFile;
import osp.IFLModules.*;
import osp.Interrupts.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import java.io.*;

/**
*    The page fault handler is responsible for handling a page
*    fault.  If a swap in or swap out operation is required, the page fault
*    handler must request the operation.
*
*    @OSPProject Memory
*/
public class PageFaultHandler extends IflPageFaultHandler
{
    /**
    *   This method handles a page fault. 
    *
    *   It must check and return if the page is valid, 
    *
    *   It must check if the page is already being brought in by some other
    *	thread, i.e., if the page's has already pagefaulted
    *	(for instance, using getValidatingThread()).
    *   If that is the case, the thread must be suspended on that page.
    *
    *   If none of the above is true, a new frame must be chosen 
    *   and reserved until the swap in of the requested 
    *   page into this frame is complete. 
    *
    *	Note that you have to make sure that the validating thread of
    *	a page is set correctly. To this end, you must set the page's
    *	validating thread using setValidatingThread() when a pagefault
    *	happens and you must set it back to null when the pagefault is over.
    *
    *   If a swap-out is necessary (because the chosen frame is
    *   dirty), the victim page must be dissasociated 
    *   from the frame and marked invalid. After the swap-in, the 
    *   frame must be marked clean. The swap-ins and swap-outs 
    *   must are preformed using regular calls read() and write().
    *
    *   The student implementation should define additional methods, e.g, 
    *   a method to search for an available frame.
    *
    *	Note: multiple threads might be waiting for completion of the
    *	page fault. The thread that initiated the pagefault would be
    *	waiting on the IORBs that are tasked to bring the page in (and
    *	to free the frame during the swapout). However, while
    *	pagefault is in progress, other threads might request the same
    *	page. Those threads won't cause another pagefault, of course,
    *	but they would enqueue themselves on the page (a page is also
    *	an Event!), waiting for the completion of the original
    *	pagefault. It is thus important to call notifyThreads() on the
    *	page at the end -- regardless of whether the pagefault
    *	succeeded in bringing the page in or not.
    *
    *   @param thread the thread that requested a page fault
    *   @param referenceType whether it is memory read or write
    *   @param page the memory page 
    *
    *	@return SUCCESS is everything is fine; FAILURE if the thread
    *	dies while waiting for swap in or swap out or if the page is
    *	already in memory and no page fault was necessary (well, this
    *	shouldn't happen, but...). In addition, if there is no frame
    *	that can be allocated to satisfy the page fault, then it
    *	should return NotEnoughMemory
    *
    *        @OSPProject Memory
    */
    public static int do_handlePageFault(ThreadCB thread, 
					 int referenceType,
					 PageTableEntry page)
    {
        do_entering(thread, page);
        //Log
        MyOut.print(thread, "Handle page fault: " + page);

        //Check if the page becomes valid, if it is then some other
        //pagefault of this frame is done.
        if (page.isValid()) {
            MyOut.print(thread, "\t Abort: page is valid.");
            return do_exit(FAILURE, page, null);
        }
        //Check there exists frame for replacement
        if (allFramesAreOccupied()) {
            MyOut.print(thread, "\t Abort: not enough memory.");
            return do_exit(NotEnoughMemory, page, null);
        }

        //Suspend the thread
        SystemEvent event = new SystemEvent("PageFault(page-thread):" + page + "-" + thread);
        thread.suspend(event);
        MyOut.print(thread, "\t Suspend the requesting thread.");

        //Find a suitable frame and reserve it, do page replacement if 
        //necessary. If there is no free frame, it will evict a page if
        //possible.
        //
        //Search for empty frame first. For placement algorithm, not 
        //much difference when using paging
        int frameTableSize = MMU.getFrameTableSize();
        FrameTableEntry selectedFrame = null;
        for (int i = 0; i < frameTableSize; i ++) {
            FrameTableEntry frame = MMU.getFrame(i);
            //If the frame is free, reserve it with the page
            if (frame.getPage() == null) {
                //Reserve the frame
                if (SUCCESS == do_reserveFrame(frame, thread)){
                    selectedFrame = frame;
                    MyOut.print(thread, "\t Found a free frame: " + selectedFrame);
                    break;
                }
            }
        }
        //If there is no free frame, search a frame for replacement
        if (selectedFrame == null) {
            MyOut.print(thread, "\t Cannot find free frame.");
            //Apply LRU algorithm 
            do{
                //Find the least recently used frame that is not occupied.
                selectedFrame = do_LRU();

                //If the LRU algorithm cannot find a suitable frame, it
                //means there is no available frame in memory
                if (selectedFrame == null) {
                    MyOut.print(thread, "\t Abort: all frame in memory is in use.");
                    return do_exit(NotEnoughMemory, page, event);
                }

                //Reserve the frame
            } while (FAILURE == do_reserveFrame(selectedFrame, thread));
            MyOut.print(thread, "\t Select frame " + selectedFrame + " by LRU");

            //If LRU find a dirty frame, swap out the original page in 
            //that frame
            PageTableEntry originalPage = selectedFrame.getPage();
            if (selectedFrame.isDirty()) {
                OpenFile swapFile = originalPage.getTask().getSwapFile();
                //Swap out the original page on behalf of the new thread
                swapFile.write(originalPage.getID(), originalPage, thread);
                MyOut.print(thread, "\tswap out original page " + originalPage);
            }

            //The thread may be killed during swap
            if (thread.getStatus() == ThreadKill || 
                thread.getTask().getStatus() == TaskTerm) {
                do_unreserveFrame(selectedFrame, thread);
                MyOut.print(thread, "\t Abort: requesting thread is killed.");
                return do_exit(FAILURE, page, event);
            }

            //The frame is selected and it is written back to swap file,
            //Then it is safe to free the frame
            do_freeFrame(selectedFrame);
            originalPage.setValid(false);
            originalPage.setFrame(null);
        }
        //At this point, the frame should be free and reserved.
        //Assign this frame to the faulty page
        FrameTableEntry originalFrame = page.getFrame();
        page.setFrame(selectedFrame);
        MyOut.print(thread, "\t Free the frame and assign it to the new page " + page);

        //Swap in the page from swap file to memory frame, this handler
        //will be suspended during read automatically.
        OpenFile swapFile = page.getTask().getSwapFile();
        swapFile.read(page.getID(), page, thread);
        MyOut.print(thread, "\t Swap in new page " + page + ", and suspend pagefult handler");

        //The thread may be killed during swap
        if (thread.getStatus() == ThreadKill || 
                thread.getTask().getStatus() == TaskTerm) {
            do_unreserveFrame(selectedFrame, thread);
            MyOut.print(thread, "\t Abort: requesting thread is killed.");
            page.setFrame(originalFrame);
            return do_exit(FAILURE, page, event);
        }

        //When image of the page is copied, this handler update the page 
        //table and set the validity bit. 
        selectedFrame.setPage(page);
        page.setValid(true);
        selectedFrame.setReferenced(true); //? should lock be excluded
        if (referenceType == MemoryWrite) {
            selectedFrame.setDirty(true);
        } else selectedFrame.setDirty(false);
        MMU.newLRU(selectedFrame);
        MyOut.print(thread, "\t Update the page table and frame attributes.");

        //Unset reserved bit
        do_unreserveFrame(selectedFrame, thread);

        return do_exit(SUCCESS, page, event);
    }

    /**
    *   This method reserve a frame for a thread
    *
    *   @param frame
    *   @param thread
    *
    *   @return SUCCESS when the frame is not reserved to another task, 
    *   SUCCESS otherwise.
    */
    synchronized private static int do_reserveFrame(
        FrameTableEntry frame, ThreadCB thread){
        MyOut.print(thread, "\t Reserve " + frame);
        if (frameIsOccupied(frame)) {
            return FAILURE;
        }
        //If other thread is going to modify the frame, do not 
        //choose this frame
        if (frame.getPage() != null && 
            frame.getPage().getValidatingThread() != null) {
            return FAILURE;
        }
        frame.setReserved(thread.getTask());
        return SUCCESS;
    }
    synchronized private static void do_unreserveFrame(
        FrameTableEntry frame, ThreadCB thread){
        MyOut.print(thread, "\t Unreserve " + frame);
        if (frame.isReserved()) {
            frame.setUnreserved(thread.getTask());
        }
    }

    /**
    *   This method checks if there is no memory frames available
    *   @return false if there exists available frames, true otherwise.
    */
    private static boolean allFramesAreOccupied(){
        int frameTableSize = MMU.getFrameTableSize();
        FrameTableEntry frame = null;
        for (int i = 0; i < frameTableSize; i ++) {
            frame = MMU.getFrame(i);
            if (!frameIsOccupied(frame)) {
                return false;
            }
        }
        return true;
    }

    /**
    *   This method choose a frame for replacement using LRU algorithm.
    *   @return the selected frame. null if not enough memory
    */
    private static FrameTableEntry do_LRU(){
        return MMU.getLRUframe();
    }

    /**
    *   This method checks if a frame is locked or reserved
    *   @return false if the frame is free, true otherwise
    */
    private static boolean frameIsOccupied(FrameTableEntry frame){
        return frame.isOccupied();
    }

    /**
    *   This method free the frame from the original page. 
    */
    private static void do_freeFrame(FrameTableEntry frame){
        MMU.free(frame);
    }

    /**
    *   This method set the validating thread of a page.
    */
    synchronized private static void do_entering(
        ThreadCB thread, PageTableEntry page){
        MyOut.print(page, "Entering page fault.");
        while (page.getValidatingThread() != null) {
            thread.suspend(page);
        }
        page.setValidatingThread(thread);
    }

    /**
    *   This method unset the validating thread of a page.
    */
    private static int do_exit(int exitFlag, PageTableEntry page, SystemEvent event){
        MyOut.print(page, "Exiting page fault.");
        page.setValidatingThread(null);
        //The thread that caused the pagefault is resumed and placed on 
        //the ready queue. This is done by notifyThreads() of the event.
        if (event != null) {
            event.notifyThreads();    
        }
        //All threads that waiting on the page must be notified
        page.notifyThreads();
        //Call the dispatcher to give control of CPU
        ThreadCB.dispatch();
        do_stats(page, (exitFlag == SUCCESS)?true:false);
        return exitFlag;
    }

    /**
    *   This method is called before pagefault function return. It
    *   calculates some statistics and append them to a file.
    */
    private static void do_stats(PageTableEntry page, boolean successful){
        //Statistics
        MMU.addPFstats(successful);
        String statStr = "";

        statStr += ("[clock:" + HClock.get() + "]: SPF/PF/REF = (" + 
            MMU.getSuccessfulPFAmount() + "/" + 
            MMU.getPFAmount() + "/" + 
            MMU.getReferencedPageNum() + ")\r\n");
        //Write the statics to file
        if (fw == null) {
            String filename= "Statistics.txt";
            fw = new FileWriter(filename,true);
        }
        try{ //the true will append the new data
            fw.write(statStr);//appends the string to the file
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        MyOut.print(page, statStr);
    }
}
