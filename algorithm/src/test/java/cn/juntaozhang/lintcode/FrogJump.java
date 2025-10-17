package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 
 */
public class FrogJump {
    Map<Integer, Integer> map = new HashMap<>();
    int[] stones;
    int len;

    @Test
    public void test() {
        System.out.println(canCross(new int[]{0, 1, 3, 5, 6, 8, 12, 17}));
    }

    public boolean canCross(int[] stones) {
        // Write your code here
        HashMap<Integer, HashSet<Integer>> dp = new HashMap<>(stones.length);
        for (int i = 0; i < stones.length; i++) {
            dp.put(stones[i], new HashSet<>());
        }
        dp.get(0).add(0);

        for (int i = 0; i < stones.length - 1; ++i) {
            int stone = stones[i];
            for (int k : dp.get(stone)) {
                // k - 1
                if (k - 1 > 0 && dp.containsKey(stone + k - 1))
                    dp.get(stone + k - 1).add(k - 1);
                // k
                if (dp.containsKey(stone + k))
                    dp.get(stone + k).add(k);
                // k + 1
                if (dp.containsKey(stone + k + 1))
                    dp.get(stone + k + 1).add(k + 1);
            }
        }

        return !dp.get(stones[stones.length - 1]).isEmpty();
    }


}
