package cn.juntaozhang.leetcode.sort;

import org.junit.Test;

import java.util.Arrays;

public class L912_merge {
    @Test
    public void case1() {
        System.out.println(Arrays.toString(sortArray(new int[]{5, 2, 3, 1})));
    }

    @Test
    public void case2() {
        System.out.println(Arrays.toString(sortArray(new int[]{5, 1, 1, 2, 0, 0})));
    }

    public int[] sortArray(int[] arr) {
        merge(arr, 0, arr.length - 1, new int[arr.length]);
        return arr;
    }

    public void merge(int[] arr, int left, int right, int[] tmp) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        merge(arr, left, mid, tmp);
        merge(arr, mid + 1, right, tmp);
        // max of left <= min of right, no need to merge
        if (arr[mid] <= arr[mid + 1]) {
            return;
        }
        merge(arr, left, mid, right, tmp);
    }

    // 将 arr[left...mid] 和 arr[mid+1...right] 合并
    public void merge(int[] arr, int left, int mid, int right, int[] tmp) {
        int i = left;
        int j = mid + 1;
        int k = 0;
        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                tmp[k++] = arr[i++];
            } else {
                tmp[k++] = arr[j++];
            }
        }
        while (i <= mid) {
            tmp[k++] = arr[i++];
        }
        while (j <= right) {
            tmp[k++] = arr[j++];
        }
        for (int l = 0; l < k; l++) {
            arr[left + l] = tmp[l];
        }
    }

}
