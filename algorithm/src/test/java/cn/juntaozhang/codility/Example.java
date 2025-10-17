package cn.juntaozhang.codility;

import org.junit.Test;

import java.util.Arrays;

/**
 *
 * 
 */
public class Example {

    @Test
    public void solution() {
        System.out.println(solution(new int[]{1, -2, 0, 9, -1, -2, -1, -1, -1, 0, 0, 1}));
    }

    public int solution(int[] A) {
        int len = A.length;
        if (len < 2) {
            return -1;
        }
        long total = 0L;
        for (int a : A) {
            total += a;
        }

        long prefix = 0L;
        for (int i = 0; i < len; i++) {
            if (prefix == (total - prefix - A[i])) {
                return i;
            }
            prefix += A[i];
        }
        return -1;
    }
}
