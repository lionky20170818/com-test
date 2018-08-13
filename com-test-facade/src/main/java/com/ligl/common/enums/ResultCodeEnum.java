package com.ligl.common.enums;


/**
 * ClassName: ResultCodeEnum <br/>
 * Function: 业务处理结果. <br/>
 * Date: 2016年12月13日 下午2:14:31 <br/>
 *
 * @author
 * @since JDK 1.7
 */
public enum ResultCodeEnum {

    /**
     * 0000: 业务处理成功
     **/
    SUCCESS("0000", "业务处理成功"),

    /**
     * 9999: 业务处理失败
     **/
    FAIL("9999", "业务处理失败"),

    /**
     * 9003: 参数校验未通过
     **/
    PARAMS_VALIDATE_FAIL("9003", "参数校验未通过"),

    /**
     * 请求已处理
     */
    UNIQUE_INDEX_FAIL("8888", "请求已处理"),

    /**
     * NOT_FOUND_INFO: 未找到信息
     **/
    NOT_FOUND_INFO("9000", "未找到信息"),
    /**
     * NOT_FOUND_TRADE_ACCOUNT: 平台id与产品编码渠道对应的交易系统账户不存在
     **/
    NOT_FOUND_TRADE_ACCOUNT("9001", "平台id与产品编码渠道对应的交易系统账户不存在"),
    /**
     * INVEST_ORDER_NO_EXIST: 交易系统申购申请订单已经存在请不要重复提交
     **/
    INVEST_ORDER_NO_EXIST("9100", "交易系统申购申请订单已经存在请不要重复提交"),

    INVEST_SUM_AMOUNT_ERROR("9101", "募集金额与实际金额不符"),

    /**
     * REDEEM_ORDER_NO_EXIST: 交易系统预约兑付订单已经存在请不要重复提交
     **/
    REDEEM_ORDER_NO_EXIST("9200", "交易系统预约兑付订单已经存在请不要重复提交"),
    /**
     * REDEEM_AMOUNT_ILLEGAL: 交易系统预约赎回金额大于投资金额
     **/
    REDEEM_AMOUNT_ILLEGAL("9201", "交易系统预约赎回金额大于可赎回金额"),
    /**
     * BATCH_ILLEGAL: 非法批次号
     **/
    BATCH_ILLEGAL("9203", "非法批次号"),

    /**
     * DATA_PROCESSING: 数据处理中
     **/
    DATA_PROCESSING("9300", "数据处理中"),

    FILE_NOT_EXIST("8000", "文件不存在"),
    BATCH_NOT_EXIST("8001", "批次不存在"),
    BATCH_IS_EXIST("8002", "批次已存在"),
    BATCH_IS_CONFIRM("8003", "批次已被处理"),
    BATCH_FILENAME_EXISTED("8004", "文件名称已经存在"),
    FILENAME_FILEPATH_NO_MATCH("8005", "文件名与地址不匹配");


    private String code;
    private String desc;

    ResultCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String code() {
        return code;
    }

    public String desc() {
        return desc;
    }


}

