package com.ligl.common.design.adapter;

/**
 * Function:类适配器 使用对象继承的方式，是静态的定义方式
 * Author: created by liguoliang
 * Date: 2017/11/7 0007 上午 10:00
 * Version: 1.0
 */
public class DrawAdapter4Class extends DrawRectangle implements IDrawCircle {//既能画方又能画圆

    @Override
    public void drawCircle() {
        System.out.println("DrawAdapter4Class: drawCircle");
    }

}