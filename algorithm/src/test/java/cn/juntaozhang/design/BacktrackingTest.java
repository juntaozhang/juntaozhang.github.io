package cn.juntaozhang.design;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * 递归回溯法
 */
public class BacktrackingTest {
    public static Integer sum(List<Integer> list) {
        return list.stream().reduce(0, Integer::sum);
    }

    private static BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }


    @Test
    public void case1() throws InterruptedException {
        int[] arr = {2, 3, 3, 5};
        assertEquals(List.of(3, 3), findClosest(arr, 6));
        assertEquals(List.of(2), findClosest(arr, 1));
        assertEquals(List.of(2), findClosest(arr, 2));
        assertEquals(List.of(3), findClosest(arr, 3));
        assertEquals(List.of(2, 3), findClosest(arr, 4));
        assertEquals(List.of(2, 3), findClosest(arr, 5));
        assertEquals(List.of(2, 5), findClosest(arr, 7));
        assertEquals(List.of(2, 3, 3), findClosest(arr, 8));
        assertEquals(List.of(2, 3, 5), findClosest(arr, 9));
        assertEquals(List.of(2, 3, 5), findClosest(arr, 10));
        assertEquals(List.of(3, 3, 5), findClosest(arr, 11));
        assertEquals(List.of(2, 3, 3, 5), findClosest(arr, 12));
        assertEquals(List.of(2, 3, 3, 5), findClosest(arr, 13));
    }

    @Test
    public void case2() throws InterruptedException {
        int[] arr = {1, 2, 5, 7};
        assertEquals(List.of(1), findClosest(arr, 1));
        assertEquals(List.of(2), findClosest(arr, 2));
        assertEquals(List.of(1, 2), findClosest(arr, 3));
        assertEquals(List.of(5), findClosest(arr, 4));
        assertEquals(List.of(5), findClosest(arr, 5));
        assertEquals(List.of(1, 5), findClosest(arr, 6));
        assertEquals(List.of(2, 5), findClosest(arr, 7));
        assertEquals(List.of(1, 2, 5), findClosest(arr, 8));
        assertEquals(List.of(2, 7), findClosest(arr, 9));
        assertEquals(List.of(1, 2, 7), findClosest(arr, 10));
        assertEquals(List.of(5, 7), findClosest(arr, 11));
        assertEquals(List.of(5, 7), findClosest(arr, 12));
        assertEquals(List.of(1, 5, 7), findClosest(arr, 13));
        assertEquals(List.of(2, 5, 7), findClosest(arr, 14));
        assertEquals(List.of(1, 2, 5, 7), findClosest(arr, 15));
    }

    @Test
    public void case3() throws InterruptedException {
        int size = 20000;
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = new Random().nextInt(100) + 1;
        }
        List<Integer> sorted = Arrays.stream(arr).boxed().sorted().collect(Collectors.toList());
        int total = sorted.stream().reduce(0, Integer::sum);
        int target = 2000;// target 如果包含 20个 有性能问题
        StopWatch watch = StopWatch.createStarted();
        List<Integer> result = findClosest(arr, target, true);
        System.out.println("cost=>" + watch.getTime(TimeUnit.SECONDS) + "S");
        int actual = sum(result);
        System.out.println("total=>" + total + ",target=>" + target + ",actual=>" + actual + "\nresult=>" + result + "\narr=>" + sorted);
        System.out.println("===========================================");
//        watch = StopWatch.createStarted();
//        result = findClosest1(arr, target);
//        System.out.println("cost=>" + watch.getTime(TimeUnit.SECONDS) + "S");
//        System.out.println("total=>" + total + ",target=>" + target + ",actual=>" + actual + "\nresult=>" + result + "\narr=>" + sorted);
    }

    @Test
    public void case4() throws InterruptedException {
        int[] arr = {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3};
        assertEquals(List.of(1, 3), findClosest(arr, 4));
        assertEquals(List.of(1, 3), findClosest1(arr, 4));
    }

    @Test
    public void case5() {
        int target = 221;
        int[] arr = {1, 3, 5, 6, 8, 9, 15, 16, 18, 18, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29};
        List<Integer> sorted = Arrays.stream(arr).boxed().sorted().collect(Collectors.toList());
        int total = sorted.stream().reduce(0, Integer::sum);
        List<Integer> result = findClosest(arr, target, true);
        System.out.println("total=>" + total + ",target=>" + target + ",actual=>" + sum(result) + "\nresult=>" + result + "\narr=>" + sorted);
    }


    public List<Integer> findClosest(int[] arr, int target) {
        return findClosest(arr, target, false);
    }

    public List<Integer> findClosest(int[] arr, int target, boolean approximate) {
        List<Integer> sorted = Arrays.stream(arr).boxed().sorted().collect(Collectors.toList());
        SlotPicker picker = new SlotPicker(sorted, target, approximate);
        return picker.pick();
    }

    public List<Integer> findClosest1(int[] arr, int target) {
        List<Integer> sorted = Arrays.stream(arr).boxed().sorted().collect(Collectors.toList());
        return new SlotPicker1(sorted, target).pick();
    }


    public static class SlotPicker {
        private final List<Integer> allSlots;
        private final List<Integer> current;
        private final List<Integer> closest;
        private int target;
        private boolean approximate;
        private boolean[] allSlotsFlag;
        private int count = 0;

        public SlotPicker(List<Integer> allSlots, int target, boolean approximate) {
            this.allSlots = new ArrayList<>();
            setAllSlots(allSlots);
            this.current = new ArrayList<>();
            this.closest = new ArrayList<>(allSlots);
            this.target = target;
            this.approximate = approximate;
        }

        public void setAllSlots(List<Integer> allSlots) {
            this.allSlots.clear();
            this.allSlots.addAll(allSlots);
            this.allSlotsFlag = new boolean[allSlots.size()];
        }

        public boolean isApproximate() {
            return this.allSlots.size() > 20 && this.approximate;
        }

        public List<Integer> pick() {
            this.allSlots.sort(Integer::compareTo);
            System.out.println("!n=>" + factorial(allSlots.size()));
            System.out.println("n^2=>" + allSlots.size() * allSlots.size());
            if (isApproximate()) {
                pick(0, this::isClosest);
                System.out.println("closest size =>" + closest.size());
                System.out.println("first actual closest=>" + sum(closest) + "\n" + closest);
                if (sum(closest) == target) {
                    return closest;
                }
                int oldTarget = target;
                List<Integer> oldClosest = new ArrayList<>(closest);


                // 找差值，然后去掉
                target = sum(closest) - target;
                this.count = 0;
                setAllSlots(closest);
                this.closest.clear();
                this.current.clear();
                this.approximate = false;
                pick(0, this::isEq);
                if (!this.closest.isEmpty()) {
                    System.out.println("second actual remove diff=>" + closest);
                    for (Integer integer : this.closest) {
                        this.allSlots.remove(integer);
                    }
                    return this.allSlots;
                }

                // 倒序找最优解
                this.approximate = true;
                target = oldTarget;
                this.count = 0;
                setAllSlots(oldClosest);
                closest.addAll(oldClosest);
                this.allSlots.sort((a, b) -> b - a);
                this.current.clear();
                pick(0, this::isClosest);


                this.closest.sort((a, b) -> b - a);
                System.out.println("final actual closest=>" + sum(closest) + "\n" + closest);
                return closest;
            } else {
                pick(0, this::isClosest);
            }
            System.out.println("recursion count=>" + count);
            return closest;
        }

        private boolean isClosest() {
            int currentSum = sum(current);
            if (currentSum >= target) {
                if (currentSum < sum(closest)) {
                    closest.clear();
                    closest.addAll(current);
                }
                return true;
            }
            return false;
        }

        private boolean isEq() {
            int currentSum = sum(current);
            if (currentSum >= target) {
                if (currentSum == target) {
                    closest.clear();
                    closest.addAll(current);
                }
                return true;
            }
            return false;
        }

        private void pick(int start, Supplier<Boolean> breakFunction) {
            count++;
            if (breakFunction.get()) {
                return;
            }
            if (start >= this.allSlots.size()) {
                return;
            }

            for (int i = start; i < allSlots.size(); i++) {
                // allSlotsFlag[i - 1] 避免纵向比较
                if (i > 0 && !allSlotsFlag[i - 1] && allSlots.get(i).equals(allSlots.get(i - 1))) {
                    continue;
                }
                current.add(allSlots.get(i));
                allSlotsFlag[i] = true;
                pick(i + 1, breakFunction);
                int currentSum = sum(current);
                if (!isApproximate()) {
                    current.remove(current.size() - 1);
                    allSlotsFlag[i] = false;
                }
                if (currentSum >= target) {
                    break;
                }
            }
        }
    }

    public static class SlotPicker1 {
        private final List<Integer> allSlots;
        private final List<Integer> current;
        private final List<Integer> closest;
        private final int target;
        private int count = 1;

        public SlotPicker1(List<Integer> allSlots, int target) {
            this.allSlots = allSlots;
            this.current = new ArrayList<>();
            this.closest = new ArrayList<>(allSlots);
            this.target = target;
        }

        private static BigInteger factorial(int n) {
            BigInteger result = BigInteger.ONE;
            for (int i = 2; i <= n; i++) {
                result = result.multiply(BigInteger.valueOf(i));
            }
            return result;
        }

        public List<Integer> pick() {
            System.out.println("!n=>" + factorial(allSlots.size()));
            System.out.println("n^2=>" + allSlots.size() * allSlots.size());
            pick(0);
            System.out.println("recursion count=>" + count);
            return closest;
        }

        private void pick(int idx) {
            count++;
//            System.out.println(target + ",current=>" + StringUtils.join(current) + ",closest=>" + StringUtils.join(closest));
            int currentSum = sum(current);
            if (currentSum >= target) {
                if (currentSum < sum(closest)) {
                    closest.clear();
                    closest.addAll(current);
                }
                return;
            }

            if (idx >= this.allSlots.size()) {
                return;
            }
            // include the current element
            current.add(allSlots.get(idx));
            pick(idx + 1);
            currentSum = sum(current);
            // Exclude the current element
            current.remove(current.size() - 1);
            // pruning
            if (currentSum < target) {
                pick(idx + 1);
            }
        }
    }
}
