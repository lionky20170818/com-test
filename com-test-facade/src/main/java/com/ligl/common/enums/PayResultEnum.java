package com.ligl.common.enums;

/**
 * 支付结果枚举
 * @author liguoliang
 * @version V1.0
 */
public enum PayResultEnum {
	
	//结果状态
	RSP_SUCC("0000","处理成功"),
	RSP_FAIL("9999","处理失败"),
	RSP_PROCESS("3T05", "处理中"),
	//支付状态
	PAY_STATUS_I("I","初始"),
	PAY_STATUS_P("P","处理中"),
	PAY_STATUS_S("S","成功"),
	PAY_STATUS_F("F","失败");

	/**
	 * 返回码
	 */
	private String rspCode;
	
	/**
	 * 返回信息
	 */
	private String rspMsg;
	
	private PayResultEnum(String rspCode, String rspMsg){
		this.rspCode=rspCode;
		this.rspMsg=rspMsg;
	}
	
	/**
	 * @return the rspCode
	 */
	public String getRspCode() {
		return rspCode;
	}

	/**
	 * @param rspCode the rspCode to set
	 */
	public void setRspCode(String rspCode) {
		this.rspCode = rspCode;
	}

	/**
	 * @return the rspMsg
	 */
	public String getRspMsg() {
		return rspMsg;
	}

	/**
	 * @param rspMsg the rspMsg to set
	 */
	public void setRspMsg(String rspMsg) {
		this.rspMsg = rspMsg;
	}
	
	public static PayResultEnum valueByRspCode(String rspCode){
		for(PayResultEnum constants:PayResultEnum.values()){
			if(constants.getRspCode().equals(rspCode)){
				return constants;
			}
		}
		
		return null;
		
	}
	
}