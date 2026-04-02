# linux VIRT java程序VIRT过大问题

```
top - 12:21:52 up 12 days, 23:23,  7 users,  load average: 0.08, 0.07, 0.07
Tasks: 777 total,   1 running, 774 sleeping,   2 stopped,   0 zombie
Cpu(s):  0.2%us,  0.2%sy,  0.0%ni, 99.6%id,  0.0%wa,  0.0%hi,  0.0%si,  0.0%st
Mem:  132042608k total, 22637428k used, 109405180k free,   459720k buffers
Swap:  4194300k total,        0k used,  4194300k free, 17177816k cached

   PID USER      PR  NI  VIRT  RES  SHR S %CPU %MEM    TIME+  COMMAND
132653 nobody    20   0 4657m 335m 6104 S  8.9  0.3  12:42.16 [ET_NET 0]
  8493 mongod    20   0 7186m 403m 8708 S  1.3  0.3 278:49.31 mongod
  8575 mongod    20   0 11.5g 302m  11m S  1.3  0.2 528:33.00 mongod
  8645 mongod    20   0 2354m  85m 7408 S  0.3  0.1  79:57.10 mongod
132646 root      20   0  227m 3724 2808 S  0.3  0.0   0:00.35 traffic_cop
134901 logstash  20   0 9534m 751m  14m S  0.3  0.6   1:10.15 java
135102 root      20   0 36.2g 330m  15m S  0.3  0.3   0:09.04 java
```