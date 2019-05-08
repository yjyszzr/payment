package com.dl.shop.payment.pay.yiniupay.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * 公用的原子方法
 * 
 */
public class TdExpBasicFunctions {

	private static String DIGIT_UPPER = "零壹贰叁肆伍陆柒捌玖";

	private static String AMT_UPPER = "分角元拾佰仟万拾佰仟亿拾佰仟万拾佰仟亿";

	private static int ULEAD_MONTH_DAYS[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private static int LEAD_MONTH_DAYS[] = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	// 正数 EBCDIC格式
	private static String EBCD_POSITIVE = "{ABCDEFGHI";
	// 负数 EBCDIC格式
	private static String EBCD_NEGATIVE = "}JKLMNOPQR";

	/**
	 * <p>
	 * 将输入的字符串按顺序进行拼接处理
	 * </p>
	 * 
	 * @param arg0
	 *            字符串1
	 * @param arg1
	 *            字符串2
	 * @return 拼接后的字符串
	 */
	public static String STRCAT(String... args) {
		if (args.length < 2)
			throw new RuntimeException("STRCAT表达式至少有两个参数");
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			result.append(args[i]);
		}
		return result.toString();
	}

	public static String ENCODEBASE64(String src) {
		return new String(Base64.encodeBase64(src.getBytes()));
	}

	public static String DECODEBASE64(String dst) {
		return new String(Base64.decodeBase64(dst.getBytes()));
	}

	/**
	 * <p>
	 * 将输入的字符串转换为长整数
	 * </p>
	 * 
	 * @param arg0
	 *            字符串1
	 * @return 字符串转换的长整数
	 */
	public static long STRTOLONG(String args) {
		try {
			return Long.parseLong(args);
		} catch (NumberFormatException e) {
			throw new RuntimeException("表达式STRTOLONG参数不合法", e);
		}
	}

	/**
	 * <p>
	 * 将输入的字符串转换为整数
	 * </p>
	 * 
	 * @param arg0
	 *            字符串1
	 * @return 字符串转换的整数
	 */
	public static int STRTOINT(String args) {
		try {
			return Integer.parseInt(args);
		} catch (NumberFormatException e) {
			throw new RuntimeException("表达式STRTOINT参数不合法", e);
		}
	}

	/**
	 * <p>
	 * 定长字符串拼接 字符串拼接函数，将第二个字符串的开始n 位连接到第一个字符串后面。
	 * </p>
	 * 
	 * @param arg0
	 *            字符串1
	 * @param arg1
	 *            字符串2
	 * @param n
	 *            字符串2的头N位
	 * @return 拼接后的字符串
	 */
	public static String STRNCAT(String args1, String args2, int len) {
		if (StringUtils.isEmpty(args1))
			throw new RuntimeException("STRNCAT 参数不合法");
		StringBuffer result = new StringBuffer(args1);
		result.append(StringUtils.substring(args2, 0, len));
		return result.toString();
	}

	/**
	 * <p>
	 * 对两个字符串进行比较
	 * </p>
	 * <p>
	 * <b>处理说明:</b>按字典顺序比较两个字符串。该比较基于字符串中各个字符的 Unicode 值.
	 * </p>
	 * 
	 * @param 参数配置说明
	 *            - 两个参数
	 * @param args
	 *            - 待比较的参数
	 * @return >0: 第一个参数比第二个参数大 =0: 两个参数相等 <0: 第一个参数比第二个参数小 @
	 */
	public static int STRCMP(String args1, String args2) {
		return args1.compareTo(args2);
	}

	/**
	 * <p>
	 * 将一个串的一部分与另一个字符串进行比较
	 * </p>
	 * <p>
	 * <b>处理说明:</b>将字符串1的第num位开始的Len长度的字符串与字符串2进行比较.（num的基数为1）
	 * </p>
	 * 
	 * @param args
	 *            [0]: 比较字符串1
	 * @param args
	 *            [1]: 从string1地第num位开始比较
	 * @param args
	 *            [2]: string1的 num位开始头len位
	 * @param args
	 *            [3]: 比较字符串2
	 * @return >0: string1的len位> string2 <br>
	 *         =0: string1的len位= string2 <br>
	 *         <0: string1的len位< string2 <br>
	 *         @
	 */
	public static int SUBCMP(String args1, int num1, int num2, String args2) {
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			throw new RuntimeException("SUBCMP");
		int offset = num1 - 1;
		if (offset < 0)
			offset = 0;
		if (offset > args1.length())
			offset = args1.length() - 1;

		if (num2 < 0)
			num2 = 0;
		// if (offset + num2 > args1.length())
		// num2 = args1.length() - offset;
		int ret = StringUtils.substring(args1, offset, offset + num2).compareTo(args2);
		return ret;
	}

	/**
	 * <p>
	 * 计算长整型数的N 次方
	 * </p>
	 * 
	 * @param 参数配置说明
	 *            - 两个以上参数
	 * @param args
	 * @return 长整数结果 @
	 */
	public static long LONGPOWER(long... param) {
		if (param.length < 2)
			throw new RuntimeException("LONGPOWER");
		long result = param[0];
		for (int i = 1; i < param.length; i++) {
			result = (long) Math.pow(result, param[i]);
		}
		return result;
	}

	/**
	 * <p>
	 * 计算短整型数的N 次方，得到一个整形数
	 * </p>
	 * 
	 * @param 参数配置说明
	 *            - 两个以上参数
	 * @param args
	 * @return 整形数 @
	 */

	public static long SHORTPOWER(long... param) {
		if (param.length < 2)
			throw new RuntimeException("SHORTPOWER");
		long result = param[0];
		for (int i = 1; i < param.length; i++) {
			result = (long) Math.pow(result, param[i]);
		}
		return result;
	}

	/**
	 * 两个整数之间的比较
	 * <p>
	 * 比较运算符:
	 * </p>
	 * 
	 * <pre>
	 * 
	 *       1:&lt; 
	 *       2:&lt;= 
	 *       3:= 
	 *       4:!= 
	 *       5:&gt;= 
	 *       6:&gt;
	 * </pre>
	 * 
	 * @param 参数配置说明
	 *            - 三个参数
	 * @param args
	 *            - 参数一:整数1; 参数二(op):比较运算符; 参数三:整数2;
	 * @return 1:真 0:假 @
	 */
	public static int INTCMP(int args1, int args2, int args3) {
		int result = args1;
		int op = args2;
		switch (op) {
		case 1:
			if (result < args3) {
				return 1;
			}
			return 0;
		case 2:
			if (result <= args3) {
				return 1;
			}
			return 0;
		case 3:

			if (result == args3) {
				return 1;
			}
			return 0;
		case 4:
			if (result != args3) {
				return 1;
			}
			return 0;
		case 5:
			if (result >= args3) {
				return 1;
			}
			return 0;
		case 6:
			if (result > args3) {
				return 1;
			}
			return 0;
		default:
			throw new RuntimeException("INTCMP");
		}
	}

	/**
	 * <p>
	 * 两个浮点数之间的比较
	 * </p>
	 * 比较运算符:
	 * 
	 * <pre>
	 * 
	 *       1:&lt; 
	 *       2:&lt;= 
	 *       3:= 
	 *       4:!= 
	 *       5:&gt;= 
	 *       6:&gt;
	 * </pre>
	 * 
	 * @param 参数配置说明
	 *            - 三个参数
	 * @param args
	 *            - 参数一:浮点数1; 参数二(op):比较运算符; 参数三:浮点数2;
	 * @return 1:真 0:假 @
	 */
	public static int DOUBLECMP(double args1, int args2, double args3) {
		double result = args1;
		int op = args2;
		switch (op) {
		case 1:
			if (result < args3) {
				return 1;
			}
			return 0;
		case 2:
			if (result <= args3) {
				return 1;
			}
			return 0;
		case 3:
			if (result == args3) {
				return 1;
			}
			return 0;
		case 4:
			if (result != args3) {
				return 1;
			}
			return 0;
		case 5:
			if (result >= args3) {
				return 1;
			}
			return 0;
		case 6:
			if (result > args3) {
				return 1;
			}
			return 0;
		default:
			throw new RuntimeException("DOUBLECMP 比较符参数不正确!");
		}
	}

	/**
	 * 判断两个字符串不相等
	 * 
	 * @param arg0
	 * @param arg1
	 * @return true: 不相等(true) 1: false
	 */
	public static boolean IS_NOEQUAL_STRING(String args1, String args2) {
		if (StringUtils.equals(args1, args2))
			return false;
		else
			return true;
	}

	/**
	 * 判断两个整数相等
	 * 
	 * @param arg0
	 * @param arg1
	 * @return true 相等 false不相等
	 */
	public static boolean IS_EQUAL_INT(int int1, int int2) {
		if (int1 == int2)
			return true;
		else
			return false;

	}

	/**
	 * 比较两个DOUBLE 数是否相等
	 * 
	 * @param args
	 * @return true 相等 false 不相等 @
	 */
	public static boolean IS_EQUAL_DOUBLE(double d1, double d2) {
		if (d1 == d2)
			return true;
		else
			return false;
	}

	/**
	 * 判断两个字符串相等
	 * 
	 * @param arg0
	 * @param arg1
	 * @return true相等 false不相等
	 */
	public static boolean IS_EQUAL_STRING(String args1, String args2) {
		if (StringUtils.equals(args1, args2))
			return true;
		else
			return false;
	}

	/**
	 * <p>
	 * 判断字符串是否为空，如果字符串为空返回真，否则返回假
	 * </p>
	 * 
	 * @param str
	 *            待判断的字符串
	 * @return 字符串为空返回true，不为空返回false
	 */
	public static boolean ISNULL(String... args) {
		if (args.length == 0) {
			throw new RuntimeException("ISNULL 参数不正确!至少要求有一个参数");
		}
		for (int i = 0; i < args.length; i++) {
			if (!(StringUtils.isEmpty(args[i])))
				return false;
		}
		return true;
	}

	/**
	 * 
	 * <p>
	 * 判断字符串是否为中文域
	 * </p>
	 * 
	 * @param str
	 *            待判断的字符串
	 * @return 字符串为全中文域返回true，否则返回false
	 */
	public static boolean ISCHIN(String args) {
		if (StringUtils.isEmpty(args)) {
			throw new RuntimeException("ISCHIN 参数不正确");
		}

		char[] buf = args.toCharArray();
		int sz = args.length();
		for (int i = 0; i < sz; i++) {
			if (CharUtils.isAscii(buf[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * 计算字符串的长度
	 * </p>
	 * 
	 * @param str
	 *            待计算的字符串
	 * @return 字符串长度
	 */
	public static int STRLEN(String args) {
		if (args == null)
			throw new RuntimeException("表达式 STRLEN 参数不能为空");
		return args.length();
	}

	/**
	 * <p>
	 * 从字符串中截取指定长度的字符串
	 * </p>
	 * <p>
	 * 截取字符串string的pos位开始len长度的字符串，pos从1开始
	 * </p>
	 * 
	 * @param str
	 *            被截取的字符串
	 * @param pos
	 *            从哪一位开始截取
	 * @param len
	 *            截取的长度
	 * @return 截取的字符串
	 */
	public static String SUBSTR(String args, int pos, int len) {
		int beginIndex = pos - 1;
		if (beginIndex < 0)
			beginIndex = 0;
		if (beginIndex > args.length())
			beginIndex = args.length();

		int length = len;
		if (length < 0)
			length = 0;

		if (beginIndex + length > args.length())
			length = args.length() - beginIndex;

		byte[] bytes = args.getBytes();
		return new String(bytes, beginIndex, length);
	}

	/**
	 * <p>
	 * 获取右边len个长度的子串
	 * </p>
	 * 
	 * @param arg0
	 *            被截取的字符串
	 * @param len
	 *            截取的长度
	 * @return 从最右边开始的len长度的子串
	 */
	public static String SUBRIGHT(String str, int len) {
		if (len < 0)
			len = 0;
		if (len > str.length())
			len = str.length();
		byte[] bytes = str.getBytes();
		return new String(bytes, str.length() - len, len);
	}

	/**
	 * <p>
	 * 测试输入字符串是否全为数字型
	 * </p>
	 * 
	 * @param str
	 *            待测试的字符串
	 * @return 全是数字返回true 不全是数字返回 false
	 */
	public static boolean ISNUMBER(String... args) {
		if (args.length == 0)
			throw new RuntimeException("ISNUMBER 至少要求有一个参数");

		for (int i = 0; i < args.length; i++) {
			// 2013-08-30 粟泽
			if (StringUtils.isEmpty(args[i])) {
				return false;
			}
			// 2013-08-30 粟泽 END
			if (!StringUtils.isNumeric(args[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取指定字符在字符串中的位置，如果有多个，只返回第一个
	 * 
	 * @param args
	 *            [0]: 被查找的字符串
	 * @param args
	 *            [1]: 要查找的字符
	 * @param args
	 *            [2]: 查找的长度，即data字符串的前len位
	 * @return 返回指定字符ch 在data中的第几位, 和C语言的下标类似，如是第一个字符，返回值（位置号）为0 @
	 */
	public static int GETCHARPOS(String args1, String args2, int len) {
		// 参数位置与文档不一致 粟泽
		// public static int GETCHARPOS(String args1, int len,String args2 )
		// 参数位置与文档不一致 粟泽 END
		if (StringUtils.isEmpty(args1))
			return -1;
		if (len > args1.length())
			len = args1.length();
		int lenght = len - 1;
		if (lenght < 0)
			return -1;

		for (int i = 0; i < lenght; i++) {
			if (args1.charAt(i) == args2.charAt(0))
				return i;

		}
		return -1;
	}

	/**
	 * <p>
	 * 在输入字符串中指定位置开始查找指定字符，找到第一个后返回它的位置号
	 * </P>
	 * 
	 * <pre>
	 *             如果from=0，则查找输入字符串所有字符
	 *             例如输入字符串为1234567890，需要在2位字符后查找字符’5’
	 *             GETCHARPOSFROM(1234567890,2,5)，返回结果为5。
	 * 
	 * </pre>
	 * 
	 * @param args
	 *            [0] 被查找的字符串
	 * @param args
	 *            [1] 查找的起始位置，即str字符串的后from位
	 * @param args
	 *            [2] 要查找的字符
	 * 
	 * @return 返回指定字符在string中的第几位, 位置号从1开始,包括from的起始位置； 返回<0 表示没有找到。
	 * 
	 */
	public static int GETCHARPOSFROM(String args1, int index, String args2) {
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			return -1;

		int ret = StringUtils.indexOf(args1, args2, index);
		if (ret != -1)
			ret += 1;

		return ret;
	}

	/**
	 * <p>
	 * 获取指定字符串在输入字符串中的位置，如果有多个，只返回第一个
	 * </P>
	 * 
	 * @param str
	 *            被查找的字符串
	 * @param substr
	 *            要查找的子串
	 * @return 返回指定子串sub_string在string中的第几位, 位置号从1开始。 返回<0，表示没有找到子串。
	 * 
	 */
	public static int GETSTRPOS(String args1, String args2) {
		int idx = StringUtils.indexOf(args1, args2);
		if (idx == -1)
			return -1;
		else
			return idx + 1;
		// return String.valueOf(StringUtils.indexOf(args[0].trim(), args[1]
		// .trim()) + 1);
	}

	/**
	 * <p>
	 * 根据指定的方式删除空格
	 * </p>
	 * 
	 * @param str
	 *            待删除空格的字符串
	 * @param mode
	 *            删除的模式 left : 删除左边空格 right: 删除右边空格 all : 删除全部空格(即两边和中间的空格) both
	 *            : 删除左右两边空格
	 * @return 删除空格后的字符串
	 */
	public static String DELSPACE(String args1, String args2) {
		if (args1 == null || StringUtils.isEmpty(args2))
			throw new RuntimeException("DELSPACE");
		args2 = args2.toUpperCase();

		if (!args2.equals("LEFT") && !args2.equals("RIGHT") && !args2.equals("BOTH") && !args2.equals("ALL"))
			throw new RuntimeException("DELSPACE第二个参数错误");

		if (StringUtils.equalsIgnoreCase("LEFT", args2)) {
			return StringUtils.stripStart(args1, " ");
		} else if (StringUtils.equalsIgnoreCase("RIGHT", args2)) {
			return StringUtils.stripEnd(args1, " ");
		} else if (StringUtils.equalsIgnoreCase("BOTH", args2)) {
			return StringUtils.strip(args1);
		} else if (StringUtils.equalsIgnoreCase("ALL", args2)) {
			return StringUtils.replaceChars(args1, " \t\f\r\n　", "");
		} else {
			return args1;
		}
	}

	/**
	 * <p>
	 * 去掉头和尾的空格
	 * </p>
	 * 
	 * @param str
	 *            待处理的字符串
	 * @return 去掉头和尾的空格后的字符串
	 */
	public static String DELBOTHSPACE(String args) {
		if (args == null)
			throw new RuntimeException("[DELBOTHSPACE 参数不允许为空]");
		return StringUtils.strip(args);
	}

	/**
	 * <p>
	 * 去掉尾部的空格
	 * </p>
	 * 
	 * @param str
	 *            待处理的字符串
	 * @return 去掉尾部空格后的字符串
	 */
	public static String DELRIGHTSPACE(String args) {
		if (args == null)
			throw new RuntimeException("DELRIGHTSPACE 参数不允许为空");

		return StringUtils.stripEnd(args, " ");
	}

	/**
	 * <p>
	 * 生成指定数量的空格
	 * </p>
	 * 
	 * <pre>
	 * SPACE(10, 1);
	 * </pre>
	 * 
	 * @param str
	 *            要生产空格的长度
	 * @param flag
	 *            生产空格的模式： 1: 全角空格 0: 半角空格
	 * @return 指定长度空格的字符串
	 */
	public static String SPACE(int args, int len) {
		if (args == 0)
			throw new RuntimeException("SPACE");
		switch (len) {
		case 0:
			return StringUtils.repeat(" ", args);
		case 1:
			return StringUtils.repeat("　", args);
		default:
			throw new RuntimeException("SPACE 参数产生空格的模式不正确，只能是[0]或者[1]");
		}
	}

	/**
	 * <p>
	 * 将整型数转换成字符串型,不足指定长度的前补0
	 * </p>
	 * 
	 * @param num
	 *            整型数
	 * @param len
	 *            转换的长度
	 * @return 指定长度的字符串
	 * 
	 */
	public static String INTTOSTR(String args, int len) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("INTTOSTR");
		return StringUtils.leftPad(StringUtils.trim(args), len, '0');
	}

	/**
	 * <p>
	 * 字符串用指定字符指定方向扩展,用ch补齐字符串string的空白区域
	 * </p>
	 * 
	 * @param str
	 *            字符串
	 * @param length
	 *            补满后的长度
	 * @param ch
	 *            填充的字符
	 * @param dire
	 *            方向，0：右边，1: 左边
	 * @return
	 */
	public static String ADDCHAR(String args1, int len, String args2, String args3) {
		if (args1 == null || args2 == null || args3 == null) {
			throw new RuntimeException("ADDCHAR 参数不允许为空!");
		}
		byte[] arg1 = args1.getBytes();
		byte[] chs = args2.getBytes();
		if (chs.length != 1) {
			throw new RuntimeException("ADDCHAR 参数不合法，要填充的必须是字符！！");
		}
		int arglen = arg1.length;
		// if (arglen > len) {
		// throw new RuntimeException(String.format(
		// "ADDCHAR 参数不合法，要填充的字符串[%s]长度[%s]大于[%s]！！", args1, arglen,
		// len));
		// }
		int strLen = args1.length();
		len = len - (arglen - strLen);
		if (len < 0) {
			throw new RuntimeException("ADDCHAR 参数不合法，字符串的长度大于要补充后的长度！！");
		}
		switch (NumberUtils.toInt(StringUtils.trim(args3))) {
		case 0:
			return StringUtils.rightPad(args1, len, (char) chs[0]);
		case 1:
			return StringUtils.leftPad(args1, len, (char) chs[0]);
		default:
			throw new RuntimeException("ADDCHAR 补充字符方向不合法，必须为[0][1]中的一个");
		}
	}

	/**
	 * <p>
	 * 字符串用指定字符指定方向扩展,用ch补齐字符串string的空白区域
	 * </p>
	 * 
	 * @param str
	 *            字符串
	 * @param length
	 *            补满后的长度
	 * @param ch
	 *            填充的字符
	 * @param dire
	 *            方向，0：右边，1: 左边
	 * @return
	 */
	public static String ADDCHAR2(String args1, int len, String args2, String args3) {

		switch (NumberUtils.toInt(StringUtils.trim(args3))) {
		case 0:
			return StringUtils.rightPad(StringUtils.trim(args1), len, StringUtils.trim(args2));
		case 1:
			return StringUtils.leftPad(StringUtils.trim(args1), len, StringUtils.trim(args2));
		default:
			break;
		}
		throw new RuntimeException("ADDCHAR");
	}

	/**
	 * <p>
	 * 超长忽略,截断多余数据;按指定长度截取字符串，剩余右边部分
	 * </p>
	 * 
	 * @param str
	 *            源字符串
	 * @param length
	 *            字符串保留长度
	 * @return 截断字符数据字符串，出错返回null
	 */
	public static String CUTDATALENGTH(String args, int len) {
		return StringUtils.substring(StringUtils.trim(args), StringUtils.trim(args).length() - len);
	}

	/**
	 * <p>
	 * 将字符串转化为大写
	 * </p>
	 * 
	 * @param str
	 *            待转换的字符串
	 * @return 转换后的字符串
	 */
	public static String TOUPPER(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("TOUPPER");

		return StringUtils.upperCase(args);
	}

	/**
	 * <p>
	 * 将字符串转换为小写
	 * </p>
	 * 
	 * @param str
	 *            待转换的字符串
	 * @return 转换后的字符串
	 */
	public static String TOLOWER(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("TOLOWER");
		return StringUtils.lowerCase(args);
	}

	/**
	 * <p>
	 * 查找字符串中第一个需要替换的字符串，并替换成目标字符串
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @param src
	 *            替换的源字符串
	 * @param dest
	 *            替换的目标字符串
	 * @return 替换后的字符串
	 */
	public static String REPSTR(String... args) {
		if (args.length != 3)
			throw new RuntimeException("REPSTR");

		return StringUtils.replaceOnce(args[0].trim(), args[1].trim(), args[2].trim());
	}

	/**
	 * <p>
	 * 查找字符串中所有需要替换的字符串，并替换成目标字符串
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @param src
	 *            替换的源字符串
	 * @param dest
	 *            替换的目标字符串
	 * @return 替换后的字符串
	 */
	public static String REPALLSTR(String... args) {
		if (args.length != 3)
			throw new RuntimeException("REPALLSTR");

		return StringUtils.replace(args[0], args[1], args[2]);
	}

	/**
	 * <p>
	 * 删除字符串中所有的控制字符
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @return 删除控制字符后的字符串
	 */
	public static String DELCTRL(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("DELCTRL");

		String str = args.trim();
		if (StringUtils.isEmpty(str))
			throw new RuntimeException("DELCTRL");
		char[] buf = str.toCharArray();
		for (int i = 0; i < buf.length; i++) {
			if (CharUtils.isAsciiControl(buf[i])) {
				str = StringUtils.replace(str, Character.toString(buf[i]), "");
			}
		}
		return str;
		// return StringUtils.trim(args[0].trim());
	}

	/**
	 * <p>
	 * 处理单引号
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @return 删除控制字符后的字符串
	 */
	public static String TURNSPACIALCHAR(String args) {
		return args.replaceAll("'", "''");
	}

	/**
	 * <p>
	 * 查找字符串中所有的控制字符，并替换成目标字符
	 * </p>
	 * 
	 * @param str
	 *            待处理的字符串
	 * @param ch
	 *            替换的目标字符
	 * @return 替换后的字符串
	 */
	public static String REPCTRL(String args1, String args2) {
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			throw new RuntimeException("REPCTRL");

		String str = args1;
		String ch = args2;
		// char[] buf = str.toCharArray();
		// for (int i = 0; i < str.length(); i++) {
		// if (CharUtils.isAsciiControl(buf[i])) {
		// str = StringUtils.replace(str, Character.toString(buf[i]),
		// ch.charAt(0)+"");
		// }
		// }

		char[] buf = str.toCharArray();
		for (int i = 0; i < buf.length; i++) {
			if (CharUtils.isAsciiControl(buf[i])) {
				buf[i] = ch.charAt(0);
			}
		}
		return new String(buf);
	}

	/**
	 * <p>
	 * 字符串取反操作，对输入的字符串每个字符进行取反操作规则是：字符0转换成字符1，字符1转换成字符0，其它字符不变。
	 * </p>
	 * 
	 * @param str
	 * @return 取反后的字符串
	 */
	public static String REVERSAL(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("REVERSAL");
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < args.length(); i++) {
			switch (args.charAt(i)) {
			case '0':
				result.append('1');
				break;
			case '1':
				result.append('0');
				break;
			default:
				result.append(args.charAt(i));
				break;
			}
		}
		return result.toString();
	}

	/**
	 * <p>
	 * 删除字符串左边指定的字符
	 * </p>
	 * 
	 * @param args
	 *            [0] 输入字符串
	 * @param args
	 *            [1] 指定字符
	 * @return @
	 */

	public static String LTRIM(String args1, String args2) {
		// 注释掉 粟泽
		// System.out.println(args1 + "==" + args2);
		// 注释掉 粟泽END
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			return args1;
		int i;
		for (i = 0; i < args1.length(); i++) {
			if (args1.charAt(i) == args2.charAt(0)) {
				return args1.substring(i + 1);
			}
		}
		return args1;
	}

	// public static String LTRIM(String args,char ch) {
	// return LTRIM(args,String.valueOf(ch));
	// }
	/**
	 * <p>
	 * 删除字符串右边指定的字符
	 * </p>
	 * 
	 * @param args
	 *            [0] 输入字符串
	 * @param args
	 *            [1] 指定字符
	 * @return @
	 */
	public static String RTRIM(String args1, String args2) {
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			return args1;
		int i;
		for (i = args1.length() - 1; i > -1; i--) {
			if (args1.charAt(i) == args2.charAt(0)) {
				return args1.substring(0, i);
			}

		}
		return args1;
	}

	/**
	 * <p>
	 * 删除字符串中的全角、半角空格，TAB符、换行符，换页符。
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @param mode
	 *            删除模式 left 左边 right 右边 both 两边 all 全部
	 * @return 删除后的字符串
	 */
	public static String TRIM(String args1, String args2) {

		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2))
			return args1;
		args2 = args2.toUpperCase();
		if (!args2.equals("LEFT") && !args2.equals("RIGHT") && !args2.equals("BOTH") && !args2.equals("ALL"))
			throw new RuntimeException("TRIM第二个参数错误");

		if (StringUtils.equals(args2.trim(), "BOTH")) {
			return StringUtils.trim(args1);
		} else if (StringUtils.equals(args2.trim(), "RIGHT")) {
			return StringUtils.stripEnd(args1, " \t　\0xa3\n\r\f");
		} else if (StringUtils.equals(args2.trim(), "LEFT")) {
			return StringUtils.stripStart(args1, " \t　\0xa3\n\r\f");
		} else if (StringUtils.equals(args2.trim(), "ALL")) {
			String buf3[] = args1.split(" |　|\t|\n|\f");
			String ret = "";
			for (int j = 0; j < buf3.length; j++) {
				ret += buf3[j];
			}
			return ret;
		} else {
			throw new RuntimeException("TRIM");
		}

	}

	/**
	 * <p>
	 * 将null转换为empty
	 * </p>
	 * 
	 * @param str
	 *            输入字符串
	 * @return
	 */
	public static String NullToEmpty(String args) {
		if (args == null)
			return "";
		else
			return args;
	}

	/**
	 * <p>
	 * 在给定字符串间隔中插入指定长度或递变长度字符
	 * </p>
	 * 
	 * <pre>
	 *             INSERTCHAR(ABCD, y, 2, 1)     结果为： AyyByyyCyyyyD
	 * 
	 * </pre>
	 * 
	 * @param ch
	 *            输入字符串
	 * @param str
	 *            待插入的字符
	 * @param times
	 *            第一个间隔插入times个指定字符
	 * @param step
	 *            递变值，间隔插入times+step个指定字符
	 * @return 插入操作的字符串
	 */
	public static String INSERTCHAR(String str, String ch, int times, int step) {
		// 若为null,返回本身
		if (str == null) {
			return null;
		}
		// 插入指定字符次数,默认为1
		int iTimes = 1;
		// 递变值, 默认为0
		int iStep = 0;
		if (iTimes < times)
			iTimes = times;

		if (iStep < step)
			iStep = step;

		StringBuffer result = new StringBuffer();

		int interval = str.length() - 1;
		for (int i = 0; i < interval; i++) {
			result.append(str.charAt(i));
			for (int j = 0; j < iTimes; j++) {
				result.append(ch);
			}
			iTimes += iStep;
		}
		result.append(str.charAt(interval));
		return result.toString();
	}

	/**
	 * <p>
	 * 在有分隔符的字符串中取指定序号的单词
	 * </p>
	 * <p>
	 * 如果分隔符字符串是 , $ ( ) 这4个字符中的任何一个，不能直接在表达式中使用， 需要分别用/d ，/D ，/L , /R
	 * 代替,同时也支持这些字符的组合
	 * </p>
	 * 
	 * <pre>
	 * 
	 * 
	 *             例子:有字符串hello/china/i/love/you , 该字符串以 ‘/’ 分割，
	 *             则如果需要取单词‘love’，
	 *             则调用此函数GETWORDDELIMITER (hello/china/i/love/you, &quot;/&quot;, 4)
	 * 
	 * </pre>
	 * 
	 * @param str
	 *            有分隔符的字符串
	 * @param seperator
	 *            分隔符
	 * @param seq
	 *            序号
	 * @return 返回指定的单词
	 */
	public static String GETWORDDELIMITER(String str, String seperator, int len) {
		if (StringUtils.isEmpty(str))
			throw new RuntimeException("GETWORDDELIMITER");
		String temp[] = StringUtils.splitByWholeSeparator(str, seperator);
		int seq = len;
		if (seq > temp.length)
			return "";
		return temp[seq - 1];
	}

	/**
	 * <p>
	 * 取字符串前几个字符
	 * </p>
	 * 
	 * <pre>
	 *           n = 0 的时候返回空串，
	 *           n &gt; strlen(str) 的时候返回整个字符串
	 *           n &lt; 0出错
	 * 
	 * </pre>
	 * 
	 * @param str
	 *            源字符串
	 * @param n
	 *            要取字符的个数
	 * @return 返回取得的字符串,出错返回null
	 */
	public static String LEFTSTR(String args, int n) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("LEFTSTR");
		return StringUtils.left(args.trim(), n);
	}

	/**
	 * <p>
	 * 取字符串后几个字符
	 * </p>
	 * 
	 * <pre>
	 *           n = 0 的时候返回空串，
	 *           n &gt; strlen(str) 的时候返回整个字符串
	 *           n &lt; 0出错
	 * 
	 * </pre>
	 * 
	 * @param str
	 *            源字符串
	 * @param n
	 *            要取字符的个数
	 * @return 返回取得的字符串,出错返回null
	 */
	public static String RIGHTSTR(String args, int n) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("RIGHTSTR");
		return StringUtils.right(args.trim(), n);
	}

	/**
	 * <p>
	 * 计算字符串中分割符的个数
	 * </p>
	 * 
	 * <pre>
	 *           如果分隔符字符串是
	 *            , $ ( )
	 *             这4个字符中的任何一个，不能直接在表达式中使用，
	 *             需要分别用/d ，/D ，/L , /R 代替,同时也支持这些字符的组合
	 * 
	 * </pre>
	 * 
	 * @param args
	 *            [0] 源字符串 [1] 分割字符串
	 * @return 返回分割字符串的个数
	 */
	public static int STRGETCOUNT(String args1, String args2) {
		if (StringUtils.isEmpty(args1))
			throw new RuntimeException("STRGETCOUNT");
		return StringUtils.countMatches(args1, args2);
		// String buf[] = args[0].trim().split(args[1].trim());
		// return Integer.toString(buf.length);
	}

	/**
	 * <p>
	 * 求金额的绝对值，不足指定长度则左补0
	 * </p>
	 * 
	 * @param str
	 *            金额1 可以带有正负号，不带逗号、小数点
	 * @param len
	 *            转换后的金额字符串的长度
	 * @return 处理后的金额，出错，返回null
	 */
	public static String FABSAMT(String args, int len) {

		if (StringUtils.isEmpty(args))
			throw new RuntimeException("FABSAMT");
		String str = args.trim();
		// 金额不带逗号和小数点
		if (!StringUtils.containsNone(str, ",.") || !NumberUtils.isNumber(str)) {
			throw new RuntimeException("FABSAMT");
		}
		DecimalFormat ft = new DecimalFormat("#");
		double dNum;
		dNum = NumberUtils.toDouble(str);
		String ret = ft.format(Math.abs(dNum));

		if (ret.length() < len) {
			StringBuffer buf = new StringBuffer(ret);
			for (int i = 0; i < len - ret.length(); i++) {
				buf.insert(0, '0');
			}
			ret = buf.toString();
		}
		return ret;
	}

	/**
	 * <p>
	 * 判断是否为闰年
	 * </p>
	 * 
	 * @param year
	 *            输入年份，要求大于1900
	 * @return 1： 是闰年； -1：非闰年； -2、-3：错误年份 NULL：出错
	 */
	public static int ISLEAPYEAR(int year) {
		if (year < 1900)
			return -2;

		if ((year % 4) == 0) {
			if ((year % 100) == 0) {
				if ((year % 400) == 0) { // 能被400除
					return 1;
				} else {
					return -1;
				}
			} else { // 不能被100除
				return 1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * <p>
	 * 对日期型数据按指定模式进行转换
	 * </p>
	 * 
	 * <pre>
	 * 
	 *          				  日期格式分为六种：
	 *          				  0：4Y2M2D；
	 *          				  1：4Y/2M/2D；
	 *          				  2：2M/2D/4Y；
	 *          				  3：4Y.2M.2D；
	 *          				  4：4Y-2M-2D；
	 *          				  5：4Y年2M月2D天
	 * </pre>
	 * 
	 * @param str
	 *            输入日期字符串
	 * @param type1
	 *            输入日期的类型
	 * @param type2
	 *            输出日期的类型
	 * @Return String --指定输出格式的日期字符串 null --错误
	 * 
	 */
	public static String FMTDATE(String args, int type1, int type2) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("FMTDATE");
		String str = args.trim();
		if (StringUtils.isEmpty(str) || type1 > 5 || type1 < 0 || type2 > 5 || type2 < 0) {
			throw new RuntimeException("FMTDATE");
		}

		String pattern1 = "", pattern2 = "";
		String buf1[] = { "yyyyMMdd", "yyyy/MM/dd", "MM/dd/yyyy", "yyyy.MM.dd", "yyyy-MM-dd", "yyyy年MM月dd天" };
		// String buf2[] = { "4Y2M2D", "4Y/2M/2D", "2M/2D/4Y", "4Y.2M.2D",
		// "4Y-2M-2D", "4Y年2M月2D天" };
		str = str.trim();
		pattern1 = buf1[type1];
		pattern2 = buf1[type2];

		if (StringUtils.isEmpty(pattern1) || StringUtils.isEmpty(pattern2)) {
			throw new RuntimeException("FMTDATE");
		}

		String[] pattern = { pattern1 };
		Date date;
		try {
			date = DateUtils.parseDate(str, pattern);
			return DateFormatUtils.format(date, pattern2);
		} catch (ParseException e) {
			throw new RuntimeException("FMTDATE");
		}
	}

	/**
	 * 
	 * @param time
	 * @param old
	 * @param newParttern
	 * @return
	 */
	public static String FMTTIME(String time, String old, String newParttern) {
		Date date;
		try {
			date = DateUtils.parseDate(time, new String[] { old });
			return DateFormatUtils.format(date, newParttern);
		} catch (ParseException e) {
			throw new RuntimeException("FMTTIME 格式不正确");
		}
	}

	/**
	 * <p>
	 * 判断日期是否合法，精确度天，年度要求大于1900
	 * </p>
	 * 
	 * @param str
	 *            被判定的日期，日期格式为4位年，2位月，2位天， 后面带有时间或者其他信息不影响判断
	 */
	public static int CHECKDATE(String args) {
		String str = args.trim();
		if (StringUtils.isEmpty(str)) {
			throw new RuntimeException("CHECKDATE");
		}

		try {
			// 按顺序取得年（4位）、月（2位）、日（2位）,字符串前8位
			str = str.trim();
			if (str.length() != 8) {
				return 0;
			}
			int iYear = Integer.parseInt(str.substring(0, 4));
			int iMonth = Integer.parseInt(str.substring(4, 6));
			int iDay = Integer.parseInt(str.substring(6, 8));

			if (iYear <= 1900)
				return 0;
			if (iMonth < 1 || iMonth > 12) {
				return 0;
			}
			if (ISLEAPYEAR(iYear) == 1) {
				// 闰年
				if (iDay > 0 && iDay <= LEAD_MONTH_DAYS[iMonth - 1]) {
					return 1;
				} else {
					return 0;
				}
			} else {
				if (iDay > 0 && iDay <= ULEAD_MONTH_DAYS[iMonth - 1]) {
					return 1;
				} else {
					return 0;
				}
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>
	 * 获取当时的时间
	 * </p>
	 * 
	 * <pre>
	 *            获取当时的时间。 
	 *            替换字符串中的YYYY、YY、MM、DD、HH、MI、SS为对应的数值，其它字符不变。
	 *            GETDATETIME（YYMMDD）     040801
	 *            GETDATETIME（YYYY年MM月）  2004年08月
	 * </pre>
	 * 
	 * @param str
	 *            日期格式
	 * @return 时间格式字符串
	 */
	public static String GETDATETIME(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("GETDATETIME");
		String[] buf1 = { "YYYY", "YY", "MM", "DD", "HH", "MI", "SS" };
		String[] buf2 = { "yyyy", "yy", "MM", "dd", "HH", "mm", "ss" };
		String str = args.trim();
		for (int i = 0; i < buf1.length; i++) {
			str = StringUtils.replace(str, buf1[i], buf2[i]);
		}
		Calendar calendar = Calendar.getInstance();
		return DateFormatUtils.format(calendar.getTime(), str);
	}

	/**
	 * <p>
	 * 获取当时的时间
	 * </p>
	 * 
	 * <pre>
	 *            获取当时的时间。 
	 *            GETDATETIME（）  20040801123059
	 * </pre>
	 * 
	 * @return 时间格式字符串
	 */
	public static String GETDATETIME() {
		return GETDATETIME("yyyyMMDDHHmmss");
	}

	/**
	 * <p>
	 * 获取当前日期
	 * </p>
	 * 
	 * @return 返回但前日期字符串
	 */
	public static String GETDATE() {
		return GETDATETIME("yyyyMMdd");
	}

	/**
	 * <p>
	 * 获取秒数，指从1970年1月1日0点开始累加到目前的秒数
	 * </p>
	 * 
	 * @return 返回当前的秒数
	 */
	public static long GETSECOND() {
		Date dt = new Date();
		long curSec = dt.getTime();
		curSec = curSec / 1000; // 毫秒转秒
		return curSec;
	}

	/**
	 * <p>
	 * 对输入日期中的年月日进行加、减运算,对输入日期中的年月日进行加、减运算。
	 * 如果输入日期是月末，自动对日进行调整。如果输入或者输出的年份是闰年，自动对2月份的日进行调整。
	 * </p>
	 * 
	 * <pre>
	 *               CALCTIME(0520123412,-,m,1) –&gt; 0420123412  // 对月份减1
	 *               CALCTIME(0520123412,+,d,3) –&gt; 0523123412  // 对日加3
	 *               CALCTIME(0520123412,+,h,3) –&gt; 0520153412  // 对时加3
	 *               CALCTIME(0520123412,+,m,3) –&gt; 0520123712  // 对分加3
	 *               CALCTIME(0520123412,+,s,3) –&gt; 0520123715  // 对秒加3
	 * </pre>
	 * 
	 * @param str
	 *            输入日期字符串
	 * @param op
	 *            运算符 // +，加运算； -，减运算
	 * @param obj
	 *            运算对象 // M，对月份； d，对日; h-时；m-分; s-秒
	 * @param num
	 *            加减数值 // 数字
	 * @return 时间格式为：YYMMDDHHMMSS，出错返回null
	 */
	public static String CALCTIME(String... args) {
		if (args.length < 4)
			throw new RuntimeException("CALCTIME");
		String datefmt = "yyyyMMddHHmmss";
		Date dt;
		try {
			if (args.length == 5) {
				datefmt = args[4];
				dt = DateUtils.parseDate(args[0], new String[] { args[4] });
			} else {
				dt = DateUtils.parseDate(args[0], new String[] { datefmt });
			}
			int num = NumberUtils.toInt(args[3]);
			if (num == 0) {
				return args[0];
			}
			int sign = args[1].equals("+") ? 1 : -1;
			num = num * sign;
			if (args[2].equals("y")) {
				dt = DateUtils.addYears(dt, num);
			} else if (args[2].equals("M")) {
				dt = DateUtils.addMonths(dt, num);
			} else if (args[2].equals("D")) {
				dt = DateUtils.addDays(dt, num);
			} else if (args[2].equalsIgnoreCase("H")) { // H/h 忽略大小写 2013/8/20
														// zhb
				dt = DateUtils.addHours(dt, num);
			} else if (args[2].equals("m")) {
				dt = DateUtils.addMinutes(dt, num);
			} else if (args[2].equals("s")) {
				dt = DateUtils.addSeconds(dt, num);
			} else {
				throw new RuntimeException(args[2]);
			}
			return DateFormatUtils.format(dt, datefmt);
		} catch (ParseException e) {
			throw new RuntimeException(args[0]);
		}
	}

	/**
	 * <p>
	 * 对输入日期中的年月日进行加、减运算,对输入日期中的年月日进行加、减运算。
	 * 如果输入日期是月末，自动对日进行调整。如果输入或者输出的年份是闰年，自动对2月份的日进行调整。
	 * </p>
	 * 
	 * <pre>
	 *               CALCDATE(20040520,+,y,2) –&gt; 20060520  // 对年份加2
	 *               CALCDATE(20040531,-,m,1) –&gt; 20040430  // 对月份减1
	 *               CALCDATE(20040530,+,d,3) –&gt; 20040602  // 对日加3
	 * </pre>
	 * 
	 * @param str
	 *            输入日期字符串
	 * @param op
	 *            运算符 // +，加运算； -，减运算
	 * @param obj
	 *            运算对象 // y，对年份； m，对月份； d，对日
	 * @param num
	 *            加减数值 // 数字
	 * @return 时间格式为：YYYYMMDD，出错返回null
	 */
	public static String CALCDATE(String... args) {
		// TODO
		int iYear, iMonth, iDay, iNum;
		int buf3[] = { 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 3, 2, 1 }; // 辅助月份表

		if (args.length != 4)
			throw new RuntimeException("CALCDATE");

		String str = args[0].trim();
		String op = args[1].trim();
		String obj = args[2].trim();
		String num = args[3].trim();
		// 日期校验
		if (CHECKDATE(str) != 1)
			throw new RuntimeException("CALCDATE 输入的日期不合法");

		if (StringUtils.isEmpty(str) || str.length() != 8)
			throw new RuntimeException("CALCDATE");
		try {
			String[] pattern = { "yyyyMMdd" };
			Date dt = DateUtils.parseDate(str, pattern);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			iYear = calendar.get(Calendar.YEAR);
			iMonth = calendar.get(Calendar.MONTH) + 1;
			iDay = calendar.get(Calendar.DAY_OF_MONTH);
			iNum = NumberUtils.toInt(num);
			if (obj.toUpperCase().equals("Y")) {
				if (op.equals("+")) {
					iYear = iYear + iNum;
				} else if (op.equals("-")) {
					iYear = iYear - iNum;
				}
				if (iMonth == 2) { // 对2月的处理
					if (ISMONTHEND(str)) { // 如果是月末
						if (ISLEAPYEAR(iYear) == 1) {
							iDay = 29;
						} else {
							iDay = 28;
						}
					}
				}
			} else if (obj.toUpperCase().equals("M")) { // 月份相加
				if (op.equals("+")) {
					iMonth = iMonth + iNum;
				} else if (op.equals("-")) {
					iMonth = iMonth - iNum;
				}
				calendar.set(iYear, iMonth - 1, iDay); // 得到正确的年份
				iYear = calendar.get(Calendar.YEAR); // 保存正确的年份
				if (iYear < 1900)
					throw new RuntimeException("CALCDATE");

				/**
				 * 计算闰年和月末的情况,重新计算新的月份，和日
				 */
				int index = iMonth;
				if (index <= 0) {
					index = buf3[Math.abs(iMonth) % 12];
				} else {
					if (index % 12 == 0) {
						index = 12;
					} else {
						index = index % 12;
					}
					// index = index % 12 + (index / 12);
				}
				if (ISLEAPYEAR(iYear) == 0) { // 闰年
					if (iDay > LEAD_MONTH_DAYS[index - 1]) {
						iDay = LEAD_MONTH_DAYS[index - 1];
					}
				} else {
					if (iDay > ULEAD_MONTH_DAYS[index - 1]) {
						iDay = ULEAD_MONTH_DAYS[index - 1];
					}
				}

				calendar.set(iYear, index - 1, iDay);
				return DateFormatUtils.format(calendar.getTime(), "yyyyMMdd");
			} else if (obj.toUpperCase().equals("D")) { // 天数相加
				if (op.equals("+")) {
					iDay = iDay + iNum;
				} else if (op.equals("-")) {
					iDay = iDay - iNum;
				}
			}

			if (iYear < 1900)
				throw new RuntimeException("CALCDATE");
			calendar.set(iYear, iMonth - 1, iDay);
			return DateFormatUtils.format(calendar.getTime(), "yyyyMMdd");
		} catch (ParseException e) {
			throw new RuntimeException("CALCDATE");
		}
	}

	/**
	 * <p>
	 * 判断输入的日期是否年终
	 * </p>
	 * 
	 * @param str
	 *            输入日期
	 * @return true：是年终，false不是年终，出错返回null
	 */
	public static boolean ISYEAREND(String args) {
		String str = args.trim();
		if (StringUtils.isEmpty(str) || CHECKDATE(str) != 1)
			return false;
		try {
			String[] pt = { "yyyyMMdd" };
			Date dt = DateUtils.parseDate(str, pt);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			int iMonth = calendar.get(Calendar.MONTH) + 1;
			int iDay = calendar.get(Calendar.DAY_OF_MONTH);
			if (iMonth == 12 && iDay == 31) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			throw new RuntimeException("ISYEAREND");
		}
	}

	/**
	 * <p>
	 * 判断是不是季度终
	 * </p>
	 * 
	 * @param str
	 *            输入日期
	 * @return true：是季终，false:不是季终，出错，返回null
	 */
	public static boolean ISQUARTEREND(String args) {

		String str = args.trim();
		if (StringUtils.isEmpty(str) || CHECKDATE(str) != 1)
			return false;
		try {
			String[] pt = { "yyyyMMdd" };
			Date dt = DateUtils.parseDate(str, pt);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			int iMonth = calendar.get(Calendar.MONTH) + 1;
			int iDay = calendar.get(Calendar.DAY_OF_MONTH);
			if (iMonth == 3 && iDay == 31) { // 第1季度
				return true;
			} else if (iMonth == 6 && iDay == 30) { // 第2季度
				return true;
			} else if (iMonth == 9 && iDay == 30) { // 第3季度
				return true;
			} else if (iMonth == 12 && iDay == 31) { // 第4季度
				return true;
			} else
				return false;
		} catch (ParseException e) {
			throw new RuntimeException("ISQUATEREND");
		}
	}

	/**
	 * <p>
	 * 判断输入的日期是否月终
	 * </p>
	 * 
	 * @param str
	 *            输入日期
	 * @return true：是月终，false：不是月终，出错，返回null
	 */
	public static boolean ISMONTHEND(String args) {

		String str = args.trim();
		if (StringUtils.isEmpty(str) || CHECKDATE(str) != 1)
			return false;
		try {
			String[] pt = { "yyyyMMdd" };
			Date dt = DateUtils.parseDate(str, pt);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			int iYear = calendar.get(Calendar.YEAR);
			int iMonth = calendar.get(Calendar.MONTH) + 1;
			int iDay = calendar.get(Calendar.DAY_OF_MONTH);

			// int buf1[] = {31,28,31,30,31,30,31,31,30,31,30,31};
			// int buf2[] = {31,29,31,30,31,30,31,31,30,31,30,31};
			if (ISLEAPYEAR(iYear) == 1) { // 闰年
				if (LEAD_MONTH_DAYS[iMonth - 1] == iDay) {
					return true;
				} else
					return false;
			} else { // 非闰年
				if (ULEAD_MONTH_DAYS[iMonth - 1] == iDay) {
					return true;
				} else
					return false;
			}
		} catch (ParseException e) {
			throw new RuntimeException("ISMONTHEND", e);
		}
	}

	/**
	 * <p>
	 * 判断输入的日期是否周末,周六、周日均为周末
	 * </p>
	 * 
	 * @param str
	 *            输入日期
	 * @return tre：是周末，false：不是周末，出错返回null
	 */
	public static boolean ISWEEKEND(String args) {

		String str = args.trim();
		if (StringUtils.isEmpty(str) || CHECKDATE(args) != 1)
			return false;

		try {
			Date dt = DateUtils.parseDate(str, new String[] { "yyyyMMdd" });
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			int week = calendar.get(Calendar.DAY_OF_WEEK);
			if (week == Calendar.SATURDAY || week == Calendar.SUNDAY) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			throw new RuntimeException("ISMONTHEND");
		}
	}

	/**
	 * <p>
	 * 计算两个日期之间相差的天数
	 * </p>
	 * 
	 * <pre>
	 *           例子:计算２００４年１０月１１号到２００４年11月１２号 之间的天数
	 *           DIFFDATE (20041011, 20041112)  返回 32
	 * </pre>
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @return 相差的天数，出错，返回null
	 */
	public static String DIFFDATE(String args1, String args2) {
		String date1 = args1.trim();
		String date2 = args2.trim();
		if (StringUtils.isEmpty(date1) || StringUtils.isEmpty(date2) || CHECKDATE(args1) != 1 || CHECKDATE(args2) != 1)
			throw new RuntimeException("DIFFDATE Y");
		String[] pt = { "yyyyMMdd" };
		try {
			Date dt1 = DateUtils.parseDate(date1, pt);
			Date dt2 = DateUtils.parseDate(date2, pt);
			long diff = Math.abs(dt2.getTime() - dt1.getTime());
			return Long.toString(diff / (86400 * 1000));
		} catch (ParseException e) {
			throw new RuntimeException("ISMONTHEND");
		}
	}

	/**
	 * <p>
	 * 计算两个日期之间相差的月数
	 * </p>
	 * 
	 * <pre>
	 *           例子: 
	 *           CALCMONTH(20040102,20040201,1)返回结果为1
	 *           CALCMONTH(20030101,20040302,0)返回结果为14
	 * </pre>
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param flag
	 *            标志，考虑日是否满月 0：需要 1：不需要，直接将年、月进行相减
	 * 
	 * @return 相差月份数
	 */
	public static String CALCMONTH(String args1, String args2, int num) {
		int iYear1, iMonth1, iDay1;
		int iYear2, iMonth2, iDay2;
		int diff = 0;
		String date1 = args1.trim();
		String date2 = args2.trim();

		if (StringUtils.isEmpty(date1) || StringUtils.isEmpty(date2) || CHECKDATE(args1) != 1 || CHECKDATE(args2) != 1) {
			throw new RuntimeException("CALCMONTH");
		}

		try {
			String[] pt = { "yyyyMMdd" };
			Date dt1 = DateUtils.parseDate(date1, pt);
			Date dt2 = DateUtils.parseDate(date2, pt);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt1);
			iYear1 = calendar.get(Calendar.YEAR);
			iMonth1 = calendar.get(Calendar.MONTH) + 1;
			iDay1 = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.setTime(dt2);
			iYear2 = calendar.get(Calendar.YEAR);
			iMonth2 = calendar.get(Calendar.MONTH) + 1;
			iDay2 = calendar.get(Calendar.DAY_OF_MONTH);

			if (num == 0) {
				diff = Math.abs(iYear2 - iYear1);
				if (dt2.getTime() >= dt1.getTime()) { // dt2 > dt1
					diff = diff * 12 + iMonth2 - iMonth1;
					if (iDay2 < iDay1) {
						diff -= 1;
					}
				} else {
					diff = diff * 12 + iMonth1 - iMonth2;
					if (iDay1 < iDay2) {
						diff -= 1;
					}
				}
			} else if (1 == num) { // 不足月
				diff = Math.abs(iYear2 - iYear1);
				if (dt2.getTime() >= dt1.getTime()) { // dt2 > dt1
					diff = diff * 12 + iMonth2 - iMonth1;
				} else {
					diff = diff * 12 + iMonth1 - iMonth2;
				}
			} else {
				throw new RuntimeException("CALCMONTH");
			}
			return Integer.toString(diff);
		} catch (ParseException e) {
			throw new RuntimeException("CALCMONTH");
		}
	}

	/**
	 * <p>
	 * 计算两个日期之间相差的年数
	 * </p>
	 * 
	 * <pre>
	 *             CALCMONTH(20031231,20040101,1)返回结果为1 
	 *             CALCMONTH(20050102,20010101,0)返回结果为4
	 *           &lt;pre&gt;
	 *           
	 *           &#064;param date1 日期1
	 *           &#064;param date2 日期2
	 *           &#064;param flag  标志，考虑月、日是否满年
	 *                        0：需要
	 *                        1：不需要，直接将年份相减
	 * 
	 * &#064;return 相差年数，出错返回null
	 * 
	 */
	public static String CALCYEAR(String args1, String args2, int num) {
		int iYear1, iMonth1, iDay1;
		int iYear2, iMonth2, iDay2;
		int diff = 0;
		String date1 = args1.trim();
		String date2 = args2.trim();
		int flag = num;

		if (StringUtils.isEmpty(date1) || StringUtils.isEmpty(date2) || CHECKDATE(date1) != 1 || CHECKDATE(date2) != 0) {
			throw new RuntimeException("CALCYEAR");
		}
		try {
			String[] pt = { "yyyyMMdd" };
			Date dt1 = DateUtils.parseDate(date1, pt);
			Date dt2 = DateUtils.parseDate(date2, pt);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt1);
			iYear1 = calendar.get(Calendar.YEAR);
			iMonth1 = calendar.get(Calendar.MONTH) + 1;
			iDay1 = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.setTime(dt2);
			iYear2 = calendar.get(Calendar.YEAR);
			iMonth2 = calendar.get(Calendar.MONTH) + 1;
			iDay2 = calendar.get(Calendar.DAY_OF_MONTH);

			if (flag == 0) {
				diff = Math.abs(iYear2 - iYear1);
				if (dt2.getTime() >= dt1.getTime()) { // dt2 > dt1
					if (iMonth2 < iMonth1 || iDay2 < iDay1) {
						diff -= 1;
					}
				} else {
					if (iMonth1 < iMonth2 || iDay1 < iDay2) {
						diff -= 1;
					}
				}
			} else if (flag == 1) { // 不足年
				diff = Math.abs(iYear2 - iYear1);
			}
			return Integer.toString(diff);

		} catch (ParseException e) {
			throw new RuntimeException("CALCYEAR");
		}
	}

	/**
	 * <p>
	 * 将带正负号的金额转换成COBOL数据格式
	 * </p>
	 * 
	 * <pre>
	 *           判断金额是否带负号，如果带有负号，判断金额字符串的最后一位，
	 *           如果为0，则改为p, 如果为1，则改为q …… 依次类推。
	 *           第二个参数为转换后金额的长度
	 * </pre>
	 * 
	 * @param amt
	 *            不带逗号的金额字符串，可以有负号
	 * @param len
	 *            转换后COBOL格式的金额字符串的长度
	 * @return 返回COBOL金额，不带逗号、小数点，出错返回null
	 */
	public static String NORMAL_TO_COBOL(String args1, int len) {
		String ret = "";
		String amt = args1.trim();

		if (StringUtils.isEmpty(amt)) {
			throw new RuntimeException("NORMAL_TO_COBOL");
		}

		if (StringUtils.contains(amt, ',') || !NumberUtils.isNumber(StringUtils.trim(amt))) {
			throw new RuntimeException("NORMAL_TO_COBOL");
		}
		int iLen = len;
		if (iLen <= 0)
			throw new RuntimeException("NORMAL_TO_COBOL");

		double fAmt = NumberUtils.toDouble(amt);
		int flag = 0;

		if (amt.indexOf('-') != -1) {
			flag = -1;
		}

		DecimalFormat df = new DecimalFormat("#"); // 去掉小数点
		ret = df.format(Math.abs(fAmt));

		if (ret.length() < iLen) {
			StringBuffer buf = new StringBuffer(ret);
			for (int i = 0; i < iLen - ret.length(); i++) {
				buf.insert(0, "0");
			}
			ret = buf.toString();
		}
		if (flag == -1) { // 为负值
			char[] buf1 = ret.toCharArray();
			int i = '0';
			int j = 'p';
			int k = buf1[buf1.length - 1];
			buf1[buf1.length - 1] = (char) (k - i + j);

			ret = new String(buf1);
		}
		return ret;
	}

	/**
	 * <p>
	 * 将COBOL数据格式的金额转换成带正负号的金额
	 * </p>
	 * 
	 * @param amt
	 *            COBOL金额格式
	 * @param len
	 *            转换后带正负号金额的长度
	 * @return 可以带正符号的金额，出错，返回null
	 * 
	 */
	public static String COBOL_TO_NORMAL(String args, int len) {
		int flag = 1;
		String ret = "";
		if (args == "")
			throw new RuntimeException("COBOL_TO_NORMAL");
		String amt = StringUtils.trim(args);
		if (amt == null) {
			return StringUtils.leftPad("", len, '0');
		}

		// if (StringUtils.isEmpty(amt) || !StringUtils.isNumeric(len.trim())) {
		// throw new RuntimeException(HiMessageCode.ERR_EXP_ARGS,
		// "COBOL_TO_NORMAL", "amt|len");
		// }

		if (amt.charAt(amt.length() - 1) >= 'p' && amt.charAt(amt.length() - 1) <= 'y') {
			char[] buf = amt.toCharArray();
			flag = -1;
			buf[amt.length() - 1] = (char) (buf[amt.length() - 1] - 'p' + '0');
			amt = new String(buf);
		}

		double dAmt = NumberUtils.toDouble(amt);
		DecimalFormat ft = new DecimalFormat("#");
		ret = ft.format(dAmt);
		StringBuffer buf1 = new StringBuffer(ret);
		if (ret.length() < len - 1) { // 长度不够，前补0
			for (int i = 0; i < len - 1 - ret.length(); i++) {
				buf1.insert(0, "0");
			}
			ret = buf1.toString();
		}
		if (flag == -1) { // 补上符号
			buf1.insert(0, '-');
		} else if (buf1.length() < len) {
			buf1.insert(0, '0');
		}
		// return StringUtils.leftPad(buf1.toString(), len, '0');
		return buf1.toString();
	}

	/**
	 * <p>
	 * 将带正负号的金额转换成EBCDIC格式 判断金额是否带负号，根据它对最后一位字符作一下的处理：
	 * 
	 * 正数: 0 -> { 1~9 -> A~I 负数： 0 -> } 1~9 -> J~R
	 * </p>
	 * 
	 * <pre>
	 *           NORMAL_TO_EBCD(-123,15)，结果为：00000000000012L 
	 *           NORMAL_TO_EBCD(12300,15)，结果为：00000000001230{
	 * </pre>
	 * 
	 * @param amt
	 *            不带逗号的金额字符串，可以有正负号
	 * @param len
	 *            输出金额长度
	 * @return EBCD格式金额，出错返回null
	 */
	public static String NORMAL_TO_EBCD(String args, int len) {
		double dNum;
		int flag = 1, iLen = 0;
		String ret = "";
		dNum = NumberUtils.toDouble(args);
		;
		iLen = len;
		if (dNum < 0d)
			flag = -1;
		DecimalFormat ft = new DecimalFormat("#");
		ret = ft.format(Math.abs(dNum));
		char[] buf = ret.toCharArray();
		if (flag == -1) { // 负数
			if (buf[buf.length - 1] == '0') {
				buf[buf.length - 1] = '}';
			} else {
				buf[buf.length - 1] = (char) (buf[buf.length - 1] - '1' + 'J');
			}
		} else { // 正数
			if (buf[buf.length - 1] == '0') {
				buf[buf.length - 1] = '{';
			} else {
				buf[buf.length - 1] = (char) (buf[buf.length - 1] - '1' + 'A');
			}
		}
		ret = new String(buf);

		if (ret.length() < iLen) {
			StringBuffer retBuf = new StringBuffer(ret);
			for (int i = 0; i < iLen - ret.length(); i++) {
				retBuf.insert(0, '0');
			}
			ret = retBuf.toString();
		}

		return ret;
	}

	/**
	 * <p>
	 * 给字符型金额添加小数点
	 * </p>
	 * 
	 * 
	 * @param str
	 * @return 添加小数点的金额
	 */
	public static String AMTADDDOT(String args) {

		String str = args.trim();
		if (StringUtils.isEmpty(args) || !NumberUtils.isNumber(str))
			throw new RuntimeException("AMTADDDOT");
		StringBuffer buf = new StringBuffer(str);
		int flag = 1;
		if (buf.charAt(0) == '-') {
			flag = -1;
			buf = buf.delete(0, 1);
		}
		if (buf.length() >= 3) {
			buf.insert(buf.length() - 2, '.');
		} else if (buf.length() == 2) {
			buf.insert(0, "0.");
		} else if (buf.length() == 1) {
			buf.insert(0, "0.0");
		} else if (buf.length() == 0) {
			buf.insert(0, '0');
		}
		if (flag == -1) {
			buf.insert(0, '-');
		}

		return buf.toString();
	}

	/**
	 * <p>
	 * 将金额转换为大写，金额不带小数点
	 * </p>
	 * 
	 * @param amt
	 *            金额不带小数点
	 * @return 大写金额字符串,出错返回NULL
	 * 
	 */
	public static String AMTTOCAP(String args) {

		String amt = args.trim();
		if (StringUtils.isEmpty(amt) || !StringUtils.isNumeric(amt))
			throw new RuntimeException("AMTTOCAP");

		if (amt.length() > 15)
			throw new RuntimeException("AMTTOCAP");

		char[] Amount = amt.toCharArray();
		StringBuffer ChAmount = new StringBuffer();
		int i, cunt = 1000, len, flag = 0;
		int endzero = 0, count_zero;
		char tmp;

		len = amt.length();

		for (i = 0; i < len; i++) {
			if (Amount[i] < '0' || Amount[i] > '9') {
				throw new RuntimeException("AMTTOCAP");
			} else {
				if (cunt == 1000 && Amount[i] != '0') {
					cunt = i;
				}
				if (Amount[i] != '0' && i < len - 2) {
					endzero = i;
				}
			}
		}

		count_zero = 0;
		for (i = cunt; i < len; i++) {
			flag = Amount[i] - '0';
			if (flag == 0) { // 数字0的情况
				count_zero++;
				if (len - i != 3 && len - i != 7 && len - i != 11 && len - i != 15) {
					/**
					 * 不在元、万、亿、万亿位 且第i+1位不为零，如果为零，则继续执行，直到不为零止
					 */
					if (i < len - 1 && Amount[i + 1] != '0' && i <= endzero) {
						tmp = DIGIT_UPPER.charAt(flag); // 为零
						ChAmount.append(tmp);
						count_zero = 0;
					} else if (i == len - 2 && Amount[len - 1] != '0') {
						// 角位为零,并且分位不为0
						ChAmount.append("零");
						count_zero = 0;
					}
				} else if (count_zero == 4 && len - i == 7) {
					/* 万元前4位都为零,不能加万字 */
					count_zero = 0;
				} else {
					/**
					 * 元、亿、万亿位要加上金额数字 例如：1000 0000 0000 00 壹仟亿元整
					 */
					tmp = AMT_UPPER.charAt(len - i - 1);
					ChAmount.append(tmp);
					count_zero = 0;
				}
			} else { // 不为0
				count_zero = 0;
				tmp = DIGIT_UPPER.charAt(flag);
				ChAmount.append(tmp);
				tmp = AMT_UPPER.charAt(len - i - 1);
				ChAmount.append(tmp);
			}
		}

		if (flag == 0) {
			// 末尾为零，加整
			ChAmount.append("整");
		}

		return ChAmount.toString();
	}

	/**
	 * <p>
	 * 去掉金额前面的零
	 * </p>
	 * 
	 * @param amt
	 *            金额，不带小数点，不带正负号
	 * @return 去零后的金额字符串，出错返回null
	 * 
	 */
	public static String AMTDELZERO(String agrs) {
		String amt = agrs.trim();
		if (StringUtils.isEmpty(amt) || !StringUtils.isNumeric(amt)) {
			throw new RuntimeException("AMTDELZERO");
		}
		double dou = NumberUtils.toDouble(amt);
		DecimalFormat ft = new DecimalFormat("#");
		return ft.format(dou);
	}

	/**
	 * <p>
	 * 格式化金额，去掉前面的零，加小数点和‘,’分隔符
	 * </p>
	 * 
	 * 
	 * @param amt
	 *            金额,不能有逗号，小数点，可以有前补0
	 * @return 格式化后的金额
	 */
	public static String AMTFMT(String args) {
		String amt = args.trim();
		if (StringUtils.isEmpty(amt) || !NumberUtils.isNumber(amt))
			throw new RuntimeException("AMTFMT");
		int flag = 1;
		int i, j, len;

		amt = AMTDELZERO(amt);
		if (amt.charAt(0) == '-') {
			flag = -1;
			amt = amt.substring(1);
		}

		StringBuffer buf = new StringBuffer(amt);
		if (amt.length() < 3) {
			amt = AMTADDDOT(amt);
			buf.replace(0, buf.length(), amt);
			if (flag == -1) {
				buf.insert(0, '-');
			}
			return buf.toString();
		}

		len = amt.length();
		if ((len - 2) % 3 == 0) {
			j = (len - 2) / 3 - 1;
		} else {
			j = (len - 2) / 3;
		}

		for (i = 0; i < j; i++) {
			buf.insert(len - 2 - (i + 1) * 3, ',');
		}
		buf.insert(buf.length() - 2, '.');
		if (flag == -1) {
			buf.insert(0, '-');
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * 金额简单格式化，去掉前面的零，加小数点,与AMTFMT功能相似，只是不用逗号分割
	 * </p>
	 * 
	 * @param amt
	 *            金额,可以有前补0，不能有逗号和小数点
	 * @return 没有前导零，有小数点的金额字符串，出错返回null
	 */
	public static String AMTSIMPLEFMT(String args) {

		String amt = args.trim();
		if (StringUtils.isEmpty(args) || !NumberUtils.isNumber(amt))
			throw new RuntimeException("AMTSIMPLEFMT");
		if (StringUtils.contains(amt, '.') || StringUtils.contains(amt, ',')) {
			throw new RuntimeException("AMTSIMPLEFMT 表达式不允许带有小数点或者逗号");
		}
		int flag = 1;
		amt = AMTDELZERO(amt);
		if (amt.charAt(0) == '-') {
			flag = -1;
			amt = amt.substring(1);
		}
		StringBuffer buf = new StringBuffer(amt);
		amt = AMTADDDOT(amt);
		buf = new StringBuffer(amt);
		if (flag == -1) {
			buf.insert(0, '-');
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * 检查某币种金额是否错误
	 * </p>
	 * 
	 * <pre>
	 *           检查某种币种的金额是否错误。检查某币种的金额数据的小数点后
	 *           位数是否符合规定。如果不符合规定，则返回真。
	 * </pre>
	 * 
	 * @param cyccod
	 *            币种，如CHY
	 * @param amt
	 *            以分为单位的金额字符串
	 * @return 不符合返回1，符合返回0，出错返回null
	 * 
	 */
	public static int CYCAMTFALSE(String args1, String args2) {

		String cyccod = args1.trim();
		String amt = args2.trim();
		if (!NumberUtils.isNumber(amt) || StringUtils.isEmpty(cyccod))
			throw new RuntimeException("CYCAMTFALSE");

		int len = 0; // 金额最后两位数字非零的个数
		int ret = 0;
		for (int i = 0; i < 2; i++) {
			if (amt.charAt(amt.length() - i - 1) != '0') {
				len++;
			}
		}
		if (StringUtils.equals(cyccod, "ITL") || StringUtils.equals(cyccod, "JPY")) {
			if (len != 0) {
				ret = 1; // 不符合，返回“1”
			}
		}
		return ret;
	}

	/**
	 * <p>
	 * 将金额乘10 的NUM 次方，返回乘积中小数点左边的数据
	 * </p>
	 * 
	 * @param 参数配置说明
	 *            - 两个参数
	 * @param args
	 *            - 参数一:金额 不带逗号,可以带小数点; 参数二:10 的次方数
	 * @return 金额，不带逗号、小数点 @
	 */
	public static String AMTPOWER(String args, int num) {
		if (StringUtils.isEmpty(args) || !NumberUtils.isNumber(args)) {
			throw new RuntimeException("AMTPOWER");
		}
		int idx = 1;
		for (int c = 0; c < num; c++)
			idx *= 10;
		long l = new BigDecimal(args.trim()).multiply(new BigDecimal(idx)).longValue();
		return String.valueOf(l);
	}

	/**
	 * <p>
	 * 取指定系统环境变量名的值
	 * </p>
	 * 
	 * @param 参数配置说明
	 *            - 一个参数
	 * @param args
	 *            - 参数一:环境变量名
	 * @return 系统环境变量的值 @
	 */
	public static String GETENV(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("GETENV");
		String value;
		try {
			value = System.getenv(args.trim());
			if (StringUtils.isEmpty(value)) {
				value = System.getProperty(args.trim());
			}
			return value;
		} catch (Throwable t) {
			return System.getProperty(args.trim());
		}
		// return "/home/icsdev";
	}

	/**
	 * <p>
	 * 金额相加
	 * </p>
	 * 
	 * @param args
	 *            [0] : 金额1 不带逗号、小数点
	 * @param args
	 *            [0] : 金额2 不带逗号、小数点
	 * @return 金额，不带逗号、小数点; 出错:-1 @
	 */
	public static long ADDAMT(long args1, long args2) {
		return args1 + args2;
	}

	/**
	 * <p>
	 * 金额相减, 注意金额不带逗号, 小数点，可以带正负号
	 * </p>
	 * 
	 * @param args
	 *            [0] : 金额1
	 * @param args
	 *            [1] : 金额2
	 * @return 相减后的数字 @
	 */
	public static long SUBAMT(long args1, long args2) {

		return args1 - args2;
	}

	/**
	 * <p>
	 * 将十六进制字符串转换成普通字符串
	 * </p>
	 * HEX2STR(313233343536)返回结果为123456 <br>
	 * 
	 * @param args
	 *            [0] : 十六进制字符串
	 * @return 转换后的字符串 @
	 */
	public static String HEX2STR(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("HEX2STR");
		try {
			return new String(Hex.decodeHex(args.toCharArray()));
		} catch (DecoderException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * <p>
	 * 将普通字符串转换成十六进制字符串
	 * </p>
	 * STR2HEX(123456)返回结果为313233343536 <br>
	 * 
	 * @param args
	 *            [0] : 普通字符串
	 * @return 十六进制字符串 @
	 */
	public static String STR2HEX(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("HEX2STR");
		return new String(Hex.encodeHex(args.getBytes()));
	}

	/**
	 * <p>
	 * 替换字符串中指定字符的所有字符 查找字符串中所有需要替换的字符，并替换成目标字符
	 * </p>
	 * 
	 * @param args
	 *            [0] : 输入字符串
	 * @param args
	 *            [1] : 需要替换的源字符
	 * @param args
	 *            [2] : 替换后的目标字符
	 * @return 替换处理后的字符串 @
	 */
	public static String REPCHAR(String args1, String args2, String args3) {
		if (StringUtils.isEmpty(args1) || StringUtils.isEmpty(args2) || StringUtils.isEmpty(args3))
			throw new RuntimeException("REPCHAR");
		return StringUtils.replace(args1, args2, args3);
	}

	/**
	 * <p>
	 * 将EBCDIC 数据格式的金额转换成带正负号的金额,转换关系参见函数NORMAL_TO_EBCD <br>
	 * 例子： EBCD_TO_NORMAL(000000012{,15)，结果为：000000000000120
	 * EBCD_TO_NORMAL(00001230R,15)， 结果为：-00000000012309
	 * 
	 * @param args
	 * @return @
	 */
	public static String EBCD_TO_NORMAL(String args, int len) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("EBCD_TO_NORMAL");
		if (StringUtils.isBlank(args)) {
			throw new RuntimeException("novalid ebcd value[" + args + "]");
		}
		int outPutLen = len;

		String ebcd = args;

		char lab = ebcd.charAt(ebcd.length() - 1);
		String normal = ebcd.substring(0, ebcd.length() - 1);

		int index = EBCD_POSITIVE.indexOf(lab);
		if (index >= 0) {
			normal += index;
			normal = StringUtils.leftPad(normal, outPutLen, '0');
			return normal;
		}

		index = EBCD_NEGATIVE.indexOf(lab);
		if (index >= 0) {
			normal += index;

			normal = "-" + StringUtils.leftPad(normal, outPutLen - 1, '0');
			return normal;
		}

		throw new RuntimeException("novalid ebcd value[" + ebcd + "]");
	}

	/**
	 * <p>
	 * 将二进制字符串转换成十六进制字符串 <br>
	 * 处理说明:将由0、1 组成的字符串，按每8 个字节转换成2 个字节十六进制字符
	 * 
	 * @param args
	 * @return @
	 */
	public static String BIN2HEX(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("BIN2HEX");
		try {
			return Integer.toHexString(Integer.valueOf(args.trim(), 2).intValue());
		} catch (NumberFormatException e) {
			throw new RuntimeException("含有非二进制的数字");
		}
	}

	/**
	 * <p>
	 * 将十六进制字符串转换成二进制字符串 <br>
	 * 处理描述 按每2 个字节转换成8 个字节二进制字符
	 * 
	 * @param args
	 * @return @
	 */
	public static String HEX2BIN(String args) {
		if (StringUtils.isEmpty(args))
			throw new RuntimeException("HEX2BIN");
		String binStr = "";
		try {
			binStr = Integer.toBinaryString(Integer.valueOf(args.trim(), 16).intValue());
		} catch (NumberFormatException e) {
			throw new RuntimeException("含有非十六进制的数字");
		}

		// 补足4位
		int tmpLen = 4 - binStr.length() % 4;
		if (tmpLen != 0) {
			binStr = StringUtils.repeat("0", tmpLen) + binStr;
		}
		return binStr;
	}

	/**
	 * <p>
	 * 将输入的YYYYMMDD 八格式的日期转成大写,
	 * <p>
	 * 例子： DATETOCAP(20001110)，结果为：贰零零零年壹拾壹月壹拾零日
	 * 
	 * @param args
	 * @return @
	 */
	public static String DATETOCAP(String args) {
		if (StringUtils.isEmpty(args) || args.trim().length() != 8)
			throw new RuntimeException("DATETOCAP 参数错误");

		if (CHECKDATE(args) != 1) {
			throw new RuntimeException(args.trim() + "不是合法的日期 ");
		}

		char[] dateArray = args.trim().toCharArray();
		int flag = 0;
		String year = "", month = "", day = "";
		for (int i = 0; i < 4; i++) {
			if (dateArray[i] < '0' || dateArray[i] > '9') {
				throw new RuntimeException(args.trim());
			}
			flag = dateArray[i] - '0';
			year += DIGIT_UPPER.charAt(flag);
		}
		year += "年";

		for (int i = 4; i < 6; i++) {
			if (dateArray[i] < '0' || dateArray[i] > '9') {
				throw new RuntimeException(args.trim());
			}
			flag = dateArray[i] - '0';
			month += DIGIT_UPPER.charAt(flag);
		}
		month += "月";

		for (int i = 6; i < 8; i++) {
			if (dateArray[i] < '0' || dateArray[i] > '9') {
				throw new RuntimeException(args.trim());
			}
			flag = dateArray[i] - '0';
			day += DIGIT_UPPER.charAt(flag);
		}
		day += "日";
		return year + month + day;
	}

	/**
	 * <p>
	 * 取指定文件的行数
	 * 
	 * @param args
	 *            文件名 支持绝对路径和相对路径。如果是相对路径，取HOME 环境变量和 输入文件名组合成全路径文件名
	 * @return @
	 */
	public static String GETFILELINES(String... args) {
		if (args.length < 1)
			throw new RuntimeException("GETFILELINES");
		String file = args[0].trim();
		File filepath = new File(file);
		// 相对路径要添加HOME 环境变量
		if (!filepath.isAbsolute()) {
			String root = GETENV("HOME");
			if (root != null) {
				if (root.endsWith("/")) {
					file += root;
				} else {
					file += root + "/";
				}
			} else {
				throw new RuntimeException("HOME");
			}
		}

		int total = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			while (br.ready()) {
				br.readLine();
				total += 1;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(file);
		} catch (IOException e) {
			throw new RuntimeException(file);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return String.valueOf(total);
	}

	/**
	 * <p>
	 * 取指定文件的大小（字节总数）
	 * 
	 * @param args
	 *            文件名 支持绝对路径和相对路径。如果是相对路径，取HOME 环境变量和 输入文件名组合成全路径文件名
	 * @return @
	 */
	public static long GETFILESIZE(String args) {
		String file = args.trim();
		File filepath = new File(file);
		// 相对路径要添加HOME 环境变量
		if (!filepath.isAbsolute()) {
			String root = GETENV("HOME");
			if (root != null) {
				filepath = new File(root, file);
			} else {
				throw new RuntimeException("HOME");
			}
		}

		long total = 0;

		total = filepath.length();

		return total;
	}

	/**
	 * <p>
	 * 中文域乱码处理 <br>
	 * 将字符串中存在不规范的双字节字符替换成全角空格
	 * 
	 * @param args
	 * @return @
	 */
	public static String CHAOSCODE(String... args) {
		if (args.length < 1)
			throw new RuntimeException("CHAOSCODE");
		if (args[0] == null)
			return null;

		String str = args[0];
		String tStr;
		StringBuffer outStr = new StringBuffer();
		byte[] b;
		int len = str.length();

		for (int i = 0; i < len; i++) {
			tStr = str.substring(i, i + 1);
			try {
				b = tStr.getBytes("GBK");
				if (b[0] > -1 || b[1] > -1) {
					tStr = "　";
					outStr.append(tStr);
				} else {
					outStr.append(tStr);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return outStr.toString();
	}

	/**
	 * <p>
	 * 正则表达式匹配
	 * </p>
	 * 
	 * @param args
	 *            [0]: 字符串
	 * @param args
	 *            [1]: Pattern字符串
	 * @param args
	 *            [2]: Case Sensitive,可选
	 * @return @
	 */
	public static int IS_MATCH(String... args) {
		if (args.length < 2)
			throw new RuntimeException("IS_MATCH");
		boolean ignoreCase = true;
		if (args.length == 3) {
			if ("1".equals(args[2]))
				ignoreCase = false;
		}

		Pattern p = null;

		if (ignoreCase)
			p = Pattern.compile(args[1]);
		else
			p = Pattern.compile(args[1], Pattern.CASE_INSENSITIVE);

		Matcher m = p.matcher(args[0]);

		if (m.matches())
			return 1;
		else
			return 0;
	}

	/**
	 * <p>
	 * 三元表达式
	 * </p>
	 * 
	 * @param args
	 *            [0]: 条件
	 * @param args
	 *            [1]: 表达式1
	 * @param args
	 *            [2]: 表达式2
	 * @return @
	 */
	public static String CONDITION3(String cond, String arg1, String arg2) {
		boolean r = cond.equals("1");
		// boolean r = cond.equalsIgnoreCase("true");
		return r ? arg1 : arg2;
	}

	/**
	 * <p>
	 * 判断字符串是否为NULL
	 * </p>
	 * 
	 * @param args
	 *            [0]: 子符串
	 * @return 0 存在 1不存在
	 */
	public static int IsExistNode(String... args) {
		if (args.length < 1)
			throw new RuntimeException("IsExistNode");
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null || "".equals(args[i]))
				return 0;
		}
		return 1;
	}

	/**
	 * 取随机字符串 <br>
	 * 
	 * @param arg0
	 *            构造指定长度的随机字符串
	 * @param arg1
	 *            指明是否包含字母，0-包含字母,数字和字母混合,默认是2 1-不包含数字,只有字母 2－不包含字母,只有数字
	 * @return @
	 */
	public static String RANDOM(int args1, String args2) {
		if (StringUtils.isEmpty(args2))
			throw new RuntimeException("RANDOM");
		int len = args1;
		args2 = StringUtils.trim(args2);

		if (StringUtils.equals(args2, "0")) {
			return RandomStringUtils.randomAlphanumeric(len);
		} else if (StringUtils.equals(args2, "1")) {
			return RandomStringUtils.randomAlphabetic(len);
		} else {
			return RandomStringUtils.randomNumeric(len);
		}

	}

	/**
	 * 把二进制格式转为十进制整数
	 * 
	 * @param bin
	 *            二进制形式的字符串,eg: "10101010"
	 * @return 十进制整数
	 * @throws HiException
	 */
	public static int binary2Int(String bin) throws RuntimeException {
		try {
			return Integer.valueOf(bin, 2).intValue();
		} catch (NumberFormatException e) {
			// TODO bin 包含非法二进制
			throw new RuntimeException("错误提示信息: 待转换的bin-[" + bin + "], 包含非法二进制形式.");
		}
	}

	/**
	 * 将一个ASCII值转为对应的字符
	 * 
	 * @param strAsc
	 *            ASCII值
	 * @return 字符
	 */
	public static String asc2bin(String strAsc) throws RuntimeException {
		try {
			Integer deliInt = Integer.valueOf(strAsc);
			if (deliInt.intValue() > 255 || deliInt.intValue() < -128) {
				throw new RuntimeException("转换为bin有误, asc=[" + strAsc + "]");
			}

			byte[] asc = { deliInt.byteValue() };
			return new String(asc, "ISO-8859-1");

			// return String.valueOf((char)Byte.parseByte(strAsc));
		} catch (NumberFormatException e) {
			// TODO log asc error
			throw new RuntimeException("asc2bin执行出错, 一个ASCII值 + [" + strAsc + "],转为对应的字符时失败.");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("asc2bin执行出错, 不支持相应编码集.");
		}
	}

	/**
	 * 把十进制整数转化为二进制格式
	 * 
	 * @param val
	 * @return 二进制形式的字符串,eg: "10101010"
	 */
	public static String int2Binary(int val) {
		return Integer.toBinaryString(val);
	}

	public static String HexToDecimal(String val) {
		return String.valueOf(Integer.valueOf(val, 16));
	}

	public static String URLDECODER(String str) throws Exception {
		String res = URLDecoder.decode(str, "GBK");
		return res;
	}

	/** 已GBK编码 */
	public static String URLECODERGBK(String str) throws Exception {
		return URLEncoder.encode(str, "GBK");
	}

	public static String URLDECODER(String str, String encoding) throws Exception {
		String res = URLDecoder.decode(str, encoding);
		return res;
	}

}