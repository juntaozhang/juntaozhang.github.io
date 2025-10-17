package cn.juntaozhang.leetcode.二分查找;

import org.junit.Test;

/**
 * <blockquote>
 * <pre>
 * int binarySearch(int[] nums, int target) {
 *     int left = 0, right = ...;
 *     while(...) {
 *         int mid = (right + left) / 2;
 *         if (nums[mid] == target) {
 *             ...
 *         } else if (nums[mid] < target) {
 *             left = ...
 *         } else if (nums[mid] > target) {
 *             right = ...
 *         }
 *     }
 *     return ...;
 * }
 * </pre>
 */
public class 模版 {
    /**
     * [left, right]
     */
    int binarySearch(int[] nums, int target) {
        if (nums == null || nums.length == 0)
            return -1;

        int left = 0, right = nums.length - 1;
        while (left <= right) {
            // Prevent (left + right) overflow
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // End Condition: left > right
        return -1;
    }

    /**
     * [left, right)
     */
    int binarySearch2(int[] nums, int target) {
        if (nums == null || nums.length == 0)
            return -1;

        int left = 0, right = nums.length;
        while (left < right) {
            // Prevent (left + right) overflow
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid; // right是开区间，所以不需要减1
            }
        }

        // Post-processing:
        // End Condition: left == right
        if (left != nums.length && nums[left] == target) return left;
        return -1;
    }

    /**
     * (left, right)
     */
    int binarySearch3(int[] nums, int target) {
        if (nums == null || nums.length == 0)
            return -1;

        int left = 0, right = nums.length - 1;
        while (left + 1 < right) {
            // Prevent (left + right) overflow
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid;
            } else {
                right = mid;
            }
        }

        // Post-processing:
        // End Condition: left + 1 == right
        if (nums[left] == target) return left;
        if (nums[right] == target) return right;
        return -1;
    }

    @Test
    public void case1() {
        int[] nums = new int[]{1, 2, 3, 6, 7};
        int target = 1;
        System.out.println(binarySearch3(nums, target));
    }
}
