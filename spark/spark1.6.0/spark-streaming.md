# 关于spark streaming执行逻辑分析:

## 1.steaming生成的相关性
spark streaming 通过`DStream.register`判断是否是`outputStreams`(DStreamGraph中),	
上封邮件分析过`DStreamGraph.generateJobs`,主要是根据outputStreams生成job(早晨我说的两个steaming如果是无关的也会等待是错的),  
应该是根据outputStreams生成/提交job,和inputsteams无关

```
  def generateJobs(time: Time): Seq[Job] = {
    logDebug("Generating jobs for time " + time)
    val jobs = this.synchronized {
      outputStreams.flatMap { outputStream =>	--------------------------------- 这里就可以看出来
        val jobOption = outputStream.generateJob(time)
        jobOption.foreach(_.setCallSite(outputStream.creationSite))
        jobOption
      }
    }
    logDebug("Generated " + jobs.length + " jobs for time " + time)
    jobs
  }
```

## 2.`inputsteams`与`outputStreams`相关性

DSteam中再生成job的时候,调用`getOrCompute`,获取rdd,如果没有会根据不同类型的DStream`compute`		

```
  private[streaming] def generateJob(time: Time): Option[Job] = {
    getOrCompute(time) match {---------------------------------------------------------------所有DStreams的之间依赖都在getOrCompute这个方法获取
      case Some(rdd) =>
        val jobFunc = () => {
          val emptyFunc = { (iterator: Iterator[T]) => {} }
          context.sparkContext.runJob(rdd, emptyFunc)
        }
        Some(new Job(time, jobFunc))
      case None => None
    }
  }

  private[streaming] final def getOrCompute(time: Time): Option[RDD[T]] = {
    // If RDD was already generated, then retrieve it from HashMap,
    // or else compute the RDD
    generatedRDDs.get(time).orElse {
      // Compute the RDD if time is valid (e.g. correct time in a sliding window)
      // of RDD generation, else generate nothing.
      if (isTimeValid(time)) {

        val rddOption = createRDDWithLocalProperties(time, displayInnerRDDOps = false) {
          // Disable checks for existing output directories in jobs launched by the streaming
          // scheduler, since we may need to write output to an existing directory during checkpoint
          // recovery; see SPARK-4835 for more details. We need to have this call here because
          // compute() might cause Spark jobs to be launched.
          PairRDDFunctions.disableOutputSpecValidation.withValue(true) {
            compute(time)
          }
        }

        rddOption.foreach { case newRDD =>
          // Register the generated RDD for caching and checkpointing
          if (storageLevel != StorageLevel.NONE) {
            newRDD.persist(storageLevel)
            logDebug(s"Persisting RDD ${newRDD.id} for time $time to $storageLevel")
          }
          if (checkpointDuration != null && (time - zeroTime).isMultipleOf(checkpointDuration)) {
            newRDD.checkpoint()
            logInfo(s"Marking RDD ${newRDD.id} for time $time for checkpointing")
          }
          generatedRDDs.put(time, newRDD)
        }
        rddOption
      } else {
        None
      }
    }
  }
```

我们假设两个kafka DStream join,这时候生成两个`DirectKafkaInputDStream`, join的时候会生成`TransformedDStream`,该DStream会关联		
parent streams,`compute`的时候遍历获取parent rdd,这里就和上封邮件衔接起来了,`DirectKafkaInputDStream`会获取当前spark与kafka的offset确定数据范围,	
最后提交job

```
  def join[W: ClassTag](
      other: DStream[(K, W)],
      partitioner: Partitioner
    ): DStream[(K, (V, W))] = ssc.withScope {
    self.transformWith(
      other,
      (rdd1: RDD[(K, V)], rdd2: RDD[(K, W)]) => rdd1.join(rdd2, partitioner)
    )
  }

  override def compute(validTime: Time): Option[RDD[U]] = {
    val parentRDDs = parents.map { parent => parent.getOrCompute(validTime).getOrElse(
      // Guard out against parent DStream that return None instead of Some(rdd) to avoid NPE
      throw new SparkException(s"Couldn't generate RDD from parent at time $validTime"))
    }
    val transformedRDD = transformFunc(parentRDDs, validTime)
    if (transformedRDD == null) {
      throw new SparkException("Transform function must not return null. " +
        "Return SparkContext.emptyRDD() instead to represent no element " +
        "as the result of transformation.")
    }
    Some(transformedRDD)
  }
```





