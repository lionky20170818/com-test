package com.ligl.common.sms;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移通工具类
 * @author zhushanshan
 * 2017年7月3日下午3:08:43
 */
public class ETUtils {
	private static final Logger log = LoggerFactory.getLogger(ETUtils.class);
	
    
    public final static String YT_MLINK_COMMAND_SINGLE = "MT_REQUEST";
    public final static String YT_MLINK_COMMAND_MULTI = "MULTI_MT_REQUEST";
    public final static String RT_MLINK_COMMAND_SINGLE = "RT_REQUEST";
    public final static String RT_MLINK_COMMAND_MULTI = "MULTI_RT_REQUEST";
    public final static String YT_MLINK_DC = "15";
    
    public final static String MT_ERR_CODE_KEY = "mterrcode";
    public final static String MT_ERR_CODE_VALUE_SUCCESS = "000";
    public final static String MT_STAT_KEY = "mtstat";
    public final static String MT_STAT_VALUE_SUCCESS = "ACCEPTD";
    public final static String MT_MSG_IDS_KEY = "mtmsgids";
    public final static String MT_MSG_ID_KEY = "mtmsgid";

    
    /**
     * 发送移通短信
     * @param mobiles
     * @param content
     * @param isMarketing :是否是营销短信
     * @return
     */
    public static int sendMTSms(String[] mobiles,String content,boolean isMarketing){
    	log.info(">>>>>>调用移通类短信API发送短信,手机个数： {},content :{},isMarketing:{}",mobiles.length,content,isMarketing);
    	if(mobiles == null || mobiles.length == 0) return -1;
    	if(StringUtil.isBlank(content)) return -1;
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("dc", YT_MLINK_DC);
		param.put("sm", contentByHex(content));	
		String url = "";
		if(isMarketing){
			param.put("spid", ETConfig.mlinkMarketingSPId);	
			param.put("sppassword", ETConfig.mlinkMarketingSPPwd);
			url = ETConfig.mlinkMarketingHost;
		}else{
			param.put("spid", ETConfig.mlinkSPId);	
			param.put("sppassword", ETConfig.mlinkSPPwd);
			url = ETConfig.mlinkHost;
		}
		if(mobiles.length == 1){
			param.put("da", convertMobiles(mobiles[0]));
			param.put("command", YT_MLINK_COMMAND_SINGLE);
		}else{
			String ms = StringUtils.join(mobiles, ",");
			param.put("das", convertMobiles(ms));
			param.put("command", YT_MLINK_COMMAND_MULTI);
		}
	    String  result = HttpUtil.postForm(url, param);
	    log.info(">>>>>>调用移通类短信API发送短信,result:{}",result);
	    return handleResult(result)?1:0;
    }
    
    /**
     * 发送多号码多内容短信
     * @param mobileAndContent：手机号码和内容之间用/隔开
     * @return
     */
    public static int sendMTSms(List<MobileContent> mobileAndContent){
    	if(mobileAndContent == null || mobileAndContent.size() == 0) return -1;
    	log.info(">>>>>>调用移通类短信API发送短信,mobileAndContent ： {}",mobileAndContent.size());
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("command", "MULTIX_MT_REQUEST");
		param.put("dc", YT_MLINK_DC);
		param.put("dasm", convertMobileContent(mobileAndContent));
		param.put("spid", ETConfig.mlinkMarketingSPId);	
		param.put("sppassword", ETConfig.mlinkMarketingSPPwd);
		String url = ETConfig.mlinkMarketingHost;
	    String  result = HttpUtil.postForm(url, param);
	    log.info(">>>>>>调用移通类短信API发送短信,result:{}",result);
	    return handleResult(result)?1:0;
    }
    
    private static String contentByHex(String content){
    	try{
    		content = content.replace("【摇旺理财】", "").replace("[摇旺理财]", "").replace("[摇旺]", "").replace("【摇旺】", "");
    		return new String(Hex.encodeHex(content.getBytes("GBK")));
    	}catch(Exception e){
    		 log.info(">>>>>>调用移通API发送短信,消息内容转换HEX格式异常:{}",e.getMessage());
    		 return content;
    	}
    }
    
    private static String convertMobiles(String mobiles){
    	mobiles = "86"+mobiles.replace(",1", ",861");
    	return mobiles;
    }
    
    /**
     * 根据KEY查询调用移通的结果值
     * @param msg
     * @param key
     * @return
     */
    private static String getMTResValueByKey(String msg ,String key){
    	if(StringUtil.isBlank(msg) || StringUtil.isBlank(key)) return null;
    	String[] msgs = msg.split("&");
    	String value = null;
    	for(int i = 0;i<msgs.length;i++){
    		String[] ks = msgs[i].split("=");
    		if(ks[0].trim().equals(key)){
    			value = ks[1].trim();
    			break;
    		}
    	}
    	return value;
    }
    
    private static boolean handleResult(String result){
    	String code = getMTResValueByKey(result,"mterrcode");
    	if(StringUtil.equals(code, "000"))return true;
    	return false;
    }
    
    private static String convertMobileContent(List<MobileContent> mobileAndContent){
    	StringBuilder sb = new StringBuilder();
    	for(MobileContent mc : mobileAndContent){
    		sb.append("86").append(mc.getMobile());
    		sb.append("/");
    		sb.append(contentByHex(mc.getContent())).append(",");
    	}
    	return sb.toString().substring(0,sb.toString().length()-1);
    }
}