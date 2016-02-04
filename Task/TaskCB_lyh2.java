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
*       	   super();
*
*       as its first statement.
*
*       @OSPProject Tasks
    */
    public TaskCB()
    {
	super();
        threadList = new Vector<ThreadCB>();
        openFileList = new Vector<OpenFile>();
        portList = new Vector<PortCB>();
    }
    /**
    *   Sets the page table of the task.
    */
    final public void setPageTable(PageTable table){
        this.taskPageTable = table;
    }
    /**
    *   Returns the page table of the task.
    */
    final public PageTable getPageTable(){
        return this.taskPageTable;
    }
    /**
    *   Sets the status of the task.
    */
    final public void setStatus(int status){
        this.taskStatus = status;
    }
    /**
    *   Returns the status of the task. Allowed
    *   values are TaskLive and TaskTerm.
    */
    final public int getStatus(){
        ///?
        return (this.taskStatus == GlobalVariables.TaskTerm) ? this.taskStatus : GlobalVariables.TaskLive;
    }
    /**
    *   Sets the priority of the task
    */
    final public void setPriority(int priority){
        this.taskPriority = priority;
    }
    /**
    *   Returns the priority of the task
    */
    final public int getPriority(){
        return this.taskPriority;
    }
    /**
    *   Sets the current thread of the task.
    */
    public void setCurrentThread(ThreadCB t){
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
        return this.taskThread;
    }
    /**
    *   Returns the ID of the task.
    */
    final public int getID(){
        return this.taskID;
    }
    /**
    *   Sets the task creation time to time
    */
    final public void setCreationTime(double time){
        this.taskCreationTime = time;
    }
    /**
    *   Returns the task creation time.
    */
    final public double getCreationTime(){
        return this.taskCreationTime;
    }
    /**
    *   Sets the swap file of task to file.
    */
    public final void setSwapFile(OpenFile file){
        this.taskSwapFile = file;
    }
    /**
    *   Returns the swap file of the task.
    */
    public final OpenFile getSwapFile(){
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
    }

    /** 
*        Sets the properties of a new task, passed as an argument. 
        *
*        Creates a new thread list, sets TaskLive status and creation time,
*        creates and opens the task's swap file of the size equal to the size
*	(in bytes) of the addressable virtual memory.
*
*	@return task or null
*
*        @OSPProject Tasks
    */
    static public TaskCB do_create()
    {
        // Creates Task object
        TaskCB newTask = new TaskCB();

        // Create PageTable and associate it to the task using setPageTable()
        PageTable newPageTable = new PageTable(newTask);
        newTask.setPageTable(newPageTable);

        // Set task-creation time, set status to TaskLive, and set priority
        newTask.setCreationTime(HClock.get());
        newTask.setStatus(GlobalVariables.TaskLive);
        newTask.setPriority(2);

        // Create ThreadCB, PortCB and OpenFile for task
        ///?

        /**
        *   Create swap file, get address space size by MMU.getVirtualAddressBits(), 
        *   set name as ID, get directory by SwapDeviceMountPoint. Use FileSys.create()
        *   to create and then use OpenFile.open() to open it.
        */

        FileSys.create(Integer.toString(newTask.getID()), MMU.getVirtualAddressBits());
        OpenFile swapFile = OpenFile.open(GlobalVariables.SwapDeviceMountPoint + Integer.toString(newTask.getID()), newTask);
	newTask.setSwapFile(swapFile);

        /**
        *   Save the file handle using setSwapFile(), but dispatch a new thread
        *   if fail to open
        */
        if (swapFile == null) {
            ThreadCB.dispatch();
            return null;
        }

	ThreadCB.create(newTask);
        // return the Task object
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
        // Kill all threads
        for (ThreadCB newTask : threadList) {
            newTask.kill();
        }

        // Destroy all ports
        for (PortCB newPort : portList) {
            newPort.destroy();
        }

        // Terminate task
        setStatus(GlobalVariables.TaskTerm);

        // Release memory
	PageTable pagetable = this.getPageTable();
        pagetable.deallocateMemory();

        // Close all swap files
        for (OpenFile newOpenFile : openFileList) {
            newOpenFile.close();
        }

        FileSys.delete(GlobalVariables.SwapDeviceMountPoint + Integer.toString(getID());
    }

    /** 
*	Returns a count of the number of threads in this task. 
	*
*	@OSPProject Tasks
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
        if (getCurrentThread() == thread) {
            //return GlobalVariables.FAILURE;
            //setCurrentThread(NULL);
        }
	if (!threadList.contains(thread)) {
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
        if (portList.size() >= PortCB.MaxPortsPerTask) {
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
        if (!portList.contains(oldPort)) {
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
        if (!openFileList.contains(file)) {
            openFileList.add(file);
        }
    }

    /** 
*	Remove file from the task's open files table.
*
*	@OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
        if (!openFileList.contains(file)) {
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
        System.out.println("YOYO!");
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
        System.out.println("blublub...lublu...");
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
