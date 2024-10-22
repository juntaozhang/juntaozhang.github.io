package cn.juntaozhang.leetcode.dp;

public class L1444 {
    int rowNum;
    int colNum;
    Integer[][][] dp;
    int[][] cnt;

    public int ways(String[] pizza, int K) {
        rowNum = pizza.length;
        colNum = pizza[0].length();
        dp = new Integer[rowNum][colNum][K];
        cnt = new int[rowNum][colNum];
        count(pizza);

        for (int k = 0; k < K; k++) {
            for (int r = rowNum - 1; r >= 0; r--) {
                for (int c = colNum - 1; c >= 0; c--) {
                    count(pizza, r, c, k);
                }
            }
        }

        return dp[0][0][K - 1] == null ? 0 : dp[0][0][K - 1];
    }

    public int count(String[] pizza, int row, int col, int k) {
        if (dp[row][col][k] != null) {
            return dp[row][col][k];
        }
        if (cnt[row][col] <= k) {
            return 0;
        }
        if (k == 0) {
            dp[row][col][k] = 1;
            return 1;
        }

        int ri = getRow(pizza, row, col);
        int ci = getCol(pizza, row, col);

        int res = 0;
        if (dp[ri][ci][k] == null) {
            for (ri++; ri < rowNum; ri++) {
                res = (res + count(pizza, ri, col, k - 1)) % 1000000007;
            }

            for (ci++; ci < colNum; ci++) {
                res = (res + count(pizza, row, ci, k - 1)) % 1000000007;
            }
        } else {
            res = dp[ri][ci][k];
        }
        dp[row][col][k] = res;
        return res;
    }

    private int getRow(String[] pizza, int row, int col) {
        int ri = row;
        while (ri < pizza.length) {
            if (pizza[ri].substring(col).indexOf('A') == -1) ri++;
            else break;
        }
        return ri;
    }

    private int getCol(String[] pizza, int row, int col) {
        for (int ci = col; ci < colNum; ci++) {
            for (int ri = row; ri < rowNum; ri++) {
                if (pizza[ri].charAt(ci) == 'A') {
                    return ci;
                }
            }
        }
        return col;
    }

    public void count(String[] pizza) {
        for (int ri = rowNum - 1; ri >= 0; ri--) {
            for (int ci = colNum - 1; ci >= 0; ci--) {
                if (pizza[ri].charAt(ci) == 'A') {
                    cnt[ri][ci]++;
                }
                if (ri + 1 < rowNum) {
                    cnt[ri][ci] += cnt[ri + 1][ci];
                }
                if (ci + 1 < colNum) {
                    cnt[ri][ci] += cnt[ri][ci + 1];
                }
                if (ri + 1 < rowNum && ci + 1 < colNum) {
                    cnt[ri][ci] -= cnt[ri + 1][ci + 1];
                }

            }
        }
    }

    public int ways2(String[] pizza, int k) {
        if (pizza == null || pizza.length == 0 || pizza[0] == null || pizza[0].length() == 0 || k <= 0)
            return 0;
        int[][][] map = new int[pizza.length][pizza[0].length()][k];

        //到左边的距离
        int rightest = -1;
        for (int r = pizza.length - 1; r >= 0; r--) {
            for (int c = pizza[0].length() - 1; c >= 0; c--) {
                if (c > rightest) {
                    if (pizza[r].charAt(c) == 'A') {
                        //更新距离
                        rightest = c;
                        map[r][c][0] = 1;
                    } else {
                        map[r][c][0] = 0;
                    }
                } else {
                    map[r][c][0] = 1;
                }
            }
        }
        for (int ki = 1; ki < k; ki++) {
            for (int row = 0; row < pizza.length; row++) {
                for (int col = 0; col < pizza[0].length(); col++) {
                    int ri = row, ci = col;
                    while (ri < pizza.length) {
                        if (pizza[ri].substring(col).indexOf('A') == -1) ri++;
                        else break;
                    }
                    while (ci < pizza[0].length()) {
                        boolean hasA = false;
                        int r = ri;
                        while (r < pizza.length) {
                            if (pizza[r].charAt(ci) == 'A') {
                                hasA = true;
                                break;
                            }
                            r++;
                        }
                        if (hasA) break;
                        else ci++;
                    }
                    int res = 0;
                    for (int i = ri + 1; i < pizza.length; i++) {
                        res = (res + map[i][ci][ki - 1]) % 1000000007;
                    }
                    for (int i = ci + 1; i < pizza[0].length(); i++) {
                        res = (res + map[ri][i][ki - 1]) % 1000000007;
                    }
                    map[row][col][ki] = res;
                    //发现为最上一层，可以直接返回，此时是第一次ki == k - 1，所以row == 0， col == 0
                    if (ki == k - 1) return res;
                }
            }
        }
        return map[0][0][k - 1];
    }

    public static void main(String[] args) {
        System.out.println(new L1444().ways(new String[]{
                        "..A.A.AAA...AAAAAA.AA..A..A.A......A.AAA.AAAAAA.AA",
                        "A.AA.A.....AA..AA.AA.A....AAA.A........AAAAA.A.AA.",
                        "A..AA.AAA..AAAAAAAA..AA...A..A...A..AAA...AAAA..AA",
                        "....A.A.AA.AA.AA...A.AA.AAA...A....AA.......A..AA.",
                        "AAA....AA.A.A.AAA...A..A....A..AAAA...A.A.A.AAAA..",
                        "....AA..A.AA..A.A...A.A..AAAA..AAAA.A.AA..AAA...AA",
                        "A..A.AA.AA.A.A.AA..A.A..A.A.AAA....AAAAA.A.AA..A.A",
                        ".AA.A...AAAAA.A..A....A...A.AAAA.AA..A.AA.AAAA.AA.",
                        "A.AA.AAAA.....AA..AAA..AAAAAAA...AA.A..A.AAAAA.A..",
                        "A.A...A.A...A..A...A.AAAA.A..A....A..AA.AAA.AA.AA.",
                        ".A.A.A....AAA..AAA...A.AA..AAAAAAA.....AA....A....",
                        "..AAAAAA..A..A...AA.A..A.AA......A.AA....A.A.AAAA.",
                        "...A.AA.AAA.AA....A..AAAA...A..AAA.AAAA.A.....AA.A",
                        "A.AAAAA..A...AAAAAAAA.AAA.....A.AAA.AA.A..A.A.A...",
                        "A.A.AA...A.A.AA...A.AA.AA....AA...AA.A..A.AA....AA",
                        "AA.A..A.AA..AAAAA...A..AAAAA.AA..AA.AA.A..AAAAA..A",
                        "...AA....AAAA.A...AA....AAAAA.A.AAAA.A.AA..AA..AAA",
                        "..AAAA..AA..A.AA.A.A.AA...A...AAAAAAA..A.AAA..AA.A", "AA....AA....AA.A......AAA...A...A.AA.A.AA.A.A.AA.A", "A.AAAA..AA..A..AAA.AAA.A....AAA.....A..A.AA.A.A...", "..AA...AAAAA.A.A......AA...A..AAA.AA..A.A.A.AA..A.", ".......AA..AA.AAA.A....A...A.AA..A.A..AAAAAAA.AA.A", ".A.AAA.AA..A.A.A.A.A.AA...AAAA.A.A.AA..A...A.AAA..", "A..AAAAA.A..A..A.A..AA..A...AAA.AA.A.A.AAA..A.AA..", "A.AAA.A.AAAAA....AA..A.AAA.A..AA...AA..A.A.A.AA.AA", ".A..AAAA.A.A.A.A.......AAAA.AA...AA..AAA..A...A.AA", "A.A.A.A..A...AA..A.AAA..AAAAA.AA.A.A.A..AA.A.A....", "A..A..A.A.AA.A....A...A......A.AA.AAA..A.AA...AA..", ".....A..A...A.A...A..A.AA.A...AA..AAA...AA..A.AAA.", "A...AA..A..AA.A.A.AAA..AA..AAA...AAA..AAA.AAAAA...", "AA...AAA.AAA...AAAA..A...A..A...AA...A..AA.A...A..", "A.AA..AAAA.AA.AAA.A.AA.A..AAAAA.A...A.A...A.AA....", "A.......AA....AA..AAA.AAAAAAA.A.AA..A.A.AA....AA..", ".A.A...AA..AA...AA.AAAA.....A..A..A.AA.A.AA...A.AA", "..AA.AA.AA..A...AA.AA.AAAAAA.....A.AA..AA......A..", "AAA..AA...A....A....AA.AA.AA.A.A.A..AA.AA..AAA.AAA", "..AAA.AAA.A.AA.....AAA.A.AA.AAAAA..AA..AA.........", ".AA..A......A.A.AAA.AAAA...A.AAAA...AAA.AAAA.....A", "AAAAAAA.AA..A....AAAA.A..AA.A....AA.A...A.A....A..", ".A.A.AA..A.AA.....A.A...A.A..A...AAA..A..AA..A.AAA", "AAAA....A...A.AA..AAA..A.AAA..AA.........AA.AAA.A.", "......AAAA..A.AAA.A..AAA...AAAAA...A.AA..A.A.AA.A.", "AA......A.AAAAAAAA..A.AAA...A.A....A.AAA.AA.A.AAA.", ".A.A....A.AAA..A..AA........A.AAAA.AAA.AA....A..AA", ".AA.A...AA.AAA.A....A.A...A........A.AAA......A...", "..AAA....A.A...A.AA..AAA.AAAAA....AAAAA..AA.AAAA..", "..A.AAA.AA..A.AA.A...A.AA....AAA.A.....AAA...A...A", ".AA.AA...A....A.AA.A..A..AAA.A.A.AA.......A.A...A.", "...A...A.AA.A..AAAAA...AA..A.A..AAA.AA...AA...A.A.", "..AAA..A.A..A..A..AA..AA...A..AA.AAAAA.A....A..A.A"},
                7));
//        System.out.println(new L1444().ways(new String[]{".A", "AA", "A."}, 3));
//        System.out.println(new L1444().ways(new String[]{"A....", "...A.", ".A...", "..A..", "....."}, 1));
//        System.out.println(new L1444().ways(new String[]{"A....", "...A.", ".A...", "..A..", "....."}, 2));
//        System.out.println(new L1444().ways(new String[]{"A....", "...A.", ".A...", "..A..", "....."}, 3));
//        System.out.println(new L1444().ways(new String[]{"A....", "...A.", ".A...", "..A..", "....."}, 4));
//        System.out.println(new L1444().ways(new String[]{"A....", "...A.", ".A...", "..A..", "....."}, 5));
    }
}
