# graphite(centos6 python2.6.6 mysql uWSGI)安装
## graphite install
##### Dependencies

```
yum install libevent-devel
yum install libffi-devel
yum install bitmap-fonts
yum install python-devel

pip install Twisted
pip install Django==1.5
pip install MySQL-python
pip install django-tagging
pip install django-fields
pip install pytz
pip install cairocffi

pip install fontconfig
    (AttributeError: 'module' object has no attribute 'check_output'=>自己build)
pip install uwsgi

py2cairo-1.10.0 cairo 如果pip或者build失败
Cairo and pycairo can be installed from yum.
yum install cairo-devel
yum install pycairo-devel

```

#####conf/storage-schemas.conf
配置metrics下面tree需要保存的粒度

***特别注意：***
`对于修改的pattern的retentions，如果metric的wsp文件已经存在，需要删除文件才能生效`

```
[cdn]
pattern = ^bj19*
retentions = 10s:1d,60s:7d,10m:30d,6h:1y

10s:1d——1天以内的数据是10秒为一个值
60s:7d——大于1天小于7天内的数据是以60秒为一个值
10m:30d——大于7天小于30天内的是以10分钟为一个值
1h:1y——大于30天小于1年的，是以1小时为一个值
```



##### local_settings

```
cd webapp/graphite
cp local_settings.py{.example,}
vi local_settings.py
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'USER': 'graphite',
        'PASSWORD': 'graphite',
        'HOST': 'localhost',
        'PORT': 'graphite'
    }
}
```
##### MYSQL syncdb

```
cp conf/storage-schemas.conf{.example,}
cp conf/storage-aggregation.conf{.example,}

create mysql database
CREATE DATABASE graphite;
GRANT ALL PRIVILEGES ON graphite.* TO 'graphite'@'192.168.11.78' IDENTIFIED BY 'graphite';
FLUSH PRIVILEGES;

python manage.py syncdb

bin/carbon-cache.py start
```

#####启动服务

```
cp conf/graphite.wsgi.example conf/wsgi.py

bin/carbon-cache.py start
```

##### graphite.ini配置

```
[uwsgi]
socket = 192.168.11.78:8630
chdir=/opt/graphite/conf
module=wsgi
master=True
processes=4
pidfile=/var/run/uwsgi-graphite.pid
vacuum=True
max-requests=5000
daemonize=/var/log/uwsgi-graphite.log
stats=192.168.11.78:9192
protocol=http

uwsgi --ini graphite.ini
```

#####修改Graphite默认的时区

```
打开webapp/graphite/settings.py，找到TIME_ZONE，默认是UTC，将其修改为Asia/Shanghai 
然后找到USE_TZ，没有的话自己在文件末尾添加，设置为True。
```

##trouble shooting
- Unable to install Twisted on CentOS server

```
wget --no-check-certificate https://pypi.python.org/packages/source/T/Twisted/Twisted-15.2.1.tar.bz2
tar -jxf Twisted-15.2.1.tar.bz2
cd Twisted-15.2.1
python setup.py install
```

- ImportError: No module named fields

```
https://github.com/hw-cookbooks/graphite/issues/227
pip install django-fields
```
- ImportError: Twisted requires zope.interface 3.6.0 or later: no module named zope.interface.

``` 
 easy_install zope.interface
```

- AttributeError: 'NoneType' object has no attribute 'clone'

``` 
 easy_install -U setuptools
```

- graphite Internal Server Error

```    
yum install libevent-devel
export LC_ALL=C
pip install --upgrade setuptools
no python application found, check your startup logs for errors
vi webapp/graphite/local_settings.py
修改GRAPHITE_ROOT等参数路径
```

- error: command 'gcc' failed with exit status 1

```
c/_cffi_backend.c:6728: error: 'FFI_DEFAULT_ABI' undeclared
yum install libffi-devel
```

- ImportError: No module named wsgi

```
cp conf/graphite.wsgi.example conf/wsgi.py
```


- MemoryError when drawing fonts, 图不稳定

```
traceback (most recent call last):
  File "/usr/lib/python2.6/site-packages/django/core/handlers/base.py", line 115, in get_response
    response = callback(request, *callback_args, **callback_kwargs)
  File "/usr/local/graphite/webapp/graphite/render/views.py", line 215, in renderView
    image = doImageRender(requestOptions['graphClass'], graphOptions)
  File "/usr/local/graphite/webapp/graphite/render/views.py", line 436, in doImageRender
    img = graphClass(**graphOptions)
  File "/usr/local/graphite/webapp/graphite/render/glyph.py", line 196, in __init__
    self.drawGraph(**params)
  File "/usr/local/graphite/webapp/graphite/render/glyph.py", line 543, in drawGraph
    self.drawText("No Data", x, y, align='center')
  File "/usr/local/graphite/webapp/graphite/render/glyph.py", line 261, in drawText
    extents = self.getExtents(text)
  File "/usr/local/graphite/webapp/graphite/render/glyph.py", line 233, in getExtents
    F = self.ctx.font_extents()
MemoryError
http://johannilsson.com/2012/05/13/graphite.html
```

##待解决的问题

- AttributeError: 'module' object has no attribute 'check_output'

```
AttributeError: 'module' object has no attribute 'check_output'
Checking for header Python.h             : Could not find the python development headers
The configuration failed
(complete log in /home/package/py2cairo-1.10.0/build_directory/config.log)
```
- **ImportError: Could not import settings 'graphite.settings' (Is it on sys.path?): No module named graphite.settings**

```
重新编译python 后解决 具体问题不清楚???
```
- pycairo安装失败

```
pip install pycairo
py2cairo-1.10.0 build也失败
Checking for 'cairo' >= 1.10.0           : not found
The configuration failed
```


##相关文章
- [官网installing Dependencies](https://graphite.readthedocs.io/en/latest/install.html#dependencies)
- [官网Installing From Pip](https://graphite.readthedocs.io/en/latest/install-pip.html)
- [mac下安装 https://gist.github.com/JuntaoZhang/f8761068062c3f0877e1ad0cac974d2d](https://gist.github.com/JuntaoZhang/f8761068062c3f0877e1ad0cac974d2d)
- [mac下安装 https://gist.github.com/relaxdiego/7539911](https://gist.github.com/relaxdiego/7539911)





