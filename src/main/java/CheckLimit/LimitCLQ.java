import java.util.concurrent.ConcurrentLinkedQueue;

public class LimitCLQ {

//    public static class Edge {
//        private Random random = new Random();
//        private long v1;
//        private long v2;
//        public Edge() {
//            this.v1 = random.nextLong();
//            this.v2 = random.nextLong();
//        }
//
//        public static Edge createEdge(){
//            return new Edge();
//        }
//    }

    public static void main(String[] args) {
        long counter = 1;
        System.out.println("Max JVM memory: " + Runtime.getRuntime().maxMemory());
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>() ;

        while(counter > 0) {
            try {
                queue.add(counter);
            } catch (OutOfMemoryError e) {
                System.out.println("Counter - " + counter);
                e.printStackTrace();
                break;
            }
            counter++;
        }

        System.exit(0);
    }
}
