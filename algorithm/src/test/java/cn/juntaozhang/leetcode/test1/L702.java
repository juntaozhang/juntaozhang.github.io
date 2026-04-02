package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

public class L702 {
    public int search(int[] nums, int target) {
        return search(nums, 0, nums.length - 1, target);
    }

    private int search(int[] nums, int start, int end, int target) {
        if (start >= end) {
            return nums[start] == target ? start : -1;
        }

        int mid = start + (end - start) / 2;

        if (nums[mid] < target) {
            return search(nums, mid + 1, end, target);
        } else if (nums[mid] > target) {
            return search(nums, start, mid - 1, target);
        } else {
            return mid;
        }
    }

    @Test
    public void case1() {
        Assert.assertEquals(-1, search(new int[]{-1, 0, 3, 5, 9, 12}, 13));
    }

    @Test
    public void case2() {
        Assert.assertEquals(4, search(new int[]{-1, 0, 3, 5, 9, 12}, 9));
    }
}
