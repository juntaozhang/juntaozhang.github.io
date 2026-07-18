package cn.juntaozhang.leetcode.dp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class L354 {
    public int maxEnvelopes(int[][] envelopes) {
        Arrays.sort(envelopes, (p1, p2) -> p1[0] != p2[0] ? p1[0] - p2[0] : p2[1] - p1[1]);
        int[] dp = new int[envelopes.length];
        int result = 0;
        for (int i = 0; i < envelopes.length; i++) {
            // TODO 这里与 L646 相比需要优化
            int t = 0;
            for (int j = i - 1; j >= 0; j--) {
                if (envelopes[j][0] < envelopes[i][0] && envelopes[j][1] < envelopes[i][1]) {
                    t = Math.max(t, dp[j]);
                }
            }
            dp[i] = t + 1;
            result = Math.max(result, dp[i]);
        }
        return result;
    }


    public int maxEnvelopes2(int[][] envelopes) {
        // 只需要考虑第二个维度
        Arrays.sort(envelopes, (p1, p2) -> p1[0] != p2[0] ? p1[0] - p2[0] : p2[1] - p1[1]);
        // [1,2}, {1,3}, {1,4] -> [1,4}, {1,3}, {1,2] 如果只看第一个维度就是 3， 倒序之后还是 1
        // 倒序之后，这样可以处理 第一个维度 相同的问题

        // greedy list, 单调
        List<Integer> list = new ArrayList<Integer>();
        list.add(envelopes[0][1]);
        for (int i = 1; i < envelopes.length; i++) {
            if (envelopes[i][1] > list.get(list.size() - 1)) {
                list.add(envelopes[i][1]);
            } else {
                int j = binarySearch(list, envelopes[i][1]);
                list.set(j, envelopes[i][1]);
            }
        }
        return list.size();
    }

    private static int binarySearch(List<Integer> list, int num) {
        int l = 0, r = list.size() - 1;
        while (l <= r) {// 为什么要 =, 为了找到> l 的第一个值
            int mid = l + (r - l) / 2;
            if (list.get(mid) < num) {
                l = mid + 1;
            } else if (list.get(mid) > num) {
                r = mid - 1;
            } else {
                return mid;
            }
        }
        return l;
    }

    @Test
    public void searchCase1() {
        System.out.println(binarySearch(List.of(3, 6, 7), 4));
    }

    @Test
    public void case1() {
        System.out.println(maxEnvelopes2(new int[][]{{5, 4}, {6, 4}, {6, 7}, {2, 3}}));
    }

    @Test
    public void case2() {
        System.out.println(maxEnvelopes2(new int[][]{{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}, {9, 9}, {10, 10}, {11, 11}, {12, 12}, {13, 13}, {14, 14}, {15, 15}, {16, 16}, {17, 17}, {18, 18}, {19, 19}, {20, 20}, {21, 21}, {22, 22}, {23, 23}, {24, 24}, {25, 25}, {26, 26}, {27, 27}, {28, 28}, {29, 29}, {30, 30}, {31, 31}, {32, 32}, {33, 33}, {34, 34}, {35, 35}, {36, 36}, {37, 37}, {38, 38}, {39, 39}, {40, 40}, {41, 41}, {42, 42}, {43, 43}, {44, 44}, {45, 45}, {46, 46}, {47, 47}, {48, 48}, {49, 49}, {50, 50}, {51, 51}, {52, 52}, {53, 53}, {54, 54}, {55, 55}, {56, 56}, {57, 57}, {58, 58}, {59, 59}, {60, 60}, {61, 61}, {62, 62}, {63, 63}, {64, 64}, {65, 65}, {66, 66}, {67, 67}, {68, 68}, {69, 69}, {70, 70}, {71, 71}, {72, 72}, {73, 73}, {74, 74}, {75, 75}, {76, 76}, {77, 77}, {78, 78}, {79, 79}, {80, 80}, {81, 81}, {82, 82}, {83, 83}, {84, 84}, {85, 85}, {86, 86}, {87, 87}, {88, 88}, {89, 89}, {90, 90}, {91, 91}, {92, 92}, {93, 93}, {94, 94}, {95, 95}, {96, 96}, {97, 97}, {98, 98}, {99, 99}, {100, 100}, {101, 101}, {102, 102}, {103, 103}, {104, 104}, {105, 105}, {106, 106}, {107, 107}, {108, 108}}));
    }

    @Test
    public void case3() {
        System.out.println(maxEnvelopes2(new int[][]{{1, 1}, {2, 3}, {3, 4}, {4, 2}, {5, 3}, {6, 4}}));
    }

    @Test
    public void case4() {
        System.out.println(maxEnvelopes2(new int[][]{{46, 89}, {50, 53}, {52, 68}, {72, 45}, {77, 81}}));
    }

    @Test
    public void case5() {
        System.out.println(maxEnvelopes2(new int[][]{{4,5}, {4,6}, {6,7}, {2,3}, {1,1}}));
    }
}
