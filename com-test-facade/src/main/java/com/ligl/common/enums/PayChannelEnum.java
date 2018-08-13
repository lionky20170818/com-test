package com.ligl.common.enums;

/**
 * 功能: 支付账户类型
 * 创建: liguoliang
 * 日期: 2017/5/26 0026 17:34
 * 版本: V1.0
 */
public enum PayChannelEnum {
	
    QUICK("QUICK", "快捷支付"),
    B2C("B2C", "个人网银"),
    B2B("B2B", "企业网银");

    private String code;
    private String desc;

    PayChannelEnum(String code, String desc) {
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

    public static PayChannelEnum getItem(String code) {
        for (PayChannelEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
