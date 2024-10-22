package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Stack;

/**
 * 
 */
public class FirstPositionUniqueCharacter {
    /**
     * @param s a string
     * @return it's index
     */
    public int firstUniqChar(String s) {
        int[] buff = new int[128];
        int p = -1;
        for (int i = s.length() - 1; i > -1; i--) {
            buff[(int)s.charAt(i)] += 1;
            if (buff[(int)s.charAt(i)] > 1) {
//                p = i - 1;
            } else {
                p = i;
            }
        }
        return p;
    }

    @Test
    public void firstUniqChar() {
        Stack<String> s = new Stack();
        System.out.println(firstUniqChar("lovelintcode"));
    }
}
