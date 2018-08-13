package com.ligl.common.enums;

/**
 * 功能: 主题名称枚举
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/26 0026 15:17
 * 版本: V1.0
 */
public enum TopicNameEnum {

    RECHARGE("RECHARGE", "充值"),
    WITHDRAW("WITHDRAW", "提现"),
    INVEST("INVEST", "投资"),
    CREDIT_TRANSFER("CREDIT_TRANSFER", "债权转让")  ,
    REPAY("REPAY", "兑付"),
    LOAN("LOAN", "放款"),
    AUTO_CHARGE("AUTO_CHARGE", "自动扣款"),
    ORGAN_TRANSFER("ORGAN_TRANSFER", "机构转账");

//    CALLBACK_RECHARGE("CALLBACK_RECHARGE", "充值回调"),
//    CALLBACK_WITHDRAW("CALLBACK_WITHDRAW", "提现回调"),
//    CALLBACK_INVEST("CALLBACK_INVEST", "投资回调"),
//    CALLBACK_CREDIT_TRANSFER("CALLBACK_CREDIT_TRANSFER", "债权转让回调");

    private String code;
    private String desc;

    TopicNameEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static TopicNameEnum getItem(String code) {
        for (TopicNameEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
