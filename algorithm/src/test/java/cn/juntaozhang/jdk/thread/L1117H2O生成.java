package cn.juntaozhang.jdk.thread;

import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class L1117H2O生成 {
    class H2O {
        private final Lock lock = new ReentrantLock(true);
        private final Condition condition = lock.newCondition();
        private final AtomicInteger hCnt = new AtomicInteger(0);
        private final AtomicInteger oCnt = new AtomicInteger(0);

        public H2O() {
        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            lock.lock();
            try {
                while (hCnt.get() == 2) {
                    condition.await();
                }
                // releaseHydrogen.run() outputs "H". Do not change or remove this line.
                releaseHydrogen.run();
                hCnt.getAndIncrement();
                if (hCnt.get() == 2) {
                    oCnt.set(1);
                }
                condition.signalAll();
            } finally {
                lock.unlock();
            }

        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            lock.lock();
            try {
                while (oCnt.get() == 0) {
                    condition.await();
                }
                // releaseOxygen.run() outputs "O". Do not change or remove this line.
                releaseOxygen.run();
                oCnt.getAndSet(0);
                hCnt.set(0);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    class H2O_2 {
        private final Semaphore h = new Semaphore(0);
        private final Semaphore o = new Semaphore(2);

        public H2O_2() {
        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            o.acquire();
            // releaseHydrogen.run() outputs "H". Do not change or remove this line.
            releaseHydrogen.run();
            h.release();
        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            h.acquire(2);
            // releaseOxygen.run() outputs "O". Do not change or remove this line.
            releaseOxygen.run();
            o.release(2);
        }
    }

    public void test(){
        H2O_2 h2O = new H2O_2();
        new Thread(() -> {
            try {
                h2O.hydrogen(() -> System.out.print("H"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                h2O.hydrogen(() -> System.out.print("H"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                h2O.oxygen(() -> System.out.print("O"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        new L1117H2O生成().test();
    }
}
