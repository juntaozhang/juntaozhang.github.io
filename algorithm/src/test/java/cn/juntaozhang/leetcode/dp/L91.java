package cn.juntaozhang.leetcode.dp;

/**
 * @author juntzhang
 */
public class L91 {

    public int numDecodings0(String s) {
        int[] dp = new int[s.length()];
        dp[0] = s.charAt(0) == '0' ? 0 : 1;
        if (s.length() < 2) {
            return dp[0];
        }
        int t = Integer.parseInt(s.substring(0, 2));
        if (t <= 26 && s.charAt(1) != '0' && s.charAt(0) != '0') {
            dp[1] = 2;
        } else if (s.charAt(1) == '0' && s.charAt(0) != '0' && t <= 26) {
            dp[1] = 1;
        } else if (t > 26 && s.charAt(1) != '0') {
            dp[1] = 1;
        } else {
            dp[1] = 0;
        }
        for (int i = 2; i < s.length(); i++) {
            String tt = s.charAt(i - 1) + "" + s.charAt(i);
            t = Integer.parseInt(tt);
            if (t <= 26 && s.charAt(i - 1) != '0' && s.charAt(i) != '0') {
                dp[i] = dp[i - 1] + dp[i - 2];
            } else if (s.charAt(i) == '0' && s.charAt(i - 1) != '0' && t <= 26) {
                dp[i] = dp[i - 2];
            } else if ((t > 26 && s.charAt(i) != '0') || (s.charAt(i - 1) == '0' && s.charAt(i) != '0')) {
                // xxx6,1 || xxx0,1
                dp[i] = dp[i - 1];
            } else {
                dp[i] = 0;
            }
        }
        return dp[s.length() - 1];
    }

    public int numDecodings(String s) {
        int[] dp = new int[s.length()];
        dp[0] = s.charAt(0) == '0' ? 0 : 1;
        for (int i = 1; i < s.length(); i++) {
            char c1 = s.charAt(i - 1);
            char c2 = s.charAt(i);
            int num = Integer.valueOf(c1 + "" + c2);
            // xxx0x
            if (c1 == '0') {
                // xxx00
                if (c2 == '0') {
                    dp[i] = 0;
                }
                // xxx06
                else {
                    dp[i] = dp[i - 1];
                }
            }
            // xxx1x
            else {
                // xxx6|0
                if (num > 26 && c2 == '0') {
                    dp[i] = 0;
                }
                // xxx6|3
                else if (num > 26 && c2 != '0') {
                    dp[i] = dp[i - 1];
                }
                // xxx|10
                else if (c2 == '0') {
                    if (i - 2 >= 0) {
                        dp[i] += dp[i - 2];
                    } else {
                        dp[i] = 1;
                    }
                }
                //  xxx|11 xxx1|1
                else {
                    if (i - 2 >= 0) {
                        dp[i] = dp[i - 1] + dp[i - 2];
                    } else {
                        dp[i] = 2;
                    }
                }

            }
        }
        return dp[s.length() - 1];
    }

    public static void main(String[] args) {
//    System.out.println((byte) 'A');
        System.out.println(new L91().numDecodings("12"));
        System.out.println(new L91().numDecodings("226"));
        System.out.println(new L91().numDecodings("301"));
        System.out.println(new L91().numDecodings("230"));
        System.out.println(new L91().numDecodings("2261"));
        System.out.println(new L91().numDecodings("22610"));
        System.out.println(new L91().numDecodings("2261011"));
        System.out.println(new L91().numDecodings("061"));
        System.out.println(new L91().numDecodings("10011"));
    }
}








