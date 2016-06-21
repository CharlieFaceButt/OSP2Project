package osp.FileSys;

import java.util.*;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Utilities.*;
import osp.Devices.*;
import java.io.*;


/**
*   This class implements a mount table -- an array of directory names
*   associated with logical devices.
*
*   Note that all methods in this class are static, so the mount table
*   has to be implemented as a static data structure.
*
*   @OSPProject FileSys
*/

public class MountTable extends IflMountTable
{
    /**
    *       Returns true, if dirname is a mount point; false otherwise.
    *       @param dirname
    *
    *       @OSPProject FileSys
    */
    public static boolean do_isMountPoint(String dirname)
    {
        int id = getDeviceID(dirname);
        String mountPoint = getMountPoint(id);
        if (dirname.equals(mountPoint)) {
            System.out.print(dirname + " is a mount point.");
            return true;
        } else {
            System.out.print(dirname + " is not a mount point.");
            return false;
        }
    }

    /**
    *       Returns the Id of the device where <code>pathname</code> resides.
    *
    *       @param pathname a file or directory name to find the deviceID for
    *       @return deviceID of the device, where this file or dir resides, 
    *       NONE if the file device is not found. Since there is a root directory 
    *       with mount poit that consists just of a DirSeparator, this 
    *       should only happen if the name does not start with the directory 
    *       separator symbol.
    *
    *       @OSPProject FileSys
    */
    public static int do_getDeviceID(String pathname)
    {
        Vector<String> pathHier = getPathHierarchy("root/" + pathname);
        //Compare mount point path with the pathname
        int tableSize = Device.getTableSize();
        int bestMatches = 0;
        int suggestMount = 0;
        for (int i = 0 ; i < tableSize ; i ++) {
            String mountPoint = getMountPoint(i);
            Vector<String> mountHier = getPathHierarchy("root/" + mountPoint);
            int matches = getMatches(pathHier, mountHier);
            if (matches > bestMatches) {
                bestMatches = matches;
                suggestMount = i;
            }
        }
        return suggestMount;
    }

    /**
    *   This method compares a mount point and a path, then determine
    *   how much they matches.
    *
    *   @param pathHier the hierarchical description of a path
    *   @param mountHier the hierarchical description of a mount point
    *   @return the depth of identical path between them. 0 means not 
    *   match. For example, path /foo/bar/abc and mount /foo/bar will 
    *   produce 2; while the same path and mount /foo/bar/ab will pro-
    *   duce 0 because they do not match.
    */
    private static int getMatches(
        Vector<String> pathHier, Vector<String> mountHier)
    {
        int match = 0;
        for (int i = 0 ; i < mountHier.size() ; i ++) {
            String dirName = mountHier.get(i);
            if (pathHier.size() <= i){
                return 0;
            } else if (!pathHier.get(i).equals(dirName)) {
                return 0;
            }
            match ++;
        }
        return match;
    }

    /**
    *   Get a list of string that hierarchically describe the pathname
    */
    private static Vector<String> getPathHierarchy(String pathname)
    {
        //Get hierarchical names of the path
        String[] pathSplit = pathname.split("/");
        Vector<String> hierarchy = new Vector<String>();
        for (String dirName : pathSplit) {
            if (dirName != null && dirName.length() > 0) {
                hierarchy.add(dirName);
            }
        }
        return hierarchy;
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
