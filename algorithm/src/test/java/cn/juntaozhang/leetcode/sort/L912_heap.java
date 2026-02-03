package cn.juntaozhang.leetcode.sort;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.Arrays;

public class L912_heap {
    @Test
    public void case1() {
        System.out.println(Arrays.toString(sortArray(new int[]{5, 2, 3, 1})));
    }

    @Test
    public void case2() {
        System.out.println(Arrays.toString(sortArray(new int[]{5, 1, 1, 2, 0, 0})));
    }

    public int[] sortArray(int[] arr) {
        buildMaxHeap(arr);
        System.out.println("buildMaxHeap:" + Arrays.toString(arr));
        for (int i = arr.length - 1; i > 0; i--) {
            swap(arr, 0, i);
            heapify(arr, i, 0);
            System.out.println(i + ":" + Arrays.toString(arr));
        }
        return arr;
    }

    public void buildMaxHeap(int[] nums) {
        for (int i = nums.length / 2; i >= 0; i--) {
            heapify(nums, nums.length, i);
        }
    }

    public void heapify(int[] nums, int n, int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int largest = i;
        if (l < n && nums[l] > nums[largest]) {
            largest = l;
        }

        if (r < n && nums[r] > nums[largest]) {
            largest = r;
        }

        if (largest != i) {
            swap(nums, i, largest);
            heapify(nums, n, largest);
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
        heapify(arr, arr.length, 2);
        StringUtils.print2(arr);
        heapify(arr, arr.length, 1);
        StringUtils.print2(arr);
        heapify(arr, arr.length, 0);
        StringUtils.print2(arr);
    }

    @Test
    public void testSortArray() {
        /*
        i = 6
          7
       5     6
      1  2  4  3

        i = 6
          3              6               6
       5     6  ->    5     3  ->     5     4
      1  2  4        1  2  4         1  2  3
         */
        int[] arr = new int[]{7, 5, 6, 1, 2, 4, 3};
        swap(arr, 0, 6);
        heapify(arr, 6, 0);
        StringUtils.print2(arr);
    }
}
