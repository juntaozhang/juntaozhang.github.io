
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

* nginx upstream跨域名配置

```
  location /backend-1.0.0/ {
    add_header 'Access-Control-Allow-Origin' '*';
    add_header 'Access-Control-Allow_Credentials' 'true';
    add_header 'Access-Control-Allow-Headers' 'Authorization,Accept,Origin,DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range';
    add_header 'Access-Control-Allow-Methods' 'GET,POST,OPTIONS,PUT,DELETE,PATCH';

    if ($request_method = 'OPTIONS') {
      add_header 'Access-Control-Allow-Origin' '*';
      add_header 'Access-Control-Allow_Credentials' 'true';
      add_header 'Access-Control-Allow-Headers' 'Authorization,Accept,Origin,DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range';
      add_header 'Access-Control-Allow-Methods' 'GET,POST,OPTIONS,PUT,DELETE,PATCH';
      add_header 'Access-Control-Max-Age' 1728000;
      add_header 'Content-Type' 'text/plain charset=UTF-8';
      add_header 'Content-Length' 0;
      return 204;
    }

    proxy_redirect off;
    proxy_set_header host $host;
    proxy_set_header X-real-ip $remote_addr;
    proxy_set_header X-forward-for $proxy_add_x_forwarded_for;
    proxy_pass http://124.116.245.100:18001/backend-1.0.0/;
  }

  location / {
    root   html/var/www/dist;
    index  index.html index.htm;
  }
```


