package com.ligl.common.design.Singleton;

/**
 * Function:单例模式
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 上午 11:11
 * Version: 1.0
 * 单例模式很简单，就是在构造函数中多了加一个构造函数，访问权限是 private 的就可以了，这个模
 式是简单，但是简单中透着风险，风险？什么风险？在一个 B/S 项目中，每个 HTTP Request 请求到 J2EE
 的容器上后都创建了一个线程,每个线程都要创建同一个单例对象,怎么办?,好,我们写一个通用的单例程
 序,然后分析一下:
 http://www.blogjava.net/kenzhh/archive/2013/03/15/357824.html总共有7总单例模式的写法
 */
public class SingletonPattern {
    private static final SingletonPattern singletonPattern= new SingletonPattern();

    //限制住不能直接产生一个实例
    private SingletonPattern(){

    }
    // 直接 new 一个对象传递给类的成员变量 singletonpattern，你要的时候 getInstance（）直接返回给 你，解决问题！
    public synchronized static SingletonPattern getInstance(){
        return singletonPattern;
    }

    public void singletonInfo(){
        System.out.println("新模式的单例模式.....");
    }


}