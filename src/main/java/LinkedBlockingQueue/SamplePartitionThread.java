package LinkedBlockingQueue;

import Definition.Edge;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SamplePartitionThread implements Runnable{
    private long DECISION_TIME = 1;
    private LinkedBlockingQueue<Edge> IOQueue;
    private final AtomicBoolean toBeFilled;
    private AtomicLong edgeCountDown = null;
    private CountDownLatch edgeCountDown2 = null;

    public SamplePartitionThread( AtomicBoolean toBeFilled, AtomicLong edgeCountDown , LinkedBlockingQueue<Edge> IOQueue) {
        this.IOQueue = IOQueue;
        this.toBeFilled = toBeFilled;
        this.edgeCountDown = edgeCountDown;
    }

    public void decidePartition() {
        try {
            Thread.sleep(DECISION_TIME);
        } catch (InterruptedException e) {
            System.out.println("Exception in decidePartition method");
            e.printStackTrace();
        }

    }

    public void run() {
//            while(edgeCountDown.get() != 0) {
//                Edge e = IOQueue.poll();
//                String threadName = Thread.currentThread().getName();
//                if (e != null) {
////                    System.out.println(threadName + ": Edge in process - " + e.toString());
//                    decidePartition();
//                    long edgeCount = edgeCountDown.decrementAndGet();
////                    System.out.println(threadName + ": Records Remaining - " + edgeCount + " Just processed - " + e.toString());
//                } else {
//                    System.out.println(threadName + " setting encountered null, telling reader");
//                    toBeFilled.set(true);
//                    long waitStart = System.currentTimeMillis();
//                    waitForQueueFill();
//                    long waitEnd = System.currentTimeMillis();
//                    Main.totalWaitTime.addAndGet(waitEnd - waitStart);
//                }
//            }
        while (true) {
            Edge edge = IOQueue.poll();

            if (edge != null) {
                long beforeDecide = System.currentTimeMillis();
                decidePartition();
                Main.decisionTime.addAndGet( System.currentTimeMillis() - beforeDecide);
                edgeCountDown.decrementAndGet();
            } else {
                synchronized (Main.moreLines) {
                    if (!Main.moreLines) {
                        System.out.println(Thread.currentThread().getId() + " consumer thread exiting");
                        Main.awaitLatch.countDown();
                        break;
                    }
                }

                try {
                    // contention point
                    Main.lock.acquire();
                    if (!IOQueue.isEmpty()) {
                        Main.lock.release();
                        continue;
                    }

                    synchronized (Main.producer) {
                        System.out.println(Thread.currentThread().getId() + " notifies for read");
                        Main.producer.notify();
                    }

                    long beforeWait = System.currentTimeMillis();
                    synchronized (Main.consumer) {
                        System.out.println(Thread.currentThread().getId() + " releasing lock and waiting");
                        Main.lock.release();
                        Main.consumer.wait();
                    }
                    long delta = System.currentTimeMillis() - beforeWait;
                    Main.totalWaitTime_Consumer.addAndGet(delta);

                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getId() +  " consumer thread interrupted");
                    e.printStackTrace();
                }


            }
        }

    }
}