package osp.Tasks;

import java.util.Vector;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;

/**
    The student module dealing with the creation and killing of
    tasks.  A task acts primarily as a container for threads and as
    a holder of resources.  Execution is associated entirely with
    threads.  The primary methods that the student will implement
    are do_create(TaskCB) and do_kill(TaskCB).  The student can choose
    how to keep track of which threads are part of a task.  In this
    implementation, an array is used.

    @OSPProject Tasks
*/
public class TaskCB extends IflTaskCB
{
	private ArrayList<ThreadCB> ThreadList;
	private ArrayList<PortCB> PortsList;
	private ArrayList<OpenFile> OpenFilesList;
    /**
       The task constructor. Must have

       	   super();

       as its first statement.

       @OSPProject Tasks
    */
    public TaskCB()
    {
		// your code goes here
		super();

    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Tasks
    */
    public static void init()
    {
        // your code goes here

    }

    /** 
        Sets the properties of a new task, passed as an argument. 
        
        Creates a new thread list, sets TaskLive status and creation time,
        creates and opens the task's swap file of the size equal to the size
	(in bytes) of the addressable virtual memory.

	@return task or null

        @OSPProject Tasks
    */
    static public TaskCB do_create()
    {
		// your code goes here
		TaskCB newTask = new TaskCB();
		PageTable newPage = new PageTable(newTask);
		newTask.setPageTable(newPage);
		
		newTask.ThreadsList = new ArrayList<ThreadCB>();
		newTask.PortsList = new ArrayList<PortCB>();
		newTask.OpenFilesList = new ArrayList<OpenFile>();
		
		newTask.setCreationTime(HClock.get());
		newTask.setStatus(TaskLive);
		newTask.setPriority(1);
		
		String pathname = new String(SwapDeviceMountPoint+"/"+String.valueOf(newTask.getID()));
		int size = (int)Math.pow(2,MMU.getVirtualAddressBits());
		
		FileSys.create(pathname, size);
		OpenFile swapfile = OpenFile.open(pathname, newTask);
		newTask.setSwapFile(swapfile);
		
		if(swapfile == null) {
			ThreadCB.dispatch();
			newTask.do_kill();
			return null;
		}
		
		ThreadCB.create(newTask);
		return newTask;
    }

    /**
       Kills the specified task and all of it threads. 

       Sets the status TaskTerm, frees all memory frames 
       (reserved frames may not be unreserved, but must be marked 
       free), deletes the task's swap file.
	
       @OSPProject Tasks
    */
    public void do_kill()
    {
        // your code goes here
		this.setStatus;
		
		for(int i = this.ThreadsList.size() - 1; i >= 0; i--) {
			this.ThreadsList.get(i).kill();
		}
		
		for(int i = this.PortsList.size() - 1; i >= 0; i--) {
			this.PortsList.get(i).destroy();
		}
		
		for(int i = this.OpenFilesList.size() - 1; i >= 0; i--) {
			this.OpenFilesList.get(i).close();
		}
		
		PageTable pagetable = this.getPageTable();
		pagetable.deallocateMemory();
		
		String pathname = new String(SwapDeviceMountPoint+"/"+String.valueOf(this.getID()));
		FileSys.delete(pathname); 
    }

    /** 
	Returns a count of the number of threads in this task. 
	
	@OSPProject Tasks
    */
    public int do_getThreadCount()
    {
        // your code goes here
		return this.ThreadsList.size();
    }

    /**
       Adds the specified thread to this task. 
       @return FAILURE, if the number of threads exceeds MaxThreadsPerTask;
       SUCCESS otherwise.
       
       @OSPProject Tasks
    */
    public int do_addThread(ThreadCB thread)
    {
        // your code goes here
		if(this.do_getThreadCount() < ThreadCB.MaxThreadsPerTask) {
			this.ThreadList.add(thread);
			return TaskCB.SUCCESS;
		}
		else {
			return TaskCB.FAILURE;
		}
    }

    /**
       Removes the specified thread from this task. 		

       @OSPProject Tasks
    */
    public int do_removeThread(ThreadCB thread)
    {
        // your code goes here
		if(this.ThreadsList.contains(thread)) {
			this.ThreadsList.remove(thread);
			return TaskCB.SUCCESS;
		}
		else {
			return TaskCB.FAILURE;
		}
    }

    /**
       Return number of ports currently owned by this task. 

       @OSPProject Tasks
    */
    public int do_getPortCount()
    {
        // your code goes here
		return this.PortsList;
    }

    /**
       Add the port to the list of ports owned by this task.
	
       @OSPProject Tasks 
    */ 
    public int do_addPort(PortCB newPort)
    {
        // your code goes here
		if(this.do_getPortCount() < PortCB.MaxThreadsPerTask) {
			this.PortsList.add(newPort);
			return TaskCB.SUCCESS;
		}
		else {
			return TaskCB.FAILURE;
		}
    }

    /**
       Remove the port from the list of ports owned by this task.

       @OSPProject Tasks 
    */ 
    public int do_removePort(PortCB oldPort)
    {
        // your code goes here
		if(this.PortsList.contains(oldPort)) {
			this.PortsList.remove(oldPort);
			return TaskCB.SUCCESS;
		}
		else {
			return TaskCB.FAILURE;
		}
    }

    /**
       Insert file into the open files table of the task.

       @OSPProject Tasks
    */
    public void do_addFile(OpenFile file)
    {
        // your code goes here
		this.OpenFilesList.add(file);
    }

    /** 
	Remove file from the task's open files table.

	@OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
        // your code goes here
		if(this.OpenFilesList.contains(file)) {
			this.OpenFilesList.remove(file);
			return TaskCB.SUCCESS;
		}
		else {
			return TaskCB.FAILURE;
		}
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures
       in their state just after the error happened.  The body can be
       left empty, if this feature is not used.
       
       @OSPProject Tasks
    */
    public static void atError()
    {
        // your code goes here
		
    }

    /**
       Called by OSP after printing a warning message. The student
       can insert code here to print various tables and data
       structures in their state just after the warning happened.
       The body can be left empty, if this feature is not used.
       
       @OSPProject Tasks
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
