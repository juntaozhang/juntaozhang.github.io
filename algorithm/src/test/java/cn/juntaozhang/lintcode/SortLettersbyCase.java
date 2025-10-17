package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class SortLettersbyCase {
    @Test
    public void sortLetters() {
        sortLetters("baDAc".toCharArray());
    }
    public void sortLetters(char[] chars) {
        int i = 0, j = chars.length - 1;
        char tmp ;
        while ( i <= j) {
            while (i <= j && Character.isLowerCase(chars[i]) ) i++;
            while (i <= j && Character.isUpperCase(chars[j]) ) j--;
            if (i <= j) {
                tmp = chars[i];
                chars[i] = chars[j];
                chars[j] = tmp;
                i++; j--;
            }
        }
        //write your code here
        return ;
    }


    public void sortLetters1(char[] chars) {
        //'A'=65 'a'=97
        for (int i = 0;i < chars.length; i++) {
            if (chars[i] < 'a') {
                chars[i] += 100;
            }
        }
        //冒泡
        for (int i = 0;i < chars.length; i++) {
            int t = i;
            for (int j = i + 1;j < chars.length; j++) {
                if (chars[t] > chars[j]) {
                    t = j;
                }
            }
            if (t != i) {
                char min = chars[t];
                chars[t] = chars[i];
                chars[i] = min;
            }
        }
        for (int i = 0;i < chars.length; i++) {
            if (chars[i] >= 'A' + 100) {
                chars[i] -= 100;
            }
        }
    }

    public void sortLetters2(char[] chars) {
        //'A'=65 'a'=97
        for (int i = 0;i < chars.length; i++) {
            if (chars[i] < 'a') {
                chars[i] += 100;
            }
        }
        //交换排序
        for (int i = 0;i < chars.length; i++) {
            for (int j = i + 1;j < chars.length; j++) {
                if (chars[i] > chars[j]) {
                    char min = chars[i];
                    chars[i] = chars[j];
                    chars[j] = min;
                }
            }
        }
        for (int i = 0;i < chars.length; i++) {
            if (chars[i] >= 'A' + 100) {
                chars[i] -= 100;
            }
        }
    }
}
