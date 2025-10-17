package cn.juntaozhang.leetcode;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 给定整数数组 nums 和整数 k，请返回数组中第 k 个最大的元素。
 * <p>
 * 请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。
 * <p>
 * 你必须设计并实现时间复杂度为 O(n) 的算法解决此问题。
 * <p>
 * <p>
 * <p>
 * 示例 1:
 * <p>
 * 输入: [3,2,1,5,6,4], k = 2
 * 输出: 5
 * 示例 2:
 * <p>
 * 输入: [3,2,3,1,2,4,5,5,6], k = 4
 * 输出: 4
 */
public class L215 {
//    int quickSelect(int[] nums, int l, int r, int k) {
//        if (l == r) return nums[k];
//        int x = nums[l], i = l - 1, j = r + 1;
//        while (i < j) {
//            do i++; while (nums[i] < x);
//            do j--; while (nums[j] > x);
//            if (i < j) {
//                int tmp = nums[i];
//                nums[i] = nums[j];
//                nums[j] = tmp;
//            }
//        }
//        if (k <= j) return quickSelect(nums, l, j, k);
//        else return quickSelect(nums, j + 1, r, k);
//    }

//
//
//    public int partition(int[] nums, int l, int r) {
//        int p = nums[l], i = l - 1, j = r + 1;
//        while (i < j) {
//            do i++; while (nums[i] < p);
//            do j--; while (nums[j] > p);
//            if (i < j) {
//                swap(nums, i, j);
//            }
//        }
//        return j;
//    }
//
//    public int findKthLargest(int[] nums, int k) {
//        int n = nums.length;
//        return quickSelect(nums, 0, n - 1, n - k);
//    }
//
//    public int quickSelect(int[] nums, int l, int r, int k) {
//        int p = partition(nums, l, r);
//        if (p == k) return nums[k];
//        else if (k < p) return quickSelect(nums, l, p, k);
//        else return quickSelect(nums, p + 1, r, k);
//    }
//
////    public int partition(int[] nums, int l, int r) {
////        int p = nums[r];
////        int i = l;
////        for (int j = l; j < r; j++) {
////            if (nums[j] > p) {
////                swap(nums, i, j);
////                i++;
////            }
////        }
////        swap(nums, i, r);
////        return i;
////    }
//
////    public int partition(int[] nums, int start, int end) {
////        int pivot = nums[start];
////        int i = start + 1;
////        int j = end;
////        while (true) {
////            while (i <= j && nums[i] <= pivot) {
////                i++;
////            }
////            while (i <= j && nums[j] > pivot) {
////                j--;
////            }
////            if (i < j) {
////                swap(nums, i, j);
////            } else {
////                break;
////            }
////        }
////        swap(nums, start, j);
////        return j;
////    }
//
    public int partition(int[] nums, int start, int end) {
        int pivot = nums[end];
        int i = start;
        int j = end - 1;
        while (true) {
            while (i <= j && nums[i] <= pivot) {
                i++;
            }
            while (i <= j && nums[j] > pivot) {
                j--;
            }
            if (i < j) {
                swap(nums, i, j);
            } else {
                break;
            }
        }
        swap(nums, end, i);
        return i;
    }


    private void swap(int[] nums, int i, int j) {
        if (i == j) return;
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
//
//
////    public int findKthLargest(int[] nums, int k) {
////        buildMaxHeap(nums);
////        int len = nums.length;
////        for (int i = 0; i < k; i++) {
////            swap(nums, 0, len - 1 - i);
////            maxHeapify(nums, len - 1 - i, 0);
////        }
////        return nums[len - k];
////    }
//
//    public void buildMaxHeap(int[] nums) {
//        for (int i = nums.length / 2; i >= 0; i--) {
//            maxHeapify(nums, nums.length, i);
//        }
//    }
//
//    public void maxHeapify(int[] nums, int len, int i) {
//        int l = 2 * i + 1;
//        int r = 2 * i + 2;
//        int largest = i;
//        if (l < len && nums[l] > nums[largest]) {
//            largest = l;
//        }
//        if (r < len && nums[r] > nums[largest]) {
//            largest = r;
//        }
//
//        if (largest != i) {
//            swap(nums, i, largest);
//            maxHeapify(nums, len, largest);
//        }
//    }

    private int quickSelect(List<Integer> nums, int k) {
        // 随机选择基准数
        Random rand = new Random();
        int pivot = nums.get(rand.nextInt(nums.size()));
        // 将大于、小于、等于 pivot 的元素划分至 big, small 中
        List<Integer> big = new ArrayList<>();
        List<Integer> small = new ArrayList<>();
        for (int num : nums) {
            if (num > pivot)
                big.add(num);
            else if (num < pivot)
                small.add(num);
        }
        int size = nums.size();
        nums.clear();
        // 第 k 大元素在 big 中，递归划分
        if (k <= big.size()) {
            small.clear();
            return quickSelect(big, k);
        }
        // 第 k 大元素在 small 中，递归划分
        if (size - small.size() < k) {
            big.clear();
            return quickSelect(small, k - size + small.size());
        }
        // 第 k 大元素在 equal 中，直接返回 pivot
        return pivot;
    }

    public int findKthLargest(int[] nums, int k) {
        List<Integer> numList = new ArrayList<>();
        for (int num : nums) {
            numList.add(num);
        }
        return quickSelect(numList, k);
    }

    @Test
    public void case1() {
        int[] arr = {2, 3, 1, 5, 6, 4};
        new QuickSort().quickSort(arr, 0, arr.length - 1);
        StringUtils.print(arr);
    }

    @Test
    public void case3_0() {
        int[] arr = {3, 2, 1, 5, 6, 4};
        Assert.assertEquals(4, findKthLargest(arr, 3));
        StringUtils.print(arr);
    }

    @Test
    public void case3_1() {
        int[] arr = {3, 2, 1, 5, 6, 4};
        Assert.assertEquals(5, findKthLargest(arr, 2));
        StringUtils.print(arr);
    }


    @Test
    public void case3_2() {
        int[] arr = {3, 2, 3, 1, 2, 4, 5, 5, 6};
        Assert.assertEquals(1, findKthLargest(arr, 9));
        StringUtils.print(arr);
    }

    @Test
    public void case3_3() {
        int[] arr = {3, 2, 3, 1, 2, 4, 5, 5, 6, 7, 7, 8, 2, 3, 1, 1, 1, 10, 11, 5, 6, 2, 4, 7, 8, 5, 6};
        Assert.assertEquals(2, findKthLargest(arr, 20));
        StringUtils.print(arr);
    }

    @Test
    public void case3_4() throws FileNotFoundException {
        int[] arr = StringUtils.readArray("/Users/juntzhang/src/github/juntaozhang/algorithm/src/test/java/cn/juntaozhang/leetcode/L215.txt");
        Assert.assertEquals(1, findKthLargest(arr, 5000));
    }


    @Test
    public void case4() {
        int[] arr = {3, 2, 4};
        System.out.println(partition(arr, 0, arr.length - 1));
        StringUtils.print(arr);
    }

    @Test
    public void case4_1() {
        int[] arr = {3, 3, 3};
        System.out.println(partition(arr, 0, arr.length - 1));
        StringUtils.print(arr);
    }

    @Test
    public void case4_2() {
        int[] arr = {3, 2};
        System.out.println(partition(arr, 0, arr.length - 1));
        StringUtils.print(arr);
    }

    @Test
    public void case4_3() {
        int[] arr = {3, 4};
        System.out.println(partition(arr, 0, arr.length - 1));
        StringUtils.print(arr);
    }

    @Test
    public void case4_4() {
        int[] arr = {3};
        System.out.println(partition(arr, 0, arr.length - 1));
        StringUtils.print(arr);
    }

    public class QuickSort {
        public void quickSort(int[] nums, int low, int high) {
            if (low < high) {
                int pivot = partition(nums, low, high);
                quickSort(nums, low, pivot - 1);
                quickSort(nums, pivot + 1, high);
            }
        }
    }

}
