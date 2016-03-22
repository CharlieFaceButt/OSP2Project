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



public class ThreadCB extends IflThreadCB 
{

    public ThreadCB()
    {
       super();
    }

    public static void init()
    {
        GenericList readyList = new GenericList();
    }


    static public ThreadCB do_create(TaskCB task)
    {
        ThreadCB thread = new ThreadCB();
        thread.setTask(task);

        if((task.getThreadCount() == MaxThreadPerTask) || (task.addThread(thread) == FAILURE)) {
            dispatch();
            return null;
        }

        thread.setPriority(task.getPriority());	
        thread.setStatus(ThreadReady);

        readyList.append(thread);		//insert or append?

        dispatch();
        return thread;
    }


    public void do_kill()
    {
        if(this.getStutas() == ThreadReady) {
            readyList.remove(this);
            this.setStatus(ThreadKill);
        }

        if(this.getStatus() == ThreadRunning) {
            if(MMU.getPTBR().getTask().getCurrentThread() == this) {
                MMU.setPTBR(null);
                getTask().setCurrentThread(null);
            }
        }
        
        if(this.getStatus() == ThreadWaiting) {
            this.setStatus(ThreadKill);
        }

        theTask = this.getTask();
        theTask.removeTask(this);
        this.setStatus(ThreadKill);

        for(int i = 0; i < Device.getTableSize(); i++) {
            Device.get(i).cancelPendingIO(this);
        }
        ResourceCB.giveupResources(this);

        if(this.getTask().getThreadCount() == 0) {
            this.getTask.kill();
        }

        dispatch();
    }


    public void do_suspend(Event event)
    {
        // your code goes here
        boolean changedToWait = false;

        if(this.getStatus() == ThreadRunning) {
            if(MMU.getPTBR().getTask().getCurrentThread() == this) {
                MMU.setPTBR(null);
                this.getTask().setCurrentThread(null);
                this.setStatus(ThreadWaiting);
                event.addThread(this);
                changedToWait = true;
            }
        }

        if(this.getStatus() >= ThreadWaiting && !changedToWait) {
            this.setStatus(this.getStatus() + 1);

            if(!readyList.contains(this)) {
                event.addThread(this);
            }
        }

        dispatch();
    }


    public void do_resume()
    {
        // your code goes here
        if(getStatus() < ThreadWaiting) {
            MyOut.print(this, "Attempt to resume " + this + ", which wasn't waiting");
            return;
        }

        MyOut.print(this, "Resuming " + this);

        if(this.getStatus() == ThreadWaiting) {
            setStatus(ThreadReady);
        } else if (this.getStatus() > ThreadWaiting) {
            setStatus(getStatus() - 1);
        }

        if (getStatus() == ThreadReady) {

        }
    }


    public static int do_dispatch()
    {
        // your code goes here
        
    }


    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
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
