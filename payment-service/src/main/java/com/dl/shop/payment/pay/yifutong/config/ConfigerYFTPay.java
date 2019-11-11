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
	
	@Value("${yifutong.app.debug}")
	private String DEBUG;
	
	@Value("${yifutong.app_mchNo}")
	private String APP_MCHNO;
	
	@Value("${yifutong.app_notifyUrl}")
	private String APP_NOTIFYURL;
	
	@Value("${yifutong.app_succPage}")
	private String APP_SUCCPAGE;
	
	@Value("${yifutong.app_token}")
	private String APP_TOKEN;
	
	@Value("${yifutong.pay_url}")
	private String PAY_URL;
	
	@Value("${yifutong.query_url}")
	private String QUERY_URL;
	
//	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "1";
	
	public static String getPayTime() {
		DateTime date = new DateTime();
		return String.valueOf(date.getMillis());
	}
}
