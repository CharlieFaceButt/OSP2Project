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
        long maxNumberOfPagesAllowed = (long)Math.(2, pageAddressBit);

        //Initiate page table
        pages = new GenericList(null);
        //Create each page table entry
        for (long i = 0; i < maxNumberOfPagesAllowed; i ++) {
          //?correct page number
          pages.insert(new PageTableEntry(this, i));
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
        Enumeration enum = pages.forwardIterator();
        while(enum.hasMoreElements()){
          Object obj = enum.nextElement();
          PageTableEntry entry = (PageTableEntry)obj;
          //If the frame is allocated to the task, unset the flags for
          //this frame
          if (entry.getTask().getID() == getTask().getID()) {
            FrameTableEntry frame = entry.getFrame();
            frame.setPage(null);
            frame.setDirty(False);
            frame.setReferenced(False);
            frame.setReserved(False); //?
          }
        }
        //?Unlocking inside the memory management module can lead to 
        //inconsistencies
    }

}