package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

// 215. 数组中的第 K 个最大元素
public class L215 {
    public static void main(String[] args) {
//        System.out.println(new L215().findKthLargest(new int[]{3}, 1));
        System.out.println(new L215().findKthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
//        System.out.println(new L215().findKthLargest(new int[]{3, 2, 3, 1, 2, 4, 5, 5, 6}, 4));
    }

    public int findKthLargest(int[] nums, int k) {
//        Arrays.sort(nums);
//        return nums[nums.length - k];

        buildMaxHeap(nums);
        System.out.println("buildMaxHeap:" + Arrays.toString(nums));
        int len = nums.length;
        for (int i = 0; i < k; i++) {
            swap(nums, 0, len - 1 - i);
            maxHeapify(nums, len - 1 - i, 0);
            System.out.println(i + ":" + Arrays.toString(nums));
        }
        return nums[len - k];
    }

    public void buildMaxHeap(int[] nums) {
        for (int i = nums.length / 2; i >= 0; i--) {
            maxHeapify(nums, nums.length, i);
        }
    }

    public void maxHeapify(int[] nums, int len, int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int largest = i;
        if (l < len && nums[l] > nums[largest]) {
            largest = l;
        }
        if (r < len && nums[r] > nums[largest]) {
            largest = r;
        }

        if (largest != i) {
            swap(nums, i, largest);
            maxHeapify(nums, len, largest);
        }
    }

    public void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
