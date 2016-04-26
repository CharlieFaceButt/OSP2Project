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
        int maxNumberOfPagesAllowed = (int)Math.pow(2, pageAddressBit);

        //? how to determine the number of pages
        //Initiate page table
        pages = new PageTableEntry[maxNumberOfPagesAllowed];
        //Create each page table entry
        for (int i = 0; i < maxNumberOfPagesAllowed; i ++) {
            //?correct page number
            pages[i] = new PageTableEntry(this, i);
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
        MyOut.print(getTask(), "Deallocate memory of a task.");
        int frameTableSize = MMU.getFrameTableSize();
        FrameTableEntry frame = null;
        for (int i = 0; i < frameTableSize; i++) {
            frame = MMU.getFrame(i);
            PageTableEntry page = frame.getPage();
            if (page != null &&
                page.getTask().getID() == getTask().getID()){
                MMU.free(frame);
            }
        }
        //?Unlocking inside the memory management module can lead to 
        //inconsistencies
    }

}