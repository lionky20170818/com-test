package com.ligl.common.fenbushiLock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/10/17 0017 上午 11:05
 * Version: 1.0
 */
@Service
public class DistributedLockServiceImpl {
    @Value("${dubbo.registry.address:192.168.0.65:2181}")
    private String zookeeperConnectionString;
    @Value("${zkLock.pool.max:8}")
    private int maxConn = 8;
    //分布式锁专用zk节点
    private String LOCK_ZK_NODE = "/p2pTradeProcess/";

    private CuratorFramework curatorFramework;

    private ThreadLocal<Map<String, InterProcessMutex>> threadLocalLock = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        curatorFramework.start();
    }

    public void tryLock(String lockKey) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, LOCK_ZK_NODE + lockKey);
        Map<String, InterProcessMutex> lockMap = threadLocalLock.get();
        if (lockMap == null) {
            lockMap = new HashMap<>();
        }
        lockMap.put(lockKey, lock);
        threadLocalLock.set(lockMap);
        lock.acquire();
    }

    public boolean tryLock(String lockKey, long timeOut, TimeUnit timeUnit) throws Exception {
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, LOCK_ZK_NODE + lockKey);
        if (lock.acquire(timeOut, timeUnit)) {
            Map<String, InterProcessMutex> lockMap = threadLocalLock.get();
            if (lockMap == null) {
                lockMap = new HashMap<>();
            }
            lockMap.put(lockKey, lock);
            threadLocalLock.set(lockMap);
            return true;
        }
        return false;
    }

    public void unLock(String lockKey) throws Exception {
        threadLocalLock.get().get(lockKey).release();
    }

}
