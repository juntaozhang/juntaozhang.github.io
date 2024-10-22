package cn.juntaozhang.leetcode;

/**
 * @author juntzhang
 */
public class L209 {

    public int minSubArrayLen(int target, int[] nums) {
        int ans = 0;
        int i = 0;
        int j = 0;
        int sum = 0;
        while (i <= j && j <= nums.length) {
            if (sum < target) {
                if (j == nums.length) {
                    break;
                }
                sum = nums[j] + sum;
                j++;
            } else {
                ans = ans != 0 ? Math.min(ans, j - i) : j - i;
                sum = sum - nums[i];
                i++;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L209().minSubArrayLen(7, new int[]{2, 3, 1, 2, 4, 3}));
        System.out.println(new L209().minSubArrayLen(4, new int[]{1, 4, 4}));
        System.out.println(new L209().minSubArrayLen(11, new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
    }
}
