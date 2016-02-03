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
*    The student module dealing with the creation and killing of
*    tasks.  A task acts primarily as a container for threads and as
*    a holder of resources.  Execution is associated entirely with
*    threads.  The primary methods that the student will implement
*    are do_create(TaskCB) and do_kill(TaskCB).  The student can choose
*    how to keep track of which threads are part of a task.  In this
*    implementation, an array is used.
*
*    @OSPProject Tasks
*/
public class TaskCB extends IflTaskCB
{

  private static int DefaultPriority;
  private static String DefaultStr;
  
    private PageTable taskPageTable;
    private int taskStatus;
    private int taskPriority;
    private ThreadCB taskThread;
    private int taskID;
    private double taskCreationTime;
    private OpenFile taskSwapFile;
    private Vector<ThreadCB> threadList;
    private Vector<OpenFile> openFileList;
    private Vector<PortCB> portList;

    /**
*       The task constructor. Must have
*
*           super();
*
*       as its first statement.
*
*       @OSPProject Tasks
    */
    public TaskCB()
    {
      super();
    }
    
    /**
     * Setup table for threads, ports, and files
     */
    public void setupResources(){
      MyOut.print(this, DefaultStr + " setupResources()");
        threadList = new Vector<ThreadCB>();
        openFileList = new Vector<OpenFile>();
        portList = new Vector<PortCB>();
    }
    
    /**
    *   Sets the page table of the task.
    */
    final public void setPageTable(PageTable table){
      MyOut.print(this, DefaultStr + " setPageTable()");
      System.out.println("XXL@TaskCB.setpageTable");
        this.taskPageTable = table;
    }
    /**
    *   Returns the page table of the task.
    */
    final public PageTable getPageTable(){
      MyOut.print(this, DefaultStr + " getPageTable()");
        return this.taskPageTable;
    }
    /**
    *   Sets the status of the task.
    */
    final public void setStatus(int status){
      ///?
      MyOut.print(this, DefaultStr + " setStatus to " + status);
        this.taskStatus = status;
    }
    /**
    *   Returns the status of the task. Allowed
    *   values are TaskLive and TaskTerm.
    */
    final public int getStatus(){
      MyOut.print(this, DefaultStr + " getStatus");
        ///?
        return (this.taskStatus == GlobalVariables.TaskTerm) ? this.taskStatus : GlobalVariables.TaskLive;
    }
    /**
    *   Sets the priority of the task
    */
    final public void setPriority(int priority){
      MyOut.print(this, DefaultStr + " setPriority to " + priority);
        this.taskPriority = priority;
    }
    /**
    *   Returns the priority of the task
    */
    final public int getPriority(){
      MyOut.print(this, DefaultStr + " getPriority()");
        return this.taskPriority;
    }
    /**
    *   Sets the current thread of the task.
    */
    public void setCurrentThread(ThreadCB t){
      MyOut.print(this, DefaultStr + " setCurrentThread:" + t);
        if (!threadList.contains(t)) {
            addThread(t);
        }
        
        this.taskThread = t;
    }
    /**
    *   Return the current thread of the task. The current thread is the thread
    *   that will run when the task is made current by the dispatcher.
    */
    public void getCurrentThread(){
      MyOut.print(this, DefaultStr + " getCurrentThread");
        return this.taskThread;
    }
    /**
    *   Returns the ID of the task.
    */
    final public int getID(){
      MyOut.print(this, DefaultStr + " getID()");
        return this.taskID;
    }
    /**
    *   Sets the task creation time to time
    */
    final public void setCreationTime(double time){
      MyOut.print(this, DefaultStr + " setCreationTime as " + time);
        this.taskCreationTime = time;
    }
    /**
    *   Returns the task creation time.
    */
    final public double getCreationTime(){
      MyOut.print(this, DefaultStr + " getCreationTime()");
        return this.taskCreationTime;
    }
    /**
    *   Sets the swap file of task to file.
    */
    public final void setSwapFile(OpenFile file){
      MyOut.print(this, DefaultStr + " setSwapFile as " + file);
        this.taskSwapFile = file;
    }
    /**
    *   Returns the swap file of the task.
    */
    public final OpenFile getSwapFile(){
      MyOut.print(this, DefaultStr + " getSwapFile()");
        return this.taskSwapFile;
    }

    /**
*       This method is called once at the beginning of the
*       simulation. Can be used to initialize static variables.
*
*       @OSPProject Tasks
    */
    public static void init()
    {
      DefaultStr = "XXL@";
      MyOut.print(this, DefaultStr + " priority is initiated to 2");
      DefaultPriority = 2;
    }

    /** 
*        Sets the properties of a new task, passed as an argument. 
        *
*        Creates a new thread list, sets TaskLive status and creation time,
*        creates and opens the task's swap file of the size equal to the size
* (in bytes) of the addressable virtual memory.
*
* @return task or null
*
*        @OSPProject Tasks
    */
    static public TaskCB do_create()
    {
      MyOut.print(this, DefaultStr + " creating task...");
        // Creates Task object
        TaskCB newTask = new TaskCB();

        // Create PageTable and associate it to the task
        PageTable newPageTable = new PageTable(newTask);
        newTask.setPageTable(newPageTable);

        // Create table for ThreadCBs, PortCBs and OpenFiles for task
        newTask.setupResources();
        
        // Set task-creation time, set status to TaskLive, and set priority
        newTask.setCreationTime(HClock.get());
        newTask.setStatus(GlobalVariables.TaskLive);
        newTask.setPriority(DefaultPriority);


        /**
        *   Create swap file, get address space size by MMU.getVirtualAddressBits(), 
        *   set name as ID, get directory by SwapDeviceMountPoint. Use FileSys.create()
        *   to create and then use OpenFile.open() to open it.
        */
        FileSys.create(Integer.toString(newTask.getID()), MMU.getVirtualAddressBits());
        OpenFile swapFile = OpenFile.open(GlobalVariables.SwapDeviceMountPoint + Integer.toString(newTask.getID()), newTask);


        /**
        *   Save the file handle using setSwapFile(), but dispatch a new thread
        *   if fail to open; set swap file to the task otherwise
        */
        if (swapFile == null) {
      MyOut.warning(this, DefaultStr + " task creation fail.");
            ThreadCB.dispatch();
            return null;
        } else{
           newTask.setSwapFile(swapFile);
        }

        // Create the first thread of the task
        ThreadCB firstThread = ThreadCB.create(newTask);
        newTask.setCurrentThread(firstThread);
        
        // Return the Task object
      MyOut.print(this, DefaultStr + " task creation successful.");
        return newTask;
    }


    /**
*       Kills the specified task and all of it threads. 
*
*       Sets the status TaskTerm, frees all memory frames 
*       (reserved frames may not be unreserved, but must be marked 
*       free), deletes the task's swap file.
 *
*       @OSPProject Tasks
    */
    public void do_kill()
    {
      MyOut.print(this, DefaultStr + " killing task");
        // Kill all threads
      MyOut.print(this, DefaultStr + " release threads, ports and memories.");
        for (ThreadCB newTask : threadList) {
            newTask.kill();
        }

        // Destroy all ports
        for (PortCB newPort : portList) {
            newPort.destroy();
        }

      MyOut.print(this, DefaultStr + " set task status to terminate.");
        // Terminate task
        setStatus(GlobalVariables.TaskTerm);

        // Release memory
 PageTable pagetable = this.getPageTable();
        pagetable.deallocateMemory();

      MyOut.print(this, DefaultStr + " close swap files of task");
        // Close all swap files
        for (OpenFile newOpenFile : openFileList) {
            newOpenFile.close();
        }
        FileSys.delete(GlobalVariables.SwapDeviceMountPoint + Integer.toString(getID());
    }

    /** 
* Returns a count of the number of threads in this task. 
 *
* @OSPProject Tasks
    */
    public int do_getThreadCount()
    {
 return threadList.size();
    }

    /**
 *      Adds the specified thread to this task. 
   *    @return FAILURE, if the number of threads exceeds MaxThreadsPerTask;
 *      SUCCESS otherwise.
  *     
    *   @OSPProject Tasks
  *  */
    public int do_addThread(ThreadCB thread)
    {
      MyOut.print(this, DefaultStr + " add thread " + thread + " to task");
        if (threadList.size() >= ThreadCB.MaxThreadsPerTask) {
            return GlobalVariables.FAILURE;
        }
        if (!threadList.contains(thread)) {
            threadList.add(thread);
        }
        return GlobalVariables.SUCCESS;
    }

    /**
   *    Removes the specified thread from this task.   
*
   *    @OSPProject Tasks
    */
    public int do_removeThread(ThreadCB thread)
    {
      MyOut.print(this, DefaultStr + " remove thread " + thread + " from task");
      if (!threadList.contains(thread)) {
      MyOut.warning(this, DefaultStr + " fail to remove thread " + thread);
            return GlobalVariables.FAILURE;
        }
        threadList.remove(thread);        
        return GlobalVariables.SUCCESS;
    }

    /**
    *   Return number of ports currently owned by this task. 
*
    *   @OSPProject Tasks
    */
    public int do_getPortCount()
    {
        return portList.size();
    }

    /**
     *  Add the port to the list of ports owned by this task.
* 
     *  @OSPProject Tasks 
    */ 
    public int do_addPort(PortCB newPort)
    {
      MyOut.print(this, DefaultStr + " add port " + newPort + " to task.");
        if (portList.size() >= PortCB.MaxPortsPerTask) {
      MyOut.print(this, DefaultStr + " cannot add new port cause index out of bound");
            return GlobalVariables.FAILURE;
        }
        if (!portList.contains(newPort)) {
            portList.add(newPort);
        }
        return GlobalVariables.SUCCESS;
    }

    /**
      * Remove the port from the list of ports owned by this task.
*
      * @OSPProject Tasks 
    */ 
    public int do_removePort(PortCB oldPort)
    {
      MyOut.print(this, DefaultStr + " remove port " + oldPort + " from task.");
        if (!portList.contains(oldPort)) {
      MyOut.print(this, DefaultStr + " fail to remove port " + oldPort + " from task.");
            return GlobalVariables.FAILURE;
        }
        portList.remove(oldPort);
        return GlobalVariables.SUCCESS;
    }

    /**
       *Insert file into the open files table of the task.
*
       *@OSPProject Tasks
    */
    public void do_addFile(OpenFile file)
    {
      MyOut.print(this, DefaultStr + " add file " + file + " to task.");
        if (!openFileList.contains(file)) {
            openFileList.add(file);
        }
    }

    /** 
* Remove file from the task's open files table.
*
* @OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
      MyOut.print(this, DefaultStr + " remove file " + file + " from task.");
        if (!openFileList.contains(file)) {
      MyOut.print(this, DefaultStr + " fail to remove file " + file + " from task.");
            return GlobalVariables.FAILURE;
        }
        openFileList.remove(file);
        return GlobalVariables.SUCCESS;
    }

    /**
 *      Called by OSP after printing an error message. The student can
 *      insert code here to print various tables and data structures
 *      in their state just after the error happened.  The body can be
  *     left empty, if this feature is not used.
  *     
  *     @OSPProject Tasks
    */
    public static void atError()
    {
      MyOut.error(this, DefaultStr + " qicheren chuji!");
    }

    /**
*       Called by OSP after printing a warning message. The student
*       can insert code here to print various tables and data
*       structures in their state just after the warning happened.
*       The body can be left empty, if this feature is not used.
*       
*       @OSPProject Tasks
    */
    public static void atWarning()
    {
      MyOut.warning(this, DefaultStr + " blublu...");
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
