package cn.juntaozhang.leetcode.二分查找;

import org.junit.Test;

public class L162寻找峰值 {
    // (left, right)
    public int findPeakElement(int[] nums) {
        if (nums == null || nums.length == 0) return -1;
        int left = 0, right = nums.length - 1;
        while (left + 1 < right) {
            int mid = left + (right - left) / 2;
            // mid点下坡
            if (nums[mid] > nums[mid + 1]) {
                right = mid;
            }
            // mid点上坡
            else {
                left = mid;
            }
        }
        return nums[left] > nums[right] ? left : right;
    }

    @Test
    public void case1() {
        System.out.println(findPeakElement(new int[]{2}));
    }

    @Test
    public void case2() {
        System.out.println(findPeakElement(new int[]{2, 1}));
    }

    @Test
    public void case3() {
        System.out.println(findPeakElement(new int[]{2, 4, 3}));
    }

    @Test
    public void case4() {
        System.out.println(findPeakElement(new int[]{1, 2, 3, 1}));
    }

    @Test
    public void case5() {
        System.out.println(findPeakElement(new int[]{1, 2, 1, 3, 5, 6, 4}));
    }
}
