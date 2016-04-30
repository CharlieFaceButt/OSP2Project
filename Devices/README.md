# OSP2Project - Memory

This project is the OSP 2 student project of module Device.

# Group name

XXL

# Group member

* Yuhao Liu (SUID:444219389)
* Shuchang Liu (SUID:968892838)

# Project work

There are four implementations for this sub-project corresponding to four scheduling algorithms: FIFO, [C-SCAN](https://en.wikipedia.org/wiki/Elevator_algorithm), [SSTF](https://en.wikipedia.org/wiki/Shortest_seek_first), and [F-SCAN](https://en.wikipedia.org/wiki/FSCAN). Each implemation will followed by an performance analysis.

## Tasks

|Tasks		|Status	|Assignment	|
|-----------|-------|-----------|
|FIFO |Completed|Shuchang Liu are the major contributor of this part|
|C-SCAN |Completed|Shuchang Liu are the major contributor of this part|
|SSTF |Completed|Shuchang Liu are the major contributor of this part|
|F-SCAN |Completed|Shuchang Liu are the major contributor of this part|
|Performance record|Completed|Yuhao Liu is the major contributor of this part|
|Analysis and Readme |Completed|Shuchang Liu is the major contributor of this part|

## Design

Several classes for implementation mentioned in the manual are:

* **Device**: receive request and scheduling
* **IORB**: the request block
* **DiskInterruptHandler**: handler for disk interrupt, this interrupt is generated when an actual disk operation is finished.

Addition to these class, one **Stats** class is add for the purpose of performance data recording.

### Device

There is a iorbQueue interface to be implemented for scheduling. Along with it, there are three required function by manual:

* do_enqueue()
* do_dequeue()
* do_cancelPendingIO(): 

Different scheduling algorithm implement iorbQueue and these functions in different ways. The solution is provided by our **IORBQueue** class which implements **GenericQueueInterface**.:

|Algorithm|queue structure|enqueue()|dequeue()|cancelingPendingIO()|
|---------|---------|---------|---------|--------------------|
|FIFO|GenericList|append()|removeHead()||
|C-SCAN|Vector|sortEnqueue()|do_cscanDequeue()||
|SSTF|Vector|sortEnqueue()|do_sstfDequeue()||
|F-SCAN|2 Vectors|sortEnqueue() for queuing vector|do_fscanDequeue() for scanning vector|do this for both vectors|

Several issues about our design:
* Super class **IflDevice** already has an interface iorbQueue to be implemented, it should always be maintained by the algorithm though there may be additional representations in **Device** class.
* Except for FIFO, all other algorithm need **IORB** sorted by their track number, so they put new request to a proper position in the sorted queue.
* For dequeue, FIFO just retrieve the oldest one in the head of queue; C-SCAN will scan the sorted queue in a certain direction and return to the beginning if reaches the other edge; SSTF will look for the closest request based on track number; F-SCAN will do [SCAN](https://en.wikipedia.org/wiki/Elevator_algorithm) on the scanning queue;

### IORB

To record performance data, we add some public attributes:

* createTime: set when an instance is constructed
* enqueueTime: set when *do_enqueue()*
* dequeueTime: set when *do_dequeue()*
* handleTime: not used 
* finishTime: not used

### DiskInterruptHandler

Nothing special for this class, see our code.

### Stats

This class records head movement for a specific **Device**. For each **Device** created, there will be a **Stats** class associated with it. Records are maintained in a List of **HeadStat** which described at what clock time the head is going to move from which position to which target position.

The public function *inputHeadStat()* put new record into the list and output this new record into a file. When write new record to file, both information in **HeadStat** and creation time in **IORB** will be output. The function is called by *do_dequeue()* of class **Device**, and each device has its own output file.

## Performance analysis

We generate head movement data for each device. And do this for all algorithms. Each record in the file should looks like:

> [18]: 0-1	IORB created: 18

meaning at clock time 18, disk head is now at track 0 and will move to 1, the requesting IORB is created at clock time 18.

The list of record gives the behavior of the disk head and the response time of IORB request. The behavior of the disk head is recorded so that we can recognize whether the pattern follows locality principle, which shows indeed:

![pattern](/Devices/img/FIFO-swap.png)

For device with large amount of operation, the response time:

|algorithm|response time (ordered by dequeue time)|
|---------|---------------------------------------|
|FIFO|![FIFO](/Devices/img/RT-FIFO-swap.png)|
|FSCAN|![FSCAN](/Devices/img/RT-FSCAN-swap.png)|
|CSCAN|![CSCAN](/Devices/img/RT-CSCAN-swap.png)|
|SSTF|![SSTF](/Devices/img/RT-SSTF-swap.png)|

Compare with FIFO, FSCAN performs slightly better, while CSCAN and SSTF performs pretty good on average. But CSCAN and SSTF, which make use of locality principle, may starve certain IORBs and these requests may have to wait for time that are tenfold of that in FIFO. So this trade off is shown by the comparison of both average response time and maximum response time:

![average RT](/Devices/img/averageRT.png)

![maximum RT](/Devices/img/maximumRT.png)

In terms of maximum response time, CSCAN and SSTF are no longer desirable choices. Generally, FSCAN not only improves the average response time but also avoid starvation.