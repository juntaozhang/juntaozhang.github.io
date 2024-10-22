package cn.juntaozhang.leetcode.sort;

import org.junit.Test;

import java.util.Arrays;

public class L912_quick {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(new L912_quick().quickSort(new int[]{4})));
        System.out.println(Arrays.toString(new L912_quick().quickSort(new int[]{4, 4})));
        System.out.println(Arrays.toString(new L912_quick().quickSort(new int[]{1, 2, 3})));
        System.out.println(Arrays.toString(new L912_quick().quickSort(new int[]{4, 3, 2, 1})));
//        System.out.println(new L912_quick().partition(new int[]{4, 4, 1}, 0, 2));
    }

    public static int partition(int[] nums, int start, int end) {
        int i = start + 1, j = end;
        int pivot = nums[start]; // first left need swap
        while (i < j) {
            while (i < j && nums[i] <= pivot) i++;
            while (i < j && nums[j] > pivot) j--;
            if (i < j) {
                swap(nums, i, j);
                i++;
                j--;
            }
        }
        if (nums[j] > pivot) j--;
        swap(nums, start, j);
        return j;
    }

    public static void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

    public int[] quickSort(int[] arr) {
        quickSort(arr, 0, arr.length - 1);
        return arr;
    }

    public void quickSort(int[] arr, int start, int end) {
        if (start >= end) return;
        // 将数组分区，并获得中间值的下标
        int middle = partition(arr, start, end);
        // 对左边区域快速排序
        quickSort(arr, start, middle - 1);
        // 对右边区域快速排序
        quickSort(arr, middle + 1, end);
    }

    @Test
    public void partition1() {
        partition(new int[]{2, 2, 3, 1}, 0, 3);
    }

    @Test
    public void partition2() {
        partition(new int[]{110, 100, 0}, 0, 2);
    }
}