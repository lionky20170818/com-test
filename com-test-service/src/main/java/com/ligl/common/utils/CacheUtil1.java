package com.ligl.common.utils;

import java.util.List;

/**
 * Function:ThreadLocal
 * Author: created by liguoliang
 * Date: 2017/9/7 0007 上午 10:42
 * ThreadLocal不是用来解决对象共享访问问题的，而主要是提供了线程保持对象的方法和避免参数传递的方便的对象访问方式
 * ThreadLocal的应用场合，最适合的是按线程多实例（每个线程对应一个实例）的对象的访问，并且这个对象很多地方都要用到。
 * Version: 1.0
 */
public class CacheUtil1 {

    //每个线程操作单独的产品
    private static final ThreadLocal threadLocal = new ThreadLocal<>();

    public static List selectTasks() {
        threadLocal.set("111111");
        return null;
    }

    public static void select() {
        threadLocal.get();
    }
}