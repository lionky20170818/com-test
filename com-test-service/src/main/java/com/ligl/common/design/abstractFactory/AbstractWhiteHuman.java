package com.ligl.common.design.abstractFactory;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:48
 * Version: 1.0
 */
public abstract class AbstractWhiteHuman implements Human {

    public void cry() {
        System.out.println("白色人种会哭");
    }

    public void laugh() {
        System.out.println("白色人种会大笑，侵略的笑声");
    }

    public void talk() {
        System.out.println("白色人种会说话，一般都是但是单字节！");
    }

}
