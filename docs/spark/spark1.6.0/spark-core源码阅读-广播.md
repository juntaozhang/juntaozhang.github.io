# spark-core源码阅读-广播

## 前言
(spark core源码阅读-Task介绍(六))我们讨论过`submitMissingTasks`,期间谈到broadcast,当时只是说把序列化过得taskBytes
广播到出去,这里详细讨论广播实现原理

BroadcastManager在SparkEnv初始化时被实例化,实例化中`initialize`实例化`BroadcastFactory`,默认是`TorrentBroadcastFactory`

- BroadcastFactory类别
  
  - TorrentBroadcastFactory
    对应TorrentBroadcast
  - HttpBroadcastFactory
    对应HttpBroadcast

- BroadcastFactory主要两个方法:

  - newBroadcast:创建广播数据变量
    `SparkContext.broadcast`广播数据
  - unbroadcast:删除广播数据
    `registerBroadcastForCleanup=>doCleanupBroadcast=>unbroadcast`
    `registerBroadcastForCleanup`用到知识点见`WeakReference(弱引用)与WeakHashMap`虚引用(PhantomReference)类似

- Broadcast
  广播变量,通过BroadcastFactory创建出来
  
  ```txt
  scala> val broadcastVar = sc.broadcast(Array(1, 2, 3))
  broadcastVar: org.apache.spark.broadcast.Broadcast[Array[Int]] = Broadcast(0)
  scala> broadcastVar.value
  res0: Array[Int] = Array(1, 2, 3)
  ```


## 如何触发BroadcastFactory方法

1.`SparkContext.broadcast`
```scala
  def broadcast[T: ClassTag](value: T): Broadcast[T] = {
    assertNotStopped()
    if (classOf[RDD[_]].isAssignableFrom(classTag[T].runtimeClass)) {
      // This is a warning instead of an exception in order to avoid breaking user programs that
      // might have created RDD broadcast variables but not used them:
      logWarning("Can not directly broadcast RDDs; instead, call collect() and "
        + "broadcast the result (see SPARK-5063)")
    }
    //创建
    val bc = env.broadcastManager.newBroadcast[T](value, isLocal)
    val callSite = getCallSite
    logInfo("Created broadcast " + bc.id + " from " + callSite.shortForm)
    cleaner.foreach(_.registerBroadcastForCleanup(bc))
    bc
  }
```

2.弱引用CleanupTask,每次GC时如果broadcast变量没有强引用,则回收`CleanupTaskWeakReference`,并添加到`referenceQueue`

```scala
  def registerBroadcastForCleanup[T](broadcast: Broadcast[T]): Unit = {
    registerForCleanup(broadcast, CleanBroadcast(broadcast.id))
  }
  private def registerForCleanup(objectForCleanup: AnyRef, task: CleanupTask): Unit = {
    referenceBuffer += new CleanupTaskWeakReference(task, objectForCleanup, referenceQueue)
  }
  
  private class CleanupTaskWeakReference(
      val task: CleanupTask,
      referent: AnyRef,
      referenceQueue: ReferenceQueue[AnyRef])
    extends WeakReference(referent, referenceQueue)
```

3.注册清理线程,`keepCleaning`,不断处理`referenceQueue`队列task,当没有task时,timeout 100ms
```scala
  private val cleaningThread = new Thread() { override def run() { keepCleaning() }}
  private def keepCleaning(): Unit = Utils.tryOrStopSparkContext(sc) {
    while (!stopped) {
      try {
        val reference = Option(referenceQueue.remove(ContextCleaner.REF_QUEUE_POLL_TIMEOUT))
          .map(_.asInstanceOf[CleanupTaskWeakReference])
        // Synchronize here to avoid being interrupted on stop()
        synchronized {
          reference.map(_.task).foreach { task =>
            logDebug("Got cleaning task " + task)
            referenceBuffer -= reference.get
            task match {
              case CleanRDD(rddId) =>
                doCleanupRDD(rddId, blocking = blockOnCleanupTasks)
              case CleanShuffle(shuffleId) =>
                doCleanupShuffle(shuffleId, blocking = blockOnShuffleCleanupTasks)
              case CleanBroadcast(broadcastId) =>
                doCleanupBroadcast(broadcastId, blocking = blockOnCleanupTasks)
              case CleanAccum(accId) =>
                doCleanupAccum(accId, blocking = blockOnCleanupTasks)
              case CleanCheckpoint(rddId) =>
                doCleanCheckpoint(rddId)
            }
          }
        }
      } catch {
        case ie: InterruptedException if stopped => // ignore
        case e: Exception => logError("Error in cleaning thread", e)
      }
    }
  }
```

4.真正的broadcast变量清除逻辑

```scala
  /** Perform broadcast cleanup. */
  def doCleanupBroadcast(broadcastId: Long, blocking: Boolean): Unit = {
    try {
      logDebug(s"Cleaning broadcast $broadcastId")
      broadcastManager.unbroadcast(broadcastId, true, blocking)
      listeners.foreach(_.broadcastCleaned(broadcastId))
      logDebug(s"Cleaned broadcast $broadcastId")
    } catch {
      case e: Exception => logError("Error cleaning broadcast " + broadcastId, e)
    }
  }
```

## HttpBroadcast
### 1.write
`value_`为暂存变量,即序列化时跳过

```scala
private[spark] class HttpBroadcast[T: ClassTag](
    @transient var value_ : T, isLocal: Boolean, id: Long)
  extends Broadcast[T](id) with Logging with Serializable 
```

Driver端:
HttpBroadcast对象初始化中把需要广播数据写入临时文件`HttpBroadcast.write(id, value_)`,write代码如下:

```scala
  private def write(id: Long, value: Any) {
    val file = getFile(id)
    val fileOutputStream = new FileOutputStream(file)
    Utils.tryWithSafeFinally {
      val out: OutputStream = {
        if (compress) {
          compressionCodec.compressedOutputStream(fileOutputStream)
        } else {
          new BufferedOutputStream(fileOutputStream, bufferSize)
        }
      }
      val ser = SparkEnv.get.serializer.newInstance()
      val serOut = ser.serializeStream(out)
      Utils.tryWithSafeFinally {
        serOut.writeObject(value)
      } {
        serOut.close()
      }
      files += file
    } {
      fileOutputStream.close()
    }
  }
```

在单例对象初始化过程中,创建httpServer(这是一个基于jetty的服务端)

```scala
  private def createServer(conf: SparkConf) {
    broadcastDir = Utils.createTempDir(Utils.getLocalDir(conf), "broadcast")
    val broadcastPort = conf.getInt("spark.broadcast.port", 0)
    server =
      new HttpServer(conf, broadcastDir, securityManager, broadcastPort, "HTTP broadcast server")
    server.start()
    serverUri = server.uri
    logInfo("Broadcast server started at " + serverUri)
  }
```
### 2.read
Executor端:
通过反序列化获取对象`readObject`,从下面代码可以看出先调用`defaultReadObject`序列化非transient变量,
`value_`先通过blockManager从内从中获取该变量如果没有则通过    `HttpBroadcast.read`获取

```scala
  /** Used by the JVM when deserializing this object. */
  private def readObject(in: ObjectInputStream): Unit = Utils.tryOrIOException {
    in.defaultReadObject()
    HttpBroadcast.synchronized {
      SparkEnv.get.blockManager.getSingle(blockId) match {
        case Some(x) => value_ = x.asInstanceOf[T]
        case None => {
          logInfo("Started reading broadcast variable " + id)
          val start = System.nanoTime
          value_ = HttpBroadcast.read[T](id)
          /*
           * We cache broadcast data in the BlockManager so that subsequent tasks using it
           * do not need to re-fetch. This data is only used locally and no other node
           * needs to fetch this block, so we don't notify the master.
           */
          SparkEnv.get.blockManager.putSingle(
            blockId, value_, StorageLevel.MEMORY_AND_DISK, tellMaster = false)
          val time = (System.nanoTime - start) / 1e9
          logInfo("Reading broadcast variable " + id + " took " + time + " s")
        }
      }
    }
  }
```

从driver端http server中通过http获取该数据

```scala
  private def read[T: ClassTag](id: Long): T = {
    logDebug("broadcast read server: " + serverUri + " id: broadcast-" + id)
    val url = serverUri + "/" + BroadcastBlockId(id).name

    var uc: URLConnection = null
    if (securityManager.isAuthenticationEnabled()) {
      logDebug("broadcast security enabled")
      val newuri = Utils.constructURIForAuthentication(new URI(url), securityManager)
      uc = newuri.toURL.openConnection()
      uc.setConnectTimeout(httpReadTimeout)
      uc.setAllowUserInteraction(false)
    } else {
      logDebug("broadcast not using security")
      uc = new URL(url).openConnection()
      uc.setConnectTimeout(httpReadTimeout)
    }
    Utils.setupSecureURLConnection(uc, securityManager)

    val in = {
      uc.setReadTimeout(httpReadTimeout)
      val inputStream = uc.getInputStream
      if (compress) {
        compressionCodec.compressedInputStream(inputStream)
      } else {
        new BufferedInputStream(inputStream, bufferSize)
      }
    }
    val ser = SparkEnv.get.serializer.newInstance()
    val serIn = ser.deserializeStream(in)
    Utils.tryWithSafeFinally {
      serIn.readObject[T]()
    } {
      serIn.close()
    }
  }
```

### 3.delete
首先通过BlockManagerMaster删除内存中数据,然后判断是否是driver端,driver端删除http server中该对象文件

```scala
  def unpersist(id: Long, removeFromDriver: Boolean, blocking: Boolean): Unit = synchronized {
    SparkEnv.get.blockManager.master.removeBroadcast(id, removeFromDriver, blocking)
    if (removeFromDriver) {
      val file = getFile(id)
      files.remove(file)
      deleteBroadcastFile(file)
    }
  }
```

## TorrentBroadcast
### 1.write

(1) `private val numBlocks: Int = writeBlocks(obj)`
(2) 先写入本地cache,不需要告诉`BlockManagerMaster`,然后把该对象切块,为了传播该块数组,则需要告诉`BlockManagerMaster`
```scala
  /**
   * Divide the object into multiple blocks and put those blocks in the block manager.
   * @param value the object to divide
   * @return number of blocks this broadcast variable is divided into
   */
  private def writeBlocks(value: T): Int = {
    // 写driver cache
    // Store a copy of the broadcast variable in the driver so that tasks run on the driver
    // do not create a duplicate copy of the broadcast variable's value.
    SparkEnv.get.blockManager.putSingle(broadcastId, value, StorageLevel.MEMORY_AND_DISK,
      tellMaster = false)
    // 分块
    val blocks =
      TorrentBroadcast.blockifyObject(value, blockSize, SparkEnv.get.serializer, compressionCodec)
    // 把块写入cache,并告知cache master
    blocks.zipWithIndex.foreach { case (block, i) =>
      SparkEnv.get.blockManager.putBytes(
        BroadcastBlockId(id, "piece" + i),
        block,
        StorageLevel.MEMORY_AND_DISK_SER,
        tellMaster = true)
    }
    blocks.length
  }
```

### 2.read
(1) `@transient private lazy val _value: T = readBroadcastBlock()`

(2) 先从本地缓存获取,如果不存在从集群内存中获取根据写时的分片,从不同的节点获取分片

```scala
  // 获取block value
  private def readBroadcastBlock(): T = Utils.tryOrIOException {
    TorrentBroadcast.synchronized {
      setConf(SparkEnv.get.conf)
      // 先从本地缓存获取
      SparkEnv.get.blockManager.getLocal(broadcastId).map(_.data.next()) match {
        case Some(x) =>
          x.asInstanceOf[T]

        case None =>
          logInfo("Started reading broadcast variable " + id)
          val startTimeMs = System.currentTimeMillis()
          // 如果不存在从集群内存中获取根据写时的分片,从不同的节点获取分片
          val blocks = readBlocks()
          logInfo("Reading broadcast variable " + id + " took" + Utils.getUsedTimeMs(startTimeMs))
          // 把分割的objects数组合并广播对象
          val obj = TorrentBroadcast.unBlockifyObject[T](
            blocks, SparkEnv.get.serializer, compressionCodec)
          // Store the merged copy in BlockManager so other tasks on this executor don't
          // need to re-fetch it.
          // 写回本地内存
          SparkEnv.get.blockManager.putSingle(
            broadcastId, obj, StorageLevel.MEMORY_AND_DISK, tellMaster = false)
          obj
      }
    }
  }
```

(3) 远程从其他节点获取块

```scala
  /** Fetch torrent blocks from the driver and/or other executors. */
  private def readBlocks(): Array[ByteBuffer] = {
    // Fetch chunks of data. Note that all these chunks are stored in the BlockManager and reported
    // to the driver, so other executors can pull these chunks from this executor as well.
    val blocks = new Array[ByteBuffer](numBlocks)
    val bm = SparkEnv.get.blockManager

    //
    for (pid <- Random.shuffle(Seq.range(0, numBlocks))) {
      val pieceId = BroadcastBlockId(id, "piece" + pid)
      logDebug(s"Reading piece $pieceId of $broadcastId")
      // First try getLocalBytes because there is a chance that previous attempts to fetch the
      // broadcast blocks have already fetched some of the blocks. In that case, some blocks
      // would be available locally (on this executor).
      def getLocal: Option[ByteBuffer] = bm.getLocalBytes(pieceId)
      def getRemote: Option[ByteBuffer] = bm.getRemoteBytes(pieceId).map { block =>
        // If we found the block from remote executors/driver's BlockManager, put the block
        // in this executor's BlockManager.
        SparkEnv.get.blockManager.putBytes(
          pieceId,
          block,
          StorageLevel.MEMORY_AND_DISK_SER,
          tellMaster = true)
        block
      }
      val block: ByteBuffer = getLocal.orElse(getRemote).getOrElse(
        throw new SparkException(s"Failed to get $pieceId of $broadcastId"))
      blocks(pid) = block
    }
    blocks
  }
```

### 3.delete

 (1) `TorrentBroadcastFactory`中unbroadcast,调用`TorrentBroadcast.unpersist`
 
```scala
   /**
    * Remove all persisted blocks associated with this torrent broadcast on the executors.
    * If removeFromDriver is true, also remove these persisted blocks on the driver.
    */
   def unpersist(id: Long, removeFromDriver: Boolean, blocking: Boolean): Unit = {
     logDebug(s"Unpersisting TorrentBroadcast $id")
     SparkEnv.get.blockManager.master.removeBroadcast(id, removeFromDriver, blocking)
   }
```
 
 (2) BlockManager中删除Broadcast变量,包含该变量的块
 
```scala
   def removeBroadcast(broadcastId: Long, tellMaster: Boolean): Int = {
     logDebug(s"Removing broadcast $broadcastId")
     val blocksToRemove = blockInfo.keys.collect {
       case bid @ BroadcastBlockId(`broadcastId`, _) => bid
     }
     blocksToRemove.foreach { blockId => removeBlock(blockId, tellMaster) }
     blocksToRemove.size
   }
```
 
 