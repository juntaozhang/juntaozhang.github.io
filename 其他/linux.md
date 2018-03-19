# linux

### 查看当前操作系统内核信息
>uname -a

### CPU信息
[linux中查看CPU信息.md](./linux中查看CPU信息.md)

### 查看内存信息
>cat /proc/meminfo
Linux euis1 2.6.9-55.ELsmp #1 SMP Fri Apr 20 17:03:35 EDT 2007 i686 i686 i386 GNU/Linux

### 查看当前操作系统发行版信息 
>cat /etc/issue
Red Hat Enterprise Linux AS release 4 (Nahant Update 5)

>cat /etc/redhat-release

### 查看机器型号
>dmidecode | grep "Product Name"

### 查看网卡信息
>dmesg | grep -i eth

### [linux磁盘](./linux磁盘.md)

### linux查看内存条
>dmidecode|grep -P -A5 "Memory\s+Device"|grep Size|grep -v "No Module Installed"


## 其他工具

### 网络配置工具iproute2
https://linux.cn/article-4326-1.html
>brew install iproute2mac

### ss


