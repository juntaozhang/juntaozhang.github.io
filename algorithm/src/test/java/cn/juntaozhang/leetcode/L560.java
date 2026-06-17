package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * 给你一个整数数组 nums 和一个整数 k ，请你统计并返回 该数组中和为 k 的子数组的个数 。
 * 子数组是数组中元素的连续非空序列。
 */
public class L560 {

    public int subarraySum0(int[] nums, int k) {
        int[] s = new int[nums.length + 1];
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, -1);
        for (int i = 0; i < nums.length; i++) {
            s[i + 1] = s[i] + nums[i];
            map.put(s[i + 1], i);
        }

        int res = 0;
        for (int i = 1; i < s.length; i++) {
            if (map.containsKey(s[i] - k)) {
                res++;
            }
        }
        return res;
    }

    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> s = new HashMap<>();
        int sum = 0;
        int res = 0;
        s.put(0, 1);
        for (int num : nums) {
            sum += num;
            if (s.containsKey(sum - k)) {
                res += s.get(sum - k);
            }
            s.put(sum, s.getOrDefault(sum, 0) + 1);
        }
        return res;
    }

    @Test
    public void case1() {
        System.out.println(subarraySum(new int[]{1, 1, 1}, 2));
    }

    @Test
    public void case2() {
        System.out.println(subarraySum(new int[]{1, 1, 1}, 1));
    }

    @Test
    public void case3() {
        System.out.println(subarraySum(new int[]{3, 4, 7, 2, -3, 1, 4, 2}, 7));
    }
}
