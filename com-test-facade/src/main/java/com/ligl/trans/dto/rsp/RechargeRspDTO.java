package com.ligl.trans.dto.rsp;


import com.ligl.common.model.BaseRequestDTO;

import java.math.BigDecimal;

/**
 * 充值响应DTO
 * @author liguoliang
 * @param <>
 */
public class RechargeRspDTO extends BaseRequestDTO {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8410972913177010376L;

	/* 充值金额(元) */
    private BigDecimal tradeAmt;
    
    /* 手续费(元) */
    private BigDecimal feeAmt;
    
    /* 支付渠道 */
    private String payChannel;
    
    /* 交易状态 */
    private String tradeStatus;

    /* 银行卡号 */
    private String cardNo;

    /* 加签网银信息*/
    private String jsonStr;
    
    /* 请求地址 */
    private String requestUrl;

    
	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public BigDecimal getFeeAmt() {
		return feeAmt;
	}

	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
	}

	public String getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(String payChannel) {
		this.payChannel = payChannel;
	}

	public BigDecimal getTradeAmt() {
		return tradeAmt;
	}

	public void setTradeAmt(BigDecimal tradeAmt) {
		this.tradeAmt = tradeAmt;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}
	
}
