package cn.juntaozhang.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 找到所有数组中消失的数字
 *
 * 
 */
public class L448 {
    class Solution {
        /**
         * 原始数组：[4,3,2,7,8,2,3,1]
         * 重置后为：[-4,-3,-2,-7,8,2,-3,-1]
         * 结论：[8,2] 分别对应的index为[5,6]（消失的数字）
         */
        public List<Integer> findDisappearedNumbers(int[] nums) {
            List<Integer> ans = new ArrayList<>();
            if (nums == null || nums.length == 0) {
                return ans;
            }
            for (int num : nums) {
                int i = Math.abs(num);
                nums[i - 1] = -Math.abs(nums[i - 1]);
            }
            for (int i = 0; i < nums.length; i++) {
                if (nums[i] > 0) {
                    ans.add(i + 1);
                }
            }
            return ans;
        }
    }

    public static void main(String[] args) {
        Solution s = new L448().new Solution();
        for (int i : s.findDisappearedNumbers(new int[]{4, 3, 2, 7, 8, 2, 3, 1})) {
            System.out.println(i);
        }
    }
}
