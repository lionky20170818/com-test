package com.ligl.trans.service.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Function:既然如此，我们什么时候才应该使用 ReentrantLock 呢？答案非常简单 ——
 * 在确实需要一些 synchronized 所没有的特性的时候，比如时间锁等候、可中断锁等候、无块结构锁、多个条件变量或者锁投票。
 * Author: created by liguoliang
 * Date: 2017/10/25 0025 上午 11:23
 * Version: 1.0
 */
public class ReenTrantLockTest extends Thread {

    public void lockTest() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            // update object state
            System.out.println("ReenTrantLockTest.lockTest===");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        lockTest();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new ReenTrantLockTest().start();
            System.out.println("==" + i);
        }
    }

}
