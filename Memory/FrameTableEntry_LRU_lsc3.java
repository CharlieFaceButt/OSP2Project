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
    public FrameTableEntry(int frameID){
        super(frameID);
        // max_lru_count = MMU.getFrameTableSize();
        // lru_count = -1;
    }

    public boolean isOccupied(){
        if (getLockCount() > 0 || isReserved()) {
            return true;
        } else return false;
    }
}
