package cn.juntaozhang.leetcode;

import org.junit.Test;

public class L35 {
    public int searchInsert(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;
        while(l <= r) {
            int t = l + (r - l) / 2;
            if(nums[t] == target) {
                return t;
            } else if(nums[t] < target) {
                l = t + 1;
            } else {
                r = t - 1;
            }
        }
        return l;
    }

    @Test
    public void case1() {
        System.out.println(searchInsert(new int[]{1,3,5,6}, 2));
    }
}
