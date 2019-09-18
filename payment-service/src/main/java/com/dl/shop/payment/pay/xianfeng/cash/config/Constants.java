package com.dl.shop.payment.pay.xianfeng.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
public class Constants {
	public static final String TRANSCUR = "156";
	public static final String CERTIFICATETYPE = "0";	//证件类型
	@Value("${xianfeng.isdebug}")
	private Boolean IS_DEBUBG;
	@Value("${xianfeng.testMode}")
	private Boolean TESTMODE;
//	http://49.232.65.109:9805/user/quickinfo?id=89348
	@Value("${xianfeng.pay_h5_url}")
	private String PayH5Url;
	
	
//	public static String MER_ID = "M200000550";
	@Value("${xianfeng.app_merid}")
	private String MER_ID;
	
//	public static String MER_RSAKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChFetx5+VKDoEXzZ+5Wozt3MfWMM/TiKMlWmAKXBViv8/e6j6SU/lSlWkMajd59aiWczs+qf9dMuRpe/l9Qke9DnVMn24JNLXjWD+y+w3yKRwd3CTtF7gx8/ToZl5XqFIT5YB1QfQCdAf8Z18IdQrJIijs8ssczY/RfqKZLo+KLQIDAQAB";
	@Value("${xianfeng.app_mer_rsakey}")
	private String MER_RSAKEY;

	@Value("${xianfeng.app_secid}")
	private String SEC_ID = "RSA";
	
//	public static String UCF_GATEWAY_URL = "http://sandbox.firstpay.com/security/gateway.do";
	@Value("${xianfeng.app_ufc_gateway}")
	private String UCF_GATEWAY_URL;
	
//	public static String RETURN_URL = "http://1.2.7.1:8080/withdraw/ReceiveReturn";
	
//	public static String NOTICE_URL = "http://49.232.65.109:7076/cash/notify";
	@Value("${xianfeng.app_notice_url}")
	private String NOTICE_URL;
	
	@Value("${xianfeng.app_payment_notice_url}")
	private String APP_PAYMENT_NOTICE_URL;
	
	@Value("${xianfeng.app_version}")
	private String VERSION;
}
