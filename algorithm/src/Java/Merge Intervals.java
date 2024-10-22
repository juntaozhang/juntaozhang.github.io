E

方法1：O(nlogn)         
扫描线+Count无敌手。注意start end把interval给合起来。   
count==0的时候，就是每次start end双数抵消的时候，就应该是一个interval的开头/结尾。写个例子就知道了。   

空间：O(2n) -> O(n)   
时间,priorityqueue: O(nlogn)   

记得怎么写comparator    

在 LeetCode里面，Scan line比方法2要快很多.

方法2：    
Collections.sort() on interval.start之后，试着跑一遍，按照merge的需求，把需要merge的地方续好，然后减掉多余的interval就好。

(不知为何LeetCode把Merge Interval, Insert Interval 标为Hard)

Collections.sort(..., new comparator): sort by Interval.start.

画两个相连的Interval， prev, curr:
prev只有 prev.end覆盖了 curr.start， 才需要merge. 那么比较一下, marege.     
记得如果merge, 一定要list.remove(i), 并且i--， 因为改变了List的大小。

若没有重合，就继续iteration: prev = curr. move on.

/*
    new Comparator<Object>(){
        public int compare(obj1, obj2) {
            return obj1.x - obj2.x;
        }

    }
*/


```
/*
Given a collection of intervals, merge all overlapping intervals.

For example,
Given [1,3],[2,6],[8,10],[15,18],
return [1,6],[8,10],[15,18].

Tags: Array, Sort
Similar Problems: (H) Insert Interval, (E) Meeting Rooms (M) Meeting Rooms II

*/

/*
Thoughts:
Again use scan line. Quite similar to Meeting Rooms, flight schedules... etc
This one: when count ==0, make sure to keep track start and end, add into the rst
When writing out example, whenever count==0, that indicates an end of a interval.

HOWEVER, this uses O(n) space, while this problem requests O(1) space
*/

/**
 * Definition for an interval.
 * public class Interval {
 *     int start;
 *     int end;
 *     Interval() { start = 0; end = 0; }
 *     Interval(int s, int e) { start = s; end = e; }
 * }
 */
public class Solution {
    class Point{
        int x, flag;
        public Point(int x, int flag) {
            this.x = x;
            this.flag = flag;
        }
    }
    public List<Interval> merge(List<Interval> intervals) {
        List<Interval> rst = new ArrayList<Interval>();
        if (intervals == null || intervals.size() == 0) {
            return rst;
        }
        PriorityQueue<Point> queue = new PriorityQueue<Point>(2, new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                return p1.x - p2.x;
            }
        });
        
        for (Interval entry : intervals) {
            queue.offer(new Point(entry.start, 1));
            queue.offer(new Point(entry.end, -1));            
        }
        
        int count = 0;
        int start = 0;
        int end = 0;
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (count == 0) {//detect start
                start = p.x;
            }    
            count += p.flag;
            while (!queue.isEmpty() && p.x == queue.peek().x) {//proces all points on same position x
                p = queue.poll();
                count += p.flag;
            }
            if (count == 0) {//detect end
                end = p.x;
                rst.add(new Interval(start, end));
            }
        }
        
        return rst;
    }
}



/*
    Thoughts: 12.09.2015
    Recap. Use O(1)

    Sort by start time.
    then it overlaps: check on pre.end and curr.start.
    if overlaps: curr.start will be overlapped; also check on curr.end and pre.end, decide who ends this interval
    
    border case: null, return itself; or length==1, return.
*/

class Solution {
    public List<Interval> merge(List<Interval> intervals) {
        if (intervals == null || intervals.size() <= 1) {
            return intervals;
        }

        Collections.sort(intervals, new Comparator<Interval>(){
            public int compare(Interval a, Interval b) {
                return a.start - b.start;
            }
        });
        Interval prev = intervals.get(0);
        Interval curr;

        for (int i = 1; i < intervals.size(); i++) {
            curr  = intervals.get(i);
            if (prev.end >= curr.start) {
                if (prev.end <= curr.end) {
                    prev.end = curr.end;
                }
                intervals.remove(i);
                i--;
            } else {
                prev = curr;         
            }
        }

        return intervals;
    }

}

```