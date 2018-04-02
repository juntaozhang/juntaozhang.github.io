
* nginx 1.8.1 安装

```
yum -y install pcre-devel openssl openssl-devel gcc
./configure --prefix=/home/ideal/nginx
make 
make install
```

* nginx-gridfs mongo

http://www.cnblogs.com/wintersun/p/4622205.html?utm_source=tuicool
https://github.com/mdirolf/nginx-gridfs/tree/v0.8

2017/03/26 19:24:15 [error] 18111#0: Invalid mongo user/pass: test/test
2017/03/26 19:24:15 [alert] 18110#0: worker process 18111 exited with fatal code 2 and cannot be respawned


这个似乎是个 Bug，之前我部署 Ruby China 的时候也是遇到这个问题，后面就放弃了。 自己用 Sinatra 高了个服务器跑，前端用 Nginx 反向代理，并 cache，效率比 nginx-gridfs 搞多了
我用 Sinatra + rack-gridfs