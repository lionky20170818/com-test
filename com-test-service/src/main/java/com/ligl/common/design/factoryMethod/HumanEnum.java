package com.ligl.common.design.factoryMethod;

public enum HumanEnum {

	BLACK("BLACK","黑种人"),
	WHITE("WHITE","白种人"),
	YELLOW("YELLOW","黄种人")
	;

	private String code;
	private String value;

	HumanEnum(String code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static HumanEnum getEnum(String code) {
		for (HumanEnum item : HumanEnum.values()) {
			if (item.getCode().equals(code)) {
				return item;
			}
		}
		return null;
	}
	

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
