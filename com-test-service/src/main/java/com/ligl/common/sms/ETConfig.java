package com.ligl.common.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 
 * @author zhushanshan
 * 2017年10月16日 下午2:37:09
 */
@Component
public class ETConfig {
 
	public static String mlinkHost; 
	public static String mlinkSPId; 
	public static String mlinkSPPwd;
	 
    public static String mlinkMarketingHost;
    public static String mlinkMarketingSPId;
    public static String mlinkMarketingSPPwd;
    public static Integer batchSize;


	public static Integer getBatchSize() {
		return batchSize;
	}
	public static void setBatchSize(Integer batchSize) {
		ETConfig.batchSize = batchSize;
	}
	public static String getMlinkHost() {
		return mlinkHost;
	}
	
	@Value("${et.mlinkHost.key}")
	public  void setMlinkHost(String mlinkHost) {
		ETConfig.mlinkHost = mlinkHost;
	}
	public  String getMlinkSPId() {
		return mlinkSPId;
	}
	
	@Value("${et.mlinkSPId}")
	public  void setMlinkSPId(String mlinkSPId) {
		ETConfig.mlinkSPId = mlinkSPId;
	}
	public  String getMlinkSPPwd() {
		return mlinkSPPwd;
	}
	
	@Value("${et.mlinkSPPwd}")
	public  void setMlinkSPPwd(String mlinkSPPwd) {
		ETConfig.mlinkSPPwd = mlinkSPPwd;
	}
	public static String getMlinkMarketingHost() {
		return mlinkMarketingHost;
	}
	public static void setMlinkMarketingHost(String mlinkMarketingHost) {
		ETConfig.mlinkMarketingHost = mlinkMarketingHost;
	}
	public static String getMlinkMarketingSPId() {
		return mlinkMarketingSPId;
	}
	public static void setMlinkMarketingSPId(String mlinkMarketingSPId) {
		ETConfig.mlinkMarketingSPId = mlinkMarketingSPId;
	}
	public static String getMlinkMarketingSPPwd() {
		return mlinkMarketingSPPwd;
	}
	public static void setMlinkMarketingSPPwd(String mlinkMarketingSPPwd) {
		ETConfig.mlinkMarketingSPPwd = mlinkMarketingSPPwd;
	}
}
