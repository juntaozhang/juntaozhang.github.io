
#### logstash
- [document](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Working with plugins](https://www.elastic.co/guide/en/logstash/current/working-with-plugins.html)
- [github logstash-plugins](https://github.com/logstash-plugins)
- [logstash插件开发](https://www.elastic.co/guide/en/logstash/current/plugin-generator.html)
- [官网filter plugin](https://www.elastic.co/guide/en/logs- tash/current/_how_to_write_a_logstash_filter_plugin.html)
- [手把手教你编写Logstash插件](http://www.cnblogs.com/xing901022/p/5259750.html)
- [logstash中文网Grok正则捕获](http://udn.yyuap.com/doc/logstash-best-practice-cn/filter/grok.html)
- [logstash-patterns-core](https://github.com/logstash-plugins/logstash-patterns-core/tree/master/patterns) // 
[logstash grok 内置正则](https://github.com/elastic/logstash/blob/v1.4.2/patterns/grok-patterns)
- [logstash性能文档](http://kibana.logstash.es/content/logstash/performance/generator.html)

#### [grok debug](http://grokdebug.herokuapp.com/?#)

```
9789123.12 12 209.131.54.138 TCP_HIT/200 4771 GET https://docs.trafficserver.apache.org/en/latest/admin-guide/monitoring/logging/log-formats.en.html?highlight=logformat - NONE/- image/jpeg

%{BASE16FLOAT:cqtq} %{INT:ttms} %{IPORHOST:chi} %{WORD:crc}/%{INT:pssc} %{INT:psql} %{WORD:cqhm} %{URI:cauc} %{USERNAME:caun} %{WORD:phr}/%{ATS_PQSN:pqsn} %{ATS_CONTENT_TYPE:psct}

POSTFIX_QUEUEID [0-9A-F]{10,11}
ATS_CONTENT_TYPE [a-zA-Z0-9._\-/]+
ATS_PQSN [-]+
```

#####logstash-input-redis支持redis-sentinel
[https://github.com/JuntaoZhang/logstash-input-redis](https://github.com/JuntaoZhang/logstash-input-redis)

#####logstash-output-redis支持redis-sentinel
[https://github.com/JuntaoZhang/logstash-output-redis](https://github.com/JuntaoZhang/logstash-output-redis)


#####logstash插件开发

**Running `bundle update` will rebuild your snapshot from scratch, using only
the gems in your Gemfile, which may resolve the conflict.**

```
logstash-filter-example.gemspec 改为=>
  # Gem dependencies
  s.add_runtime_dependency 'logstash-core','>= 2.0.0','< 3.0.0'
  # s.add_runtime_dependency "logstash-core-plugin-api", "~> 2.0"
```

[由于国内网络原因（你懂的），导致 rubygems.org 存放在 Amazon S3 上面的资源文件间歇性连接失败](https://ruby.taobao.org/)


**???Could not find gem 'logstash-devutils' in any of the gem sources listed in your Gemfile or available on this machine.**

```
MacBookPro:logstash-filter-example juntao$ sudo gem install logstash-devutils
Password:
ERROR:  Could not find a valid gem 'logstash-devutils' (>= 0), here is why:
          Found logstash-devutils (1.0.2), but was for platform java
ERROR:  Possible alternatives: logstash-input-xls, logstash-output-xls, logstash-input-sqs, logstash-input-irc, logstash-input-rss
```


###性能调优

```
-w, --pipeline-workers COUNT  Sets the number of pipeline workers to run.
   (default: 40 逻辑cpu数量)
-b, --pipeline-batch-size SIZE Size of batches the pipeline is to work in.
   (default: 125)
                                   
logstash agent -f conf/test.conf -r -w 40 -b 1000 | pv -btr > /dev/null

测试下来只有 stdout 30K/s, output file 7k/s , redis 3~4k/s , kafka 与 stdout差不多的性能
```


###es remove_field
```
    elasticsearch {
         remove_field => ["remote_user","request_method", "message","@version","path","time_local","request_url","http_version","http_referer","http_user_agent","http_x_forwarded_for","connection","remote_addr_main" ]
         hosts => ["219.144.80.196:19200"]
         template => "/home/logstash/elasticsearch-template.json"
         template_overwrite => true
    }
```

###redis修改
```
    redis {
               data_type=>"list"
               db=>2
        batch=> true
        batch_events=> 100
               key=> "logstash-%{type}"
               sentinel_hosts=>["192.168.1.158:26379","192.168.1.156:26379"]
               password=>"redis!QAZxsw2"
   }
```

###es remove_field
```
logstash-2.4.0/vendor/bundle/jruby/1.9/gems/logstash-output-elasticsearch-2.7.1-java/lib/logstash/outputs/elasticsearch.rb

  config :timeout, :validate => :number                                                                                                                                                                                                                       
  config :remove_field, :validate => :array, :default => []
  
event_action_params
 @remove_field.each do |field|
        field = event.sprintf(field) 
        @logger.debug? and @logger.debug("filters/#{self.class.name}: removing field",                                                                                                                                                                       
                                         :field => field)                                                                                                                                                                                                     
        event.remove(field)                                                                                                                                                                                                                                   
      end

vim logstash-2.4.0/vendor/bundle/jruby/1.9/gems/logstash-output-elasticsearch-2.7.1-java/lib/logstash/outputs/elasticsearch/common.rb

```
