package com.ligl.common.design.abstractFactory;

/**
 * Function:男性工厂
 * Author: created by liguoliang
 * Date: 2017/11/6 0006 下午 4:35
 * Version: 1.0
 */
public class MaleHumanFactory extends AbstractHumanFactory {

    //创建一个男性黑种人
    public Human createBlackHuman() {
        return super.createHuman(HumanEnum.BlackMaleHuman);
    }

    //创建一个男性白种人
    public Human createWhiteHuman() {
        return super.createHuman(HumanEnum.WhiteMaleHuman);
    }

    //创建一个男性黄种人
    public Human createYellowHuman() {
        return super.createHuman(HumanEnum.YelloMaleHuman);
    }

}
