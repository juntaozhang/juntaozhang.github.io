package cn.juntaozhang.design;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class L359_RateLimiter {
    static class MyRateLimiter {
        private final int gap;
        private final Map<String, Integer> map;

        MyRateLimiter() {
            this.map = new HashMap<>();
            this.gap = 10;
        }

        public boolean shouldPrintMessage(int timestamp, String message) {
            // 获取该消息的限制时间，如果不存在则默认为 0 (表示随时可以打印)
            Integer maxTimestamp = map.getOrDefault(message, 0);

            // 如果当前时间大于等于限制时间，说明可以打印
            if (timestamp >= maxTimestamp) {
                map.put(message, timestamp + gap);
                return true;
            }
            return false;
        }
    }

    @Test
    public void case1() {
        MyRateLimiter rateLimiter = new MyRateLimiter();
        System.out.println(rateLimiter.shouldPrintMessage(1, "foo"));
        System.out.println(rateLimiter.shouldPrintMessage(2, "bar"));
        System.out.println(rateLimiter.shouldPrintMessage(3, "foo"));
        System.out.println(rateLimiter.shouldPrintMessage(8, "foo"));
        System.out.println(rateLimiter.shouldPrintMessage(10, "foo"));
        System.out.println(rateLimiter.shouldPrintMessage(11, "foo"));
        System.out.println(rateLimiter.shouldPrintMessage(20, "foo"));
    }

    @Test
    public void case2() throws InterruptedException {
        MyRateLimiter rateLimiter = new MyRateLimiter();
        for (int i = 0; i < 1000; i++) {
            if (rateLimiter.shouldPrintMessage(i, "foo")) {
                System.out.println("+");
            } else {
                System.out.print("-");
            }
        }
    }

    @Test
    public void case3() throws InterruptedException {
        ActiveLeakyBucket rateLimiter = new ActiveLeakyBucket();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                if (rateLimiter.shouldPrintMessage(i, "foo")) {
                    System.out.println("+");
                } else {
                    System.out.print("-");
                }
            }
        }
    }

    @Test
    public void case4() throws InterruptedException {
        ActiveLeakyBucket2 rateLimiter = new ActiveLeakyBucket2();
        for (int i = 0; i < 1000; i++) {
            if (rateLimiter.shouldPrintMessage("foo")) {
                System.out.println("+");
            } else {
                System.out.print("-");
            }
            Thread.sleep(20);
        }
    }

    @Test
    public void case_guava() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.create(5, Duration.ofMillis(1000));
//        RateLimiter rateLimiter = RateLimiter.create(5);
        for (int i = 0; i < 1000; i++) {
            if (rateLimiter.tryAcquire()) {
                System.out.println("+");
            } else {
                System.out.print("-");
            }
            Thread.sleep(20);
        }
    }

    static class ActiveLeakyBucket {
        private final int rate;
        private final int capacity;
        private final Map<String, int[]> map;

        ActiveLeakyBucket() {
            this.rate = 2; // 每秒漏2个
            this.capacity = 5;
            this.map = new HashMap<>();
        }

        public boolean shouldPrintMessage(int timestamp, String message) {
            int[] arr = map.getOrDefault(message, new int[]{0, 0});
            int lastTime = arr[0];
            int lastCapacity = arr[1];

            int leak = (timestamp - lastTime) * rate;
            int newCapacity = Math.max(lastCapacity - leak, 0) + 1;
            if (newCapacity > capacity) {
                return false;
            } else {
                arr[0] = timestamp;
                arr[1] = newCapacity;
                map.put(message, arr);
                return true;
            }
        }
    }

    static class ActiveLeakyBucket2 {
        private final Map<String, Integer> map;
        private final int capacity;

        public ActiveLeakyBucket2() {
            map = new ConcurrentHashMap<>();
            capacity = 10;
            startLeaking();
        }

        /**
         * 模拟后台匀速漏水
         * 不管有没有请求，这个线程都在跑，体现“匀速”
         */
        private void startLeaking() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("LeakyBucket-Leaker");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(() -> {
                map.forEach((k, v) -> {
                    if (v > 0) {
                        map.put(k, v - 1);
                    }
                });
            }, 0, 500, TimeUnit.MILLISECONDS);
        }

        public boolean shouldPrintMessage(String message) {
            Integer capacity = map.getOrDefault(message, 0);
            if (capacity >= this.capacity) {
                return false;
            } else {
                map.put(message, capacity + 1);
                return true;
            }
        }
    }
}
