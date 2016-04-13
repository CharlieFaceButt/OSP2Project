package osp.Memory;
// /**
//     The PageTable class represents the page table for a given task.
//     A PageTable consists of an array of PageTableEntry objects.  This
//     page table is of the non-inverted type.

//     @OSPProject Memory
// */
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
 //    /** 
	// The page table constructor. Must call
	
	//     super(ownerTask)

	// as its first statement.

	// @OSPProject Memory
 //    */
    public PageTable(TaskCB ownerTask)
    {
        // your code goes here
      super(ownerTask);
      int maxNumberOfPages = (int) Math.pow(2, MMU.getPageAddressBits());
      pages = new PageTableEntry[maxNumberOfPages];
      for (int i = 0; i < maxNumberOfPages; i++){
        pages[i] = new PageTableEntry(this, i);
      }
    }

    // /**
    //    Frees up main memory occupied by the task.
    //    Then unreserves the freed pages, if necessary.

    //    @OSPProject Memory
    // */
    public void do_deallocateMemory()
    {
        // your code goes here
      TaskCB task = getTask();
      for (int i = 0; i < MMU.getFrameTableSize(); i++) {
        FrameTableEntry frame = MMU.getFrame(i);
        PageTableEntry page = frame.getPage();
        if (page != null && page.getTask() == task) {
          frame.setPage(null);
          frame.setDirty(false);
          frame.setReferenced(false);
          if (frame.getReserved() == task) {
            frame.setUnreserved(task);
          }
        }
      }
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
