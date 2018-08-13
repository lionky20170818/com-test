package com.ligl.common.design.strategy;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 上午 10:31
 * Version: 1.0
 */
public class Context {
    //构造函数，你要使用那个妙计
    private IStrategy straegy;
    public Context(IStrategy strategy){
        this.straegy = strategy;
    }

    //使用计谋了，看我出招了
    public void operate(){
        this.straegy.operate();
    }
}
