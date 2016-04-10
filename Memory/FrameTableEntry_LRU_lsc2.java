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
    // public int max_lru_count;
    // private int lru_count;
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
    // /**
    // *   This method get the count of LRU counter. Each frame
    // *   has a LRU counter, each time a frame is used, its LRU
    // *   count is reset to 0, and all other frames that has lower
    // *   count increase their count.
    // */
    // public int getLRUCount(){
    //     return lru_count;
    // }
    // /**
    // *   This method increase the LRU count of this frame by one
    // */
    // public void incrementLRUCount(){
    //     if (lru_count >= max_lru_count) {
    //         lru_count = max_lru_count;
    //     }
    //     lru_count ++;
    // }
    // /**
    // *   This method reset LRU count of this frame to 0
    // */
    // public void resetLRUCount(){
    //     lru_count = 0;
    // }
}
