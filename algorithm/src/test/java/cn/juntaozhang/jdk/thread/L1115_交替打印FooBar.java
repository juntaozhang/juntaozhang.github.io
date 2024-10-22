package cn.juntaozhang.jdk.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class L1115_交替打印FooBar {
    public static void main(String[] args) {
        new L1115_交替打印FooBar().test();
    }

    public void test() {
        FooBar fooBar = new FooBar(19);
        new Thread(() -> {
            try {
                fooBar.foo(() -> System.out.print("foo"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                fooBar.bar(() -> System.out.println("bar"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static class FooBar {
        // fair 根据等待时间长短来优先获取锁
        private final Lock lock = new ReentrantLock(true);
        private final Condition condition = lock.newCondition();
        private volatile boolean flag = true;

        private int n;


        public FooBar(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (!flag) {
                        condition.await();
                    }
                    // printFoo.run() outputs "foo". Do not change or remove this line.
                    printFoo.run();
                    flag = false;
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (flag) {
                        condition.await();
                    }
                    // printBar.run() outputs "bar". Do not change or remove this line.
                    printBar.run();
                    flag = true;
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
