
#####ruby

```
NoMethodError: undefined method `getIpInfo' for Java::ComXipudataCdn::IpEnrich:Class
getIpInfo为private方法导致
```
**直接引用jar**

```
require './hi.jar'
msg = Java::Hello.new.hi("ruby")
puts msg
puts 'Hello, Ruby!'
```

**ruby使用rjb调用java代码**

```
require 'rjb'
#加载jar包
Rjb::load(classpath = '/home/deployer/DmCodec.jar', jvmargs=[])
#new一个对象
DmCodec = Rjb::import('com.zapya.DmCodec').new
#调用实例方法
tmp = DmCodec.encodeB62("aaa")
```



**sentinel.rb**

>gem install redis

```
require 'redis'

Sentinels = [{:host => "127.0.0.1", :port => 26380}]
r = Redis.new(:url => "redis://mymaster", :sentinels => Sentinels, :role => :master,:password=>'123')

puts r.get("name")
r.set("name","zhangjt")
puts r.get("name")
```