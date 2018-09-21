package com.dl.shop.payment.pay.youbei.config;

import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
public class ConfigerUBeyPay {
	
	@Value("${ubey.app.debug}")
	private String DEBUG;
	
	@Value("${ubey.app_account}")
	private String APP_ACCOUNT;
	
	@Value("${ubey.notify_url}")
	private String NOTIFY_URL;
	
	@Value("${ubey.callback_url}")
	private String CALLBACK_URL;
	
	@Value("${ubey.pay_url}")
	private String PAY_URL;
	
	@Value("${ubey.query_url}")
	private String QUERY_URL;
	
	@Value("${ubey.bank_url}")
	private String BANK_URL;
	
	@Value("${ubey.ubeyapi_url}")
	private String UBEYAPI_URL;
	
	@Value("${ubey.public_key}")
	private String PUBLIC_KEY;
	
	@Value("${ubey.private_key}")
	private String PRIVATE_KEY;
	
	public  String getNotifyUrl() throws Exception {
		return URLEncoder.encode(NOTIFY_URL,"utf-8");
	}
	
	public  String getCallbackUrl() throws Exception {
		return URLEncoder.encode(CALLBACK_URL,"utf-8");
	}
	
	public  String getPayUrl() throws Exception {
		return URLEncoder.encode(PAY_URL,"utf-8");
	}
	
	public  String getQueryUrl() throws Exception {
		return URLEncoder.encode(QUERY_URL,"utf-8");
	}
}
