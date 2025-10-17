package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author juntzhang
 */
public class L737 {

//    public int maxEnvelopes(int[][] envelopes) {
//        Arrays.sort(envelopes, (a, b) -> {
//            if (a[0] - b[0] == 0) {
//                return a[1] - b[1];
//            }
//            return a[0] - b[0];
//        });
//        int n = envelopes.length;
//
//        int[] dp = new int[n];
//        int ans = 0;
//        for (int i = 0; i < n; i++) {
//            dp[i] = 1;
////            int idx = Arrays.binarySearch(envelopes, 0, i, envelopes[i], (a, b) -> {
////                return a[0] - b[0] == 0 ? a[1] - b[1] : a[0] - b[0];
////            });
////            if (idx != -1) {
////                dp[i] = dp[idx] + 1;
////            }
//            for (int j = i - 1; j >= 0; j--) {
//                if (envelopes[j][0] < envelopes[i][0] && envelopes[j][1] < envelopes[i][1]) {
//                    if (dp[i] < dp[j] + 1) {
//                        dp[i] = dp[j] + 1;
//                        break;
//                    }
//                }
//            }
//            ans = Math.max(ans, dp[i]);
//        }
//        Arrays.binarySearch(new int[]{89,53,68,45,81},0,4,81);
//        return ans;
//    }

//    public int maxEnvelopes(int[][] envelopes) {
//        if (envelopes.length == 0) {
//            return 0;
//        }
//
//        int n = envelopes.length;
//        Arrays.sort(envelopes, new Comparator<int[]>() {
//            public int compare(int[] e1, int[] e2) {
//                if (e1[0] != e2[0]) {
//                    return e1[0] - e2[0];
//                } else {
//                    return e2[1] - e1[1];
//                }
//            }
//        });
//
//        List<Integer> f = new ArrayList<>();
//        f.add(envelopes[0][1]);
//        // 贪心 + 二分
//        for (int i = 1; i < n; ++i) {
//            int num = envelopes[i][1];
//            if (num > f.get(f.size() - 1)) {
//                f.add(num);
//            } else {
//                int index = binarySearch(f, num);
//                f.set(index, num);
//            }
//        }
//        return f.size();
//    }
//
//    public int binarySearch(List<Integer> f, int target) {
//        int low = 0, high = f.size() - 1;
//        while (low < high) {
//            int mid = (high - low) / 2 + low;
//            if (f.get(mid) < target) {
//                low = mid + 1;
//            } else {
//                high = mid;
//            }
//        }
//        return low;
//    }

//    public int lengthOfLIS(int[] nums) {
//        int[] dp = new int[nums.length];
//        int len = 0;
//        for (int num : nums) {
//            int i = Arrays.binarySearch(dp, 0, len, num);
//            if (i < 0) {
//                i = -(i + 1);
//            }
//            dp[i] = num;
//            if (i == len) {
//                len++;
//            }
//        }
//        return len;
//    }
//
//    public int maxEnvelopes(int[][] envelopes) {
//        Arrays.sort(envelopes, (arr1, arr2) -> {
//            if (arr1[0] == arr2[0]) {
//                return arr2[1] - arr1[1];
//            } else {
//                return arr1[0] - arr2[0];
//            }
//        });
//        // extract the second dimension and run LIS
//        int[] secondDim = new int[envelopes.length];
//        for (int i = 0; i < envelopes.length; ++i) secondDim[i] = envelopes[i][1];
//        return lengthOfLIS(secondDim);
//    }


    public int maxEnvelopes(int[][] envelopes) {
        Arrays.sort(envelopes, (a, b) -> {
            if (a[0] - b[0] == 0) {
                return b[1] - a[1];
            }
            return a[0] - b[0];
        });
        int n = envelopes.length;

        List<Integer> dp = new ArrayList<>();
        dp.add(envelopes[0][1]);
        for (int i = 1; i < n; i++) {
            if (dp.get(dp.size() - 1) < envelopes[i][1]) {
                dp.add(envelopes[i][1]);
            } else {
                int idx = binarySearch(dp, envelopes[i][1]);
                dp.set(idx, envelopes[i][1]);
            }
        }
        return dp.size();
    }

    int binarySearch(List<Integer> dp, int num) {
        int low = 0, high = dp.size() - 1;
        while (low < high) {
            int mid = (low + high) / 2;
            if (dp.get(mid) < num) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }


    public static void main(String[] args) {
        System.out.println(new L737().maxEnvelopes(new int[][]{{46, 89}, {50, 53}, {52, 68}, {72, 45}, {77, 81}}));
        System.out.println(new L737()
                .maxEnvelopes(StringUtils.file2arr(Path.of("/Users/juntzhang/src/juntzhang/example/src/DP/L737.txt"))));
        System.out.println(new L737().maxEnvelopes(new int[][]{{1, 1}, {2, 3}, {4, 5}, {4, 6}, {6, 7}}));
        System.out.println(new L737().maxEnvelopes(new int[][]{{2, 3}, {5, 4}, {6, 4}, {6, 7}}));
    }
}
