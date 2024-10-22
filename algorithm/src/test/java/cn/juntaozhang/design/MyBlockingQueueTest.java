package cn.juntaozhang.design;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyBlockingQueueTest {
    @Test
    public void case1() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(11);
        MyBlockingQueue queue = new MyBlockingQueue(10);
        service.submit(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    int i = new Random().nextInt(100);
                    queue.enqueue(i);
                    System.out.println(Thread.currentThread().getName() + " enqueue " + i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (int i = 0; i < 3; i++) {
            service.submit(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);
                        System.out.println(Thread.currentThread().getName() + " dequeue " + queue.dequeue());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        service.awaitTermination(5, TimeUnit.SECONDS);
    }

    public class MyBlockingQueue {
        LinkedList<Integer> queue = new LinkedList<>();
        int capacity;

        public MyBlockingQueue(int capacity) {
            this.capacity = capacity;
        }

        public void enqueue(int n) throws InterruptedException {
            synchronized (this) {
                while (queue.size() == capacity) {
                    wait();
                }
                queue.addLast(n);
                notifyAll();
            }
        }

        public int dequeue() throws InterruptedException {
            synchronized (this) {
                while (queue.isEmpty()) {
                    wait();
                }
                int ret = queue.removeFirst();
                notifyAll();
                return ret;
            }
        }
    }
}
