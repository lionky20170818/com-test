package com.ligl.common.design.adapter;

/**
 * Function:接口适配器 的默认实现
 * Author: created by liguoliang
 * Date: 2017/11/7 0007 上午 10:03
 * Version: 1.0
 */
public class DefaultDrawAdapter implements IDraw {//画方 画圆 皆为空实现

    @Override
    public void drawCircle() {
        System.out.println("jiekoushipeiqi drawCircle ");
    }

    @Override
    public void drawRectangle() {
        System.out.println("jiekoushipeiqi drawRectangle ");
    }
}
