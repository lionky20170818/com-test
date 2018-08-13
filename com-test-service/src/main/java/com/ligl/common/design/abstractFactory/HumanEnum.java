package com.ligl.common.design.abstractFactory;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/11/6 0006 下午 4:28
 * Version: 1.0
 */
public enum HumanEnum {
    //把世界上所有人类型都定义出来
    YelloMaleHuman("com.ligl.common.design.abstractFactory.YellowMaleHuman"),

    YelloFemaleHuman("com.ligl.common.design.abstractFactory.YellowFemaleHuman"),

    WhiteFemaleHuman("com.ligl.common.design.abstractFactory.WhiteFemaleHuman"),

    WhiteMaleHuman("com.ligl.common.design.abstractFactory.WhiteMaleHuman"),

    BlackFemaleHuman("com.ligl.common.design.abstractFactory.BlackFemaleHuman"),

    BlackMaleHuman("com.ligl.common.design.abstractFactory.BlackMaleHuman");

    private String value = "";

    //定义构造函数，目的是Data(value)类型的相匹配
    private HumanEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
