package cn.juntaozhang.leetcode.greedy;

/**
 * https://leetcode.cn/problems/valid-palindrome-ii/submissions/
 * https://leetcode.cn/problems/RQku0D/
 *
 * @author juntzhang
 */
public class L680 {

    public boolean validPalindrome(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            if (s.charAt(i) == s.charAt(j)) {
                i++;
                j--;
            } else {
                if (validPalindrome(i + 1, j, s)) {
                    return true;
                } else {
                    return validPalindrome(i, j - 1, s);
                }
            }
        }
        return true;
    }

    public boolean validPalindrome(int start, int end, String s) {
        int i = start;
        int j = end;
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(new L680().validPalindrome("aaca"));
//    System.out.println(new L680().validPalindrome("ac"));
    }
}
