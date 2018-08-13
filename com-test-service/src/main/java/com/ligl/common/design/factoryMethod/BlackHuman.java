package com.ligl.common.design.factoryMethod;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:49
 * Version: 1.0
 */
public class BlackHuman implements Human {

    public void cry() {
        System.out.println("黑人会哭");
    }

    public void laugh() {
        System.out.println("黑人会笑");
    }

    public void talk() {
        System.out.println("黑人可以说话，一般人听不懂");

    }
}