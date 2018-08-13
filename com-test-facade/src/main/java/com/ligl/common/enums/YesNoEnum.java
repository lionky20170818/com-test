package com.ligl.common.enums;

/**
 * 功能: 是否枚举
 * 创建: liguoliang - liguoliang
 * 日期: 2017/5/26 0026 17:46
 * 版本: V1.0
 */
public enum YesNoEnum {

    NO(0, "否"),
    YES(1, "是");

    private int code;
    private String desc;

    YesNoEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static YesNoEnum getItem(int code) {
        for (YesNoEnum item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
}
