package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author juntzhang
 */
public class L209 {

    public int minSubArrayLen0(int target, int[] nums) {
        int sum = 0;
        TreeMap<Integer, Integer> map = new TreeMap<>(); // 前缀和 空间复杂度O(N)
        map.put(0, 0);
        int ans = 0;
        for (int i = 0; i < nums.length; i++) {
            sum = sum + nums[i];
            map.put(sum, i + 1);
            Map.Entry<Integer,Integer> entry = map.floorEntry(sum - target);
            if (entry != null) {
                ans = getResult(ans, i + 1 - entry.getValue());
            }
        }
        return ans;
    }

    private static int getResult(int oldVal, int newVal) {
        if (oldVal == 0) {
            oldVal = newVal;
        } else {
            oldVal = Math.min(oldVal, newVal);
        }
        return oldVal;
    }

    public int minSubArrayLen1(int target, int[] nums) {
        int sum = 0;
        int ans = 0;
        int j = 0;
        for (j = 0; j < nums.length; j++) {
            if (sum + nums[j] > target) {
                break;
            } else if (sum + nums[j] == target) {
                ans = j + 1;
                break;
            }
            sum = sum + nums[j];
        }
        for (int i = 1; i < nums.length; i++) {
            sum -= nums[i - 1];
            for (; j < nums.length; j++) {
                if (sum + nums[j] > target) {
                    break;
                } else if (sum + nums[j] == target) {
                    ans = getResult(ans, j - i + 1);
                    break;
                } else {
                    sum = sum + nums[j];
                }
            }
        }
        return ans;
    }

    public int minSubArrayLen(int target, int[] nums) {
        int ans = 0;
        int i = 0;
        int j = 0;
        int sum = 0;
        while (i <= j && j <= nums.length) {
            if (sum < target) {
                if (j == nums.length) {
                    break;
                }
                sum = nums[j] + sum;
                j++;
            } else {
                ans = ans != 0 ? Math.min(ans, j - i) : j - i;
                sum = sum - nums[i];
                i++;
            }
        }
        return ans;
    }

    @Test
    public void case1() {
        System.out.println(minSubArrayLen(7, new int[]{2, 3, 1, 2, 4, 3}));
    }

    @Test
    public void case2() {
        System.out.println(minSubArrayLen(4, new int[]{1, 4, 4}));
    }

    @Test
    public void case3() {
        System.out.println(minSubArrayLen(11, new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
    }
    @Test
    public void case4() {
        System.out.println(minSubArrayLen0(11, new int[]{1,2,3,4,5}));
    }
}
