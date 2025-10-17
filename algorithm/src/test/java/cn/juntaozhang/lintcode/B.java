package cn.juntaozhang.lintcode;

import org.junit.Test;

/**
 * 
 */
public class B {
    @Test
    public void test2() {

        System.out.println(solution(new int[]{2,2}, 1));
//        System.out.println(solution(new int[]{1, 1, 3, 3, 3, 4, 5, 5, 5, 5}, 1));
    }

    int solution(int[] A, int K) {
        int n = A.length;
        int best = 0;
        int count = 1;
        for (int i = 0; i < n - 1; i++) {
            if (A[i] == A[i + 1])
                count = count + 1;
            else
                count = 1;
            if (count > best)
                best = count;
        }
        int result = best + (A.length > K ? K : A.length);

        return result;
    }
}
