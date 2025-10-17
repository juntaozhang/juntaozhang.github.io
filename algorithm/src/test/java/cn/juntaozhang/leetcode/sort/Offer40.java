package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class Offer40 {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new Offer40().getLeastNumbers(new int[]{0, 1, 2, 1}, 4)));
        System.out.println(Arrays.toString(new Offer40().getLeastNumbers(new int[]{3, 2, 1}, 2)));
    }

    public int[] getLeastNumbers(int[] arr, int k) {
        int[] ans = new int[k];
        buildHeap(arr);
        int n = arr.length - 1;
        for (int i = 0; i < k; i++) {
            ans[i] = arr[0];
            swap(arr, 0, n);
            n--;
            bubbleMin(arr, n, 0);
        }
        return ans;
    }

    void buildHeap(int[] arr) {
        int n = arr.length;
        for (int i = n / 2; i >= 0; i--) {
            bubbleMin(arr, n - 1, i);
        }
    }

    void bubbleMin(int[] arr, int len, int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int smallest = i;
        if (l <= len && arr[l] < arr[smallest]) {
            smallest = l;
        }

        if (r <= len && arr[r] < arr[smallest]) {
            smallest = r;
        }

        if (smallest != i) {
            swap(arr, smallest, i);
            bubbleMin(arr, len, smallest);
        }
    }

    public void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
