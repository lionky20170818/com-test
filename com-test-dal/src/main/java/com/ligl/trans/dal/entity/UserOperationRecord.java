package com.ligl.trans.dal.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserOperationRecord {
    private Long id;

    private String refNo;

    private String sourceId;

    private String accountNo;

    private String operationType;

    private BigDecimal tradeAmt;

    private BigDecimal feeAmt;

    private String operationBy;

    private Date operationTime;

    private Date createTime;

    private String createBy;

    private Date modifyTime;

    private String modifyBy;

}