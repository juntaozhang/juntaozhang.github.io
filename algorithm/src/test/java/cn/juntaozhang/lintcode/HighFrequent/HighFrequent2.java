package cn.juntaozhang.lintcode.HighFrequent;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class HighFrequent2 {
    //================================================Mirror Numbers==============================================
    /**
     * @param num a string
     * @return true if a number is strobogrammatic or false
     */
    public boolean isStrobogrammatic(String num) {
        int total = num.length();
        for (int i = 0, half = total/2; i <= half; i++) {
            char c = num.charAt(i);
            switch (c) {
                case '0':
                case '1':
                case '8':
                    if (num.charAt(total - i - 1) != c) {
                        return false;
                    }
                    break;
                case '6':
                    if (num.charAt(total - i - 1) != '9') {
                        return false;
                    }
                    break;
                case '9':
                    if (num.charAt(total - i - 1) != '6') {
                        return false;
                    }
                    break;
            }
        }
        if ((total / 2) * 2 != total){
            char mid = num.charAt(total / 2);
            return mid == '0' || mid == '1' || mid == '8';
        }
        return true;
    }

    @Test
    public void isStrobogrammatic() {
        System.out.println(isStrobogrammatic("7"));
    }

    /**
     * @param input an abstract file system
     * @return return the length of the longest absolute path to file
     */
    public int lengthLongestPath(String input) {
        if (input == null || input.equals("")) {
            return 0;
        }
        int result = 0;
        String[] st = input.split("\n");
        int[] path = new int[st.length + 1];
        for (String line : st){
            String name = line.replaceAll("(\t)+", "");
            int depth = line.length() - name.length();
            if(name.contains("."))
                result = Math.max(result, path[depth] + name.length());
            else
                path[depth + 1] = path[depth] + name.length() + 1;
        }
        return result;
    }

    @Test
    public void lengthLongestPath() {
        System.out.println(lengthLongestPath("dir\n\tsubdir1\n\tsubdir2\n\t\tfile.ext"));
//        System.out.println(lengthLongestPath("dir\n\tsubdir2\n\t\tsubsubdir2\n\t\t\tfile2.ext\n\tsubdir1\n\t\tfile1.ext\n\t\tsubsubdir1"));
    }

    public int romanToInt(String s) {
        // Ⅰ（1）、X（10）、C（100）、M（1000）、V（5）、L（50）、D（500）
        //MCMLXXXVIII 1+1+5+10+10+50+1000-100+1000
        int result = 0;
        Map<Character, Integer> cache = new HashMap<>();
        cache.put('I', 1);
        cache.put('X', 10);
        cache.put('C', 100);
        cache.put('M', 1000);
        cache.put('V', 5);
        cache.put('L', 50);
        cache.put('D', 500);
        for (int i = 0; i < s.length(); i++) {
            if ((i + 1) < s.length() && cache
                    .get(s.charAt(i)) < cache.get(s.charAt(i + 1))) {
                result += cache.get(s.charAt(i + 1)) - cache.get(s.charAt(i));
                i++;
                continue;
            }
            result += cache.get(s.charAt(i));
        }
        return result;
    }
    @Test
    public void romanToInt() {
        System.out.println(romanToInt("MDCCCXLIXIV"));
//        System.out.println(romanToInt("MCMLXXXVIII"));
    }

    public int test(int n, StringBuilder sb,
                    int precision, String precisionStr,int nPrecision,String nPrecisionStr) {
        int t = n / precision;
        while (t > 0){
            sb.append(precisionStr);
            n -= precision;
            t--;
        }
        if (n >= nPrecision) {
            sb.append(nPrecisionStr).append(precisionStr);
            n -= nPrecision;
        }
        return n;
    }
    public String intToRoman(int n) {
        Map<Character, Integer> cache = new HashMap<>();
        // I（1）、X（10）、C（100）、M（1000）、V（5）、L（50）、D（500）
        //MCMLXXXVIII 1+1+5+10+10+50+1000-100+1000
        cache.put('I', 1);
        cache.put('X', 10);
        cache.put('C', 100);
        cache.put('M', 1000);
        cache.put('V', 5);
        cache.put('L', 50);
        cache.put('D', 500);
        StringBuilder sb = new StringBuilder();
        n = test(n, sb, 1000, "M", 900, "C");
        n = test(n, sb, 500, "D", 400, "C");
        n = test(n, sb, 100, "C", 90, "X");
        n = test(n, sb, 50, "L", 40, "X");
        n = test(n, sb, 10, "X", 9, "I");
        n = test(n, sb, 5, "V", 4, "I");
        while (n != 0){
            sb.append("I");
            n -= 1;
        }
        return sb.toString();
    }
    public String intToRoman2(int n) {
        // I（1）、X（10）、C（100）、M（1000）、V（5）、L（50）、D（500）
        StringBuilder sb = new StringBuilder();
        int[] numbers = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] labels = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int i = 0;
        while (n != 0 && i < labels.length){
            int t = n / numbers[i];
            while (t > 0){
                sb.append(labels[i]);
                n -= numbers[i];
                t--;
            }
            i++;
        }
        return sb.toString();
    }
    @Test
    public void intToRoman() {
        System.out.println(intToRoman2(1849));//MDCCCXLIX
//        System.out.println(romanToInt("MCMLXXXVIII"));
    }

    class Reader4 {
        public int read4(char[] buf) {
            return 4;
        }
    }
    public class Solution extends Reader4 {
        char[] cache = new char[4];
        int pointer = 0;
        int remain = -1;

        /**
         * @param buf destination buffer
         * @param n   maximum number of characters to read
         * @return the number of characters read
         */
        public int read(char[] buf, int n) {
            int c = 0;
            //读之前数据
            while (pointer < remain) {
                buf[c] = cache[pointer];
                pointer++;
                c++;
            }
            while (c < n && (remain >= 4 || remain == -1)) {// 达到n值 && 已经全部读完 || 初始化
                //获取新数据
                remain = read4(cache);
                int t = 0;
                for (pointer = 0; pointer < Math.min(remain, 4) && (c + pointer) < n; pointer++) {
                    buf[c + pointer] = cache[pointer];
                    t++;
                }
                c += t;
                if (remain < 4) {
                    return c;
                }
            }
            return c;
        }
    }
}
