package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class SparseMatrixMultiplication {

    @Test
    public void multiply() {
        System.out.println(Arrays.deepToString(multiply(new int[][]{{1, 0, 0}, {-1, 0, 3}}, new int[][]{{7, 0, 0}, {0, 0, 0}, {0, 0, 1}})));
    }

    public int[][] multiply(int[][] A, int[][] B) {
        List<List<int[]>> blist = new ArrayList<>();
        for (int k = 0; k < B.length; k++) {
            List<int[]> list = new ArrayList<>();
            blist.add(list);
            for (int j = 0; j < B[0].length; j++) {
                if (B[k][j] != 0) {
                    list.add(new int[]{j, B[k][j]});
                }
            }
        }
        int[][] res = new int[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int k = 0; k < A[0].length; k++) {
                if (A[i][k] != 0) {
                    for (int[] jAndVal : blist.get(k)) {
                        res[i][jAndVal[0]] += A[i][k] * jAndVal[1];
                    }
                }
            }
        }
        return res;
    }

    @Test
    public void multiply2() {
        System.out.println(multiply("123", "45"));
    }

    public String multiply(String num1, String num2) {
        int[] res = new int[num1.length() + num2.length() + 1];
        int m = 0;
        for (int i = num1.length() - 1; i >= 0; i--) {
            int k = res.length - 1;
            for (int j = num2.length() - 1; j >= 0; j--) {
                int t1 = i >= 0 ? num1.charAt(i) - '0' : 0;
                int t2 = j >= 0 ? num2.charAt(j) - '0' : 0;
                res[k - m] += t1 * t2;
                k--;
            }
            m++;
        }

        int scale = 0;
        for (int i = res.length - 1; i >= 0; i--) {
            int sum = (res[i] + scale);
            res[i] = sum % 10;
            scale = sum / 10;
        }


        int i = 0;
        while (res[i] == 0 && i < res.length - 1) {
            i++;
        }
        StringBuilder builder = new StringBuilder();
        while (i <= res.length - 1) {
            builder.append(res[i++]);
        }
        return builder.toString();
    }
}
