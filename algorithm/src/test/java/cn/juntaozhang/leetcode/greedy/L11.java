package cn.juntaozhang.leetcode.greedy;

/**
 * @author juntzhang
 */
public class L11 {

    public int maxArea(int[] height) {
        int max = 0;
        for (int i = 0, j = height.length - 1; i < j; ) {
            max = Math.max(Math.min(height[i], height[j]) * (j - i), max);
            if (height[i] > height[j]) {
                j--;
            } else {
                i++;
            }
        }
        return max;
    }

    public static void main(String[] args) {
//    System.out.println(new L11().maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}));
        System.out.println(new L11().maxArea(new int[]{3, 4, 1, 10, 3, 3}));
    }
}
