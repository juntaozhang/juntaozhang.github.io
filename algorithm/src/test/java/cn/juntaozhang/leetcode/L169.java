package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class L169 {
    // Boyer-Moore 投票算法
    public int majorityElement(int[] nums) {
        int count = 0;
        int candidate = 0;
        for (int num : nums) {
            if (count == 0) {
                candidate = num;
            }
            count += candidate == num ? 1 : -1;
        }
        return candidate;
    }

    public int majorityElement2(int[] nums) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Integer num : nums) {
            if (!counts.containsKey(num)) {
                counts.put(num, 1);
            } else {
                counts.put(num, counts.get(num) + 1);
            }
        }
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > nums.length / 2) {
                return entry.getKey();
            }
        }
        return 0;
    }


    @Test
    public void case1() {
        System.out.println(majorityElement(new int[]{3, 2, 3}));
    }

    @Test
    public void case2() {
        System.out.println(majorityElement2(new int[]{2, 2, 1, 1, 1, 2, 2}));
    }

    @Test
    public void case3() {
        System.out.println(majorityElement2(new int[]{2}));
    }

}
