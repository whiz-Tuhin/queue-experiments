package LinkedBlockingQueue;

import Definition.Edge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Reader implements Runnable {

    private LinkedBlockingQueue<Edge> IOQueue;
    private FileReader file;
    private BufferedReader input;
    private long batchSize = 0;
    private AtomicBoolean toBeFilled;
    private int notifyCounter = 0;
    private long batchCounter = 0;
    private long edgeCounter = 0;

    public Reader(String filePath, long batchSize, AtomicBoolean toBeFilled,LinkedBlockingQueue<Edge> IOQueue) {
        this.IOQueue = IOQueue;
        this.toBeFilled = toBeFilled;
        this.batchSize = batchSize;
        try {
            this.file = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        input = new BufferedReader(file);
    }


    public boolean read(long batchSize) {
        batchCounter++;
        for(int i = 0; i < batchSize; i++) {
            try {
                String line = input.readLine();
                if (line != null) {
                    Edge e = Edge.toEdge(line);
                    IOQueue.offer(e);
                    edgeCounter++;
                } else {
                    synchronized (Main.moreLines) {
                        System.out.println("End of file reached");
                        Main.moreLines = false;
                    }
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Batch No. Read -> " + batchCounter);
        System.out.println("IOQueue Size -> " + IOQueue.size());
        return  true;
    }

    public void run() {
        while(true) {
            try {
                // contention point
                Main.lock.acquire();
                long beforeIO =  System.currentTimeMillis();
                boolean hasMore = read(batchSize);
                long IOdelta = System.currentTimeMillis() - beforeIO;
                Main.IOTime.addAndGet(IOdelta);

                synchronized (Main.consumer) {
                    System.out.println("Producer notifying consumers to resume execution");
                    Main.consumer.notifyAll();
                }

                if (!hasMore) {
                    System.out.println("Reader thread exiting, edges read - " + edgeCounter);
                    Main.lock.release();
                    Main.awaitLatch.countDown();
                    return;
                }

                long beforeWait = System.currentTimeMillis();
                synchronized (Main.producer) {
                    System.out.println("Producer waiting for fill request");
                    Main.lock.release();
                    Main.producer.wait();
                }
                long waitDelta = System.currentTimeMillis() - beforeWait;
                Main.totalWaitTime_Producer.addAndGet(waitDelta);

            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
    }
}