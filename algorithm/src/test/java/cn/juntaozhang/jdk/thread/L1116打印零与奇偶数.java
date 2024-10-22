package cn.juntaozhang.jdk.thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

public class L1116打印零与奇偶数 {
    public static void main(String[] args) {
        new L1116打印零与奇偶数().test();
    }

    public void test() {
        ZeroEvenOdd zeroEvenOdd = new ZeroEvenOdd(2);
        new Thread(() -> {
            try {
                zeroEvenOdd.zero(System.out::print);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                zeroEvenOdd.even(System.out::print);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                zeroEvenOdd.odd(System.out::print);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ZeroEvenOdd {
        private final Lock lock = new ReentrantLock(true);
        private final Condition condition = lock.newCondition();
        private final int n;
        private final AtomicInteger i;
        private volatile int flag = 0;

        public ZeroEvenOdd(int n) {
            this.n = n;
            this.i = new AtomicInteger(1);
        }

        // printNumber.accept(x) outputs "x", where x is an integer.
        public void zero(IntConsumer printNumber) throws InterruptedException {
            while (i.get() <= n) {
                lock.lock();
                try {
                    while (flag != 0 && i.get() <= n) {
                        condition.await();
                    }
                    if (i.get() > n) {
                        break;
                    }
                    printNumber.accept(0);
                    if ((i.get() & 1) == 0) {
                        flag = 1;
                    } else {
                        flag = 2;
                    }
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void even(IntConsumer printNumber) throws InterruptedException {
            while (i.get() <= n) {
                lock.lock();
                try {
                    while (flag != 1 && i.get() <= n) {
                        condition.await();
                    }
                    if (i.get() > n) {
                        break;
                    }
                    printNumber.accept(i.getAndIncrement());
                    flag = 0;
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void odd(IntConsumer printNumber) throws InterruptedException {
            while (i.get() <= n) {
                lock.lock();
                try {
                    while (flag != 2 && i.get() <= n) {
                        condition.await();
                    }
                    if (i.get() > n) {
                        break;
                    }
                    printNumber.accept(i.getAndIncrement());
                    flag = 0;
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
