package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.TreeSet;

/**
 * https://leetcode.cn/problems/contains-duplicate-iii/description/
 */
public class L220 {

    public boolean containsNearbyAlmostDuplicate(int[] nums, int indexDiff, int valueDiff) {
        TreeSet<Long> set = new TreeSet<>();
        for (int i = 0; i < nums.length; i++) {
            Long f = set.floor((long) nums[i]);
            if (f != null && (long) nums[i] - f <= valueDiff) return true;
            Long c = set.ceiling((long) nums[i]);
            if (c != null && c - (long) nums[i] <= valueDiff) return true;

            set.add((long) nums[i]);
            if (set.size() > indexDiff) {
                set.remove((long) nums[i - indexDiff]);
            }
        }
        return false;
    }

    @Test
    public void case1() {
        boolean ans = containsNearbyAlmostDuplicate(new int[]{1, 2, 3, 1}, 3, 0);
        System.out.println(ans);
    }

    @Test
    public void case2() {
        boolean ans = containsNearbyAlmostDuplicate(new int[]{1, 5, 9, 1, 5, 9}, 2, 3);
        System.out.println(ans);
    }
}
