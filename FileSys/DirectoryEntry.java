package osp.FileSys;

import java.util.*;
import osp.IFLModules.*;
import osp.Utilities.*;

/**
   Implements the file system directory entry.

   @OSPProject FileSys
*/
public class DirectoryEntry extends IflDirectoryEntry
{
    /** Creates a new directory entry for an already existing inode.
	This constructor must have

	    super(pathname,type,inode);
	    
	as its first statement.

        @param pathname name of file or directory
        @param type type of directory entry: file or directory
        @param inode file's inode

	@OSPProject FileSys
    */
    public DirectoryEntry(String pathname, int type, INode inode)
    {
        // your code goes here

    }

    /** 
        Returns the i-node associated with the given name. 
        @param pathname pathname to return inode for
        @return an INode object 

        @OSPProject FileSys
    */
    public static INode do_getINodeOf(String pathname)
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
