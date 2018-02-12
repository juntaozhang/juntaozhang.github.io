## Task

一个执行单位, Spark中有两种Task:
Spark工作由一个或多个阶段组成, 作业的最后一个阶段由多个ResultTasks组成，而早期的阶段由ShuffleMapTasks组成

- ShuffleMapTask
ShuffleMapTask执行任务并将任务输出分为多个桶（基于任务的分区程序）

- ResultTask
ResultTask执行任务并将任务输出发送回驱动程序应用程序。 

### TaskScheduler
driver执行


### task提交

`submitMissingTasks`

`stage.findMissingPartitions`:首先找出要计算的分区id的索引

