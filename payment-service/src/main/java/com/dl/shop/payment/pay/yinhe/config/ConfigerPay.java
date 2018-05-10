package com.dl.shop.payment.pay.yinhe.config;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigerPay {
	public static final String ORG_NO = "2188";
	public static final String CHAR_SET = "UTF-8";
	public static final String SIGN_TYPE = "MD5";
	
	public static final String URL_PAY = "http://zfyun.com.cn:8080/YinHeLoan/yinHe";
	public static final String URL_PAY_CALLBACK = "http://39.106.18.39:7076/payment/wxpay/notify";
	public static final String URL_REDIRECT = "http://caixiaomi.net";
//	public static final String MERCHANT_NO = "1344296701";
	public static final String MERCHANT_NO = "1503174711";
	public static final String DEVICE_NO = "kdt1070605";
									   //388bef8044e16a554bd0f46bc2768071
//	public static final String SECRET = "77c21827d4725764696718349e5044d6";
//	public static final String SECRET = "639af6b6b390d3d8325253fa1ecd571c";
//	public static final String SECRET = "af9cc226dc9e70ae181b04c0ff181de7";
//	public static final String SECRET = "77c21827d4725764696718349e5044d6";
//	public static final String SECRET = "34884fd5f1d4eea10d3d410a44678e3f";
	public static final String SECRET_PUBLIC = "77c21827d4725764696718349e5044d6";
	public static final String SECRET = SECRET_PUBLIC;
	
	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "2";
	
	public static String getPayTime() {
		SimpleDateFormat sdfTime = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String str = sdfTime.format(date);
		return str;
	}
	
}
