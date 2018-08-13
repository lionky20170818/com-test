package com.ligl.trans.query.dto;


import com.ligl.common.model.BaseRequestDTO;

/**
 * 充值请求DTO
 * @author liguoliang
 * @param <>
 */
public class QueryRechargeReqDTO extends BaseRequestDTO {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7306487756192643799L;

	/**
	 * 交易开始时间
	 */
	private String tradeStartTime;
    
	/**
	 * 交易结束时间
	 */
    private String tradeEndTime;

    
	public String getTradeStartTime() {
		return tradeStartTime;
	}
	
	public void setTradeStartTime(String tradeStartTime) {
		this.tradeStartTime = tradeStartTime;
	}
	
	public String getTradeEndTime() {
		return tradeEndTime;
	}
	
	public void setTradeEndTime(String tradeEndTime) {
		this.tradeEndTime = tradeEndTime;
	}
	
}
