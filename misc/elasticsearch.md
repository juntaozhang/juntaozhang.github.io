# elasticsearch

### elasticsearch优化索引
[elasticsearch config](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-network.html)
[elasticsearch 优化](http://www.jianshu.com/users/92a1227beb27/latest_articles)


### mapping问题

#### get all mapping
- `http://1.82.228.134:19202/_all/_mapping`


#### get indices types
- `http://1.82.228.134:19202/logs-2017.04.07/_mapping/nginx`
- `http://1.82.228.134:19202/error-logs-2017.04.05`

### keyword vs text
```
keysword:Keyword fields are only searchable by their exact value.
text:会分词

```

```

curl -XGET '192.168.0.134:9201/_analyze' -d '
{
  "analyzer" : "standard",
  "text" : "this is a test"
}'

curl -XGET '192.168.0.134:9201/_analyze' -d '
{
  "tokenizer" : "keyword",
  "filter" : ["lowercase"],
  "text" : "this Is a test"
}'


```


### docs
- [官网 multiple_indices_and_types](https://www.elastic.co/guide/en/elasticsearch/reference/5.2/indices-get-mapping.html#_multiple_indices_and_types)
