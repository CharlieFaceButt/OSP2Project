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
    private static String threadErrorInfo;
    private static String threadWarningInfo;
    private static int targetSwitchOutStatus;

    private static Vector<ThreadCB> readyToRunThreads;
    /**
    *       The thread constructor. Must call 
    *
    *              super();
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
        targetSwitchOutStatus = GlobalVariables.ThreadReady;
        threadErrorInfo = "null";
        threadWarningInfo = "null";
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
    *   methods. However, OSP itself doesn't care what the actual value of
    *   the priority is. These methods are just provided in case priority
    *   scheduling is required.
    *
    *@return thread or null
    *
    *        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
        atLog(task, "Create thread");
        //create thread
        if (task.getThreadCount() >= MaxThreadsPerTask) {
            threadWarningInfo = "Max thread count exceeded.";
            ThreadCB.atWarning();
            dispatch();
            return null;
        }
        ThreadCB thread = new ThreadCB();

        //associate thread with task
        if (task.addThread(thread) == GlobalVariables.FAILURE){
            threadErrorInfo = "Fail to add thread to task";
            ThreadCB.atWarning();
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
    *   Kills the specified thread. 
    *
    *   The status must be set to ThreadKill, the thread must be
    *   removed from the task's list of threads and its pending IORBs
    *   must be purged from all device queues.
    *        
    *   If some thread was on the ready queue, it must removed, if the 
    *   thread was running, the processor becomes idle, and dispatch() 
    *   must be called to resume a waiting thread.
    *   
    * @OSPProject Threads
    */
    public void do_kill()
    {
        atLog(this, "Thread kill");

        int currentStatus = getStatus();

        //For ready state thread
        if (currentStatus == GlobalVariables.ThreadReady) {
            //Remove from ready queue
            readyToRunThreads.remove(this);
        }

        //For running state thread
        if (currentStatus == GlobalVariables.ThreadRunning) {
            //Context Switch: remove from CPU, and dispatch new thread
            // ThreadCB.do_preemption(GlobalVariables.ThreadKill);
            MMU.setPTBR(null);
            this.getTask().setCurrentThread(null);
        }

        if(currentStatus >= GlobalVariables.ThreadWaiting) {
            for (int id = 0; id < Device.getTableSize(); id ++) {
                Device.get(id).cancelPendingIO(this);
            }
        }        
        //Set status to ThreadKill
        setStatus(GlobalVariables.ThreadKill);

        //Remove thread from task
        TaskCB task = getTask();
        task.removeThread(this);

        //Cancelling devices request
        

        //Release resources
        ResourceCB.giveupResources(this);

        //Kill the task if no thread left
        if (getTask().getThreadCount() == 0) {
            getTask().kill();
        }
        dispatch();
    }

    /** Suspends the thread that is currenly on the processor on the 
    *   specified event. 
    *
    *   Note that the thread being suspended doesn't need to be
    *   running. It can also be waiting for completion of a pagefault
    *   and be suspended on the IORB that is bringing the page in.
    *
    *   Thread's status must be changed to ThreadWaiting or higher,
    *   the processor set to idle, the thread must be in the right
    *   waiting queue, and dispatch() must be called to give CPU
    *   control to some other thread.
    *
    *   @param event - event on which to suspend this thread.
    *
    *        @OSPProject Threads
    *    */
    public void do_suspend(Event event)
    {
        atLog(event, "Thread suspended by event");

        int currentStatus = this.getStatus();

        //Place the thread to a certain event queue
        event.addThread(this);

        //If suspend a running thread, context switch
        if (currentStatus == GlobalVariables.ThreadRunning) {
            MMU.setPTBR(null);
            this.getTask().setCurrentThread(null);
            this.setStatus(GlobalVariables.ThreadWaiting);

        } 
        //If suspend a waiting thread, increase waiting level
        else if (currentStatus >= GlobalVariables.ThreadWaiting) {
            atLog(null, "\t level up a suspended thread at " + currentStatus);
            setStatus(currentStatus + 1);
        }
        
        ThreadCB.dispatch();

    }

    /** Resumes the thread.
    *
    *   Only a thread with the status ThreadWaiting or higher
    *   can be resumed.  The status must be set to ThreadReady or
    *   decremented, respectively.
    *   A ready thread should be placed on the ready queue.
    *
    *   @OSPProject Threads
    */
    public void do_resume()
    {
        atLog(this, "Thread resume.");

        int currentStatus = getStatus();

        //Change to ready if waiting level 0
        if (currentStatus == GlobalVariables.ThreadWaiting) {
            this.setStatus(GlobalVariables.ThreadReady);
            readyToRunThreads.add(this);
        }
        //Lower the level of waiting if level is larger than 0
        else if (currentStatus > GlobalVariables.ThreadWaiting) {
            this.setStatus(currentStatus - 1);
        }
        //Other status cannot do resume
        else {
            threadWarningInfo = "Attempt to resume " + this + ", which wasn't waiting";
            ThreadCB.atWarning();
        }
        dispatch(); 
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
    *   FIFO scheduling: most context switch happens on process termination 
    *   no time slicing interruption. Overhead is minimal, but throughput
    *   can be low. Only when suspension or termination, thread stop using CPU 
    *   and be removed from ready to run queue.
    *
    *   @return SUCCESS or FAILURE
    *
    *        @OSPProject Threads
    */
    public static int do_dispatch()
    {
        ThreadCB thread = MMU.getPTBR().getTask().getCurrentThread();
        ThreadCB newThread = readyToRunThreads.remove(0);

        if(thread != null) {
            thread.getTask().setCurrentThread(null);
            MMU.setPTBR(null);

            thread.setStatus(ThreadReady);
            readyToRunThreads.add(thread);
        }

        if(readyToRunThreads.isEmpty()) {
            MMU.setPTBR(null);
            return FAILURE;
        }
        MMU.setPTBR(newThread.getTask().getPageTable());
        newThread.getTask().setCurrentThread(newThread);
        newThread.setStatus(ThreadRunning);

        return SUCCESS;
    }

    /**
    *   The context switch.
    * 
    *   Control of the CPU of the current thread is preempted and another 
    *   thread will be dispatched.
    *   @param newThread the selected thread to be dispatch.
    *   @param switchOutStatus the target status for the current thread 
    *   that is going to be switched out. It is decided by the caller, but
    *   must be either ThreadWaiting or ThreadReady
    */
    
    // private static void do_preemption()
    // {
    //     atLog(null, "Preempt thread.");

    //     //Get current running thread
    //     PageTable pt = MMU.getPTBR();
    //     if (pt == null) {
    //         atLog(null, "\t no thread running");
    //         return;
    //     }
    //     ThreadCB currentThread = pt.getTask().getCurrentThread();
        
    //     //Current thread must be running in CPU
    //     if (currentThread.getStatus() == GlobalVariables.ThreadRunning) {
    //         currentThread.setStatus(GlobalVariables.ThreadReady);
    //     } else {
    //         threadWarningInfo = "Thread has been set to " + 
    //             currentThread.getStatus() + " previously";
    //         ThreadCB.atWarning();
    //     }

    //     //Set the page table base register
    //     MMU.setPTBR(null);

    //     //Set current running thread to null
    //     pt.getTask().setCurrentThread(null);
    // }

    private static String signature = "<XXL> ";
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

    public static void atLog(Object src, String msg)
    {
        if (src == null) {
            System.out.println(signature + msg);
        } else {
            System.out.println(src + signature + msg);   
        }
    }

}
