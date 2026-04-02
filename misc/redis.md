#redis 使用

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