package cn.juntaozhang.leetcode.sort;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.Arrays;

public class L912_heap {
    @Test
    public void case1() {
        System.out.println(Arrays.toString(new L912_heap().sortArray(new int[]{5, 2, 3, 1})));
    }

    @Test
    public void case2() {
        System.out.println(Arrays.toString(new L912_heap().sortArray(new int[]{5, 1, 1, 2, 0, 0})));
    }

    public int[] sortArray(int[] nums) {
        buildMaxHeap(nums);
        System.out.println("buildMaxHeap:" + Arrays.toString(nums));
        int len = nums.length;
        for (int i = 0; i < len - 1; i++) {
            swap(nums, 0, len - 1 - i);
            maxHeapify(nums, len - 1 - i, 0);
            System.out.println(i + ":" + Arrays.toString(nums));
        }
        return nums;
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

    @Test
    public void testBuildMaxHeap() {
        /*
          5
       2     3
      1  7  4  6

        i = 2
          5
       2     6
      1  7  4  3

        i = 1
          5
       7     6
      1  2  4  3

        i = 0
          7
       5     6
      1  2  4  3
        */
        int[] arr = new int[]{5, 2, 3, 1, 7, 4, 6};
        StringUtils.print(arr);
        maxHeapify(arr, arr.length, 2);
        StringUtils.print2(arr);
        maxHeapify(arr, arr.length, 1);
        StringUtils.print2(arr);
        maxHeapify(arr, arr.length, 0);
        StringUtils.print2(arr);
    }

    @Test
    public void testSortArray() {
        /*
        i = 0
          7
       5     6
      1  2  4  3

        i = 0
          3              6               6
       5     6  ->    5     3  ->     5     4
      1  2  4        1  2  4         1  2  3
         */
        int[] arr = new int[]{7, 5, 6, 1, 2, 4, 3};
        int maxI = arr.length - 1;
        swap(arr, 0, maxI);
        maxHeapify(arr, maxI, 0);
        StringUtils.print2(arr);
    }
}
