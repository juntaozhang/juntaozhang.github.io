# spark core源码阅读-Storage shuffle(八)

本节主要介绍`RDD.aggregateByKey`导致的shuffle,`ShuffleMapTask`中Task如何处理`rdd.iterator`,
shuffle中Map端如何根据根据分区把数据写入文件.

## 主要类简述
- ShuffleManager
 Driver/Executors创建SparkEnv时创建该类,不同的ShuffleManager对应不同ShuffleWriter,
 通过Driver通过`spark.shuffle.manager`指定使用策略
 
 三个重要方法:
 
  `registerShuffle`:driver端初始化`ShuffleDependency`生成
  `getWriter`:在map任务中生成shuffle后的文件
  `getReader`:在reduce任务中获取，以从mappers中读取被组合的记录
 
 分类:
 
 (1) SortShuffleManager
 
   default,在基于排序的shuffle中，rdd iter records将根据其目标分区ID进行排序写入单个map输出文件.
   reducer获取此文件的连续区域以便阅读他们的map输出部分。 在map输出数据太大而不适合缓存的情况下
   排序的输出子集可以被分散到磁盘上，并且这些磁盘上的文件被合并生成最终的输出文件。
  
 (2) HashShuffleManager
    不需要排序的场景(reduce端不会基于排序处理相应逻辑),直接通过hash分区输出shuffle文件
    
- ShuffleHandle
  `registerShuffle`,根据不同场景选择不同`ShuffleWriter`
  
- ShuffleWriter
  
  SortShuffleManager根据不同`ShuffleHandle`有以下选择:
    
    - SortShuffleWriter
      
      最终生成一个文件
      
    - UnsafeShuffleWriter TODO
    - BypassMergeSortShuffleWriter TODO
  
  HashShuffleManager=>HashShuffleWriter
    每个分区一个临时文件,完事之后重新命名
  

- BlockStoreShuffleReader
  reducer在`ShuffledRDD.compute getReader`调用`read()`方法
  该方法中:
    
    - 通过`ShuffleBlockFetcherIterator`远程获取各个map端相应的分区数据
    - 需要map端合并,`combineCombinersByKey`,使用`ExternalAppendOnlyMap.insertAll`数据结构缓存,内存不足溢出文件
    - 需要排序,`ExternalSorter.insertAll`循环排序
    - `sorter.iterator`合并数据集(如果有多个溢出文件,合并成一个)
    - 返回`CompletionIterator`
  

- ExternalSorter

  通过Partitioner把key分区,在每个分区内排序,为每个分区输出单个分区文件

- IndexShuffleBlockResolver

  逻辑块与物理文件Mapping,生成数据文件块索引文件
  

- PartitionedAppendOnlyMap

    具有以下特征:
    
    - AppendOnlyMap 内存存储,只能添加
    - WritablePartitionedPairCollection
    
      - 键值对都有分区信息
      - 支持高效的内存排序iter
      - 支持WritablePartitionedIterator直接以字节形式写入内容


- Sorter
  TimSort:是结合了合并排序(merge sort)和插入排序(insertion sort)而得出的排序算法

![Storage-shuffle.png](img/Storage-shuffle.png)



## 主要方法

- `insertAll`

  ![ExternalSorter.insertAll.png](img/ExternalSorter.insertAll.png)

    迭代器循环处理数据
    内存不足溢出到磁盘
  
  ```scala
  // Combine values in-memory first using our AppendOnlyMap
  val mergeValue = aggregator.get.mergeValue
  val createCombiner = aggregator.get.createCombiner
  var kv: Product2[K, V] = null
  val update = (hadValue: Boolean, oldValue: C) => {
    if (hadValue) mergeValue(oldValue, kv._2) else createCombiner(kv._2)
  }
  while (records.hasNext) {
    addElementsRead()
    kv = records.next()
    // map=>PartitionedAppendOnlyMap
    map.changeValue((getPartition(kv._1), kv._1), update)
    // 内存不足溢出到磁盘
    maybeSpillCollection(usingMap = true)
  }
  ```
- `writePartitionedFile`

  将所有添加到ExternalSorter中的数据写入磁盘存储中的文件

![writePartitionedFile.png](img/writePartitionedFile.png)


**TODO**
- [TimSort](http://blog.csdn.net/yangzhongblog/article/details/8184707) JDK ComparableTimSort
- RoaringBitmap