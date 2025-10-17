package cn.juntaozhang.design;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketTest {
    @Test
    public void case1() throws InterruptedException {
        // 示例：桶的容量为10，每秒添加5个令牌
        TokenBucket bucket = new TokenBucket(5, 2, 1000);

        // 模拟请求
        for (int i = 0; i < 100; i++) {
            if (bucket.tryConsume(1)) {
                System.out.println("Request " + i + " processed.");
            } else {
                System.out.println("Request " + i + " denied.");
            }
            Thread.sleep(100);
        }

    }

    public class TokenBucket {
        private final long capacity;         // 桶的容量
        private final long refillTokens;     // 每次添加的令牌数
        private final long refillInterval;   // 添加令牌的时间间隔（毫秒）
        private final AtomicLong tokens;     // 当前桶中的令牌数
        private long lastRefillTime;         // 上次添加令牌的时间

        public TokenBucket(long capacity, long refillTokens, long refillInterval) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillInterval = refillInterval;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        // 获取令牌的方法
        public synchronized boolean tryConsume(long numTokens) {
            refill();
            if (tokens.get() >= numTokens) {
                tokens.getAndAdd(-numTokens);
                return true;
            }
            return false;
        }

        // 添加令牌的方法
        private void refill() {
            long now = System.currentTimeMillis();
            if (now > lastRefillTime) {
                long elapsedTime = now - lastRefillTime;
                long newTokens = (elapsedTime / refillInterval) * refillTokens;
                if (newTokens > 0) {
                    long newTokenCount = Math.min(tokens.get() + newTokens, capacity);
                    tokens.set(newTokenCount);
                    lastRefillTime = now;
                }
            }
        }
    }
}
