# prometheus

## [grafana](http://10.6.161.11:3000/?orgId=1)

## [prometheus for spark](http://10.6.161.11:9089/)


## [pushgateway](http://10.6.161.11:9091/#)


>echo "zhangjt 3.14" | curl --data-binary @- http://10.6.161.11:9091/metrics/job/test

```
cat <<EOF | curl --data-binary @- http://10.6.161.11:9091/metrics/job/test/instance/spark
# TYPE some_metric counter
zhangjt1{label="val1"} 42
# TYPE another_metric gauge
# HELP another_metric Just an example.
zhangjt2 2398.283
EOF

```


> curl -X DELETE http://10.6.161.11:9091/metrics/job/test/instance/spark

## DOC
- https://my.oschina.net/wangkangluo1/blog/753160
- https://www.bookstack.cn/read/prometheus_practice/pushgateway-how.md
