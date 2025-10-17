package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class SpaceReplacement {
    @Test
    public void replaceBlank() {
        System.out.println(replaceBlank("hello world".toCharArray(), 11));
    }

    public int replaceBlank(char string[], int length) {
        if (0 == length) return 0;
        int num = 0;
        for (int i = 0; i < length; i++) {
            if (string[i] == ' ') num++;
        }

        int newLen = length + num * 2;
        string[newLen] = 0;
        int j = 1;
        for (int i = length - 1; i >= 0; i--) {
            if (string[i] != ' ') {
                string[newLen - j] = string[i];
                j++;
            } else {
                string[newLen - j] = '0';
                j++;
                string[newLen - j] = '2';
                j++;
                string[newLen - j] = '%';
                j++;
            }
        }
        return newLen;
    }


    public int replaceBlank2(char[] string, int length) {
        int max = 0;
        for (int i = 0; i < length; i++) {
            if (string[i] == ' ') {
                max++;
            }
        }
        char[] string2 = new char[length + max * 2];
        for (int i = 0, j = 0; i < length; i++, j++) {
            if (string[i] == ' ') {
                string2[j++] = '%';
                string2[j++] = '2';
                string2[j] = '0';
            } else {
                string2[j] = string[i];
            }
        }
        return string2.length;
    }

}
