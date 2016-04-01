package osp.Memory;

/**
*    The FrameTableEntry class contains information about a specific page
*    frame of memory.
*
*   There are several inherited methods:
*     int getLockCount()
*     void incrementLockCount()
*     void decrementLockCount()
*     isReserved, getReserved, setReserved, setUnreserved
*     isDirty, setDirty
*     getreferenced, setreferenced
*     getID
*     getPage, setPage
*
*    @OSPProject Memory
*/
import osp.Tasks.*;
import osp.Interrupts.*;
import osp.Utilities.*;
import osp.IFLModules.IflFrameTableEntry;

public class FrameTableEntry extends IflFrameTableEntry
{
    /**
    *       The frame constructor. Must have
    *
    *       	   super(frameID)
	  *
    *       as its first statement.
    *
    *       @OSPProject Memory
    */
    public FrameTableEntry(int frameID)
    {
        super(frameID);

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
