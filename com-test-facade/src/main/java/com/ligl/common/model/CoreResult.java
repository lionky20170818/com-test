package com.ligl.common.model;


import com.ligl.common.enums.PayResultEnum;

/**
 * 含body返回对象
 *
 * @author liguoliang
 * @since 2017-06-06
 */
public final class CoreResult<T> extends BaseResult {

    /**
     * 交易返回对象
     */
    private T body;

    private CoreResult() {
        super();
    }

    private CoreResult(String rspCode, String rspMsg) {
        super(rspCode, rspMsg);
    }

    public static CoreResult build() {
        return new CoreResult(PayResultEnum.RSP_SUCC.getRspCode(), PayResultEnum.RSP_SUCC.getRspMsg());
    }

    public static CoreResult build(String rspCode, String rspMsg) {
        return new CoreResult(rspCode, rspMsg);
    }

    public static CoreResult succ() {
    	return new CoreResult(PayResultEnum.RSP_SUCC.getRspCode(), PayResultEnum.RSP_SUCC.getRspMsg());
    }
    
    public static CoreResult fail() {
    	return new CoreResult(PayResultEnum.RSP_FAIL.getRspCode(), PayResultEnum.RSP_FAIL.getRspMsg());
    }
    
    public static CoreResult fail(String msg) {
    	return new CoreResult(PayResultEnum.RSP_FAIL.getRspCode(), msg);
    }
    
    public static CoreResult process() {
    	return new CoreResult(PayResultEnum.RSP_PROCESS.getRspCode(), PayResultEnum.RSP_PROCESS.getRspMsg());
    }
    
    public static CoreResult process(String msg) {
    	return new CoreResult(PayResultEnum.RSP_PROCESS.getRspCode(), msg);
    }
    
    public static boolean IsSucc(CoreResult coreResult) {
        if (PayResultEnum.RSP_SUCC.getRspCode().equals(coreResult.getRspCode())) {
            return true;
        }
        return false;
    }
    
    public boolean IsFailed() {
        if (PayResultEnum.RSP_SUCC.getRspCode().equals(this.getRspCode())) {
            return false;
        }
        return true;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
