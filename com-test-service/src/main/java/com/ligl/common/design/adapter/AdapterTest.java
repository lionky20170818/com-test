package com.ligl.common.design.adapter;

/**
 * Function:适配器:无非就是继承实现重写
 * Author: created by liguoliang
 * Date: 2017/11/7 0007 上午 9:59
 * Version: 1.0
 * 平时我们会经常碰到这样的情况，有了两个现成的类，它们之间没有什么联系，但是我们现在既想用其中一个类的方法，同时也想用另外一个类的方法。有一个解决方法是，修改它们各自的接口，但是这是我们最不愿意看到的。这个时候Adapter模式就会派上用场了
 * 适配器 模式 有三种方式，一种是对象适配器，一种是类适配器, 一种是接口适配器
 */
public class AdapterTest {
    public static void main(String[] args) {
        //对象适配器
        System.out.println("--------Object------");
        DrawAdapter4Object objAdapter = new DrawAdapter4Object(new DrawRectangle());
        objAdapter.drawCircle();
        objAdapter.drawRectangle(" in DrawAdapter4Object");

        //类适配器
        System.out.println("--------Class------");
        DrawAdapter4Class clzAdapter = new DrawAdapter4Class();
        clzAdapter.drawCircle();
        clzAdapter.drawRectangle("in DrawAdapter4Class");

        //接口适配器
        System.out.println("-------Interface----");
        MyDrawAdapter myDrawAdapter = new MyDrawAdapter();
        myDrawAdapter.drawCircle();
        myDrawAdapter.drawRectangle();

    }

    public static class MyDrawAdapter extends DefaultDrawAdapter {

        @Override
        public void drawCircle() {
            System.out.println("drawCircle in MyDrawAdapter");
        }

    }
}

