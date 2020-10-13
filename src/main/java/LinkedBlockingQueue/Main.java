package LinkedBlockingQueue;

import Definition.Edge;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    public static String FILE = "";
    public static long batchSize = 0;
    public static long vertexCount;
    public static long edgeCount;
    public static int nThreads;
    protected static AtomicLong totalWaitTime_Consumer;
    protected static AtomicLong totalWaitTime_Producer;
    protected static AtomicLong decisionTime;
    protected static Boolean moreLines = true;
    protected static Object producer = new Object();
    protected static Object consumer = new Object();
    protected static Semaphore lock = new Semaphore(1);
    protected static AtomicLong IOTime;
    protected static CountDownLatch awaitLatch;

    public static void main(String[] args) {
        FILE = args[0];
        vertexCount = Long.parseLong(args[1]);
        edgeCount = Long.parseLong(args[2]);
        nThreads = Integer.parseInt(args[3]);
        batchSize = Long.parseLong(args[4]);
        totalWaitTime_Consumer = new AtomicLong(0);
        totalWaitTime_Producer = new AtomicLong(0);
        decisionTime = new AtomicLong(0);
        awaitLatch = new CountDownLatch(nThreads + 1);
        IOTime = new AtomicLong(0);
        AtomicBoolean toBeFilled = new AtomicBoolean(true);
        LinkedBlockingQueue<Edge> IOQueue = new LinkedBlockingQueue<Edge>();
        ExecutorService fileReadExecutor = Executors.newSingleThreadExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        AtomicLong edgeCountDown = new AtomicLong(edgeCount);
//        CountDownLatch edgeCountDown2 = new CountDownLatch(Integer.parseInt(Long.toString(edgeCount)));

//        try {
        long startTime = System.currentTimeMillis();
        Reader reader = new Reader(FILE, batchSize, toBeFilled, IOQueue);
        SamplePartitionThread partitionThread = new SamplePartitionThread(toBeFilled, edgeCountDown,IOQueue);
//            SamplePartitionThread partitionThread = new SamplePartitionThread(toBeFilled, edgeCountDown2, IOQueue);

        System.out.println("Starting file reader");
        fileReadExecutor.execute(reader);

        System.out.println("Starting partition threads");
        for(int i = 0; i < nThreads; i++) {
            executorService.execute(partitionThread);
        }

//            edgeCountDown2.await();
        System.out.println("Main waiting for edgeCountDown");
        try {
            awaitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("CountDown Latch - " + awaitLatch.getCount());
        System.out.println("Remaining edges - " + edgeCountDown.get());

        fileReadExecutor.shutdown();
        executorService.shutdown();

        long processTime = System.currentTimeMillis() - startTime;
        System.out.println("PROCESS TIME (MS) - " +  processTime);
        System.out.println("Total IO Time (MS) = " + IOTime);
        System.out.println("Total Wait Time (MS) - Producer = " + totalWaitTime_Producer);
        System.out.println("Total Decision Time (MS) - Consume = " + decisionTime);
        System.out.println("Total Wait Time (MS) - Consumer = " + totalWaitTime_Consumer);
        System.out.println("Aggregate Wait Time (MS) = " + (totalWaitTime_Producer.addAndGet(totalWaitTime_Consumer.longValue())) );

        System.exit(0);
    }
}
