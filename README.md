#### QUEUE EXPERIMENT

Everything to be conducted for both LinkedBlockingQueue and ConcurrentLinkedQueue

 - how to decide batch size for the queue ? 
 - how many edges can we put into the memory before it exceeds out ? (helps in knowing the batchsize)

How to decide which queue to be considered ?
 - For a fixed batch size, make a thread read from the file and add to the queue (fileReaderThread)
 - spawn a couple of task threads onto the queue. 
 - Task thread should consume from the queue and instruct the reader to upon acquiring a first null
 