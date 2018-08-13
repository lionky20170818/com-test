package com.ligl.common.design.factoryMethod;

/**
 * Function:工厂方法模式
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:47
 * Version: 1.0
 */
public interface Human {
    //首先定义什么是人类

    //人是愉快的，会笑的，本来是想用smile表示，想了一下laugh更合适，好长时间没有大笑了；
    public  void laugh();

    //人类还会哭，代表痛苦
    public void cry();

    //人类会说话
    public void talk();

}
