package com.ligl.common.design.factoryMethod;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/13 0013 下午 1:51
 * Version: 1.0
 * 工厂模式参考使用type判断使用哪个具体：工厂模式写法
 * https://blog.csdn.net/llussize/article/details/80276627
 * 3,工厂模式：
 * 工厂模式示例athena-openapi-service：
 * 1，根据typpe:
 * 	billStatusModify(BillMqObj bill){}
 * 	BillStatusFactory factory=getFactoryBeanByType(bill.getType());
 *
 * 2,循环执行：BillsHandlerChain.java
 * 3，通过注解：EInvoiceServiceFactory.java
 *
 */
public class NvWa {
    public static void main(String[] args) {

        //女娲第一次造人，试验性质，少造点，火候不足，缺陷产品
        System.out.println("------------造出的第一批人是这样的：白人-----------------");
            Human whiteHuman = HumanFactory.createHuman(WhiteHuman.class);
        whiteHuman.cry();
        whiteHuman.laugh();
        whiteHuman.talk();

        //女娲第二次造人，火候加足点，然后又出了个次品，黑人
        System.out.println("\n\n------------造出的第二批人是这样的：黑人-----------------");
                Human blackHuman = HumanFactory.createHuman(BlackHuman.class);
        blackHuman.cry();
        blackHuman.laugh();
        blackHuman.talk();

        //第三批人了，这次火候掌握的正好，黄色人种（不写黄人，免得引起歧义），备注：RB人不属于此列
        System.out.println("\n\n------------造出的第三批人是这样的：黄色人种-----------------");
                Human yellowHuman = HumanFactory.createHuman(YellowHuman.class);
        yellowHuman.cry();
        yellowHuman.laugh();
        yellowHuman.talk();


        //女娲烦躁了，爱是啥人种就是啥人种，烧吧
        for (int i = 0; i < 10; i++) {
            System.out.println("\n\n------------随机产生人种了-----------------" + i);
            Human human = HumanFactory.createHuman1();
            human.cry();
            human.laugh();
            human.talk();
        }

    }
}
