package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.Arrays;

// 贪心
// https://leetcode.cn/problems/assign-cookies/submissions/541943435/?envType=study-plan-v2&envId=bytedance-2023-fall-sprint
public class L455 {
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(s);
        Arrays.sort(g);
        int ans = 0;
        for (int i = 0, j = 0; i < g.length && j < s.length; ) {
            if (g[i] <= s[j]) {
                ans++;
                i++;
                j++;
            } else {
                j++;
            }
        }
        return ans;
    }

    @Test
    public void case1() {
        System.out.println(findContentChildren(new int[]{1, 2, 3}, new int[]{1, 1}));
    }

    @Test
    public void case2() {
        System.out.println(findContentChildren(new int[]{1, 2}, new int[]{3, 2, 1}));
    }

    @Test
    public void case3() {
        System.out.println(findContentChildren(new int[]{10, 9, 8, 7}, new int[]{5, 6, 7, 8}));
    }
}
