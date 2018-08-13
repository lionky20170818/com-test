/*
 * Copyright (c) 2017, 资邦金服（上海）网络科技有限公司. All Rights Reserved.
 *
 *
 *
 */
package com.ligl.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * ClassName: PropertiesUtil <br/>
 * Function: 配置文件读取. <br/>
 * Date: 2017年6月6日 下午4:20:26 <br/>
 *
 * @author liguoliang
 * @version 
 * @since JDK 1.7
 */
public class PropertiesUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
	private static Properties prop = new Properties();

	static {
		InputStream inStream = null;
		try {
			// 读取 配置信息
			inStream = new FileInputStream(new File(System.getenv("pay_conf_path")+"/config-cashier-txs.properties"));
			prop.load(inStream);
		} catch (FileNotFoundException e) {
			LOGGER.error("找不到文件config-cashier.properties", e);
			throw new RuntimeException("找不到文件config-cashier.properties");
		} catch (IOException e) {
			LOGGER.error("加载配置文件异常", e);
			throw new RuntimeException(e);
		} finally {
			if (inStream != null) {
				try {
					// 关闭流
					inStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public static String getValue(String key) {
		return prop.getProperty(key);
	}
	
}
