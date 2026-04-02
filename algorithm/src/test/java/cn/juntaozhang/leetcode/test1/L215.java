package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

// L912_heap
public class L215 {
    public int findKthLargest(int[] nums, int k) {
        buildMaxHeap(nums);
        int n = nums.length;
        for (int i = 0; i < k; i++) {
            swap(nums, 0, n - i - 1);
            heapify(nums, 0, n - i - 1);
        }
        return nums[n - k];
    }

    private void buildMaxHeap(int[] nums) {
        for (int i = nums.length / 2 - 1; i >= 0; i--) {
            heapify(nums, i, nums.length);
        }
    }

    private void heapify(int[] nums, int i, int n) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int j = i;
        if (l < n && nums[j] < nums[l]) {
            j = l;
        }
        if (r < n && nums[j] < nums[r]) {
            j = r;
        }
        if (i != j) {
            swap(nums, i, j);
            heapify(nums, j, n);
        }
    }

    private void swap(int[] nums, int i, int j) {
        int t = nums[i];
        nums[i] = nums[j];
        nums[j] = t;
    }

    public int findKthLargest2(int[] nums, int k) {
        return quickSelect(nums, 0, nums.length - 1, k);
    }

    private int quickSelect(int[] nums, int i, int j, int k) {
        int mid = partition(nums, i, j);
        if (j - mid == k - 1) {
            return nums[mid];
        } else if (j - mid >= k) {
            return quickSelect(nums, mid + 1, j, k);
        } else {
            return quickSelect(nums, i, mid - 1, k - (j - mid + 1));
        }
    }

    private int partition(int[] nums, int i, int j) {
        if(i == j) {
            return i;
        }
        int pivot = i++;
        while (i < j) {
            while (i < j && nums[i] < nums[pivot]) i++;
            while (i < j && nums[j] > nums[pivot]) j--;
            if (i < j) {
                swap(nums, i, j);
                i++;
                j--;
            }
        }

        if (nums[i] > nums[pivot]) i--;
        swap(nums, pivot, i);
        return i;
    }

    @Test
    public void case1() {
        Assert.assertEquals(5, findKthLargest2(new int[]{3, 2, 1, 5, 6, 4}, 2));
    }

    @Test
    public void case2() {
        Assert.assertEquals(1, findKthLargest2(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 5));// TODO
    }

}
