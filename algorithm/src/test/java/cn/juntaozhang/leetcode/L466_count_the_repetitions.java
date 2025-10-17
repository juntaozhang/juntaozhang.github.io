package cn.juntaozhang.leetcode;

/**
 * 统计重复个数
 *
 * 
 */
public class L466_count_the_repetitions {
    class Solution {
        public int getMaxRepetitions(String s1, int n1, String s2, int n2) {
            int count = 0;
            int pos = 0;
            int[] repeatCount = new int[s2.length() + 1];
            int[] nextIdx = new int[s2.length() + 1];
            // 循环n1次
            for (int i = 1; i <= n1 && i <= s2.length(); i++) {
                // s2中找到相同字符的位置，s1中匹配s2 count
                for (int j = 0; j < s1.length(); j++) {
                    if (s1.charAt(j) == s2.charAt(pos)) {
                        pos++;
                    }
                    if (pos == s2.length()) {
                        count++;
                        pos = 0;
                    }
                }

                repeatCount[i] = count;
                nextIdx[i] = pos;

                if (count > 0) {
                    for (int j = 0; j < i; j++) {
                        // 找到重复index
                        if (nextIdx[j] == pos) {
                            int interval = i - j;
                            int repeat = (n1 - j) / interval;
                            // aaa 4 aa 1
                            int patternCnt = (repeatCount[i] - repeatCount[j]) * repeat;
                            int remain = repeatCount[j + (n1 - j) % interval];
                            return (remain + patternCnt) / n2;
                        }
                    }
                }
            }
            return count / n2;
        }
    }

    public static void main(String[] args) {
        Solution s = new L466_count_the_repetitions().new Solution();
//        System.out.println(s.getMaxRepetitions("abacb", 6, "bca", 2));
//        System.out.println(s.getMaxRepetitions("abacb", 9, "bcaac", 1));
//        System.out.println(s.getMaxRepetitions("musicforever", 10, "lovelive", 100000));
        System.out.println(s.getMaxRepetitions("ecbafedcba", 4, "abcdef", 1));
    }
}
