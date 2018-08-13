package com.ligl.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName: CheckUtils <br/>
 * Function: 入参校验工具. <br/>
 * Date: 2017年5月2日 上午10:17:33 <br/>
 *
 * @author liguoliang
 * @version 
 * @since JDK 1.7
 */
public class CheckUtils {
	
	/**
	 * 判断电话. <br/>
	 *
	 * @param phonenumber
	 * @return
	 */
    public static boolean isTelephone(String phonenumber) {
        String phone = "0\\d{2,3}-\\d{7,8}";
        Pattern p = Pattern.compile(phone);
        Matcher m = p.matcher(phonenumber);
        return m.matches();
    }
    
    /**
     * 判断手机号. <br/>
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
    
    /**
     * 判断邮箱. <br/>
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
    
    /**
     * 判断日期格式:yyyy-mm-dd. <br/>
     *
     * @param sDate
     * @return
     */
    public static boolean isValidDate(String sDate) {
        String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";
        String datePattern2 = "^((\\d{2}(([02468][048])|([13579][26]))"
                + "[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|"
                + "(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?"
                + "((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?("
                + "(((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?"
                + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
        if ((sDate != null)) {
            Pattern pattern = Pattern.compile(datePattern1);
            Matcher match = pattern.matcher(sDate);
            if (match.matches()) {
                pattern = Pattern.compile(datePattern2);
                match = pattern.matcher(sDate);
                return match.matches();
            } else {
                return false;
            }
        }
        return false;
    }
	
	/**
	 * 判断是否是数字. <br/>
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
	}
	
	/**
	 * 验证金额. <br/>
	 *
	 * @param str
	 * @return
	 */
    public static boolean isAmount(String str) 
    { 
    	//^(?!0+(?:\\.0+)?$)(?:[1-9]\\d*|0)(?:\\.\\d*)?$
    	// 正则表达式:小数可有可无,最多两位小数,必须大于零,^(?!0+(?:\.0+)?$)(?:[1-9]\d*|0)(?:\.\d{1,2})?$
    	Pattern pattern=Pattern.compile("^(?!0+(?:\\.0+)?$)(?:[1-9]\\d*|0)(?:\\.\\d{1,2})?$"); 
        Matcher match=pattern.matcher(str);   
        if(match.matches()==false)   
        {   
           return false;   
        }   
        else   
        {   
           return true;   
        }   
    }
    
    /*
     * 身份证校验：
     * 
     * 身份证15位编码规则：dddddd yymmdd xx p
     * dddddd：6位地区编码
     * yymmdd: 出生年(两位年)月日，如：910215
     * xx: 顺序编码，系统产生，无法确定
     * p: 性别，奇数为男，偶数为女
     * 
     * 身份证18位编码规则：dddddd yyyymmdd xxx y
     * dddddd：6位地区编码
     * yyyymmdd: 出生年(四位年)月日，如：19910215
     * xxx：顺序编码，系统产生，无法确定，奇数为男，偶数为女
     * y: 校验码，该位数值可通过前17位计算获得
     * 
     * 前17位号码加权因子为 Wi = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 ]
     * 验证位 Y = [ 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 ]
     * 如果验证码恰好是10，为了保证身份证是十八位，那么第十八位将用X来代替
     * 校验位计算公式：Y_P = mod( ∑(Ai×Wi),11 )
     * i为身份证号码1...17 位; Y_P为校验码Y所在校验码数组位置
     */
	public static boolean isIdCard(String idCard) {
		// 15位和18位身份证号码的正则表达式
		Pattern pattern = Pattern
				.compile("^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$");
		Matcher match = pattern.matcher(idCard);
		 if(match.matches()==false)
		 {
		 return false;
		 }
		 else
		 {
		 return true;
		 }

//		// 如果通过该验证，说明身份证格式正确，但准确性还需计算
//		if (match.matches() == true) {
//			if (idCard.length() == 18) {
//				int[] idCardWi = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 }; // 将前17位加权因子保存在数组里
//				int[] idCardY = { 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 }; // 这是除以11后，可能产生的11位余数、验证码，也保存成数组
//				int idCardWiSum = 0; // 用来保存前17位各自乖以加权因子后的总和
//				for (int i = 0; i < 17; i++) {
//					idCardWiSum += Integer.parseInt(idCard.substring(i, i + 1))
//							* idCardWi[i];
//				}
//
//				int idCardMod = idCardWiSum % 11;// 计算出校验码所在数组的位置
//				String idCardLast = idCard.substring(17);// 得到最后一位身份证号码
//
//				// 如果等于2，则说明校验码是10，身份证号码最后一位应该是X
//				if (idCardMod == 2) {
//					if (idCardLast == "X" || idCardLast == "x") {
//						System.out.println("恭喜通过验证啦！");
//						return true;
//					} else {
//						System.out.println("身份证号码错误！");
//						return false;
//					}
//				} else {
//					// 用计算出的验证码与最后一位身份证号码匹配，如果一致，说明通过，否则是无效的身份证号码
//					if (Integer.parseInt(idCardLast) == idCardY[idCardMod]) {
//						System.out.println("恭喜通过验证啦！");
//						return true;
//					} else {
//						System.out.println("身份证号码错误！");
//						return false;
//					}
//				}
//			}
//			return true;
//		} else {
//			System.out.println("身份证格式不正确!");
//			return false;
//		}
	}
    
    public static void main(String[] args) {
		System.out.println(CheckUtils.isAmount("11111.00")); 
//		System.out.println(CheckUtils.isIdCard("340223199011310922"));
	}

}
