package com.ligl.trans.query.dto;


import com.ligl.common.model.BaseRequestDTO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值请求DTO
 * @author liguoliang
 * @param <>
 */
public class QueryRechargeRspDTO extends BaseRequestDTO {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1952271680491008659L;

	/* 充值金额(元) */
    private BigDecimal tradeAmt;
    
    /* 手续费(元) */
    private BigDecimal feeAmt;
    
    /* 支付渠道 */
    private Date tradeTime;
    
    /* 交易状态 */
    private String tradeStatus;

    
	public BigDecimal getTradeAmt() {
		return tradeAmt;
	}

	public void setTradeAmt(BigDecimal tradeAmt) {
		this.tradeAmt = tradeAmt;
	}

	public BigDecimal getFeeAmt() {
		return feeAmt;
	}

	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
	}

	public Date getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Date tradeTime) {
		this.tradeTime = tradeTime;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}
    
}
