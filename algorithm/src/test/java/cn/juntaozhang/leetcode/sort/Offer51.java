package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class Offer51 {
    public int merge(int[] nums, int s1, int e1, int s2, int e2) {
        int count = 0;
        int n = e1 - s1 + 1;
        int i = 0, k = s1, j = s2;
        int[] tmp = new int[n];
        System.arraycopy(nums, s1, tmp, 0, n);
        while (k <= e2) {
            if (i == n) {  // xxx|  y|yy
                nums[k] = nums[j++];
            } else if (j > e2) { // x|xx yyy|
                nums[k] = tmp[i++];
            } else if (tmp[i] > nums[j]) {
                nums[k] = nums[j++];
                count += (n - i);
            } else {
                nums[k] = tmp[i++];
            }
            k++;
        }
        return count;
    }

    public int mergeSort(int[] nums, int s, int e) {
        if (s >= e) return 0;
        int m = (s + e) / 2;
        int l = mergeSort(nums, s, m);
        int r = mergeSort(nums, m + 1, e);
        return merge(nums, s, m, m + 1, e) + l + r;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{7, 5, 6, 4};
//        int[] arr = new int[]{7, 3, 2, 6, 0, 1, 5, 4};
//        int[] arr = new int[]{1, 3, 2, 3, 1};
//        int[] arr = new int[]{1, 1, 1};
//        int[] arr = new int[]{1};
//        int[] arr = new int[]{};
        System.out.println(new Offer51().mergeSort(arr, 0, arr.length - 1));
        System.out.println(Arrays.toString(arr));
//        new Offer51().merge(arr, 0, 0, 1, 1);
//        new Offer51().merge(arr, 2, 2, 3, 3);
//        new Offer51().merge(arr, 0, 1, 2, 3);
    }
}
