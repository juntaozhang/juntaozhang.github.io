package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class L6 {
    public String convert(String s, int numRows) {
        Map<Integer, StringBuilder> map = new HashMap<>();
        for (int i = 1; i <= numRows; i++) {
            map.put(i, new StringBuilder());
        }
        int j = 1;
        boolean flag = true;
        for (int i = 0; i < s.length(); i++) {
            if (flag) {
                map.get(j++).append(s.charAt(i));
                if (j > numRows) {
                    flag = false;
                    j = numRows - 1;
                }
            } else {
                map.get(j--).append(s.charAt(i));
                if (j < 1) {
                    flag = true;
                    j = 2;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= numRows; i++) {
            sb.append(map.get(i).toString());
        }
        return sb.toString();
    }

    @Test
    public void case1() {
        /**
         * 1   5   9
         * 2 4 6 8
         * 3   7
         */
        System.out.println(convert("123456789", 3));
    }
}
