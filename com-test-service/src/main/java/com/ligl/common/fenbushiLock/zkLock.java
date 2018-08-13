package com.ligl.common.fenbushiLock;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/10/17 0017 上午 11:05
 * Version: 1.0
 */
public class zkLock {
    //分布式锁几种实现：http://blog.csdn.net/zdy0_2004/article/details/53070209
    //redis锁:com.ligl.common.redis
    //zklock锁:http://www.cnblogs.com/liuyang0/p/6800538.html
    //DistributedLockServiceImpl：CuratorFramework锁      参考网址http://blog.csdn.net/nysyxxg/article/details/45437857
//    @Value("${dubbo.registry.address}")
//    private String zookeeperConnectionString;
//
//    public void execute() throws Exception {
//        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, new ExponentialBackoffRetry(1000, 3));
//        client.start();
//        try {
//            InterProcessMutex lock = new InterProcessMutex(client, "/TA_LADDER_JOB:InvestAssetMatchJob");
//
//            if (lock.acquire(3, TimeUnit.SECONDS)) {
//                try {
//                    logger.info(" 资产匹配JOB开始时间: " + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS"));
//                    investAssetMatchJobService.execute();
//                    logger.info(" 资产匹配JOB结束时间: " + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS"));
//                } finally {
//                    lock.release();
//                }
//            }
//        } catch (Exception e) {
//            logger.error(" 资产匹配JOB异常: " + e.getMessage());
//        } finally {
//            CloseableUtils.closeQuietly(client);
//        }
//    }

//简单介绍一下读写锁，在使用读写锁时， 多个客户端（线程）可以同时获取 “读锁”， 但是“写入锁”是排它的，只能单独获取。
//    1、假设A,B线程获取到 “读锁”， 这时C线程就不能获取 “写锁”。
//    2、假设C线程获取了“写锁”，那么A,B线程就不能获取“读锁”。

}
