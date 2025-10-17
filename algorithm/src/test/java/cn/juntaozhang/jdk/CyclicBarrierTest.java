package cn.juntaozhang.jdk;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {
    public static void main(String[] args) {
        int numberOfThreads = 3;
        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads, new Runnable() {
            @Override
            public void run() {
                System.out.println("All parties have arrived at the barrier, let's proceed");
            }
        });

        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(new Task(barrier)).start();
        }
    }

    static class Task implements Runnable {
        private CyclicBarrier barrier;

        public Task(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " is waiting at the barrier");
                Thread.sleep(new Random().nextInt(2000));
                barrier.await();
                System.out.println(Thread.currentThread().getName() + " has crossed the barrier");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
