# Linux中查看CPU信息


## /proc/cpuinfo

`cat /proc/cpuinfo`中的信息
processor       逻辑处理器的id。
physical id    物理封装的处理器的id。
core id        每个核心的id。
cpu cores      位于相同物理封装的处理器中的内核数量。
siblings       位于相同物理封装的处理器中的逻辑处理器的数量。
  
1 查看物理CPU的个数
#cat /proc/cpuinfo |grep "physical id"|sort |uniq|wc -l
2、   查看逻辑CPU的个数
#cat /proc/cpuinfo |grep "processor"|wc -l
3、  查看CPU是几核
#cat /proc/cpuinfo |grep "cores"|uniq
4、  查看CPU的主频
#cat /proc/cpuinfo |grep MHz|uniq 
5、  # uname -a
6、  Linux euis1 2.6.9-55.ELsmp #1 SMP Fri Apr 20 17:03:35 EDT 2007 i686 i686 i386 GNU/Linux
  (查看当前操作系统内核信息)
7、  # cat /etc/issue | grep Linux
8、  Red Hat Enterprise Linux AS release 4 (Nahant Update 5(查看当前操作系统发行版信息)
9、  # cat /proc/cpuinfo | grep name | cut -f2 -d: | uniq -c
     8  Intel(R) Xeon(R) CPU   E5410   @ 2.33GHz
    (看到有8个逻辑CPU, 也知道了CPU型号)
     9   # cat /proc/cpuinfo | grep physical | uniq -c
     4   physical id      : 0
     4 physical id      : 1
(说明实际上是两颗4核的CPU)
 
10、# getconf LONG_BIT
    32
       (说明当前CPU运行在32bit模式下, 但不代表CPU不支持64bit)
 
11、# cat /proc/cpuinfo | grep flags | grep ' lm ' | wc –l
8(结果大于0, 说明支持64bit计算. lm指long mode, 支持lm则是64bit)
 
12、如何获得CPU的详细信息：
  linux命令：cat /proc/cpuinfo
13、用命令判断几个物理CPU，几个核等：
  逻辑CPU个数：
   # cat /proc/cpuinfo | grep "processor" | wc -l
  物理CPU个数：
   # cat /proc/cpuinfo | grep "physical id" | sort | uniq | wc -l
14、每个物理CPU中Core的个数：
   # cat /proc/cpuinfo | grep "cpu cores" | wc -l
15、是否为超线程？如果有两个逻辑CPU具有相同的”core id”，那么超线程是打开的。每个物理CPU中逻辑CPU(可能是core, threads或both)的个数：
# cat /proc/cpuinfo | grep "siblings"
 
  1.查看CPU信息命令
  cat /proc/cpuinfo
  2.查看内存信息命令
  cat /proc/meminfo
  3.查看硬盘信息命令
  fdisk -l

查看CPU信息（型号）
# cat /proc/cpuinfo | grep name | cut -f2 -d: | uniq -c
      8  Intel(R) Xeon(R) CPU            E5410   @ 2.33GHz
(看到有8个逻辑CPU, 也知道了CPU型号)

# cat /proc/cpuinfo | grep physical | uniq -c
      4 physical id      : 0
      4 physical id      : 1
(说明实际上是两颗4核的CPU)
PS：Jay added on 10th, May, 2011
# 其实是可能有超线程HT技术，不一定是有4核，也可能是2核4线程；当时还理解不清楚

# getconf LONG_BIT
   32
(说明当前CPU运行在32bit模式下, 但不代表CPU不支持64bit)

# cat /proc/cpuinfo | grep flags | grep ' lm ' | wc -l
   8
(结果大于0, 说明支持64bit计算. lm指long mode, 支持lm则是64bit)
再完整看cpu详细信息, 不过大部分我们都不关心而已.
# dmidecode | grep 'Processor Information'

