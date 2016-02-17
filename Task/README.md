# OSP2Project

This project is the OSP 2 student project of module Task.

#Group name

XXL

#Group member

* Yuhao Liu (SUID:444219389)
* Shuchang Liu (SUID:968892838)

#Project work

We have added three data structures for threads, ports and files:
    private Vector<ThreadCB> threadList;
    private Vector<OpenFile> openFileList;
    private Vector<PortCB> portList;

We have added one function to the class:
* public void setupResources()
	* This function is used to setup those three data structures memtioned above

We have implemented necessary interfaces for the module:
* init() 		
* do_create() 	
* do_kill() 		
* do_getThreadCount()
* do_addThread() / do_removeThread()
* do_getPortCount()
* do_addPort() / do_removePort()
* do_addFile() / do_removeFile()
* atError() / atWarning()

At last we compiled and runned and tested the project.

## FYI,  using MyOut operation, we added log output to each interface we have done so that we can keep track of the status of tasks in the running OS.

#See our signature in the log file:

Search for "My" or "XXL" notation, XXL is our group signature. YO YO!