package com.ligl.common.model;


import com.ligl.common.enums.PayResultEnum;

import java.io.Serializable;

/**
 * Created by liguoliang on 2017/6/7.
 * 基本返回对象
 */
public class BaseResult implements Serializable {
    /**
     * 返回码
     */
    protected String rspCode;

    /**
     * 返回描述
     */
    protected String rspMsg;

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }

    protected BaseResult() {
    }

    protected BaseResult(String rspCode, String rspMsg) {
        this.rspCode = rspCode;
        this.rspMsg = rspMsg;
    }

    public static BaseResult build() {
        return new BaseResult(PayResultEnum.RSP_SUCC.getRspCode(), PayResultEnum.RSP_SUCC.getRspMsg());
    }

    public static BaseResult build(String rspCode, String rspMsg) {
        return new BaseResult(rspCode, rspMsg);
    }

}
