package cn.juntaozhang.leetcode;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class L1 {
    public int[] twoSum2(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer t = map.get(target - nums[i]);
            if (t != null) {
                return new int[]{t, i};
            } else {
                map.put(nums[i], i);
            }
        }
        return new int[0];
    }

    public int[] twoSum(int[] nums, int target) {
        int[] sorted = Arrays.copyOf(nums, nums.length);
        Arrays.sort(sorted);
        int i = 0, j = sorted.length - 1;
        while (i < j) {
            int sum = sorted[i] + sorted[j];
            if (sum == target) {
                break;
            } else if (sum < target) {
                i++;
            } else {
                j--;
            }
        }
        // find index
        int[] result = new int[2];
        for (int k = 0, r = 0; k < nums.length; k++) {
            if (r == 0 && nums[k] == sorted[i]) {
                result[r++] = k;
            } else if (r == 1 && nums[k] == sorted[j]) {
                result[r] = k;
                break;
            }
        }
        return result;
    }


    @Test
    public void case1() {
        int[] result = twoSum(new int[]{15, 2, 7, 11,}, 9);
        System.out.println(ArrayUtils.toString(result));
    }

    @Test
    public void case2() {
        int[] result = twoSum(new int[]{3, 3}, 6);
        System.out.println(ArrayUtils.toString(result));
    }
}
