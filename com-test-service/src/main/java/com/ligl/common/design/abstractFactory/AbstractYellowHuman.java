package com.ligl.common.design.abstractFactory;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:48
 * Version: 1.0
 */
public abstract class AbstractYellowHuman implements Human {


    public void cry() {
        System.out.println("黄色人种会哭");
    }


    public void laugh() {
        System.out.println("黄色人种会大笑，幸福呀！");
    }


    public void talk() {
        System.out.println("黄色人种会说话，一般说的都是双字节");
    }
}
