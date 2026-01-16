
# HDFS
- 一个文件按照 256MB 分割 成为 block
- 每个 block 会被复制到多个节点上，默认是 3 个（hadoop 2）
```text
原始数据: [DATA]
存储方式: [DATA] + [DATA] + [DATA]  (3份完整副本)
存储开销: 300%
可容忍故障: 2个副本损坏 (保留1份完好即可)
```

hadoop 3 引入了纠删码(Erasure Coding)
```text
原始数据: [D1][D2][D3][D4][D5][D6] (6个数据块)
编码生成: [P1][P2][P3] (3个校验块)
存储方式: [D1][D2][D3][D4][D5][D6][P1][P2][P3]
存储开销: 150% (9/6=1.5)
可容忍故障: 任意3个块损坏 (数据块或校验块)
```

## API
hdfs upload/download/rm
```
  private def rmHdfsFile(sqlContext: SQLContext, hdfsFile: String): Unit = {
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    val hdfsPath = new Path(hdfsFile)
    val fs = hdfsPath.getFileSystem(hadoopConf)
    fs.deleteOnExit(hdfsPath)
  }

  private def copyToHdfs(sqlContext: SQLContext, fileLocation: String, hdfsTemp: String): String = {
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    val hdfsPath = new Path(fileLocation)
    val fs = hdfsPath.getFileSystem(hadoopConf)
    fs.mkdirs(new Path(hdfsTemp))
    fs.copyFromLocalFile(new Path(fileLocation), new Path(hdfsTemp))
    val filePath = hdfsTemp + "/" + hdfsPath.getName
    fs.deleteOnExit(new Path(filePath))
    filePath
  }

  private def copyFromHdfs(sqlContext: SQLContext, hdfsTemp: String,
    fileLocation: String): String = {
    val hadoopConf = sqlContext.sparkContext.hadoopConfiguration
    val hdfsPath = new Path(hdfsTemp)
    val fs = hdfsPath.getFileSystem(hadoopConf)
    fs.copyToLocalFile(new Path(hdfsTemp), new Path(fileLocation))
    fs.deleteOnExit(new Path(hdfsTemp))
    fileLocation
  }
```

## scala download file

```
  def downloadFile(url: String, filename: String): Unit = {
    import java.net.URL

    import sys.process._
    new URL(url) #> new File(filename) !!
  }
```

## scala create dir
```
  def createDirectory(root: String, namePrefix: String = "data-engine"): File = {
    var attempts = 0
    val maxAttempts = 3
    var dir: File = null
    while (dir == null) {
      attempts += 1
      if (attempts > maxAttempts) {
        throw new IOException("Failed to create a temp directory (under " + root + ") after " +
          maxAttempts + " attempts!")
      }
      try {
        dir = new File(root, namePrefix + "-" + UUID.randomUUID.toString)
        if (dir.exists() || !dir.mkdirs()) {
          dir = null
        }
      } catch {
        case e: SecurityException => dir = null;
      }
    }

    dir
  }
```


hadoop distcp -pb -update -delete /user/dataengine/tmp/get-pip.py hdfs://bd041-025.yzdns.com/user/dataengine/tmp/get-pip.py
hadoop distcp -pb -update -delete /user/dataengine/tmp/get-pip.py hdfs://bd041-041.yzdns.com/user/dataengine/tmp/get-pip.py

hadoop fs -test -e hdfs://bd041-025.yzdns.com/
