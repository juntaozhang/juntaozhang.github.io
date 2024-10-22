package cn.juntaozhang.design;

public class LeakyBucket {
    private final int capacity; // 桶的容量
    private final int leakRate; // 漏水速率（单位时间漏出的单位数量）
    private int water; // 当前桶中的水量
    private long lastLeakTimestamp; // 上次漏水的时间戳

    public LeakyBucket(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.water = 0;
        this.lastLeakTimestamp = System.currentTimeMillis();
    }

    public static void main(String[] args) throws InterruptedException {
        // 示例：桶的容量为10，每秒漏出3个单位
        LeakyBucket bucket = new LeakyBucket(5, 2);

        // 模拟请求
        for (int i = 0; i < 50; i++) {
            if (bucket.addWater(1)) {
                System.out.println("Request " + i + " processed. Current water level: " + bucket.getWater());
            } else {
                System.out.println("Request " + i + " denied. Current water level: " + bucket.getWater());
            }
            Thread.sleep(100);
        }
    }

    // 尝试向桶中添加水
    public synchronized boolean addWater(int amount) {
        leak();
        if (water + amount <= capacity) {
            water += amount;
            return true;
        } else {
            return false;
        }
    }

    // 漏水操作，根据时间间隔计算应该漏出的水量
    private void leak() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastLeakTimestamp;
        int leakedAmount = (int) (elapsedTime / 1000 * leakRate);

        if (leakedAmount > 0) {
            if (leakedAmount > water) {
                water = 0;
            } else {
                water -= leakedAmount;
            }
            lastLeakTimestamp = now;
        }
    }

    // 获取当前桶中的水量（用于调试）
    public int getWater() {
        return water;
    }
}
