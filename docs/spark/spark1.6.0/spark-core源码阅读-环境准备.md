#spark-core源码阅读-环境准备
基于1.6.0版本结合源码理解spark on yarn

##环境准备
下载代码: https://github.com/juntaozhang/spark/tree/my.v1.6.0
>git clone https://github.com/juntaozhang/spark.git  
git checkout my.v1.6.0  
mvn generate-resources generate-test-sources  

* 1.open spark/pom.xml with IDEA
* 2.generate spark/external/flume-sink/src/main/avro 
* 3.make “target/scala-2.10/src_managed/main/compiled_avro” as a source path
* 4.then you can run "Build -> Rebuild Project" in IDEA.
* 5.run org.apache.spark.examples.sql.JsonDemo

##spark组件
* spark core, spark 内核
* spark streaming, spark流计算(基于batch方式)
* spark sql
* MLlib, 机器学习lib库
* GraphX, 图计算
* SparkR, 与R语言结合

##重要概念
### RDDs(Resilient Distributed Datasets)

弹性分布式数据集, 首先是数据集合,这些数据分布在集群中, 其次RDD是对这些数据的一个抽象概念,所以RDD是有各种基本操作如map,filter,persist,而且有各种子类如KafkaRDD,JdbcRDD,NewHadoopRDD等
RDD有几个特性:

  - 分区集合
  - 计算每个块的函数
  - 依赖其他RDDs列表
  - 可选, key-value RDDs分区
  - 可选, 计算每个块首选位置(类似于机架感知,最快找到数据块)
    
### Operation
RDDs操作方法可以分为两类:

- [Transformation](http://spark.apache.org/docs/latest/rdd-programming-guide.html#transformations),如map/flatMap,reduceByKey/groupBy/join,transformation也分两种:
	- map这一类不会触发shuffle
	- reduceByKey会触发shuffle
	
- [Action](http://spark.apache.org/docs/latest/rdd-programming-guide.html#actions),如reduce/saveAsTextFile,这些Action将启动一个作业

### Shuffle
与hadoop中的Shuffle概念上很相似,本质上都是将 Map 端获得的数据使用分区器进行划分，并将数据发送给对应的Reducer 的过程,Shuffle是一种很昂贵的操作,涉及到disk I/O,数据序列化,network I/O,内存;shffle后面我们详细分类那些操作触发shuffle

### [RDD Persistence](http://spark.apache.org/docs/latest/rdd-programming-guide.html#rdd-persistence)
RDD.persist触发每个node把所有计算出来的分区保存在内存,在其他操作中能立即使用,这个操作在效率和容错上都发挥很大作用,cache可以保存在disk或内存,是否序列化来节省更多空间

### [Shared Variables](http://spark.apache.org/docs/latest/rdd-programming-guide.html#shared-variables)
有两种类型的共享变量
#### Broadcast Variables
只读变量,这里的变量是一个数据集合,这些数据被cache到每个节点,数据是被广播出去的,这种实现原理导致数据集不可能太大,这种方式很高效,避免shuffle任务

#### Accumulators
```scala
scala> val accum = sc.accumulator(0, "My Accumulator")
accum: spark.Accumulator[Int] = 0

scala> sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum += x)
...
10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

scala> accum.value
res2: Int = 10
```
累加器可以简单实现累加功能,通过下图可以很容易理解他的实现原理:

![这里写图片描述](http://spark.apache.org/docs/latest/img/spark-webui-accumulators.png)
每个节点自己计算,最后合并,这与MapReduce功能一样


##参考资料
- Apache Spark源码剖析
- [github SparkInternals](https://github.com/JerryLead/SparkInternals)
- http://spark.apache.org/docs/latest

