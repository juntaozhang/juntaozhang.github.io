package cn.juntaozhang.leetcode;

import java.util.*;

/**
 * https://leetcode.cn/problems/longest-consecutive-sequence/
 * <p>
 * 128. 最长连续序列
 * <p>
 * 并查集
 *
 * @author juntzhang
 */
public class L128 {

    static class UF {

        private final Map<Integer, Integer> parents = new HashMap<>();
        private final Map<Integer, Set<Integer>> children = new HashMap<>();

        public UF(int[] nums) {
            for (int n : nums) {
                parents.put(n, n);
                Set<Integer> set = new HashSet<>();
                set.add(n);
                children.put(n, set);
            }
        }

        public Set<Integer> getChildren(int x) {
            return children.get(x);
        }

        public void union(int a, int b) {
            Integer pa = find(a);
            Integer pb = find(b);
            if (pa == null || pb == null || pa.equals(pb)) {
                return;
            }
            // children of pb ==> pa
            for (int c : this.getChildren(pb)) {
                parents.put(c, pa);
                this.children.get(pa).add(c);
            }
            this.children.remove(pb);
        }

        // find root
        public Integer find(int x) {
            Integer p;
            while (true) {
                p = parents.get(x);
                if (p == null) {
                    break;
                }
                if (p == x) {
                    break;
                }
                x = p;
            }
            return p;
        }

        public int count(int x) {
            return this.children.get(find(x)).size();
        }
    }

    public int longestConsecutive(int[] nums) {
        UF uf = new UF(nums);
        int max = 0;
        for (int n : nums) {
            uf.union(n, n - 1);
            uf.union(n, n + 1);
            max = Math.max(uf.count(n), max);
        }
        return max;
    }

    public static void main(String[] args) {
        long a = new Random().nextInt(100);

        System.out.println(new L128().longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));
        System.out.println(new L128().longestConsecutive(new int[]{0, 3, 7, 2, 5, 9, 4, 6, 0, 1}));
    }
}
