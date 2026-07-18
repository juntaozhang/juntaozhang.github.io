
## redis 集群分类
- 主从复制：一主多从，异步复制，依赖 Sentinel 提供高可用
  - 哨兵模式：提供HA
- 集群模式：把数据自动分片到多个主节点，每个主可以有 0～N 个从节点
  - 官方的“多主 + 自动分片 + 自动故障转移”一体化方案

### 集群模式
固定 16384 哈希槽

## redis 使用

SCAN查找:`SCAN 0 MATCH 1478755240* SIZE 100`

`FLUSHDB`:清空当前库

`DBSIZE`:db key number

redis-cli -p 26179 -a 'pass' -h 192.168.1.156 -n {Database number}





python

```
>>> import redis
>>> pool = redis.ConnectionPool(host='127.0.0.1', port=6379)
>>> r = redis.Redis(connection_pool=pool)
>>> r.set('foo', 'Bar')
True
>>> print r.get('foo')
Bar
>>>
```


python sentinel

```
from redis.sentinel import Sentinel
sentinel = Sentinel(sentinels=[('192.168.0.131', 26379),('192.168.0.134', 26379),('192.168.0.136', 26379)], socket_timeout=0.1)
sentinel.discover_master(service_name='mymaster')
master = sentinel.master_for(service_name='mymaster', socket_timeout=0.1, password='123')
master.set('foo', 'bar')
master.get('foo')

slave = sentinel.slave_for('mymaster', socket_timeout=0.1, password='123')
slave.get('foo')
```


go client

https://objectrocket.com/docs/redis_go_examples.html