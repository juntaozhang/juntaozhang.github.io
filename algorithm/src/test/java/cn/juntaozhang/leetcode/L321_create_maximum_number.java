package cn.juntaozhang.leetcode;

import java.util.Arrays;

/**
 * https://www.youtube.com/watch?v=YYduNJfzWaA
 * <p>
 * http://zxi.mytechroad.com/blog/dynamic-programming/leetcode-321-create-maximum-number/
 *
 * 
 */
public class L321_create_maximum_number {
    class Solution {

        public int[] maxNumber(int[] nums1, int[] nums2, int k) {
            int[] best = new int[0];
            // 至少取k - nums2.length
            for (int i = Math.max(0, k - nums2.length); i <= Math.min(k, nums1.length); ++i) {
                int[] t = maxNumber(maxNumber(nums1, i), maxNumber(nums2, k - i));
                System.out.println(i + " -- " + (k - i) + " => " + Arrays.toString(t));
                best = max(best, 0, t, 0);
            }
            return best;
        }

        // nums中取最大k
//        public int[] maxNumber(int[] nums, int k) {
//            int[] ans = new int[k];
//            int j = 0, drop = 0, maxDrop = nums.length - k;
//            for (int i = 0; i < nums.length; i++) {
//                while (j > 0 && drop < maxDrop && ans[j - 1] < nums[i]) {
//                    j--;
//                    drop++;
//                }
//                if (j < k) {
//                    ans[j++] = nums[i];
//                } else {
//                    //  4, 0, 9, 9, 0, 5, 5,| 4, 7 =>  9, 9, 5, 5 对于4 未加入也认为是drop
//                    if (j > 0 && ans[j - 1] > nums[i]) {
//                        drop++;
//                    }
//                }
//            }
//            return ans;
//        }

        public int[] maxNumber(int[] nums, int k) {
            int[] ans = new int[k];
            int j = 0;
            for (int i = 0; i < nums.length; ++i) {
                while (j > 0 && nums[i] > ans[j - 1] && nums.length - i > k - j) {
                    --j;
                }
                if (j < k) {
                    ans[j++] = nums[i];
                }
            }
            return ans;
        }

        public int[] maxNumber(int[] nums1, int[] nums2) {
            System.out.println(Arrays.toString(nums1));
            System.out.println(Arrays.toString(nums2));
            int[] ans = new int[nums1.length + nums2.length];
            int i = 0, j = 0, k = 0;
            for (; i < nums1.length || j < nums2.length; ) {
                if (i >= nums1.length) {
                    while (j < nums2.length) {
                        ans[k++] = nums2[j++];
                    }
                } else if (j >= nums2.length) {
                    while (i < nums1.length) {
                        ans[k++] = nums1[i++];
                    }
                } else {
                    int[] t = max(nums1, i, nums2, j);
                    ans[k++] = t == nums1 ? nums1[i++] : nums2[j++];

                }

            }
            return ans;
        }

        // 比较两个数组大小
        private int[] max(int[] nums1, int s1, int[] nums2, int s2) {
            for (int i = s1, j = s2; i < nums1.length; ++i, ++j) {
                if (j >= nums2.length) {
                    return nums1;
                }
                if (nums1[i] < nums2[j]) {
                    return nums2;
                }
                if (nums1[i] > nums2[j]) {
                    return nums1;
                }
            }
            return nums2;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Solution s = new L321_create_maximum_number().new Solution();
//        System.out.println(Arrays.toString(s.maxNumber(new int[]{
//                6,9,2,3,6,7,9,9,0,9,6,2,3,3,3,4,7,4,5,6,8,5,0,4,9,9,0,7,8,5,0,0,3,7,9,3
//        }, new int[]{
//                1,6,5,7,6,0,6,5,1,0,1,0,8,2,7,4,5,4,2,6,2,4,0,1,3,9,6,0,1,3,0,1,5,3,5,1,7,2,8,3,1,9,0,3,4,5,1,7,6,1,5,9,8,5,9,9,8,7,6,0,3,9,0,2,8,7,5,4,0,5,1,8,3,2,2,7,8,9,8,5,7,4,8,1,1,1,6,5,7,1,1,4,0,4,2,3,3,3,6,2,0,2,1,3,7,9,7,2,8,0,6,9,0,2,1,8,4,6,7,9,2,5,9,4,6,1,9,5,7,9,4,1,0,6,8,0,1,3,9,4,2,9,8,0,6,9,0,7,3,4,6,2,4,8,3,2,4,1,8,3,8,1,3,9,0,9,3,5,8,2,7,5,3,7,3,1,3,5,9,8
//        }, 180)));
//        System.out.println(Arrays.toString(
//                s.maxNumber(
//                        s.maxNumber(new int[]{7, 6, 1, 9, 3, 2, 3, 1, 1}, 5),
//                        s.maxNumber(new int[]{4, 0, 9, 9, 0, 5, 5, 4, 7}, 4)
//                )
//        ));
//        System.out.println(Arrays.toString(s.maxNumber(new int[]{7,6,1,9,3,2,3,1,1}, 5)));
//        System.out.println(Arrays.toString(s.maxNumber(new int[]{4, 0, 9, 9, 0, 5, 5, 4, 7}, 4)));
        System.out.println(Arrays.toString(s.maxNumber(new int[]{6, 9, 2, 3, 6, 7, 9, 9, 0, 9, 6, 2, 3, 3, 3, 4, 7, 4, 5, 6, 8, 5, 0, 4, 9, 9, 0, 7, 8, 5, 0, 0, 3, 7, 9, 3}, 9)));

    }
    //[9,9,9,9,9,9,9,7,6,6,8,3,3,2,7,4,5,4,2,6,2,4,0,1,3,9,6,0,1,3,0,1,5,3,5,1,7,2,8,3,1,9,0,3,4,5,1,7,6,1,5,9,8,5,9,9,8,7,6,0,3,9,0,2,8,7,5,4,0,5,1,8,3,2,2,7,8,9,8,5,7,4,8,1,1,1,6,5,7,1,1,4,0,4,2,3,3,3,6,2,0,2,1,3,7,9,7,2,8,0,6,9,0,2,1,8,4,6,7,9,2,5,9,4,6,1,9,5,7,9,4,1,0,6,8,0,1,3,9,4,2,9,8,0,6,9,0,7,3,4,6,2,4,8,3,2,4,1,8,3,8,1,3,9,0,9,3,5,8,2,7,5,3,7,3,1,3,5,9,8]
    //[9,9,9,9,9,9,9,7,6,6,5,8,3,2,7,4,5,4,2,6,2,4,0,1,3,9,6,0,1,3,0,1,5,3,5,1,7,2,8,3,1,9,0,3,4,5,1,7,6,1,5,9,8,5,9,9,8,7,6,0,3,9,0,2,8,7,5,4,0,5,1,8,3,2,2,7,8,9,8,5,7,4,8,1,1,1,6,5,7,1,1,4,0,4,2,3,3,3,6,2,0,2,1,3,7,9,7,2,8,0,6,9,0,2,1,8,4,6,7,9,2,5,9,4,6,1,9,5,7,9,4,1,0,6,8,0,1,3,9,4,2,9,8,0,6,9,0,7,3,4,6,2,4,8,3,2,4,1,8,3,8,1,3,9,0,9,3,5,8,2,7,5,3,7,3,1,3,5,9,8]
}
