#!/bin/bash
nohup java  -jar com-test-web-1.0-SNAPSHOT.jar>/dev/null 2>./error.log &
tail -f /var/logs/com-test/core-spring.log



http://blog.csdn.net/catoop/article/details/50588851

指定外部的配置文件

有些系统，关于一些数据库或其他第三方账户等信息，由于安全问题，其配置并不会提前配置在项目中暴露给开发人员。
对于这种情况，我们在运行程序的时候，可以通过参数指定一个外部配置文件。
以 demo.jar 为例，方法如下：

java -jar demo.jar --spring.config.location=/opt/config/application.properties
在命令行使用 –debug 选项
在application.properties中添加debug=true

##启动
1.查看端口情况
netstat –apn |grep 9060
ps -ef|grep java获取pid
2.杀死一个进程
kill -9 pid #pid 为相应的进程号
cd 	/home/deploy/cashier-txs/
sh startcashier-txs.sh

执行：	./home/deploy/cashier-txs/startcashier-txs.sh或者 sh ./home/deploy/cashier-txs/startcashier-txs.sh
