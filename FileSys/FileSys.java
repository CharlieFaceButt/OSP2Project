package osp.FileSys;

import java.util.*;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Memory.*;

/**
   Implements the OSP file system. Supports operations like file
   creation and deletion, linking, opening, closing, reading, and
   writing files.

   @OSPProject FileSys
*/
public class FileSys extends IflFileSys
{
    /** 
        This method is called once before the simulation starts.
        Can be used to initialize static data structures. 

        @OSPProject FileSys
    */
    public static void init()
    {
        // your code goes here

    }

    /** 
        Creates a new file or directory with the given pathname
        if it does not already exist.

        This system call determines whether <code>pathname</code> 
        is a name of file or a directory. It must check, whether 
        a file or directory with such name already exists, 
        determine, which device it must be created on, find out, 
        whether there is enough space on the device for creation of
        the file with the initial size <code>size</code> bytes. 
        If it's a directory, the second parameter must be ignored
	(directory size is always 1 block). 
        If the parent directory of the file or dir to be created 
        does not exist, it must be created with by calling
        <code>create()</code> 

        This system call must create and properly initialize the 
        new <code>INode</code> and <code>DirectoryEntry</code> objects, 
        allocate the necessary amount of device blocks to hold 
        the file's data if the parameter 
        <code>size</code> is not zero, update the internal data 
        structures that hold information about the existing 
        i-nodes and directory entries and return. 

        The system call returns SUCCESS if the file or dir creation 
        was successfull, FAILURE otherwise. 

        @param pathname pathname to create. 
        @param size initial file size, ignored for directories
	(directory size is always 1 block).
        @return SUCCESS or FAILURE.
	FAILURE is returned if the device has no space, if the name is 
	a mount point, if the file with this name already exists, or if 
	we cannot create intermediate directories when creating a file.
	
        @OSPProject FileSys

    */
    final static public int do_create(String pathname, int size) 
    {
        // your code goes here

    }

    /** 
        Creates a new hard link.
        
        This system call associates the new name <code>linkname</code> 
        with the existing i-node associated with <code>name</code>.
        If the parent directory of <code>linkname</code> does not exist, 
	return with FAILURE. (Note that this is different from do_create().
        The hard link counter of that <code>INode</code> object is  
        incremented. 

        The system call makes sure that the link is created on the 
        same device as the original i-node. 
	Also, links to directories are not allowed.

        Returns FAILURE if there exists a directory entry with the 
        name <code>linkname</code> or if <code>name</code> does 
        not exist or is a directory. Returns SUCCESS if nothing went wrong.

        @param name name of the file or dir to link. 
        @param linkname name of the link to create.
        @return SUCCESS or FAILURE.
	
        @OSPProject FileSys
    */
    final static public int do_link(String pathname, String linkname) 
    {
        // your code goes here

    }

    /** This system call deletes a file or directory. 

        If the file is open, its <code>INode</code> cannot be deleted, 
        the file's blocks on the device must remain allocated 
        until the i-node can be cleaned up in the <code>close()</code> 
        system call, at which point the device blocks must also be set 
        free. However the directory entry for the file must disappear
	so that creation of a new file with the same name will again be
	possible regardless of whether the i-node is still there or not.

        A directory can be deleted only if it is empty. 

        @param name pathname of file or dir to delete
        @return SUCCESS or FAILURE. FAILURE if file is a mount point,
        does not exist, or if deleting a non-empty directory.

        @OSPProject FileSys
    */
    final static public int do_delete(String pathname) 
    {
        // your code goes here

    }

    /** 
        List the contents of a directory.

        This system call returns a <code>java.util.Vector</code> 
        object containing <code>String</code> objects, each of them 
        a fully qualified name of a file or subdirectory in the 
        directory <code>dirname</code>. Returns FAILURE if 
        <code>dirname</code> is not a directory or does not exist.

        @param dirname directory name to list contents of
        @return Vector containing the String objects with names of all 
	directory entries in the directory. NULL if the directory 
	does not exist.
	
        @OSPProject FileSys
    */
    final static public Vector do_dir(String dirname) 
    {
        // your code goes here

    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject FileSys
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
	
	@OSPProject FileSys
     */
    public static void atWarning()
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
