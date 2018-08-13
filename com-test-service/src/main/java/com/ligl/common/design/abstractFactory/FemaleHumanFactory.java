package com.ligl.common.design.abstractFactory;

/**
 * Function:女性工厂
 * Author: created by liguoliang
 * Date: 2017/11/6 0006 下午 4:36
 * Version: 1.0
 */
public class FemaleHumanFactory extends AbstractHumanFactory {

    //创建一个女性黑种人
    public Human createBlackHuman() {
        return super.createHuman(HumanEnum.BlackFemaleHuman);
    }

    //创建一个女性白种人
    public Human createWhiteHuman() {
        return super.createHuman(HumanEnum.WhiteFemaleHuman);
    }

    //创建一个女性黄种人
    public Human createYellowHuman() {
        return super.createHuman(HumanEnum.YelloFemaleHuman);
    }

}
