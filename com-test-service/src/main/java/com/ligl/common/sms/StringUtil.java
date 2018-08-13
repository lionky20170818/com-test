package com.ligl.common.sms;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串工具类
 * @author zhuss
 * @2016年8月26日 下午2:27:20
 */
public class StringUtil {
	
	public static final String ZERO = "0";
	public static final String ONE = "1";
	public static final String TWO = "2";
	public static final String EMPTY = "";
	public static final String ADD_STR = "新增";
	public static final String UPDATE_STR = "更新";

	private StringUtil(){

	}
	/**
	 * 重写判断对象比较
	 * @return
	 */
	public static boolean equals(Object obj1,Object obj2){
		if(null == obj1 || null == obj2) return false;
		if(obj1 instanceof String || obj2 instanceof String)return obj1.toString().trim().equalsIgnoreCase(obj2.toString().trim());
		return obj1.equals(obj2);
	}
	
	/**
	 * 重写判断字符串空
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str){
		if(null == str || str.equals("") || str.equals(" ") || str.equals("null") || str.equals("undefined")){
			return true;
		}
		return false;
	}
	
	/**
	 * 重写判断字符串非空
	 * @param str
	 * @return
	 */
	public static boolean isNotBlank(String str){
		if(null != str && !str.equals("") && !str.equals(" ") && !str.equals("null")&& !str.equals("undefined") && !str.equals("\"\"")) {
			return true;
		}
		return false;
	}
	
	 /**
     * 设置默认值
     */
    public static String setDefault(String val,String defaultVal) {
        if (StringUtil.isBlank(val))return defaultVal;
        return val;
    }
    
	/**
	 * 空字符串对象转换成null
	 * @param str
	 * @return
	 */
	public static String getNullByStr(String str){
		if("".equals(str)) return null;
		return str;
	}
	
	
	/**
	 * String转换成Integer
	 * @param str
	 * @return
	 */
	public static Integer getIntegerByStr(String str){
		if(StringUtil.isBlank(str)) return null;
		return Integer.valueOf(str.trim());
	}
	
	/**
	 * String转换成Double
	 * @param str
	 * @return
	 */
	public static Double getDoubleByStr(String str){
		if(StringUtil.isBlank(str)) return null;
		return Double.parseDouble(str.trim());
	}

	public static String mobile(String mobile){
		if(mobile==null) return mobile;
		if(mobile.length()>=11){
			StringBuilder sb = new StringBuilder();
			sb.append(mobile.substring(0,3)).append("****").append(mobile.substring(7));
			return sb.toString();
		}
		return mobile;
	}

	public static String trim(String str){
		if(str == null) return null;
		return str.trim();
	}
	
	/**
	 * list拆分
	 * @param list
	 * @param pageSize
	 * @return
	 */
	public static <T> List<List<T>> splitList(List<T> list, int pageSize) {
		List<List<T>> listArray = new ArrayList<>();
		if(pageSize<=0){
			return listArray;
		}
        int listSize = list.size();                                                           
        int page = (listSize + (pageSize-1))/ pageSize;                      
        
        for(int i=0;i<page;i++) {                                        
            List<T> subList = new ArrayList<>();
            for(int j=0;j<listSize;j++) {                                
                int pageIndex = ( (j + 1) + (pageSize-1) ) / pageSize;   
                if(pageIndex == (i + 1)) {                               
                    subList.add(list.get(j));                            
                }
                if( (j + 1) == ((j + 1) * pageSize) ) {                  
                    break;
                }
            }
            listArray.add(subList);                                      
        }
        return listArray;
    }
}
