package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class SingleNumber {

    @Test
    public void singleNumberII() {
        System.out.println(singleNumberII(new int[]{1, 1, 2, 3, 3, 3, 2, 2, 4, 1}));
    }

    public int singleNumberII(int[] A) {
        //int 每一位求和 % 3
        int res = 0;
        for (int i = 0; i < 32; i++) {
            int t = 0;
            for (int a : A) {
                t += (a >> i) & 1;
            }
            t = t % 3;
            res |= (t << i);
        }
        return res;
    }

    @Test
    public void singleNumberIII() {
        System.out.println(singleNumberIII(new int[]{7, 2, 2, 1, 4, 4, 3, 3}));
    }

    public List<Integer> singleNumberIII(int[] A) {
        int num1 = 0;
        for (int a : A) {
            num1 ^= a;
        }
        //(num1 & (num1 - 1) 最后一位1 7,1 111,001 最右边不同为2
        num1 = num1 - (num1 & (num1 - 1));
        int num2 = 0;
        int num3 = 0;
        for (int a : A) {
            if ((num1 & a) == 0) {
                num2 ^= a;
            } else {
                num3 ^= a;
            }
        }
        List<Integer> res = new ArrayList<>();
        res.add(num2);
        res.add(num3);
        return res;
    }
}
