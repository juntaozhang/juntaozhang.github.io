package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.*;

public class L15 {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; ) {
            int target = -nums[i];
            Set<Tuple> tmp = twoSum(nums, i + 1, target);
            if (!tmp.isEmpty()) {
                for (Tuple t : tmp) {
                    result.add(List.of(t.a, t.b, t.c));
                }
            }
            while (i < nums.length - 2 && nums[i] == nums[++i]) ;
        }
        return result;
    }

    public Set<Tuple> twoSum(int[] nums, int start, int target) {
        Set<Tuple> result = new HashSet<>();
        for (int i = start, j = nums.length - 1; i < j; ) {
            if (nums[i] + nums[j] == target) {
                result.add(new Tuple(-target, nums[i], nums[j]));
                i++;
                j--;
            } else if (nums[i] + nums[j] > target) {
                j--;
            } else {
                i++;
            }
        }
        return result;
    }

    @Test
    public void case1() {
        List<List<Integer>> result = threeSum(new int[]{-1, 0, 1, 2, -1, -4});
        System.out.println(result);
    }

    @Test
    public void case2() {
        List<List<Integer>> result = threeSum(new int[]{0, 1, 1});
        System.out.println(result);
    }

    @Test
    public void case3() {
        List<List<Integer>> result = threeSum(new int[]{0, 0, 0, 0});
        System.out.println(result);
    }

    @Test
    public void case4() {
        List<List<Integer>> result = threeSum(new int[]{-1, 0, 1, 2, -1, -4});
        System.out.println(result);
    }

    @Test
    public void case5() {
        List<List<Integer>> result = threeSum(new int[]{-2,0,0,2,2});
        System.out.println(result);
    }

    public class Tuple {
        int a;
        int b;
        int c;

        public Tuple(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public boolean equals(Object o) {
            if (o instanceof Tuple) {
                Tuple t = (Tuple) o;
                return a == t.a && b == t.b && c == t.c;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(a, b, c);
        }

        public String toString() {
            return String.format("(%d, %d, %d)", a, b, c);
        }
    }
}
