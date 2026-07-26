# spark JVM

```
Desired survivor size 721944576 bytes, new threshold 2 (max 15)
 [PSYoungGen: 2113253K->403885K(2382336K)] 2607563K->898283K(3584000K), 15.1579340 secs] [Times: user=63.25 sys=206.85, real=15.15 secs] 


real：指的是在此次GC事件中所花费的总时间；
user：指的是CPU工作在用户态所花费的时间；
sys：指的是CPU工作在内核态所花费的时间。

垃圾回收过程是通过并发执行，因此 user + sys 远大于 real 。

-XX:+PrintTenuringDistribution 指定JVM 在每次新生代GC时，输出幸存区中对象的年龄分布
```

## jconsole监控
>-Dcom.sun.management.jmxremote.port=27012 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false


## gc log
>-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution

## dump 
>-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/jvm_dump

## default
>-XX:MaxNewSize=3072M
```
RDD Blocks	Storage Memory	Disk Used	Cores	Active Tasks	Failed Tasks	Complete Tasks	Total Tasks	Task Time (GC Time)	Input	Shuffle Read	Shuffle Write	Blacklisted
Active(71)	0	0.0 B / 333.3 GB	0.0 B	140	0	0	7210	7210	22.9 h (2.4 h)	874 GB	46.2 GB	47 GB	0
Dead(6)	0	0.0 B / 28.3 GB	0.0 B	12	0	0	1	1	9 s (5 s)	0.0 B	20.7 MB	11.2 MB	0
Total(77)	0	0.0 B / 361.7 GB	0.0 B	152	0	0	7211	7211	23.0 h (2.4 h)	874 GB	46.2 GB	47 GB	0
```


## G1
>-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=35 -XX:ConcGCThreads=12

负载状态下:
```
31 (121,200G) 19m
RDD Blocks	Storage Memory	Disk Used	Cores	Active Tasks	Failed Tasks	Complete Tasks	Total Tasks	Task Time (GC Time)	Input	Shuffle Read	Shuffle Write	Blacklisted
Active(31)	0	0.0 B / 151.6 GB	0.0 B	120	0	0	9659	9659	23.1 h (30 min)	1.2 TB	63.4 GB	61.7 GB	0
Dead(10)	0	0.0 B / 49.7 GB	0.0 B	40	0	0	824	824	2.1 h (2.9 min)	95.8 GB	1.4 GB	4.6 GB	0
Total(41)	0	0.0 B / 201.3 GB	0.0 B	160	0	0	10483	10483	25.1 h (33 min)	1.3 TB	64.8 GB	66.3 GB	0



四个步骤：
	初始标记（initial mark，STW）。它标记了从GC Root开始直接可达的对象。
	并发标记（Concurrent Marking）。这个阶段从GC Root开始对heap中的对象标记，标记线程与应用程序线程并行执行，并且收集各个Region的存活对象信息。
	最终标记（Remark，STW）。标记那些在并发标记阶段发生变化的对象，将被回收。
	清除垃圾（Cleanup）。清除空Region（没有存活对象的），加入到free list。

35.242: [GC pause (young) (initial-mark), 0.6134240 secs]
   [Parallel Time: 575.3 ms, GC Workers: 18]
      [GC Worker Start (ms): Min: 35243.0, Avg: 35243.2, Max: 35243.3, Diff: 0.3]
      [Ext Root Scanning (ms): Min: 0.7, Avg: 3.4, Max: 5.6, Diff: 4.9, Sum: 62.0]
      [Code Root Marking (ms): Min: 0.0, Avg: 0.0, Max: 0.3, Diff: 0.3, Sum: 0.6]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.2]
         [Processed Buffers: Min: 0, Avg: 2.4, Max: 29, Diff: 29, Sum: 43]
      [Scan RS (ms): Min: 0.0, Avg: 0.1, Max: 0.5, Diff: 0.4, Sum: 1.7]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.2, Diff: 0.2, Sum: 0.5]
      [Object Copy (ms): Min: 564.8, Avg: 567.5, Max: 569.6, Diff: 4.7, Sum: 10214.2]
      [Termination (ms): Min: 0.0, Avg: 4.0, Max: 4.7, Diff: 4.7, Sum: 71.2]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.9]
      [GC Worker Total (ms): Min: 574.9, Avg: 575.1, Max: 575.2, Diff: 0.3, Sum: 10351.3]
      [GC Worker End (ms): Min: 35818.2, Avg: 35818.2, Max: 35818.3, Diff: 0.1]
   [Code Root Fixup: 0.4 ms]
   [Code Root Migration: 1.0 ms]
   [Clear CT: 0.3 ms]
   [Other: 36.4 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 33.6 ms]
      [Ref Enq: 0.3 ms]
      [Free CSet: 1.3 ms]
   [Eden: 551.0M(596.0M)->0.0B(164.0M) Survivors: 8192.0K->63.0M Heap: 899.0M(1007.0M)->403.2M(1007.0M)]
 [Times: user=0.27 sys=2.83, real=0.61 secs] 
35.856: [GC concurrent-root-region-scan-start]
35.860: [GC concurrent-root-region-scan-end, 0.0045800 secs]
35.860: [GC concurrent-mark-start]
35.890: [GC concurrent-mark-end, 0.0299290 secs]
35.891: [GC remark 35.891: [GC ref-proc, 0.0007240 secs], 0.0058240 secs]
 [Times: user=0.06 sys=0.00, real=0.01 secs] 
35.897: [GC cleanup 483M->482M(1007M), 0.0025250 secs]
 [Times: user=0.02 sys=0.00, real=0.00 secs] 
35.899: [GC concurrent-cleanup-start]
35.899: [GC concurrent-cleanup-end, 0.0000580 secs]

第一阶段initial mark是共用了Young GC的暂停，这是因为他们可以复用root scan操作，所以可以说global concurrent marking是伴随Young GC而发生的。
第四阶段Cleanup只是回收了没有存活对象的Region，所以它并不需要STW。

除了以上的参数，G1 GC相关的其他主要的参数有：

[参数含义](http://www.oracle.com/technetwork/cn/articles/java/g1gc-1984535-zhs.html)
-XX:G1ReservePercent=20 设置作为空闲空间的预留内存百分比，以降低目标空间溢出的风险。默认值是 10%。增加或减少百分比时，请确保对总的 Java 堆调整相同的量。Java HotSpot VM build 23 中没有此设置。
-XX:G1HeapRegionSize=n	设置Region大小，并非最终值
-XX:MaxGCPauseMillis	设置G1收集过程目标时间，默认值200ms，不是硬性条件
-XX:G1NewSizePercent	新生代最小值，默认值5%
-XX:G1MaxNewSizePercent	新生代最大值，默认值60%
-XX:ParallelGCThreads	STW期间，并行GC线程数
-XX:ConcGCThreads=n	并发标记阶段，并行执行的线程数
-XX:InitiatingHeapOccupancyPercent	设置触发标记周期的Java堆占用率阈值。默认值是45%。这里的java堆占比指的是non_young_capacity_bytes，包括old+humongous

```