package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.TreeMap;

public class L352_skiplist {
    static class SummaryRanges {
        TreeMap<Integer, Integer> ranges;

        public SummaryRanges() {
            ranges = new TreeMap<>();
        }

        public void addNum(int value) {
            Integer f = ranges.floorKey(value);
            if (f != null) {
                Integer end = ranges.get(f);
                if (value == end + 1) {
                    ranges.put(f, value);
                }
            }

            // enlarge range
            Integer c = ranges.ceilingKey(value);
            if (c != null) {
                Integer end = ranges.get(c);
                if (value + 1 == c) {
                    // merge
                    if (f != null && ranges.get(f) == value) {
                        ranges.put(f, end);
                    } else {
                        ranges.put(value, end);
                    }
                    ranges.remove(c);
                }
            }

            f = ranges.floorKey(value);
            if (f == null || ranges.get(f) < value) {
                ranges.put(value, value);
            }
        }

        public int[][] getIntervals() {
            int[][] res = new int[ranges.size()][2];
            int index = 0;
            for (Integer start : ranges.keySet()) {
                res[index][0] = start;
                res[index][1] = ranges.get(start);
                index++;
            }
            return res;
        }
    }

    @Test
    public void case1() {
        SummaryRanges summaryRanges = new SummaryRanges();
        summaryRanges.addNum(1);      // arr = [1]
        summaryRanges.getIntervals(); // 返回 [[1, 1]]
        summaryRanges.addNum(3);      // arr = [1, 3]
        summaryRanges.getIntervals(); // 返回 [[1, 1], [3, 3]]
        summaryRanges.addNum(7);      // arr = [1, 3, 7]
        summaryRanges.getIntervals(); // 返回 [[1, 1], [3, 3], [7, 7]]
        summaryRanges.addNum(2);      // arr = [1, 2, 3, 7]
        summaryRanges.getIntervals(); // 返回 [[1, 3], [7, 7]]
        summaryRanges.addNum(6);      // arr = [1, 2, 3, 6, 7]
        summaryRanges.getIntervals(); // 返回 [[1, 3], [6, 7]]
    }
}
