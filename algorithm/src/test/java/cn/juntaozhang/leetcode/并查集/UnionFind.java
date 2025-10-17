package cn.juntaozhang.leetcode.并查集;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.Arrays;

public class UnionFind {

    @Test
    public void case1() {
        int[] nums = new int[]{0, 1, 2, 3, 4, 5};
        V1 uf = new V1(nums);
        uf.union(1, 2);
        System.out.println(uf.find(2));
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(3, 5);
        StringUtils.print(nums);
        StringUtils.print2(uf.parent);
        System.out.println(uf.find(4));
    }

    @Test
    public void case2() {
        int[] nums = new int[]{5, 2, 1, 3, -4, 0};
        V2 uf = new V2(nums);
        uf.union(1, 2);
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(3, 5);
        StringUtils.print(nums);
        StringUtils.print2(uf.parent);
        System.out.println(uf.find(5));
        StringUtils.print2(uf.parent);
    }

    @Test
    public void case3() {
        int[] nums = new int[]{5, 2, 1, 3, -4, 0};
        V3 uf = new V3(nums);
        uf.union(1, 2);
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(3, 5);
        StringUtils.print(nums);
        System.out.println();
        StringUtils.print2(uf.size);
        StringUtils.print2(uf.parent);
        uf.find(5);
        System.out.println();
        StringUtils.print2(uf.size);
        StringUtils.print2(uf.parent);
    }

    @Test
    public void case4() {
        int[] nums = new int[]{5, 2, 1, 3, -4, 0};
        V4 uf = new V4(nums);
        StringUtils.print(nums);
        StringUtils.print2(uf.rank);
        StringUtils.print2(uf.parent);
        System.out.println();
        uf.union(1, 2);
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(3, 5);
        StringUtils.print2(uf.rank);
        StringUtils.print2(uf.parent);
        uf.find(5);
        System.out.println();
        StringUtils.print2(uf.rank);
        StringUtils.print2(uf.parent);
    }

    public interface UF {
        int find(int i);

        void union(int i, int j);
    }

    public class V4 implements UF {
        private int[] parent;
        private int[] rank; // parent height

        public V4(int[] nums) {
            parent = new int[nums.length];
            rank = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                parent[i] = i;
            }
            Arrays.fill(rank, 1);
        }

        public int find(int i) { // TODO: will decrease rank
            if (parent[i] != i) {
                parent[i] = find(parent[i]);
            }
            return parent[i];
        }

        public void union(int i, int j) {
            int p1 = find(i);
            int p2 = find(j);
            if (p1 == p2) {
                return;
            }

            if (rank[p1] > rank[p2]) {
                parent[p2] = p1;
            } else if (rank[p1] < rank[p2]) {
                parent[p1] = p2;
            } else {
                parent[p2] = p1;
                rank[p1]++;
            }

            StringUtils.print2(rank);
            StringUtils.print2(parent);
             System.out.println();
        }
    }


    public class V3 implements UF {
        private int[] parent;
        private int[] size; // there is a corner case: left size == right size but left height < right  height

        public V3(int[] nums) {
            parent = new int[nums.length];
            size = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                parent[i] = i;
            }
            Arrays.fill(size, 1);
        }

        public int find(int i) {
            if (parent[i] != i) {
                parent[i] = find(parent[i]);
            }
            return parent[i];
        }

        public void union(int i, int j) {
            int p1 = find(i);
            int p2 = find(j);
            if (p1 == p2) {
                return;
            }

            if (size[p1] >= size[p2]) {
                parent[p2] = p1;
                size[p1] += size[p2];
            } else {
                parent[p1] = p2;
                size[p2] += size[p1];
            }
            // StringUtils.print2(parent);
            // StringUtils.print2(size);
            // System.out.println();
        }
    }

    public class V2 implements UF {
        private int[] parent;

        public V2(int[] nums) {
            parent = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] != i) {
                parent[i] = find(parent[i]);
            }
            return parent[i];
        }

        public void union(int i, int j) {
            int p1 = find(i);
            int p2 = find(j);
            parent[p2] = p1;
        }
    }

    public class V1 implements UF {
        private int[] parent;

        public V1(int[] nums) {
            parent = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] == i) return i;
            return find(parent[i]);
        }

        public void union(int i, int j) {
            int p1 = find(i);
            int p2 = find(j);
            parent[p2] = p1;
        }
    }
}
