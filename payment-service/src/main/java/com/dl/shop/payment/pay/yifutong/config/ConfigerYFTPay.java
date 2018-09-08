package com.dl.shop.payment.pay.yifutong.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
public class ConfigerYFTPay {
	
	@Value("${yizhi.app_mchNo}")
	private String APP_MCHNO;
	
	@Value("${yizhi.app_account}")
	private String APP_ACCOUNT;
	
	@Value("${yizhi.app_notifyUrl}")
	private String APP_NOTIFYURL;
	
	@Value("${yizhi.app_succPage}")
	private String APP_SUCCPAGE;
	
	@Value("${yizhi.app_token}")
	private String APP_TOKEN;
	
//	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "1";
	
	public static String getPayTime() {
		DateTime date = new DateTime();
		return String.valueOf(date.getMillis());
	}
}
