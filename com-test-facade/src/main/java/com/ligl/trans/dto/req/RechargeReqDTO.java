package com.ligl.trans.dto.req;


import com.ligl.common.model.BaseRequestDTO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值请求DTO
 * @author liguoliang
 * @param <>
 */
public class RechargeReqDTO extends BaseRequestDTO {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8636134478106239381L;

	/**
     * 主键
     */
    private Long id;

    /**
     * 汇付客户号
     */
    private String custId;
    
    /**
     * 银行卡号
     */
    private String cardNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 短信验证码
     */
    private String smsCode;

    /**
     * 交易金额
     */
    private BigDecimal tradeAmt;

    /**
     * 手续费
     */
    private BigDecimal feeAmt;

    /**
     * 交易时间
     */
    private Date tradeTime;
    
    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 银行简码
     */
    private String bankCode;
    
    /**
     * 页面返回地址
     */
    private String pageReturnUrl;

   /**
    * 开始时间
    */
    private String startTime;
    
   /**
    * 结束时间
    */
    private String endTime;
    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public BigDecimal getTradeAmt() {
		return tradeAmt;
	}

	public void setTradeAmt(BigDecimal tradeAmt) {
		this.tradeAmt = tradeAmt;
	}

	public Date getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Date tradeTime) {
		this.tradeTime = tradeTime;
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

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getPageReturnUrl() {
		return pageReturnUrl;
	}

	public void setPageReturnUrl(String pageReturnUrl) {
		this.pageReturnUrl = pageReturnUrl;
	}
	
}
