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

    private PageTable taskPT;
    private int taskST;
    private int taskPriority;
    private ThreadCB taskThread;
    private int taskID;
    private double taskCT;
    private OpenFile taskSF;
    private Vector<ThreadCB> tList;
    private Vector<OpenFile> fList;
    private Vector<PortCB> pList;

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
        tList = new ArrayList<ThreadCB>();
        fList = new ArrayList<OpenFile>();
        pList = new ArrayList<PortCB>();
    }
    /**
    *   Sets the page table of the task.
    */
    final public void setPageTable(PageTable table){
        this.taskPT = table;
    }
    /**
    *   Returns the page table of the task.
    */
    final public PageTable getPageTable(){
        return this.taskPT;
    }
    /**
    *   Sets the status of the task.
    */
    final public void setStatus(int status){
        this.taskST = status;
    }
    /**
    *   Returns the status of the task. Allowed
    *   values are TaskLive and TaskTerm.
    */
    final public int getStatus(){
        ///?
        return (this.taskST == GlobalVariables.TaskTerm) ? this.taskST : GlobalVariables.TaskLive;
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
    final public int getPriority(int priority){
        return this.taskPriority;
    }
    /**
    *   Sets the current thread of the task.
    */
    public void setCurrentThread(ThreadCB t){
        if (!tList.contains(t)) {
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
        this.taskCT = time;
    }
    /**
    *   Returns the task creation time.
    */
    final public double getCreationTime(){
        return this.taskCT;
    }
    /**
    *   Sets the swap file of task to file.
    */
    public final void setSwapFile(OpenFile file){
        this.taskSF = file;
    }
    /**
    *   Returns the swap file of the task.
    */
    public final OpenFile getSwapFile(){
        return this.taskSF;
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
        TaskCB tcb = new TaskCB();

        // Create PageTable and associate it to the task using setPageTable()
        PageTable pt = new PageTable(tcb);
        tcb.setPageTable(pt);

        // Set task-creation time, set status to TaskLive, and set priority
        tcb.setCreationTime(HClock.get());
        tcb.setStatus(GlobalVariables.TaskLive);
        tcb.setPriority(2);

        // Create ThreadCB, PortCB and OpenFile for task
        ///?

        /**
        *   Create swap file, get address space size by MMU.getVirtualAddressBits(), 
        *   set name as ID, get directory by SwapDeviceMountPoint. Use FileSys.create()
        *   to create and then use OpenFile.open() to open it.
        */

        FileSys.create(Integer.toString(taskID), MMU.getVirtualAddressBits);
        OpenFile swapFile = OpenFile.open(GlobalVariables.SwapDeviceMountPoint + taskID, tcb);

        /**
        *   Save the file handle using setSwapFile(), but dispatch a new thread
        *   if fail to open
        */
        if (swapFile == null) {
            ThreadCB.dispatch();
            return null;
        }

        // return the Task object
        return tcb;
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
        for (ThreadCB tcb : tList) {
            tcb.kill();
        }

        // Destroy all ports
        for (PortCB pcb : pList) {
            pcb.destroy();
        }

        // Terminate task
        setStatus(GlobalVariables.TaskTerm);

        // Release memory
        taskPT.deallocateMemory();

        // Close all swap files
        for (OpenFile of : fList) {
            of.close();
        }
        FileSys.delete(GlobalVariables.SwapDeviceMountPoint + taskID);
    }

    /** 
*	Returns a count of the number of threads in this task. 
	*
*	@OSPProject Tasks
    */
    public int do_getThreadCount()
    {
        // your code goes here

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
        if (tList.size() >= ThreadCB.MaxThreadsPerTask) {
            return GlobalVariables.FAILURE;
        }
        if (!tList.contains(thread)) {
            tList.add(thread);
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
        int index = tList.indexOf(thread);
        if (taskThread == thread) {
            // return GlobalVariables.FAILURE;
            taskThread = null;
        }
        if (index != -1) {
            try{
                tList.remove(index);
            } catch (ArrayIndexOutOfBoundsException e){
                retrun GlobalVariables.FAILURE;
            }
        } else{

        }
        return GlobalVariables.SUCCESS;
    }

    /**
    *   Return number of ports currently owned by this task. 
*
    *   @OSPProject Tasks
    */
    public int do_getPortCount()
    {
        return pList.size();
    }

    /**
     *  Add the port to the list of ports owned by this task.
*	
     *  @OSPProject Tasks 
    */ 
    public int do_addPort(PortCB newPort)
    {
        if (pList.size() >= PortCB.MaxPortsPerTask) {
            return GlobalVariables.FAILURE;
        }
        if (!pList.contains(newPort)) {
            pList.add(newPort);
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
        if (!pList.contains(oldPort)) {
            return GlobalVariables.FAILURE;
        }
        pList.remove(oldPort);
        return GlobalVariables.SUCCESS;
    }

    /**
       *Insert file into the open files table of the task.
*
       *@OSPProject Tasks
    */
    public void do_addFile(OpenFile file)
    {
        if (!fList.contains(file)) {
            fList.add(file);
        } else{

        }
    }

    /** 
*	Remove file from the task's open files table.
*
*	@OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
        if (!fList.contains(file)) {
            return GlobalVariables.FAILURE;
        }
        fList.remove(file);
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
        System.out.println("呦呦切克闹，煎饼果子来一套");
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
