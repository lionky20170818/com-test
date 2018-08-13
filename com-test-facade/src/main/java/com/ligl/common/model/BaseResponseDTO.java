package com.ligl.common.model;

import java.io.Serializable;

/**
 * Created by liguoliang on 2017/6/7.
 */
public class BaseResponseDTO implements Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3935484875576695626L;
	
	/**
     * 订单号
     */
    private String refNo;
    
    /**
     * 商户会员ID
     */
    private String memberId;
    
    /**
     * CIF会员ID
     */
    private String cifMemberId;
    
    /**
     * 系统来源
     */
    private String sourceId;

    
	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getCifMemberId() {
		return cifMemberId;
	}

	public void setCifMemberId(String cifMemberId) {
		this.cifMemberId = cifMemberId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
    
}
