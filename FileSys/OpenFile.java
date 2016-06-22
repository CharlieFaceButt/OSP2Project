package osp.FileSys;

import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Devices.*;
import osp.Memory.*;

/**
   Implements the open file data structure.
   Read(), write(), and close() use this class as a file handle.

   @OSPProject FileSys

*/
public class OpenFile extends IflOpenFile
{
    /**
       Creates new instances of OpenFile.
       This constructor must have

       	   super(inode, task);
	   
       as its first statement.

       @param inode inode associated with the open file
       @param task  owner task

       @OSPProject FileSys
     */
    public OpenFile(INode inode, TaskCB task)
    {
        // your code goes here

    }

    /** 
        Opens an existing file for reading and writing.
        
        Opens the file with the specified pathname if it exists.
        Increments the count of open files for inode, creates an 
        <code>OpenFile</code> object and returns it 
        or null if the file for some reason cannot be opened, e.g., 
        if it does not exist.
        The system call must also insert this
        <code>OpenFile</code> object into the list of files opened 
        by the task that owns the thread that opened the file. 
	This is done using <code>addFile()</code>, which is a method of TaskCB.

	Mount points cannot be open.

        @param filename name of file to open.
	@param task to which the file should belong
        @return an OpenFile object or null in case of failure.
	
        @OSPProject FileSys
    */
    static public OpenFile do_open(String filename, TaskCB task) 
    {
        // your code goes here

    }

    /** Closes an open file. 

        This system call closes the open file and destroys the 
        <code>OpenFile</code> object by removing 
        it from the file system internal data structures. If all 
        directory entries associated with this i-node are deleted, 
        the i-node must be destroyed and its device blocks must be 
        marked as free. The file cannot be closed -- and the system 
        call must return FAILURE -- if there is an unfinished I/O 
        operation going on with this file. Such situation is indicated 
        by the iorbCount field of the OpenFile object being non-zero. 
        In this case the system call must only set the closePending 
        field of the object to <code>true</code>.

	On successful completion, should set closePending to false and
	use <code>removeFile()</code> to remove the file from the
	corresponding task.

        @return SUCCESS or FAILURE if either file is not open or 
        outstanding IORBs exist.
	
        @OSPProject FileSys
    */
    public int do_close() 
    {
        // your code goes here

    }

    /** 
        Reads from an opened file.

        This system call reads an amount of data equal to the size 
        of the memory page and device block (both sizes are equal in 
        <i>OSP</i> by convention) from the file at block number
        <code>fileBlockNumber</code> and writes the data to the virtual memory 
        page <code>memoryPage</code>. <code>File</code> must be a
        valid open file. The file should have more blocks than
        <code>fileBlockNumber</code>; <code>fileBlockNumber</code> must be 
        greater or equal to zero, and the <code>memoryPage</code> 
        must be not null. 

        An <code>IORB</code> object must be created 
        using a 6-argument <code>IORB()</code> constructor, the 
        <code>IORB</code> must be enqueued with a call to 
        <code>Device.get(deviceID).enqueueIORB()</code>. 

        The current thread (<code>MMU.getPTBR().getTask().getCurrentThread())
	must be suspended on the IORB by calling <code>suspend()</code>
        until the I/O is finished.

        If at least one of the listed conditions does not hold, 
        the system call must not create an IORB object or suspend 
        a thread but simply return FAILURE.

        @param fileBlockNumber block number within the file
        @param memoryPage memory page to write to.
	@param thread thread that invokes the I/O
        @return SUCCESS if the parameters are OK and the I/O operation has 
        been started.
        FAILURE if some of the parameter set is wrong.

        @OSPProject FileSys
    */
    public int do_read(int fileBlockNumber, 
		       PageTableEntry memoryPage,
		       ThreadCB thread)
    {
        // your code goes here

    }

    /** 
        Writes to an opened file.

        This system call is very similar to <code>do_read()</code> 
        except that the data goes in the opposite direction:
        from memory page to the open file. If the file has fewer blocks than
        <code>fileBlockNumber</code>, it grows unless there is no more space 
	on the device, in which case FAILURE is returned.

	Waits for the I/O operation to complete by suspending the thread
	on IORB.

        @param fileBlockNumber block number to which to write
        @param memoryPage memory page to read from 
	@param thread thread that invoked this operation
        @return SUCCESS if the parameters are OK and the IORB has been 
	successfully created and enqueued.
        FAILURE if some of the parameters are wrong.

        @OSPProject FileSys
    */
    public int do_write(int fileBlockNumber, 
			PageTableEntry memoryPage,
			ThreadCB thread)
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
