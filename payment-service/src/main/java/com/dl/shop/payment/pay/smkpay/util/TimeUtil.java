package com.dl.shop.payment.pay.smkpay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * 时间/日期工具类
 */
public class TimeUtil {
	/** 格式转换类型: yyyyMMdd ==> yyyy/MM/dd */
	public final static int FORMAT_TYPE_D8_D10_S = 1;
	/** 格式转换类型: yyyyMMdd ==> yyyy-MM-dd */
	public final static int FORMAT_TYPE_D8_D10_ML = 2;
	/** 格式转换类型: yyyy?MM?dd ==> yyyyMMdd */
	public final static int FORMAT_TYPE_D10_D8 = 3;
	/** 格式转换类型: yyyyMMddhhmmss ==> yyyyMMdd */
	public final static int FORMAT_TYPE_T14_D8 = 4;
	/** 格式转换类型: yyyyMMddHHmmss ==> yyyy/MM/dd */
	public final static int FORMAT_TYPE_T14_D10_S = 5;
	/** 格式转换类型: yyyyMMddHHmmss ==> yyyy-MM-dd */
	public final static int FORMAT_TYPE_T14_D10_ML = 6;
	/** 格式转换类型: yyyyMMddHHmmss ==> yyyy-MM-dd HH:mm:ss */
	public final static int FORMAT_TYPE_T14_T17 = 7;
	/** 格式转换类型: yyyyMMdd HH:mm:ss ==> yyyyMMddHHmmss */
	public final static int FORMAT_TYPE_T17_T14 = 8;
	/** 格式转换类型: yyyyMMddHH:mm:ss ==> yyyy-MM-dd HH:mm:ss */
	public final static int FORMAT_TYPE_T14_T19 = 9;
	/** 格式转换类型: yyyyMMddHHmmss ==> HH:mm:ss */
	public final static int FORMAT_TYPE_T14_HMS = 10;
	/** 格式转换类型: yyyyMMddHHmmss ==> yyyyMMdd HH:mm:ss */
	public final static int FORMAT_TYPE_T14_T19_HMS = 11;
	/** 格式转换类型: yyyyMM ==> yyyy-MM */
	public final static int FORMAT_TYPE_D8_D10_YM = 12;
	/** 格式转换类型: yyyy ==> yyyy */
	public final static int FORMAT_TYPE_D8_D10_Y = 13;
	/** 格式转换类型: yyyyMMdd HH:mm:ss */
	public final static String FORMAT_STRING1 = "yyyyMMdd HH:mm:ss";
	/** 格式转换类型: yyyyMMdd */
	public final static String FORMAT_STRING2 = "yyyyMMdd";
	/** 格式转换类型: HH:mm:ss */
	public final static String FORMAT_STRING3 = "HHmmss";
	/** 格式转换类型: yyyyMMddHHmmss */
	public final static String FORMAT_STRING4 = "yyyyMMddHHmmss";
	/** 格式转换类型: yyyy-MM-dd HH:mm:ss */
	public final static String FORMAT_STRING5 = "yyyy-MM-dd HH:mm:ss";
	/** 格式转换类型: yyyy-MM-dd */
	public final static String FORMAT_STRING6 = "yyyy-MM-dd";
	/** 格式转换类型: HH:mm:ss */
	public final static String FORMAT_STRING7 = "HH:mm:ss";
	/** 格式转换类型 yyyyMMddHHmm */
	public final static String FORMAT_STRING8 = "yyyyMMddHHmm";

	/**
	 * 取得当前日期时间。 格式：yyyyMMddHHmmss
	 * 
	 * @return
	 */
	public static String getCurrDateTime() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		String strYear = dt.get(Calendar.YEAR) + "";
		int iTmp = dt.get(Calendar.MONTH) + 1;
		String strMonth = iTmp + "";
		String strDay = dt.get(Calendar.DAY_OF_MONTH) + "";
		String hour = (dt.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + dt.get(Calendar.HOUR_OF_DAY)
				: dt.get(Calendar.HOUR_OF_DAY) + "";
		String minute = (dt.get(Calendar.MINUTE) < 10) ? "0" + dt.get(Calendar.MINUTE) : dt.get(Calendar.MINUTE) + "";
		String second = (dt.get(Calendar.SECOND) < 10) ? "0" + dt.get(Calendar.SECOND) : dt.get(Calendar.SECOND) + "";
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(strYear, strMonth, strDay)).append(hour).append(minute).append(second);
		return sb.toString();
	}

	public static String currentTimeMillis() {
		Calendar CD = Calendar.getInstance();
		int YY = CD.get(Calendar.YEAR);
		int MM = CD.get(Calendar.MONTH) + 1;
		int DD = CD.get(Calendar.DATE);
		int HH = CD.get(Calendar.HOUR);
		int NN = CD.get(Calendar.MINUTE);
		int SS = CD.get(Calendar.SECOND);
		int MI = CD.get(Calendar.MILLISECOND);

		return YY + "-" + MM + "-" + DD + " " + HH + ": " + NN + ": " + SS + ": " + MI;
	}

	/**
	 * 取得当前日期时间。 格式：yyyyMMddHHmmssSSS
	 * 
	 * @return
	 */
	public static String currentDateTimeMillis() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		String strYear = dt.get(Calendar.YEAR) + "";
		int iTmp = dt.get(Calendar.MONTH) + 1;
		String strMonth = iTmp + "";
		String strDay = dt.get(Calendar.DAY_OF_MONTH) + "";
		String hour = (dt.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + dt.get(Calendar.HOUR_OF_DAY)
				: dt.get(Calendar.HOUR_OF_DAY) + "";
		String minute = (dt.get(Calendar.MINUTE) < 10) ? "0" + dt.get(Calendar.MINUTE) : dt.get(Calendar.MINUTE) + "";
		String second = (dt.get(Calendar.SECOND) < 10) ? "0" + dt.get(Calendar.SECOND) : dt.get(Calendar.SECOND) + "";
		String millisecond = ("000" + dt.get(Calendar.MILLISECOND)).substring(3);
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(strYear, strMonth, strDay)).append(hour).append(minute).append(second).append(millisecond);
		return sb.toString();
	}

	/**
	 * 格式化日期：YY
	 * 
	 * @param strYear
	 * @param strMonth
	 * @param strDay
	 * @return
	 */
	static public String dateFormat(String strYear, String strMonth, String strDay) {
		if (strYear == null || strYear.equals(""))
			return "";
		if (strMonth == null || strMonth.equals(""))
			return "";
		if (strDay == null || strDay.equals(""))
			return "";

		strYear = strYear.trim();// year must be 4 digits.

		strMonth = strMonth.trim();
		if (strMonth.length() == 1)
			strMonth = "0" + strMonth;

		strDay = strDay.trim();
		if (strDay.length() == 1)
			strDay = "0" + strDay;

		String strDate = strYear + strMonth + strDay;
		return strDate;
	}

	/**
	 * 取当前时间的小时
	 */
	public static int getHour() {
		Calendar c = new GregorianCalendar();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		return hour;

	}

	/**
	 * 对所提供的日期时间进行年、月、日的累加
	 * 
	 * @param aDate
	 *            原始日期 格式必需为：yyyyMMdd
	 * @param Day
	 *            需要累加的天数
	 * @param aYear
	 *            需要累加的年数
	 * @param amonth
	 *            需要累加的月数
	 * @return 累加完成的时间 yyyyMMdd
	 */

	public static String getAddDay(String aDate, int aYear, int aMonth, int aDay) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		if (aDate == null || aDate.length() != 8)
			return "";
		int tYear = Integer.parseInt(aDate.substring(0, 4));
		int tMonth = Integer.parseInt(aDate.substring(4, 6));
		int tDay = Integer.parseInt(aDate.substring(6, 8));
		calendar.set(tYear, tMonth, tDay);
		calendar.add(Calendar.DATE, aDay);
		calendar.add(Calendar.MONTH, aMonth - 1);
		calendar.add(Calendar.YEAR, aYear);
		return df.format(calendar.getTime());
	}

	/**
	 * 对所提供的日期时间进行年、月、日、时、分、秒的累加
	 * 
	 * @param aDate
	 *            原始日期 格式必需为：yyyyMMddHHmmss
	 * @param Day
	 *            需要累加的天数
	 * @param aYear
	 *            需要累加的年数
	 * @param amonth
	 *            需要累加的月数
	 * @return 累加完成的时间 yyyyMMdd
	 */

	public static String getAddTime(String aDate, int aYear, int aMonth, int aDay, int ah, int am, int as) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		if (aDate == null || aDate.length() != 14)
			return "";
		int tYear = Integer.parseInt(aDate.substring(0, 4));
		int tMonth = Integer.parseInt(aDate.substring(4, 6));
		int tDay = Integer.parseInt(aDate.substring(6, 8));
		int th = Integer.parseInt(aDate.substring(8, 10));
		int tm = Integer.parseInt(aDate.substring(10, 12));
		int ts = Integer.parseInt(aDate.substring(12, 14));
		calendar.set(tYear, tMonth, tDay, th, tm, ts);
		calendar.add(Calendar.DATE, aDay);
		calendar.add(Calendar.MONTH, aMonth - 1);
		calendar.add(Calendar.YEAR, aYear);
		calendar.add(Calendar.HOUR, ah);
		calendar.add(Calendar.MINUTE, am);
		calendar.add(Calendar.SECOND, as);
		return df.format(calendar.getTime());
	}

	/**
	 * 日期/时间格式转换
	 * 
	 * @param orgStr
	 * @param formatType
	 * @return
	 */
	public static String format(String orgStr, int formatType) {
		if (null == orgStr) {
			return null;
		}

		String result = null;
		StringBuffer temp = new StringBuffer();

		switch (formatType) {
		case FORMAT_TYPE_D8_D10_S: {
			if (orgStr.length() != 8) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append("/").append(orgStr.substring(4, 6)).append("/")
					.append(orgStr.substring(6, 8));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_D8_D10_ML: {
			if (orgStr.length() != 8) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append("-").append(orgStr.substring(4, 6)).append("-")
					.append(orgStr.substring(6, 8));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_D10_D8: {
			if (orgStr.length() != 10) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append(orgStr.substring(5, 7)).append(orgStr.substring(8, 10));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T14_D8: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 8));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T14_D10_S: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			result = format(format(orgStr, FORMAT_TYPE_T14_D8), FORMAT_TYPE_D8_D10_S);
			break;
		}
		case FORMAT_TYPE_T14_D10_ML: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			result = format(format(orgStr, FORMAT_TYPE_T14_D8), FORMAT_TYPE_D8_D10_ML);
			break;
		}
		case FORMAT_TYPE_T14_T17: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append("-").append(orgStr.substring(4, 6)).append("-")
					.append(orgStr.substring(6, 8)).append(" ").append(orgStr.substring(8, 10)).append(":")
					.append(orgStr.substring(10, 12)).append(":").append(orgStr.substring(12, 14));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T17_T14: {
			if (orgStr.length() != 17) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 8)).append(orgStr.substring(9, 11)).append(orgStr.substring(12, 14))
					.append(orgStr.substring(15, 17));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T14_T19: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(format(orgStr, TimeUtil.FORMAT_TYPE_T14_D10_ML)).append(" ").append(orgStr.substring(8, 10))
					.append(":").append(orgStr.substring(10, 12)).append(":").append(orgStr.substring(12, 14));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T14_HMS: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(8, 10)).append(":").append(orgStr.substring(10, 12)).append(":")
					.append(orgStr.substring(12, 14));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_T14_T19_HMS: {
			if (orgStr.length() != 14) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append(orgStr.substring(4, 6)).append(orgStr.substring(6, 8))
					.append(" ").append(orgStr.substring(8, 10)).append(":").append(orgStr.substring(10, 12))
					.append(":").append(orgStr.substring(12, 14));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_D8_D10_YM: {
			if (orgStr.length() != 6) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4)).append("-").append(orgStr.substring(4, 6));
			result = temp.toString();
			break;
		}
		case FORMAT_TYPE_D8_D10_Y: {
			if (orgStr.length() != 4) {
				throw new RuntimeException("Invalid date/time string format to be format.");
			}
			temp.append(orgStr.substring(0, 4));
			result = temp.toString();
			break;
		}
		default:
			throw new RuntimeException("Invalid data/time format type.");
		}

		return result;
	}

	/**
	 * 取得对账日期
	 */
	public static String getCheckupDate() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		dt.setTimeInMillis(dt.getTimeInMillis() - (24 * 60 * 60 * 1000));
		String strYear = dt.get(Calendar.YEAR) + "";
		String strMonth = dt.get(Calendar.MONDAY) + 1 + "";
		String strDay = dt.get(Calendar.DAY_OF_MONTH) + "";
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(strYear, strMonth, strDay));
		return sb.toString();
	}

	/**
	 * 取得当前日期的前20日
	 */
	public static String get20DateToNow() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		dt.setTimeInMillis(dt.getTimeInMillis() - (20 * 24 * 60 * 60 * 1000));
		String strYear = dt.get(Calendar.YEAR) + "";
		String strMonth = dt.get(Calendar.MONDAY) + 1 + "";
		String strDay = dt.get(Calendar.DAY_OF_MONTH) + "";
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(strYear, strMonth, strDay));
		return sb.toString();
	}

	/**
	 * 获取当前日期yyyyMMdd
	 * 
	 * @return
	 */
	public static String getCurrDate() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		String strYear = dt.get(Calendar.YEAR) + "";
		int iTmp = dt.get(Calendar.MONTH) + 1;
		String strMonth = iTmp + "";
		String strDay = dt.get(Calendar.DAY_OF_MONTH) + "";
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(strYear, strMonth, strDay));
		return sb.toString();
	}

	/**
	 * 获取当前时间hhMMdd
	 * 
	 * @return
	 */
	public static String getCurrTime() {
		Calendar dt = Calendar.getInstance(TimeZone.getDefault());
		String HOUR = dt.get(Calendar.HOUR) + "";
		String MINUTE = dt.get(Calendar.MINUTE) + "";
		String SECOND = dt.get(Calendar.SECOND) + "";
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat(HOUR, MINUTE, SECOND));
		return sb.toString();
	}

	/*public static String getAddDays(String date, int days) {
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(4, 6));
		int day = Integer.parseInt(date.substring(6, 8));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		Date dt = calendar.getTime();
		return DateUtil.formatDate(dt, "yyyyMMdd");
	}*/

	/**
	 * 
	 * @Title: dateToString
	 * @Description: Date转换成String
	 * @author yang_df
	 * @since 2014年5月15日
	 * @param date
	 *            日期
	 * @param formatString
	 *            转换格式，如yyyyMMdd
	 * @return
	 */
	public static String dateToString(Date date, String formatString) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatString);
		return sdf.format(date);
	}

	/**
	 * 
	 * 获取当前的日期 格式为yyyyMMdd
	 */
	public static String getDateTime() {
		String date = getCurrDateTime();
		String strSting = date.substring(0, 7);
		System.out.println("获取当前的日期为:" + strSting);
		return strSting;
	}

	public static String getDate(String dateString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdfn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(dateString);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return sdfn.format(date);
	}

	/**
	 * 
	 * @Title: getDaysToDate
	 * @Description:获取传入时间的前几天的日期
	 * @since 2014年6月16日
	 * @param date
	 *            传入的时间
	 * @param days
	 *            传入时间的前几天
	 * @return
	 */
	public static String getDaysToDate(Date date, int days) {
		SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - days);
		return dft.format(calendar.getTime());
	}

	/**
	 * 将字符串转换成Date
	 * 
	 * @param dateString
	 *            字符串：如20120303112233
	 * @param formatString
	 *            转换格式：如yyyyMMddHHmmss,格式要与dateString格式保持一致
	 * @return
	 */
	public static Date stringToDate(String dateString, String formatString) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(formatString);
			date = sdf.parse(dateString);
		}
		catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		return date;
	}

	/**
	 * 
	 * @Title: getNMinutesAgo
	 * @Description:n分钟之前
	 * @author yang_df
	 * @since 2014年12月23日
	 * @param minutes
	 * @return
	 */
	public static String getNMinutesAgo(int minutes) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar nowTime = Calendar.getInstance();
		nowTime.add(Calendar.MINUTE, minutes);
		return sdf.format(nowTime.getTime());
	}

	/**
	 * @Title: getNowYear
	 * @Description: 获取当前年份
	 * @author yang_df
	 * @since 2015-4-14
	 * @return
	 */
	public static String getNowYear() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		return dateFormat.format(date);
	}

	/**
	 * @Title: getCurrMonth
	 * @Description: 获取当前月份
	 * @author yang_df
	 * @since 2015年7月17日
	 * @return
	 */
	public static String getCurrMonth() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");// 可以方便地修改日期格式
		return dateFormat.format(date);
	}

	/**
	 * @Title: getNextMonth
	 * @Description:获取下一个月月份
	 * @author yang_df
	 * @since 2015年7月17日
	 * @return
	 */
	public static String getNextMonth() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 取得上一个时间
		calendar.set(Calendar.MONDAY, calendar.get(Calendar.MONDAY) + 1);
		return sdf.format(calendar.getTime());
	}

	/**
	 *
	 * @Title: getNowTime
	 * @Description:获取当前时间，转成dateTimeType 类型
	 * @since 2015年4月20日
	 * @param dateTimeType
	 * @return
	 */
	public static String getNowTime(String dateTimeType) {
		return new SimpleDateFormat(dateTimeType).format(new Date());
	}

	/**
	 * @Title: lastDayOfMonth
	 * @Description:根据输入日期，获取该月的最后一天日期
	 * @author yang_df
	 * @since 2015年8月18日
	 * @param date
	 * @return
	 */
	public static String lastDayOfMonth(Date date, String dateType) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.roll(Calendar.DAY_OF_MONTH, -1);
		return new SimpleDateFormat(dateType).format(cal.getTime());
	}

	/**
	 *
	 * @Title: getDayAfterOrBefore
	 * @Description:获得指定日期的前几天或者后几天
	 * @author hejianchun
	 * @since 2016-1-4
	 * @param specifiedDate
	 *            --指定日期(yyyyMMdd)
	 * @param days
	 *            ---天数
	 * @param isBefore
	 *            -- true--before| false--after
	 * @return
	 */
	public static String getDayAfterOrBefore(String specifiedDate, int days, Boolean isBefore) {

		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyyMMdd").parse(specifiedDate);
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, (true == isBefore) ? (day - days) : (day + days));
		String dayAfter = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
		return dayAfter;
	}

	/**
	 * @Title: checkTime
	 * @Description:date表示输入时间，date2表示当前时间
	 * @author zxx时间校验
	 * @since 2016年8月23日
	 * @return
	 */
	/*public static boolean checkTime(String date1, String date2, int eTime) {
		if (!StringUtil.isNullOfStrings(date1, date2)) {
			return false;
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date dt1;
		try {
			dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			long numbert = dt1.getTime() - dt2.getTime();
			long et = eTime * 60000;
			if (Math.abs(numbert) > et) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}*/

	/**
	 * @Title: checkTimegs
	 * @Description:day 日期 time时间
	 * @author zxx时间格式校验
	 * @since 2016年8月23日
	 * @return
	 */
	public static boolean checkTimegs(String day, String time) {
		boolean dayFlag = true;
		boolean timeFlag = true;
		if (null != day) {
			String d = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
			Pattern p = Pattern.compile(d);
			dayFlag = p.matcher(day).matches();
		}

		if (null != time) {
			String t = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
			Pattern s = Pattern.compile(t);
			timeFlag = s.matcher(time).matches();
		}
		if (dayFlag && timeFlag) {
			return true;
		}
		else
			return false;
	}

	/**
	 * @Title: compareTwoDate
	 * @Description: 比较两个日期大小，格式相同；
	 *
	 *               <0: first<second， =0: first=second， >0: first>second
	 * @author yang_df
	 * @since 2016年8月31日
	 * @change yang_df @date 2016年8月31日 @Description:
	 * @param first
	 * @param second
	 * @return
	 */
	public static int compareTwoDate(Date first, Date second) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(first);
		c2.setTime(second);

		return c1.compareTo(c2);
	}

	public static Date dateFormat(Date date, String formatString) {
		return null;
	}

	// 获取两个日子相隔的日期
	/**
	 * @Title: getDate
	 * @Description: 获取连个字段之间的日期
	 * 
	 * 
	 * @author yang_df
	 * @since 2016年8月31日
	 * @change yang_df @date 2016年8月31日 @Description:
	 * @param first
	 * @param second
	 * @return
	 * @throws ParseException
	 */
	public static List<String> getDate(String first, String second) throws ParseException {
		List<String> list = new ArrayList<String>();
		Calendar startCalendar = Calendar.getInstance();
		Calendar endCalendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date startDate = null;
		startDate = df.parse(first);
		startCalendar.setTime(startDate);
		Date endDate = df.parse(second);
		endCalendar.setTime(endDate);
		list.add(first);
		while (true) {
			startCalendar.add(Calendar.DAY_OF_MONTH, 1);
			if (startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				list.add(df.format(startCalendar.getTime()));
			}
			else {
				break;
			}
		}
		return list;
	}
	
	/**
	 * @Title: getDate
	 * @Description: 获取两个日期之间的天数
	 * 
	 * 
	 * @param first
	 * @param second
	 * @return
	 * @throws ParseException
	 */
	public static String getDays(String first, String second) throws ParseException {
        //时间转换类
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse(first);
        Date date2 = sdf.parse(second);
        //将转换的两个时间对象转换成Calendard对象
        Calendar can1 = Calendar.getInstance();
        can1.setTime(date1);
        Calendar can2 = Calendar.getInstance();
        can2.setTime(date2);
        //拿出两个年份
        int year1 = can1.get(Calendar.YEAR);
        int year2 = can2.get(Calendar.YEAR);
        //天数
        int days = 0;
        Calendar can = null;
        //如果can1 < can2
        //减去小的时间在这一年已经过了的天数
        //加上大的时间已过的天数
        if(can1.before(can2)){
            days -= can1.get(Calendar.DAY_OF_YEAR);
            days += can2.get(Calendar.DAY_OF_YEAR);
            can = can1;
        }else{
            days -= can2.get(Calendar.DAY_OF_YEAR);
            days += can1.get(Calendar.DAY_OF_YEAR);
            can = can2;
        }
        for (int i = 0; i < Math.abs(year2-year1); i++) {
            //获取小的时间当前年的总天数
            days += can.getActualMaximum(Calendar.DAY_OF_YEAR);
            //再计算下一年。
            can.add(Calendar.YEAR, 1);
        }
		return String.valueOf(days);

    }

	
	public static void main(String[] args) {
		System.out.println(getCurrDateTime());
	}

}
