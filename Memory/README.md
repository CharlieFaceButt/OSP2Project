# OSP2Project - Memory

This project is part of the OSP 2 student project. Specified module: Memory.

# Group name

XXL

# Group member

* Yuhao Liu (SUID:444219389)
* Shuchang Liu (SUID:968892838)

# Project work

There are two implementations for this sub-project corresponding to two [replacement algorithm](https://en.wikipedia.org/wiki/Page_replacement_algorithm): FIFO and LRU. Each implemation will followed by an performance analysis.

## Tasks

|Tasks		|Status	|Assignment	|
|-----------|-------|-----------|
|FIFO |Completed|Yuhao Liu are the major contributor of this part|
|LRU |Completed|Shuchang Liu are the major contributor of this part|
|Record performance of both scheduling	|Completed|Yuhao Liu is the major contributor of this part|
|Analysis and Readme |Completed|Shuchang Liu is the major contributor of this part|

## Design

According to the OSP2 manual, there are several classes to be implement in memory module:

* FrameTableEntry: 	physical memory frame
* PageTableEntry: 	logical page resides in virtual memory
* PageTable: 		page table that associated with a certain task
* MMU: 				memory management unit, where replacement algorithm utilized
* PageFaultHandler: handler for page fault, where part of replacement algorithm implemented

In order to prevent 'zombie' frames that occupied memory space but no living task will remove it out, a daemon is introduced to periodically clean those frames in memory:

* MMDaemon

### FrameTableEntry

In addition to the constructor required by manual, this class provide a function *isOccupied()* that check the availability of the frame. It is determined by checking the frame's lock status and reserve status.

### PageTableEntry

One thing to mention: lock operation need to be checked whenever returning SUCCESS, because the requesting thread may be killed during the process. This is extracted as another function *final_check_do_lock(thread)*.

### PageTable

Nothing special for this class, just follow description in the manual.

### MMU

This is the major class in our implementation. The heart of this class is the *refer()* function. This function will first calculate page address and page information, then do some checking and possibly a page fault to make sure the page is valid for reference, and finally the reference.

When page fault is called, *refer()* will generate an page fault interrupt which will finally call *do_handlePageFault()* in class PageFaultHandler, also contained in our implementation. They together will do the frame replacement. This class maintains a set of FrameTableEntry representing physical memory. So replacement algorithm also are implemented in this class as a set of functions. For LRU algorithm, candidate frames for replacement is maintained in a queue, algorithm related functions are listed here:

* *getLRUframe()*: selected the least recently used frame that is valid for replacement
* *do_LRUAlignment()*: when an frame in the queue is refered, it must be refreshed to the end of the queue
* *newLRU()*: for a newly activatied frame, put it into the end of the queue.

Among these functions, *getLRUframe()* and *newLRU()* is called by PageFaultHandler where actual replacement happens, and *do_LRUAlignment()* is called each time *refer()* is called. 

FIFO algorithm in data structure is similar to LRU but it does not change the order of candidate frames when an existing frame is refered.

Another important issue is statistic recording. For this purpose, several features including page fault rate and page fault per reference are estimated. This is recorded by the following attributes:

* PFAmount: total number of page fault
* successfulPFAmount: total number of successful page fault
* referencedPageNum: total number of memory reference

Additionally, there is a *cleanFrames()* function. It is periodically called by MMDaemon, and clean the frames that are no longer legal to occupied memory spaces.

### PageFaultHandler

Nothing special for this class except for that statistic record is done by function *do_stats*. It updates the PFAmount and successfulPFAmoung in MMU and output a record to a file Statistics.txt.

## Performance analysis

OSP2 automatically generates log file for all operations and error occured. Additionally our implementation will create a file Statistics.txt recording statistics for performance data. In the file, each record consists 4 fields: the clock time record was taken, the successful page fault amount(SPF)/ the page fault amount(PF)/ reference amount(REF) at until that clock time.

We took three record file for each replacement algorithm, and the result is shown in the following graph:

picture here...

The page fault rate of LRU is x% smaller than FIFO and page fault per reference is x% smaller. This proves that the LRU algorithm is slightly better than FIFO in performance.

## A mistake in the manual

There is a mistake made by OSP2 manual. In Memory section, MMU class, it is said that getPageAddressBits() is used to get the number of bits representing offset within a page. But actually OSP2 software use it to represent the page number instead. This is also proved by the description in Device module.

