package com.ligl.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * ClassName: LeftPadUtils <br/>
 * Function: 字符串补齐工具类. <br/>
 * Date: 2017年8月2日 下午5:07:51 <br/>
 *
 * @author liguoliang
 * @version 
 * @since JDK 1.7
 */
public class LeftPadUtils {
	
	/**
	 * TwoStr:生成两位. <br/>
	 *
	 * @param str
	 * @return
	 */
	public static String twoStrNum (String str) {
		String suffix = StringUtils.leftPad(str, 2, "0");
		return suffix;
	}

}
