package com.ligl.common.sms;

/**
 * 短信对象
 * @author zhushanshan
 * 2017年10月16日 下午2:46:45
 */
public class MobileContent {

	private String mobile;
	private String content;
	
	
	public MobileContent() {
		super();
	}
	
	public MobileContent(String mobile, String content) {
		super();
		this.mobile = mobile;
		this.content = content;
	}

	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
