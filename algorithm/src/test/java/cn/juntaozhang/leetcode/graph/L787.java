package cn.juntaozhang.leetcode.graph;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author juntzhang
 */
class L787 {

    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        int[] preDist = new int[n];
        int[] currDist = new int[n];
        Arrays.fill(preDist, Integer.MAX_VALUE);
        Arrays.fill(currDist, Integer.MAX_VALUE);
        preDist[src] = 0;
        currDist[src] = 0;

        for (int[] flight : flights) {
            if (src == flight[0]) preDist[flight[1]] = flight[2];
        }

        for (int i = 1; i <= k; i++) {
            for (int[] flight : flights) {
                int _src = flight[0], _dst = flight[1], cost = flight[2];
                if (preDist[_src] != Integer.MAX_VALUE) {
                    currDist[_dst] = Math.min(preDist[_src] + flight[2], currDist[_dst]);
                }
            }
            preDist = currDist.clone();
        }

        return preDist[dst] == Integer.MAX_VALUE ? -1 : preDist[dst];
    }

    public static void main(String[] args) {
        int[][] flights = Arrays.stream(args[1].split("],\\[")).map(l -> {
            String[] arr = l.split(",");
            return new int[]{
                    Integer.parseInt(arr[0]),
                    Integer.parseInt(arr[1]),
                    Integer.parseInt(arr[2])
            };
        }).collect(Collectors.toList()).toArray(new int[0][0]);
        System.out.println(
                new L787().findCheapestPrice(
                        Integer.parseInt(args[0]),
                        flights,
                        Integer.parseInt(args[2]),
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4])));

//        System.out.println(new L787().findCheapestPrice(5, new int[][]{
//                {1, 2, 10}, {2, 0, 7}, {1, 3, 8}, {4, 0, 10}, {3, 4, 2}, {4, 2, 10}, {0, 3, 3}, {3, 1, 6}, {2, 4, 5}
//        }, 0, 4, 1));

//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 2}, {1, 2, 1}, {2, 0, 10}
//        }, 1, 2, 1));
//        System.out.println(new L787().findCheapestPrice(4, new int[][]{
//                {0, 1, 1}, {0, 2, 5}, {1, 2, 1}, {2, 3, 1}
//        }, 0, 3, 1));
//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {0, 2, 500}
//        }, 0, 2, 1));
//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {0, 2, 500}
//        }, 0, 2, 0));
//        System.out.println(new L787().findCheapestPrice(4, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {2, 0, 100}, {1, 3, 600}, {2, 3, 200}
//        }, 0, 3, 1));
    }
}