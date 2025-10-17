package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class HighFrequent6 {
    @Test
    public void rotate() {
        int[][] m = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        System.out.println(Arrays.deepToString(m));
        rotate(m);
        System.out.println(Arrays.deepToString(m));
    }

    public void rotate(int[][] matrix) {
        int len = matrix.length;
        if (len == 0) {
            return;
        }
        //上下交换
        for (int i = 0; i < len / 2; i++) {
            for (int j = 0; j < len; j++) {
                int t = matrix[i][j];
                matrix[i][j] = matrix[len - 1 - i][j];
                matrix[len - 1 - i][j] = t;
            }
        }

        //对称线交换
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < i; j++) {
                int t = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = t;
            }
        }
    }

    class Point {
        int x;
        int y;

        Point() {
            x = 0;
            y = 0;
        }

        Point(int a, int b) {
            x = a;
            y = b;
        }
    }

    @Test
    public void gcd() {
        System.out.println(gcd1(0, 10));
    }


    private int gcd1(int a, int b) {
        while (b != 0) {
            int r = b;
            b = a % b;
            a = r;
        }
        return a;
    }

    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    @Test
    public void maxPoints() {
        System.out.println(maxPoints(new Point[]{
                new Point(-4, -4),
                new Point(-8, -582),
                new Point(-9, -651),
                new Point(-3, 3),
                new Point(9, 591),
        }));
    }
    @Test
    public void maxPoints2() {
        System.out.println(maxPoints(new Point[]{
                new Point(0, 0),
                new Point(0, 0)
        }));
    }

    public int maxPoints(Point[] points) {
        // Write your code here
        if (points == null) {
            return 0;
        }
        int ans = 0;
        for (int i = 0; i < points.length; i++) {
            Map<String, Integer> slope = new HashMap<>();
            int maxPoints = 0, overlap = 0, vertical = 0;

            for (int j = i + 1; j < points.length; j++) {
                //竖直 与 重合
                if (points[i].x == points[j].x) {
                    if (points[i].y == points[j].y) {
                        overlap++;
                    } else {
                        vertical++;
                    }
                    continue;
                }

                //斜率
                int dx = points[i].x - points[j].x;
                int dy = points[i].y - points[j].y;
                int tmp = gcd(dx, dy);
                dx /= tmp;
                dy /= tmp;
                String k = dy + "/" + dx;

                if (!slope.containsKey(k)) {
                    slope.put(k, 0);
                }
                slope.put(k, slope.get(k) + 1);
                maxPoints = Math.max(maxPoints, slope.get(k));
            }
            maxPoints = Math.max(maxPoints, vertical);
            //斜率相同 + 重叠 + 本身
            ans = Math.max(ans, maxPoints + overlap + 1);
        }
        return ans;
    }

}
