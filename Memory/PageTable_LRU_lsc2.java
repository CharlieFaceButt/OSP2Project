package osp.Memory;
/**
*    The PageTable class represents the page table for a given task.
*    A PageTable consists of an array of PageTableEntry objects.  This
*    page table is of the non-inverted type.
*
*    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
    /** 
    *	The page table constructor. Must call
	  *
    *	    super(ownerTask)
    *
    *	as its first statement.
    *
    *   Page table is assumed to be an array of the size equal to the
    *   maximum number of pages allowed.
    *
    *	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        super(ownerTask);

        //Get max number of pages allowed
        int pageAddressBit = MMU.getPageAddressBits();
        int maxNumberOfPagesAllowed = (int)Math.(2, pageAddressBit);

        //Initiate page table
        pages = new PageTableEntry[maxNumberOfPagesAllowed];
        //Create each page table entry
        for (int i = 0; i < maxNumberOfPagesAllowed; i ++) {
          //?correct page number
          pages[i] = new PageTableEntry(this, i));
        }
    }

    /**
    *       Frees up main memory occupied by the task.
    *       Then unreserves the freed pages, if necessary.
    *
    *       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        for (int i = 0; i < pages.length; i ++) {
            PageTableEntry entry = pages[i];
            //If the frame is allocated to the task, unset the flags for
            //this frame
            if (entry.getTask().getID() == getTask().getID()) {
            FrameTableEntry frame = entry.getFrame();
            frame.setPage(null);
            frame.setDirty(FALSE);
            frame.setReferenced(FALSE);
            frame.setReserved(FALSE); //?
          }   
        }
        //?Unlocking inside the memory management module can lead to 
        //inconsistencies
    }

}