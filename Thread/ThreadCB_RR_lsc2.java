package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
 *   This class is responsible for actions related to threads, including
 *   creating, killing, dispatching, resuming, and suspending threads.
 *
 *   @OSPProject Threads
 */
public class ThreadCB extends IflThreadCB 
{

    // private static String threadErrorInfo;
    // private static String threadWarningInfo;
    // private static int targetSwitchOutStatus;
    private static String signature;
    private static int quantum;

    //statistics
    public class ThreadStat{
        public long runningTime;
        public long terminationTime;
        public ThreadCB thread;
        public ThreadStat(){}
        public String toString(){
            String stat = "";
            stat += "Created at: " + thread.getCreationTime();
            stat += "\tTermination time: " + terminationTime;
            stat += "\tCPU time: " + thread.getTimeOnCPU();
            stat += "\tService time: " + runningTime;
            return stat;
        }
    };
    private static Long minRunningTime;
    private static Long maxRunningTime;
    private static Vector<ThreadStat> threadStatsList;

    private static Vector<ThreadCB> readyToRunThreads;
    /**
    *       The thread constructor. Must call 
    *
    *       	   super();
    *
    *       as its first statement.
    *
    *       @OSPProject Threads
    */
    public ThreadCB()
    {
        super();
    }

    /**
    *   This method will be called once at the beginning of the
    *   simulation. The student can set up static variables here.
    *   
    *   @OSPProject Threads
    */
    public static void init()
    {
        atLog(null, "Initialize ThreadCB static features");
        readyToRunThreads = new Vector<ThreadCB>();
        signature = "<XXL> ";
        // targetSwitchOutStatus = GlobalVariables.ThreadReady;
        // threadErrorInfo = "null";
        // threadWarningInfo = "null";
        quantum = 100;
        threadStatsList = new Vector<ThreadStat>();
    }

    /** 
    *   Sets up a new thread and adds it to the given task. 
    *   The method must set the ready status 
    *   and attempt to add thread to task. If the latter fails 
    *   because there are already too many threads in this task, 
    *   so does this method, otherwise, the thread is appended 
    *   to the ready queue and dispatch() is called.
    *
	*   The priority of the thread can be set using the getPriority/setPriority
    *	methods. However, OSP itself doesn't care what the actual value of
    *	the priority is. These methods are just provided in case priority
    *	scheduling is required.
    *
	*@return thread or null
    *
    *        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
        atLog(task, "Create thread");
        //create thread when task can have more threads
        if (task.getThreadCount() >= MaxThreadsPerTask) {
            dispatch();
            return null;
        }
        ThreadCB thread = new ThreadCB();

        //associate thread with task
        if (task.addThread(thread) == GlobalVariables.FAILURE){
            dispatch();
            return null;
        } else thread.setTask(task);

        ///priority not implemented
        // thread.setPriority(task.getPriority())

        //set status to ready to run
        thread.setStatus(GlobalVariables.ThreadReady);

        //add threads to ready to run list
        readyToRunThreads.add(thread);

        //Regardless of whether the new thread was created successfully, 
        // the dispatcher must be called or else a warning will be issued
        dispatch();
        return thread;
    }


    /** 
    *	Kills the specified thread. 
    *
    *	The status must be set to ThreadKill, the thread must be
    *	removed from the task's list of threads and its pending IORBs
    *	must be purged from all device queues.
    *        
    *	If some thread was on the ready queue, it must removed, if the 
    *	thread was running, the processor becomes idle, and dispatch() 
    *	must be called to resume a waiting thread.
    *	
	* @OSPProject Threads
    */
    public void do_kill()
    {
        atLog(this, "Thread kill");

        int currentStatus = getStatus();

        //Set status to ThreadKill
        setStatus(GlobalVariables.ThreadKill);

        //For ready state thread
        if (currentStatus == GlobalVariables.ThreadReady) {
            //Remove from ready queue
            readyToRunThreads.remove(this);
        }

        //For running state thread
        if (currentStatus == GlobalVariables.ThreadRunning) {
            setStatus(GlobalVariables.ThreadKill);
            //Leave CPU
            getTask().setCurrentThread(null);
            MMU.setPTBR(null);
        }

        //Remove thread from task
        TaskCB task = getTask();
        task.removeThread(this);

        //Cancelling devices request
        for (int id = 0; id < Device.getTableSize(); id ++) {
            Device.get(id).cancelPendingIO(this);
        }

        //Release resources
        ResourceCB.giveupResources(this);

        //Kill the task if no thread left
        if (getTask().getThreadCount() == 0) {
            getTask().kill();
        }

        do_statistics(HClock.get());

        ThreadCB.dispatch();
    }

    /** Suspends the thread that is currenly on the processor on the 
    *   specified event. 
    *
    *   Note that the thread being suspended doesn't need to be
    *   running. It can also be waiting for completion of a pagefault
    *   and be suspended on the IORB that is bringing the page in.
	*
    *	Thread's status must be changed to ThreadWaiting or higher,
    *   the processor set to idle, the thread must be in the right
    *   waiting queue, and dispatch() must be called to give CPU
    *   control to some other thread.
    *
    *	@param event - event on which to suspend this thread.
    *
    *        @OSPProject Threads
    *    */
    public void do_suspend(Event event)
    {
        atLog(event, "Thread suspended by event");

        int currentStatus = getStatus();

        //If suspend a running thread, context switch
        if (currentStatus == GlobalVariables.ThreadRunning) {
            atLog(null, "\t suspend a running thread");
            setStatus(GlobalVariables.ThreadWaiting);
            //Leave CPU
            getTask().setCurrentThread(null);
            MMU.setPTBR(null);
        } 
        //If suspend a waiting thread, increase waiting level
        else if (currentStatus >= GlobalVariables.ThreadWaiting) {
            atLog(null, "\t level up a suspended thread at " + currentStatus);
            setStatus(currentStatus + 1);
        }
        
        //Place the thread to a certain event queue
        event.addThread(this);

        ThreadCB.dispatch();
    }

    /** Resumes the thread.
    *
    *	Only a thread with the status ThreadWaiting or higher
    *	can be resumed.  The status must be set to ThreadReady or
    *	decremented, respectively.
    *	A ready thread should be placed on the ready queue.
	*
    *	@OSPProject Threads
    */
    public void do_resume()
    {
        atLog(this, "Thread resume.");

        int currentStatus = getStatus();

        //Change to ready if waiting level 0
        if (currentStatus == GlobalVariables.ThreadWaiting) {
            atLog(this, "\tResume thread to ready queue");
            setStatus(GlobalVariables.ThreadReady);
            readyToRunThreads.add(this);
        }
        //Lower the level of waiting if level is larger than 0
        else if (currentStatus > GlobalVariables.ThreadWaiting) {
            setStatus(currentStatus - 1);
        }
        //Other status cannot do resume
        else {
            // threadWarningInfo = "Attempt to resume " + this + ", which wasn't waiting";
            // ThreadCB.atWarning();
        }

        ThreadCB.dispatch();
    }

    /**
    *   Timer interrupt handler will call this function
    *
    *   It preempt the current running thread and put it to the end of 
    *   the ready queue, so that thread only continuously run in a small
    *   time quantum.
    */
    public static void handleTimerInterrupt(){
        PageTable pt = MMU.getPTBR();
        //If a thread is running
        if (pt != null) {
            TaskCB currentTask = pt.getTask();
            if (currentTask != null) {
                ThreadCB currentThread = currentTask.getCurrentThread();
                if (currentThread != null) {
                    //Leave running state
                    currentThread.setStatus(GlobalVariables.ThreadReady);
                    //Put current thread to the end of ready queue
                    readyToRunThreads.add(currentThread);
                    //Change task running thread
                    currentTask.setCurrentThread(null);
                }
            }
        }
        //Leave CPU
        MMU.setPTBR(null);

        ThreadCB.dispatch();
    }

    /** 
    *   Selects a thread from the ready to run queue and dispatches it. 
    *
    *   If there is just one thread ready to run, reschedule the thread 
    *   currently on the processor.
    *
    *   In addition to setting the correct thread status it must
    *   update the PTBR.
    *
    *   Round Robin scheduling: When suspension, termination, or timer interrupt, thread stop using CPU 
    *   and be removed from ready to run queue.
	*
    *	@return SUCCESS or FAILURE
    *
    *        @OSPProject Threads
    */
    public static int do_dispatch()
    {

        //When a thread is running, cannot dispatch
        if (MMU.getPTBR() != null) {
            return GlobalVariables.SUCCESS;
        }

        //Error when no thread in ready queue
        if (readyToRunThreads.isEmpty()) {
            // threadErrorInfo = "no ready to run thread when dispatching.";
            // ThreadCB.atError();
            return GlobalVariables.FAILURE;
        }

        //Select a thread: FIFO
        ThreadCB selectedThread = readyToRunThreads.remove(0);
        TaskCB task = selectedThread.getTask();

        //Make sure it chooses different thread
        if (selectedThread == task.getCurrentThread()) {
            readyToRunThreads.add(selectedThread);
            atLog(null, "\tReschedule thread to ready queue");
        }

        //Set status to Thread Running
        if (selectedThread.getStatus() == GlobalVariables.ThreadReady) {
            selectedThread.setStatus(GlobalVariables.ThreadRunning);   
        } else{
            return GlobalVariables.FAILURE;
        }

        //Set page table and current thread
        MMU.setPTBR(task.getPageTable());
        task.setCurrentThread(selectedThread);

        //Reset timer clock
        HTimer.set(quantum);

        return GlobalVariables.SUCCESS;
    }

    /**
    *   Called by OSP after printing an error message. The student can
    *   insert code here to print various tables and data structures in
    *   their state just after the error happened.  The body can be
    *   left empty, if this feature is not used.
    *
    *       @OSPProject Threads
    */
    public static void atError()
    {
        // atLog(null, threadErrorInfo);
        // MyOut.error(
        //     // MMU.getPTBR().getTask().getCurrentThread(), 
        //     threadErrorInfo,
        //     signature + threadErrorInfo);
    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        // atLog(null, threadWarningInfo);
        // MyOut.warning(
        //     // MMU.getPTBR().getTask().getCurrentThread(), 
        //     threadWarningInfo,
        //     signature + threadWarningInfo);
    }

    /**
    *   Log to terminal console.
    */
    public static void atLog(Object src, String msg)
    {
        if (src == null) {
            System.out.println(signature + msg);
        } else {
            System.out.println(src + signature + msg);   
        }
    }

    public void do_statistics(long terminationTime){
        ThreadStat tstats = new ThreadStat();
        tstats.terminationTime = terminationTime;
        tstats.runningTime = terminationTime - getCreationTime();
        tstats.thread = this;

        threadStatsList.add(tstats);

        if (minRunningTime == null || tstats.runningTime < minRunningTime) {
            minRunningTime = tstats.runningTime;
        }
        if (maxRunningTime == null || tstats.runningTime > maxRunningTime) {
            maxRunningTime = tstats.runningTime;
        }

        for (ThreadStat ts: threadStatsList) {
            MyOut.print(ts.thread, ts.toString());    
        }
        MyOut.print(this, "Min: " + minRunningTime + 
            "\n\tMax: " + maxRunningTime);
    }
}
