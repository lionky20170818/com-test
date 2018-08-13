package com.ligl.common.utils.xuliehao;

/**
 * Created by liguoliang on 2017/9/1.
 */
public enum SequenceEnum {

    ORDER("ORDER", "订单sequence"),
    ACCOUNT("ACCOUNT", "持仓sequence");


    private String code;
    private String desc;

    SequenceEnum(String code, String desc) {
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
}
