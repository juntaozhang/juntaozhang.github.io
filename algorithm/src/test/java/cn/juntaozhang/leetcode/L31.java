package cn.juntaozhang.leetcode;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

public class L31 {
    /*
        123456
        123465
        123546
        123564
        123645
        123654
        ...
        654321

   */
    public void nextPermutation(int[] nums) {
        // 123541 -> 123 541
        int i = findAscOrderFromRight(nums);
        if (i == 0) {
            reverse(nums, 0);
        } else {
            // 123 541 -> 123 541
            //                 ^
            //                 |
            int j = findNextLargeThan(nums, i, nums[i - 1]);
            // 123 541 -> 124 531
            swap(nums, i - 1, j);
            // 124 531 -> 124 135
            reverse(nums, i);

        }
    }

    private int findNextLargeThan(int[] nums, int start, int target) {
        int val = -1, next = nums.length - 1;
        for (int i = start; i < nums.length; i++) {
            if (nums[i] > target) {
                if (val == -1 || nums[i] < val) {
                    val = nums[i];
                    next = i;
                }
            }
        }
        return next;
    }

    // 123541 -> 123 541
    private int findAscOrderFromRight(int[] nums) {
        for (int i = nums.length - 1; i > 0; i--) {
            if (nums[i] > nums[i - 1]) {
                return i;
            }
        }
        // 54321
        return 0;
    }

    private void reverse(int[] nums, int start) {
        int left = start, right = nums.length - 1;
        while (left < right) {
            swap(nums, left, right);
            left++;
            right--;
        }
    }

    private void swap(int[] nums, int i, int j) {
        if (i == j) return;
        int t = nums[i];
        nums[i] = nums[j];
        nums[j] = t;
    }

    @Test
    public void case1() {
        int[] nums = new int[]{1, 2, 3};
        nextPermutation(nums);
        StringUtils.print(nums);
    }

    @Test
    public void case2() {
        // 123645
        int[] nums = new int[]{1, 2, 3, 5, 6, 4};
        nextPermutation(nums);
        StringUtils.print(nums);
    }

    @Test
    public void case3() {
        // 467
        int[] nums = new int[]{7, 6, 4};
        nextPermutation(nums);
        StringUtils.print(nums);
    }
}
