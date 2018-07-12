

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