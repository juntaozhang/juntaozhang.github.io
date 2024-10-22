package cn.juntaozhang.leetcode.二分查找;

import org.junit.Assert;
import org.junit.Test;

public class L33搜索旋转排序数组 {

    // binary search
    public int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            }
            // left is ordered
            if (nums[left] <= nums[mid]) {
                // in left order part
                if (nums[left] <= target && target < nums[mid]) {
                    right = mid - 1;
                }
                // in right disorder part
                else {
                    left = mid + 1;
                }
            }
            // right is ordered
            else {
                // in right order part
                if (nums[mid] < target && target <= nums[right]) {
                    left = mid + 1;
                }
                // in left disorder part
                else {
                    right = mid - 1;
                }
            }
        }
        return -1;
    }

    @Test
    public void case1() {
        Assert.assertEquals(7, search(new int[]{4, 5, 6, 7, 8, 9, 0, 1, 2}, 1));
    }

    @Test
    public void case2() {
        Assert.assertEquals(-1, search(new int[]{4, 5, 6, 7, 0, 1, 2}, 3));
    }

    @Test
    public void case3() {
        Assert.assertEquals(-1, search(new int[]{4, 5, 6, 7, 8, 9, 0, 1, 2}, 3));
    }

    @Test
    public void case4() {
//        Assert.assertEquals(0, search(new int[]{1, 3}, 1));
        Assert.assertEquals(1, search(new int[]{3, 1}, 1));
    }
}
