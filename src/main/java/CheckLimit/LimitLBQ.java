package CheckLimit;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class LimitLBQ {
    public static class Edge {
        private Random random = new Random();
        private long v1;
        private long v2;

        public Edge() {
            this.v1 = random.nextLong();
            this.v2 = random.nextLong();
        }

        public static Edge createEdge() {
            return new Edge();
        }
    }

    public static void main(String[] args) {
        long counter = 0;
        LinkedBlockingQueue<Edge> queue = new LinkedBlockingQueue<>();
        System.out.println("Max JVM memory: " + Runtime.getRuntime().maxMemory());

        while(true) {
            try {
                Edge edge = Edge.createEdge();
                queue.add(edge);
                counter++;
            } catch (OutOfMemoryError | IllegalStateException e) {
                System.out.println("Counter - " + counter);
                e.printStackTrace();
                break;
            }
        }
        System.exit(0);
    }
}
