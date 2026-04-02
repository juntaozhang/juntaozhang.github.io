## Kafka 架构缺点

- IO 浪费与计算冗余（行存 vs 列存）
- 大 State 作业的稳定性与运维难
  - Checkpoint 时间过长，容易超时失败
  - 双流 join 时，需要维护一个大状态，导致内存占用高。
  - 升级困难，修改了字段等
- 数据探查困难
  - Flink State 黑盒，无法排查状态
- 数据订正困难
  - kafka 数据只能append，无法update
  - 下游 replace
  - 下游 ignore 数据
  - 下游无去重逻辑




## Reference
- https://www.bilibili.com/video/BV1faHUz3EdU