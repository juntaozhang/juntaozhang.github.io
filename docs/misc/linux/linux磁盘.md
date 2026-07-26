## linux 磁盘


- 查看磁盘挂载情况`lsblk`
- 监控io
 
```
dstat
yum install sysstat
iostat -d 3
```

#### fdisk 分区
```
fdisk /dev/sdb
```


##### linux添加新硬盘 

**TODO 需要加入id号 防止重启之后找不到**

```
lsblk
mkfs -t ext4 /dev/sdb
mount /dev/sdb /data
vi  /etc/fstab
/dev/sdb  /data      ext4    defaults        1   2

测试
hdparm -t /dev/sdb
``` 