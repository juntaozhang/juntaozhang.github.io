package cn.juntaozhang.leetcode;

import org.junit.Test;

public class L88 {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        for (int i = m + n - 1, j = m - 1, k = n - 1; i >= 0; i--) {
            if (k < 0 || (j >= 0 && nums1[j] > nums2[k])) {
                nums1[i] = nums1[j];
                j--;
            } else {
                nums1[i] = nums2[k];
                k--;
            }
        }
    }

    @Test
    public void case1() {
        int[] nums1 = new int[]{1, 2, 3, 0, 0, 0};
        int[] nums2 = new int[]{2, 5, 6};
        merge(nums1, 3, nums2, 3);
        for (int i : nums1) {
            System.out.println(i);
        }
    }

    @Test
    public void case2() {
        int[] nums1 = new int[]{1};
        int[] nums2 = new int[]{};
        merge(nums1, 1, nums2, 0);
        for (int i : nums1) {
            System.out.println(i);
        }
    }

    @Test
    public void case3() {
        int[] nums1 = new int[]{0};
        int[] nums2 = new int[]{1};
        merge(nums1, 0, nums2, 1);
        for (int i : nums1) {
            System.out.println(i);
        }
    }
}
