package com.dl.shop.payment.pay.yinhe.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
public class ConfigerPay {
	
//	@Value("${yinhe.app.debug}")
//	private String DEBUG;
	
	@Value("${yinhe.app_wechat_jump_h5}")
	private String URL_PAY_WECHAT_H5;
	
	@Value("${yinhe.app_wechat_jump_app}")
	private String URL_PAY_WECHAT_APP;
	
	@Value("${yinhe.app_org_no}")
	private String ORG_NO = "2188";
	
	@Value("${yinhe.app_charset}")
	private String CHAR_SET = "UTF-8";
	
	@Value("${yinhe.app_charset}")
	private String SIGN_TYPE = "MD5";
	
	@Value("${yinhe.app_url_pay}")
	private String URL_PAY = "http://zfyun.com.cn:8080/YinHeLoan/yinHe";
	
	@Value("${yinhe.app_notify}")
	private String URL_PAY_CALLBACK = "http://api.caixiaomi.net/api/payment/payment/wxpay/notify";
	
//	public static final String URL_REDIRECT = "http://caixiaomi.net";
	
	@Value("${yinhe.app_redirect_h5}")
	private String URL_REDIRECT_H5;
	
	@Value("${yinhe.app_redirect_app}")
	private String URL_REDIRECT_APP;
	
	@Value("${yinhe.app_mch_id}")
	private String MERCHANT_NO;
	
	@Value("${yinhe.app_device}")
	private String DEVICE_NO;
	
	@Value("${yinhe_app_screct}")
	private String SECRET_PUBLIC;
	
	@Value("${yinhe_app_screct}")
	private String SECRET;
	
	@Value("${yinhe.app.app_id}")
	private String APPID;
	
	
	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "2";
	
	public static String getPayTime() {
		SimpleDateFormat sdfTime = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String str = sdfTime.format(date);
		return str;
	}
}
