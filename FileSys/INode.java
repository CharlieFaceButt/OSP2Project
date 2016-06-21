package osp.FileSys;

import java.util.Vector;
import osp.IFLModules.*;
import osp.Devices.*;
import osp.Utilities.*;


/**
*   Implements the Unix-like INode data structure, which keeps
*   information about the storage of the file, the number of hard links
*   to it, the number of times it is open, etc. Does NOT keep the name
*   of the file: there can be several hard links to the same INode.
*
*   @OSPProject FileSys
*/
public class INode extends IflINode 
{
    /**
    *       Creates a new inode on a specified device.
    *       This constructor must have
    *
    *	    super(deviceId);
    *       as its first statement.
    *
    *       @param deviceID device number to create the inode on
    *
    *       @OSPProject FileSys
    */
    public INode(int deviceID)
    {
        super(deviceID);
        // your code goes here

    }

    /** 
    *   Tests whether a block on a device is free.
    *   @param deviceID device to test the block on
    *   @param block number of block to test
    *   @return true if block is free, false if used or deviceID is invalid
    *
    *	@OSPProject FileSys
    */
    public static boolean do_isFreeBlock(int block, int deviceID)
    {
        //Get device
        if (deviceID < 0 || deviceID >= Device.getTableSize()) {
            System.err.println("Device ID error.");
            MyOut.error(this, "Device ID not in range.");
        }
        Device device = Device.get(deviceID);


        if (block < 0 || block >= device.getNumberOfBlocks()) {
            System.err.println("Block number error.");
            MyOut.error(this, "Block number not in range.");
        }
        // your code goes here

    }

    /** 
    *   Allocates a free block to inode and 
    *	returns the block number of that block.
    *	Marks that block as used.
    *
    *   @return integer in the range from 0 to numberOfBlocks-1 
    *   if a free block was found on the inode's device, NONE otherwise.
    *
    *   @OSPProject FileSys  
    */
    public int do_allocateFreeBlock()
    {
        // your code goes here

    }

    /**
    *       Release all blocks allocated to the given inode (i.e., make them free).
    *
    *       @param inode to de-allocate free block on
    *
    *        @OSPProject FileSys
    */
    public void do_releaseBlocks()
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
