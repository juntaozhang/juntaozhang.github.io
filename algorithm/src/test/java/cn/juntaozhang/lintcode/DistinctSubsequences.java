package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Stack;

/**
 * 
 */
public class DistinctSubsequences {
    @Test
    public void numDistinct() {
//        System.out.println(numDistinct("zhtjuzjo","zjt"));
        System.out.println(numDistinct("aaa", "aa"));
    }

    public int numDistinct(String S, String T) {
        if (S == null || T == null) {
            return 0;
        }

        int[][] nums = new int[S.length() + 1][T.length() + 1];

        for (int i = 0; i <= S.length(); i++) {
            nums[i][0] = 1;
        }
        for (int i = 1; i <= S.length(); i++) {
            for (int j = 1; j <= T.length(); j++) {
                nums[i][j] = nums[i - 1][j];
                if (S.charAt(i - 1) == T.charAt(j - 1)) {
                    nums[i][j] += nums[i - 1][j - 1];
                }
            }
            print(nums);
        }
        return nums[S.length()][T.length()];
    }

    private void print(int[][] nums) {
        for (int[] arr : nums) {
            for (int i : arr) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

}
