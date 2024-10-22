package cn.juntaozhang.leetcode.greedy;

public class L921 {
    public int minAddToMakeValid(String s) {
        int l = 0, result = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                l++;
            } else if (l > 0) {
                l--;
            } else {
                result++;
            }
        }
        return result + l;
    }

    public static void main(String[] args) {
        System.out.println(new L921().minAddToMakeValid("())"));
        System.out.println(new L921().minAddToMakeValid("((("));
        System.out.println(new L921().minAddToMakeValid("()))(("));
    }
}
