package com.ligl.common.design.abstractFactory;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:49
 * Version: 1.0
 * 在java开发中，我们有时会定义了一个父类，这个父类只有对方法的描述，但却没有在父类中写出对方法的实现，这种被定义的方法称为抽象方法。
 * 那么理所当然，含有抽象方法的类就称为抽象类。用关键字abstract修饰。
 * 抽象类具有具有以下的语法规则需要遵循：
 1、一个类中如果有抽象方法，则这个类必须是抽象类。
 2、抽象类中可以没有抽象方法。可以有抽象方法也可以没有抽象方法
 3、抽象类是不能存在实例对象的，换句话说就是抽象类就是为了被继承而存在的。
 4、一个子类如果继承了抽象类，必须实现抽象类中定义的所有抽象方法。
 5、抽象类可以继承抽象类，可以不重写父类的方法。
 */
public abstract class AbstractBlackHuman implements Human {

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