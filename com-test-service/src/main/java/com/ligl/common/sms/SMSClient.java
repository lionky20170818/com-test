package com.ligl.common.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 短信调用端
 * @author zhushanshan
 * 2017年10月16日 下午2:47:16
 */
public class SMSClient {

	private static final Logger log = LoggerFactory.getLogger(SMSClient.class);

	private SMSClient(){

	}
	/**
	 * 发送通知类短信:单号码单内容
	 * @param mobilePhone
	 * @param content
	 */
	public static int sendNotifyMsg(String mobilePhone,String content){
		return ETUtils.sendMTSms(new String[]{mobilePhone},content,false);
	}
	
	/**
	 * 发送通知类短信：多号码单内容
	 * @param mobilePhone
	 * @param content
	 */
	public static int sendNotifyMsg(String[] mobilePhone,String content){
		return ETUtils.sendMTSms(mobilePhone,content,false);
	}
	
	/**
	 * 发送通知类短信：单号码单内容
	 * @param mobilePhone
	 * @param content
	 */
	public static int sendMarketMsg(String mobilePhone,String content){
		return ETUtils.sendMTSms(new String[]{mobilePhone},content,true);
	}
	
	/**
	 * 发送通知类短信：多号码单内容
	 * @param mobilePhone
	 * @param content
	 */
	public static int sendMarketMsg(String[] mobilePhone,String content){
		return ETUtils.sendMTSms(mobilePhone,content,true);
	}
	
	/**
	 * 发送短信内容:多号码多内容
	 * @param mobileAndContent
	 * @param batchSize 批次大小
	 */
	public static int sendMarketMsg(List<MobileContent> mobileAndContent,Integer batchSize){
		if(null==batchSize) batchSize=ETConfig.batchSize;
		if(mobileAndContent.isEmpty()) return -1;
		List<List<MobileContent>> batchs=StringUtil.splitList(mobileAndContent, batchSize);
		String str=String.format("本次发送短信[%s]条,分[%s]批次,每批次[%s]条",mobileAndContent.size(),batchs.size(),batchSize);
		log.info(str);
		int flag=1;
		for(List<MobileContent> batch:batchs){
			int f=ETUtils.sendMTSms(batch);
			if(f==-1) {
				flag = -1;
			}
		}
		return flag;
	}
}
