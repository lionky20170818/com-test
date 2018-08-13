/**
 * 软件著作权：百富金融技术部
 * 
 * 系统名称：互联网金融项目
 * 
 */
package com.ligl.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类.
 * @author 陈宝露
 * @version 1.0.0
 */
public class DateUtils {

    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);
    public static String HOUR_END = " 23:59:59";
	public static String HOUR_START = " 00:00:00";

    /** yyyy **/
    private static String year = "yyyy"; 
    /** MM **/
    private static String month = "MM";   
    /** dd **/
    private static String day = "dd"; 
    
    /** yyyy-MM-dd **/
    private static String ymd = "yyyy-MM-dd";
    /** yyyyMMdd **/
    private static String ymd2 = "yyyyMMdd";  
    
    /** yyyy-MM-dd HH:mm **/
	private static String ymdhm = "yyyy-MM-dd HH:mm";
	/** yyyy-MM-dd HH:mm:ss **/
	private static String ymdhms = "yyyy-MM-dd HH:mm:ss";

    public final static String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SS";
    /** yyyyMMddHHmmss **/
    public static String ymdhms2 = "yyyyMMddHHmmss";
    
    /** yyyy **/
    public static SimpleDateFormat yearSDF = new SimpleDateFormat(year);   
    /** MM **/
    public static SimpleDateFormat monthSDF = new SimpleDateFormat(month);   
    /** dd **/
    public static SimpleDateFormat daySDF = new SimpleDateFormat(day); 
    
    /** yyyy-MM-dd **/
    public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat(ymd);   
    /** yyyyMMdd **/
    public static SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat(ymd2); 
    
    /** yyyy-MM-dd HH:mm:ss  **/
    public static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);  
    /** yyyyMMddHHmmss **/
    public static SimpleDateFormat yyyyMMddHHmmss2 = new SimpleDateFormat(ymdhms2); 
    /** yyyy-MM-dd HH:mm **/
    public static SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat(ymdhm);   
    
    /** yyyy-MM-dd HH:mm:ss:SSS  **/
    public static SimpleDateFormat yyyyMMddHHmmsss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");  
    /** yyyyMMddHHmmssSSS **/
    public static SimpleDateFormat yyyyMMddHHmmsss2 = new SimpleDateFormat("yyyyMMddHHmmssSSS"); 
      
    /** 86400L **/
    public static long DATEMM = 86400L; 
    
    
    
    /** 
     *获取当前的年、月、日 . <br/>
     * @return String 
     */ 
    public static String getCurrentYear() {   
        return yearSDF.format(new Date());   
    }  
    public static String getCurrentMonth() {   
        return monthSDF.format(new Date());   
    }  
    public static String getCurrentDay() {   
        return daySDF.format(new Date());   
    }   
    
    /** 
     * 获得当前时间 . <br/>
     * 格式：yyyy-MM-dd HH:mm:ss，如：2014-12-02 10:38:53 
     * @return String 
     */ 
    public static String getCurrentTimeYMDHMS() {   
        return yyyyMMddHHmmss.format(new Date());   
    } 
    
    /**
     * 获得当前时间 . <br/>
     * 格式：yyyyMMddHHmmss，如：20141202103853
     * @return
     */
    public static String getCurrentTimeString() {   
        return yyyyMMddHHmmss2.format(new Date());   
    }  
    
    /** 
     * 获得当前时间 . <br/>
     * 格式：yyyy-MM-dd HH:mm:ss:SSS，如：2014-12-02 10:38:53:111
     * @return String 
     */ 
    public static String getCurrentTimeYMDHMSS() {   
        return yyyyMMddHHmmsss.format(new Date());   
    } 
    
    /** 
     * 获得当前时间 . <br/>
     * 格式：yyyyMMddHHmmssSSS，如：20141202103853111
     * @return String 
     */ 
    public static String getStrCurrentTimeYMDHMSS() {   
        return yyyyMMddHHmmsss2.format(new Date());   
    } 
    
    /** 
     * 获取当前时间，年月日. <br/>
     * 格式：yyyy-MM-dd，如：2014-12-02 
     * @return String 
     */ 
    public static String getCurrentYMD() {   
        return yyyyMMdd.format(new Date());   
    } 
    
    /** 
     * 获取今天的日期 . <br/>
     * 格式：yyyyMMdd，如：20141202 
     * @return String 
     */ 
    public static String getTodateString() {   
        String str = yyyyMMddHH_NOT_.format(new Date());   
        return str;   
    }  
    
    /** 
     * 获取昨天的日期,年月日 . <br/>
     * 格式：yyyy-MM-dd，如：2014-12-01 
     * @return String 
     */ 
    public static String getYesterdayYMD() {   
        Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);   
        String str = yyyyMMdd.format(date);   
        try {   
            date = yyyyMMddHHmmss.parse(str + " 00:00:00");   
            return yyyyMMdd.format(date);   
        } catch (ParseException e) {   
            e.printStackTrace();   
        }   
        return "";   
    }   
    
    /** 
     * 获取昨天的日期 ，年月日. <br/>
     * 格式：yyyyMMdd，如：20141201 
     * @return String 
     */ 
    public static String getYesterdayString() {   
        Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);   
        String str = yyyyMMddHH_NOT_.format(date);   
        return str;   
    }  
    
    /**   
     * 获得今天零点   . <br/>
     *    
     * @return Date 
     */   
    public static Date getTodayZeroHour() {   
        Calendar cal = Calendar.getInstance();   
        cal.set(Calendar.SECOND, 0);   
        cal.set(Calendar.MINUTE, 0);   
        cal.set(Calendar.HOUR, 0);   
        return cal.getTime();   
    }  
    
    /**   
     * 获得昨天零点   . <br/>
     *    
     * @return Date 
     */   
    public static Date getYesterDayZeroHour() {   
        Calendar cal = Calendar.getInstance();   
        cal.add(Calendar.DATE, -1);   
        cal.set(Calendar.SECOND, 0);   
        cal.set(Calendar.MINUTE, 0);   
        cal.set(Calendar.HOUR, 0);   
        return cal.getTime();   
    }   
    
    /** 
     * 获取今天0点开始的秒数 . <br/>
     * @return long 
     */ 
    public static long getTimeNumberToday() {   
        Date date = new Date();   
        String str = yyyyMMdd.format(date);   
        try {   
            date = yyyyMMdd.parse(str);   
            return date.getTime() / 1000L;   
        } catch (ParseException e) {   
            e.printStackTrace();   
        }   
        return 0L;   
    }  
    
    /**   
     * 获得昨天23时59分59秒   . <br/>
     *    
     * @return   
     */   
    public static Date getYesterDay24Hour() {   
        Calendar cal = Calendar.getInstance();   
        cal.add(Calendar.DATE, -1);   
        cal.set(Calendar.SECOND, 59);   
        cal.set(Calendar.MINUTE, 59);   
        cal.set(Calendar.HOUR, 23);   
        return cal.getTime();   
    }   
    
    /**   
     * 获得指定日期所在的自然周的第一天，即周日   . <br/>
     *    
     * @param date   
     *            日期   
     * @return 自然周的第一天   
     */   
    public static Date getStartDayOfWeek(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.set(Calendar.DAY_OF_WEEK, 1);   
        date = c.getTime();   
        return date;   
    }   
     
    /**   
     * 获得指定日期所在的自然周的最后一天，即周六   . <br/>
     *    
     * @param date   
     * @return   
     */   
    public static Date getLastDayOfWeek(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.set(Calendar.DAY_OF_WEEK, 7);   
        date = c.getTime();   
        return date;   
    }   
     
    /**   
     * 获得指定日期所在当月第一天   . <br/>
     *    
     * @param date   
     * @return   
     */   
    public static Date getStartDayOfMonth(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.set(Calendar.DAY_OF_MONTH, 1);   
        date = c.getTime();   
        return date;   
    }   
     
    /**   
     * 获得指定日期所在当月最后一天   . <br/>
     *    
     * @param date   
     * @return   
     */   
    public static Date getLastDayOfMonth(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.set(Calendar.DATE, 1);   
        c.add(Calendar.MONTH, 1);   
        c.add(Calendar.DATE, -1);   
        date = c.getTime();   
        return date;   
    }   
    
    /**
	 * 获取某月的最后一天. <br/> 
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getLastDayOfMonth(int year,int month)
	{
	    Calendar cal = Calendar.getInstance();
	    //设置年份
	    cal.set(Calendar.YEAR,year);
	    //设置月份
	    cal.set(Calendar.MONTH, month-1);
	    //获取某月最大天数
	    int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	    //设置日历中月份的最大天数
	    cal.set(Calendar.DAY_OF_MONTH, lastDay);
	    //格式化日期
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    String lastDayOfMonth = sdf.format(cal.getTime());
	    return lastDayOfMonth;
	}
     
    /**   
     * 获得指定日期的下一个月的第一天   . <br/>
     *    
     * @param date   
     * @return   
     */   
    public static Date getStartDayOfNextMonth(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.add(Calendar.MONTH, 1);   
        c.set(Calendar.DAY_OF_MONTH, 1);   
        date = c.getTime();   
        return date;   
    }   
     
    /**   
     * 获得指定日期的下一个月的最后一天   . <br/>
     *    
     * @param date   
     * @return   
     */   
    public static Date getLastDayOfNextMonth(Date date) {   
        Calendar c = Calendar.getInstance();   
        c.setTime(date);   
        c.set(Calendar.DATE, 1);   
        c.add(Calendar.MONTH, 2);   
        c.add(Calendar.DATE, -1);   
        date = c.getTime();   
        return date;
    }

    public static String format(Date date, String format) {
        try {
            SimpleDateFormat e = new SimpleDateFormat(format);
            return e.format(date);
        } catch (Exception var3) {
            logger.error(String.format("将日期按格式%s格式化失败", new Object[]{format}), var3);
            return null;
        }
    }
     
    /**   
     *    
     * 求某一个时间向前多少秒的时间(currentTimeToBefer)  . <br/> 
     *    
     * @param givedTime   
     *            给定的时间   
     * @param interval   
     *            间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)   
     * @param format_Date_Sign   
     *            输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；   
     */   
    public static String givedTimeToBefer(String givedTime, long interval,   
            String format_Date_Sign) {   
        String tomorrow = null;   
        try {   
            SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);   
            Date gDate = sdf.parse(givedTime);   
            long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒   
            long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒   
            Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2   
            tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);   
        } catch (ParseException e) {   
            e.printStackTrace();   
        }   
        return tomorrow;   
    }   
    
    
     
    /**   
     * 得到二个日期间的间隔日期；. <br/>   
     *    
     * @param endTime   
     *            结束时间   
     * @param beginTime   
     *            开始时间   
     * @param isEndTime   
     *            是否包含结束日期；   
     * @return   
     */   
    public static Map<String, String> getTwoDay(String endTime,   
            String beginTime, boolean isEndTime) {   
        Map<String, String> result = new HashMap<String, String>();   
        if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime   
                .equals(""))))   
            return null;   
        try {   
            java.util.Date date = yyyyMMdd.parse(endTime);   
            endTime = yyyyMMdd.format(date);   
            java.util.Date mydate = yyyyMMdd.parse(beginTime);   
            long day = (date.getTime() - mydate.getTime())   
                    / (24 * 60 * 60 * 1000);   
            result = getDate(endTime, Integer.parseInt(day + ""), isEndTime);   
        } catch (Exception e) {   
        }   
        return result;   
    }   
     
    /**   
     * 得到二个日期间的间隔日期；   . <br/>
     *    
     * @param endTime   
     *            结束时间   
     * @param beginTime   
     *            开始时间   
     * @param isEndTime   
     *            是否包含结束日期；   
     * @return   
     */   
    public static Integer getTwoDayInterval(String endTime, String beginTime,   
            boolean isEndTime) {   
        if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime   
                .equals(""))))   
            return 0;   
        long day = 0l;   
        try {   
            java.util.Date date = yyyyMMdd.parse(endTime);   
            endTime = yyyyMMdd.format(date);   
            java.util.Date mydate = yyyyMMdd.parse(beginTime);   
            day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);   
        } catch (Exception e) {   
            return 0 ;   
        }   
        return Integer.parseInt(day + "");   
    }   
    
    /** 
     * 判断时间是否在时间段内 . <br/>
     *  
     * @param date 
     *            当前时间 yyyy-MM-dd HH:mm:ss 
     * @param strDateBegin 
     *            开始时间 00:00:00 
     * @param strDateEnd 
     *            结束时间 00:05:00 
     * @return 
     */  
    public static boolean isInDate(Date date, String strDateBegin,  
            String strDateEnd) {  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        String strDate = sdf.format(date);  
        // 截取当前时间时分秒  
        int strDateH = Integer.parseInt(strDate.substring(11, 13));  
        int strDateM = Integer.parseInt(strDate.substring(14, 16));  
        int strDateS = Integer.parseInt(strDate.substring(17, 19));  
        // 截取开始时间时分秒  
        int strDateBeginH = Integer.parseInt(strDateBegin.substring(0, 2));  
        int strDateBeginM = Integer.parseInt(strDateBegin.substring(3, 5));  
        int strDateBeginS = Integer.parseInt(strDateBegin.substring(6, 8));  
        // 截取结束时间时分秒  
        int strDateEndH = Integer.parseInt(strDateEnd.substring(0, 2));  
        int strDateEndM = Integer.parseInt(strDateEnd.substring(3, 5));  
        int strDateEndS = Integer.parseInt(strDateEnd.substring(6, 8));  
        if ((strDateH >= strDateBeginH && strDateH <= strDateEndH)) {  
            // 当前时间小时数在开始时间和结束时间小时数之间  
            if (strDateH > strDateBeginH && strDateH < strDateEndH) {  
                return true;  
                // 当前时间小时数等于开始时间小时数，分钟数在开始和结束之间  
            } else if (strDateH == strDateBeginH && strDateM >= strDateBeginM  
                    && strDateM <= strDateEndM) {  
                return true;  
                // 当前时间小时数等于开始时间小时数，分钟数等于开始时间分钟数，秒数在开始和结束之间  
            } else if (strDateH == strDateBeginH && strDateM == strDateBeginM  
                    && strDateS >= strDateBeginS && strDateS <= strDateEndS) {  
                return true;  
            }  
            // 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数小等于结束时间分钟数  
            else if (strDateH >= strDateBeginH && strDateH == strDateEndH  
                    && strDateM <= strDateEndM) {  
                return true;  
                // 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数等于结束时间分钟数，秒数小等于结束时间秒数  
            } else if (strDateH >= strDateBeginH && strDateH == strDateEndH  
                    && strDateM == strDateEndM && strDateS <= strDateEndS) {  
                return true;  
            } else {  
                return false;  
            }  
        } else {  
            return false;  
        }  
    } 
    
    /**
     * 判断日期时间是否在日期时间段内. <br/>
     *
     * @param currentDate
     * 				当前时间，如2017-05-01 12:00:00
     * @param strDateBegin
     * 				开始时间，如2017-04-01 12:00:00
     * @param strDateEnd
     * 				结束时间，如2017-06-01 12:00:00
     * @return
     */
    public static boolean isInDate2(Date currentDate, Date strDateBegin,  
    		Date strDateEnd) { 
    	long dateCurrent = currentDate.getTime();
    	long dateBegin = strDateBegin.getTime();
    	long dateEnd = strDateEnd.getTime();
    	if (dateBegin<=dateCurrent && dateCurrent<=dateEnd) {
    		return true;
    	}
    	return false;
    } 
     
    /**   
     * 根据结束时间以及间隔差值，求符合要求的日期集合；   . <br/>
     *    
     * @param endTime   
     * @param interval   
     * @param isEndTime   
     * @return   
     */   
    public static Map<String, String> getDate(String endTime, Integer interval,   
            boolean isEndTime) {   
        Map<String, String> result = new HashMap<String, String>();   
        if (interval == 0 || isEndTime) {   
            if (isEndTime)   
                result.put(endTime, endTime);   
        }   
        if (interval > 0) {   
            int begin = 0;   
            for (int i = begin; i < interval; i++) {   
                endTime = givedTimeToBefer(endTime, DATEMM, ymd);   
                result.put(endTime, endTime);   
            }   
        }   
        return result;   
    }    



	/**
     * 获取n年后的某天的开始时间
     * 
     * @return
     */
    public static Date getNYearAfter(int n,Date date) {

        Calendar c = Calendar.getInstance();
        c.setTime(date);        
        c.add(Calendar.YEAR, n);
        return startOneDay(c.getTime());
    }
    
    /**
     * 某天的开始时间. <br/>
     * @param date
     * @return
     */
    public static Date startOneDay(Date date) {

		try {
			String halfFormat = yyyyMMdd.format(date);
			return yyyyMMddHHmmss.parse(halfFormat + HOUR_START);
		} catch (ParseException e) {
			return date;
		}
	}
    /**
     * 某天的结束时间. <br/>
     * @param date
     * @return
     */
    public static Date endOneDay(Date date) {

		try {
			String halfFormat = yyyyMMdd.format(date);
			return yyyyMMddHHmmss.parse(halfFormat + HOUR_END);
		} catch (ParseException e) {
			return date;
		}
	}
	
	/** 
     * 获取后退N天的日期 . <br/>
     * 格式：传入2 得到2014-11-30 
     * @param backDay 
     * @return String 
     */ 
    public String getStrDate(String backDay) { 
        Calendar calendar = Calendar.getInstance() ; 
        calendar.add(Calendar.DATE, Integer.parseInt("-" + backDay)); 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd") ; 
        String back = sdf.format(calendar.getTime()) ; 
        return back ; 
    } 
	
	
	/**   
     * long To (指定格式的)String . <br/>
     *    
     * @param date   
     *            long型日期；   
     * @param format   
     *            日期格式；   
     * @return   
     */   
    public static String longToString(long date, String format) {   
        SimpleDateFormat sdf = new SimpleDateFormat(format);   
        // 前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型   
        java.util.Date dt2 = new Date(date * 1000L);   
        String sDateTime = sdf.format(dt2); // 得到精确到秒的表示：08/31/2006 21:08:00   
        return sDateTime;   
    }   
    
    /**   
     * String To （指定格式的）Date   . <br/>
     *    
     * @param date   
     *            待转换的字符串型日期；   
     * @param format   
     *            转化的日期格式   
     * @return 返回该字符串的日期型数据；   
     */   
    public static Date stringToDate(String date, String format) {   
        SimpleDateFormat sdf = new SimpleDateFormat(format);   
        try {   
            return sdf.parse(date);   
        } catch (ParseException e) {   
            return null;   
        }   
    } 
    
    /**   
     * String To （指定格式的）long   . <br/>
     *    
     * @param date   
     *            String 型日期；   
     * @param format   
     *            日期格式；   
     * @return   
     */   
    public static long stringToLong(String date, String format) {   
        SimpleDateFormat sdf = new SimpleDateFormat(format);   
        Date dt2 = null;   
        long lTime = 0;   
        try {   
            dt2 = sdf.parse(date);   
            // 继续转换得到秒数的long型   
            lTime = dt2.getTime() / 1000;   
        } catch (ParseException e) {   
            e.printStackTrace();   
        }   
     
        return lTime;   
    }   
    
    
    /**   
     * Date To （指定格式的）String   . <br/>
     *    
     * @param date   
     *            待转换的字符串型日期；   
     * @param format   
     *            转化的日期格式   
     * @return 返回字符串型数据；   
     */   
    public static String dateToString(Date date, String format) {   
        SimpleDateFormat sdf = new SimpleDateFormat(format);   
        try {   
            return sdf.format(date);   
        } catch (Exception e) {   
            return null;   
        }
    }

    public static Date now() {
        return new Date();
    }

    /**
     * 转换日期
     *
     * @param str
     * @param format
     * @return
     */
    public static Date parse(String str, String format) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(str);
        } catch (Exception e) {
            logger.error(String.format("将日期%s按格式%s转换为日期失败", str, format), e);
        }
        return null;
    }

    /**
     * 格式化日期
     *
     * @param date
     * @param :yyyyMMddhhmmss
     * @return
     */
    public static String formatDateNotNull(Date date, String format) {
        try {
            if (date == null) {
                return "";
            }
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        } catch (Exception e) {
            logger.error(String.format("将日期按格式%s格式化失败", format), e);
        }
        return null;
    }

    /**
     * 比较日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDay(Date date1, Date date2) {
        date1 = parse(format(date1, "yyyyMMdd"), "yyyyMMdd");
        date2 = parse(format(date2, "yyyyMMdd"), "yyyyMMdd");
        return compare(date1, date2);
    }

    /**
     * 比较日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compare(Date date1, Date date2) {
        if (date1.getTime() > date2.getTime()) {
            return 1;
        } else if (date1.getTime() < date2.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }

//    @Deprecated
//    public static int daysBetween(Date date1, Date date2) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date1);
//        int day1 = calendar.get(Calendar.DAY_OF_YEAR);
//        calendar.setTime(date2);
//        int day2 = calendar.get(Calendar.DAY_OF_YEAR);
//        return day2 - day1;
//    }

    /**
     * 取得两个时间段的时间间隔
     * return t2 与t1的间隔天数
     * throws ParseException 如果输入的日期格式不是0000-00-00 格式抛出异常
     */
    public static int getBetweenDays(String t1, String t2) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = format.parse(t1);
        Date d2 = format.parse(t2);
        return getBetweenDays(d1, d2);
    }

    /**
     * 取得两个时间段的时间间隔
     * return t2 与t1的间隔天数
     * throws ParseException 如果输入的日期格式不是0000-00-00 格式抛出异常
     */
    public static int getBetweenDays(Date d1, Date d2) throws ParseException {
        int betweenDays = 0;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        // 保证第二个时间一定大于第一个时间
        if (c1.after(c2)) {
            c1 = c2;
            c2.setTime(d1);
        }
        int betweenYears = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        betweenDays = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < betweenYears; i++) {
            c1.set(Calendar.YEAR, (c1.get(Calendar.YEAR) + 1));
            betweenDays += c1.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
        return betweenDays;
    }

    public static int dateCompare(Date dt1, Date dt2) {
        try {
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static boolean isAfter(Date dt1, Date dt2) {
        if (dateCompare(dt1, dt2) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isAfterOrEqual(Date dt1, Date dt2) {
        if (dateCompare(dt1, dt2) == 1 || dateCompare(dt1, dt2) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isBeforeOrEqual(Date dt1, Date dt2) {
        if (dateCompare(dt1, dt2) == -1 || dateCompare(dt1, dt2) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isBefore(Date dt1, Date dt2) {
        if (dateCompare(dt1, dt2) == -1) {
            return true;
        }
        return false;
    }

    /** 测试 **/
	public static void main(String[] args) {
//		System.out.println("获取当前时间（精确到毫秒），getCurrentTimeYMDHMSS() => " + getCurrentTimeYMDHMSS());
//		System.out.println(startOneDay(new Date()));
//		System.out.println("获取今天0点开始的秒数,getTimeNumberToday() => " + getTimeNumberToday());
//		System.out.println("获得昨天23时59分59秒,getYesterDay24Hour() => " + getYesterDay24Hour());
//		System.out.println("获取今天零点，getTodayZeroHour() => " + getTodayZeroHour());
//		System.out.println("根据结束时间以及间隔差值，求符合要求的日期集合,getDate('2017-04-10 12:12:12',2,true) => " + getDate("2017-04-10 12:12:12",2,true) );
//		System.out.println("获取某月的最后一天,getLastDayOfMonth(2015,9) => "+getLastDayOfMonth(2015,9));
		Date date1 = stringToDate("2017-05-01 12:00:00","yyyy-MM-dd HH:mm:ss");
		Date date2 = stringToDate("2017-05-08 12:00:00","yyyy-MM-dd HH:mm:ss");
		System.out.println("判断当前时间是否在某个时间段内,isInDate：\n" 
				+ "date1="+date1.getTime()+"\n" 
				+ "date2="+date2.getTime()+"\n" 
				+ isInDate2(new Date(),date1,date2));
		
		System.out.println(new Date("20170811"));
	}
}
