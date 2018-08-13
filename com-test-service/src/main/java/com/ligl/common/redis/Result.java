package com.ligl.common.redis;

/**
 * Created by xjk on 2016/1/25.
 */
public class Result {

    /**
     * 操作成功
     */
    public final static int SUCCESS = 0;
    /**
     * 参数验证错误
     */
    public final static int PARAMCHECKERROR = 2;
    /**
     * redis服务失败
     */
    public final static int REDIS_SERVER_FAIL = 3;

    boolean success = false;
    int status = -1;
    String msg;//信息
    Object value;//对象
    String nextUrl;

    public Result() {
    }

    /**
     * 该构造器通过对传入参数判断，获取返回信息。
     *
     * @param status
     */
    public Result(int status) {
        this.setStatus(status);
        if (status == SUCCESS)
            success = true;
    }

    public Result(int status, String msg) {
        this.setStatus(status);
        this.msg = msg;
    }

    public Result setStatus(int status) {
        this.status = status;
        if (status == SUCCESS)
            success = true;
        switch (status) {
            case SUCCESS:
                msg = "操作成功！";
                break;
            case PARAMCHECKERROR:
                msg = "参数验证错误";
                break;
            case REDIS_SERVER_FAIL:
                msg = "从redis获取数据失败";
                break;
            default:
                msg = "未知错误！";
        }
        return this;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public Result setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Object> E getValue() {
        return (E) value;
    }

    public <E extends Object> Result setValue(E value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "Result [status=" + status + ", msg=" + msg + ", value=" + value
                + ", nextUrl=" + nextUrl + "]";
    }
}
