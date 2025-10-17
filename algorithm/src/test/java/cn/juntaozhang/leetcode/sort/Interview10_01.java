package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class Interview10_01 {
    public void merge(int[] A, int m, int[] B, int n) {
        int[] arr = new int[m + n];
        int i = 0, j = 0, k = 0;
        for (; i < m && j < n; k++) {
            if (A[i] >= B[j]) {
                arr[k] = B[j];
                j++;
            } else {
                arr[k] = A[i];
                i++;
            }
        }
        if (i != m) {
            for (; i < m; i++, k++) {
                arr[k] = A[i];
            }
        }
        if (j != n) {
            for (; j < n; j++, k++) {
                arr[k] = B[j];
            }
        }
        System.arraycopy(arr, 0, A, 0, m + n);
        System.out.println(Arrays.toString(A));
    }

    public static void main(String[] args) {
        new Interview10_01().merge(new int[]{1, 2, 3, 0, 0, 0}, 3, new int[]{2, 5, 6}, 3);
    }
}
