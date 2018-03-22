# LockSupport.park vs object.wait

* 1.when park, interrupt it, will not in catch, you can get isInterrupted flag
* 2.when park, unpark it, will not in catch, can't get the flag
* 3.when wait, only notify(will in catch area, flag will be reset), can't unpark
* 4.park/unpark and wait/notify can't alternate use 
* 5.park orient thread, wait orient object

## 参考
* http://agapple.iteye.com/blog/970055