package com.ligl.common.design.adapter;

/**
 * Function:对象适配器： 使用对象组合的方式，是动态组合的方式。
 * * DrawAdapter是适配器，DrawRectangle属于adapter,是被适配者，适配器将被适配者和适配目标（DrawCircle）进行适配
 * Author: created by liguoliang
 * Date: 2017/11/7 0007 上午 10:01
 * Version: 1.0
 */
public class DrawAdapter4Object implements IDrawCircle {//既能画方又能画圆

    private DrawRectangle drawRectangle;

    public DrawAdapter4Object(DrawRectangle drawRectangle) {
        this.drawRectangle = drawRectangle;
    }

    @Override
    public void drawCircle() {
        System.out.println("DrawAdapter4Object: drawcircle");
    }

    public void drawRectangle(String msg) {
        drawRectangle.drawRectangle(msg);
    }

}
