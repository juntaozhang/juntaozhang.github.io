#####mongo 使用

```
db.coll_bw.aggregate([
  { $match:{ time:{$gte:1472704442} } },
  { $match:{ domain:"asearch.alicdn.com" } },
  { $group:{ 
          _id : "$time",
          avg:{$avg:"$bandwidth"} ,
           count: { $sum: 1 },
           sum:{$sum:"$bandwidth"} ,
      } 
  }
]);
db.coll_bw.aggregate([
  { $match:{ time:{$gte:1472704442} } },
  { $match:{ domain:"asearch.alicdn.com" } },
  { $group:{ 
          _id : {
              timeof5:{
                '$multiply' : [
                    {
                        $trunc:{'$divide' : ['$time', 300 ]}
                    },
                    300
                ] 
             }
         },
          avg:{$sum:"$bandwidth"} 
      } 
  }
]);

db.coll_log_metadata.find({"domain":/.tv.sohu.com/,"time":{$gt:1474934400,$lt:1474981200 }})
```

mongo in 查询

```
> db.user.insert({username:"yunduanzhidu",password:"cc2725b87ded7d4","favor":["yellow","red"]})
WriteResult({ "nInserted" : 1 })
> db.user.find()
{ "_id" : ObjectId("58cc9d809260466493e0eb64"), "username" : "yunduanzhidu", "password" : "cc2725b87ded7d4", "favor" : [ "yellow", "red" ] }
> db.user.insert({username:"yunduanzhidu1",password:"cc2725b87ded7d41","favor":["yellow","blue"]})
WriteResult({ "nInserted" : 1 })
> db.user.insert({username:"yunduanzhidu2",password:"cc2725b87ded7d41","favor":["green"]})
WriteResult({ "nInserted" : 1 })
> db.user.find({favor:["green"]})
{ "_id" : ObjectId("58cc9da19260466493e0eb66"), "username" : "yunduanzhidu2", "password" : "cc2725b87ded7d41", "favor" : [ "green" ] }
> db.user.find({favor:["green","yellow"]})
> db.user.find({favor:{"$in":["green","yellow"]}})
{ "_id" : ObjectId("58cc9d809260466493e0eb64"), "username" : "yunduanzhidu", "password" : "cc2725b87ded7d4", "favor" : [ "yellow", "red" ] }
{ "_id" : ObjectId("58cc9d959260466493e0eb65"), "username" : "yunduanzhidu1", "password" : "cc2725b87ded7d41", "favor" : [ "yellow", "blue" ] }
{ "_id" : ObjectId("58cc9da19260466493e0eb66"), "username" : "yunduanzhidu2", "password" : "cc2725b87ded7d41", "favor" : [ "green" ] }
```
- [docs.mongodb aggregate](https://docs.mongodb.com/manual/reference/method/db.collection.aggregate/#db.collection.aggregate)
- [docs.mongodb 取整](https://docs.mongodb.com/manual/reference/operator/aggregation/trunc/#exp._S_trunc)
- [mongo批量增加](https://docs.mongodb.com/manual/core/bulk-write-operations/)

mongo 去重
```
db.createCollection("ats_cache_urls")
db.ats_cache_urls.ensureIndex({"url":1},{unique:true})
db.ats_cache_urls.insert({url:"http://www.baidu.com/1"})
db.ats_cache_urls.insert({url:"http://www.baidu.com/2"})
```


pptpsetup --create xipu  --server 174.139.231.110 --user zhangjt   --password hans198885 --encrypt


```
db.coll_log_metadata.updateOne({ "_id" : ObjectId("5847a68d2e42619987dfa3ac")},{"$set":{"upload_failed_count" : NumberInt(1) } } )

db.coll_log_metadata.insertOne({ "time" : NumberLong(1479103200), "precise_time" : NumberLong("1479106127000"), "domain" : "pcvideoyd.titan.mgtv.com", "status" : "ready", "is_upload" : false, "is_compress" : false, "upload_failed_count" : NumberInt(0), "file_path" : "/tmp/cdn_logs/2016/11/14/14/pcvideoyd.titan.mgtv.com_201611141445.log", "file_name" : "pcvideoyd.titan.mgtv.com_201611141445.log"})
```

