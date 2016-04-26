# OSP2Project - Memory

This project is part of the OSP 2 student project. Specified module: Memory.

# Group name

XXL

# Group member

* Yuhao Liu (SUID:444219389)
* Shuchang Liu (SUID:968892838)

# Project work

There are two implementations for this sub-project corresponding to two [replacement algorithm](https://en.wikipedia.org/wiki/Page_replacement_algorithm): FIFO and LRU]. Each implemation will followed by an performance analysis.

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

## Performance analysis

There is a mistake made by OSP2 manual. In Memory section, MMU class, it is said that getPageAddressBits() is used to get the number of bits representing offset within a page. But actually OSP2 software use it to represent the page number instead. 


