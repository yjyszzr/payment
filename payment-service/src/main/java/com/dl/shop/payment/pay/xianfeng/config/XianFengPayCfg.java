package com.dl.shop.payment.pay.xianfeng.config;

public class XianFengPayCfg {

	public static final String NET_GATE = "http://sandbox.firstpay.com/security/gateway.do";	//支付网关

	public static final String MERCHANT_NO = "M200000550";
	
	public static final String TRANSCUR = "156";
	
	public static final String CERTIFICATETYPE = "0";	//证件类型
	
	public static final String NOTIFY_URL = "http://39.106.18.39:7076/payment/xianfeng/notify";
	
	public static final String RSA_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChFetx5+VKDoEXzZ+5Wozt3MfWMM/TiKMlWmAKXBViv8/e6j6SU/lSlWkMajd59aiWczs+qf9dMuRpe/l9Qke9DnVMn24JNLXjWD+y+w3yKRwd3CTtF7gx8/ToZl5XqFIT5YB1QfQCdAf8Z18IdQrJIijs8ssczY/RfqKZLo+KLQIDAQAB";
	
	public static final String VERSION = "4.0.0";
	
	public static final String SEC_ID = "RSA";
	
	/**
	 * merId=M200000111
	merRSAKey=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsPVKczbQlVcn4gr3FfgPytV3oHTPuGSaVPV5n8PqaQlbb+T+z+2O1eoBFZ9bL/mvny02bJcq0SVUlQAyfK26t32m0kxrr19v0yEQIpHR8eZLUESrF0Uynm/ntUPiy0Zglt92+2fLUykWJUJJ6gFRV6yUrxGE0gCtp1cOj8R1czwIDAQAB
	secId=RSA
	gateway=http://1.2.7.2:8092/gateway.do
	returnUrl=http://1.2.7.1:8080/payQuickApply/ReceiveReturn
	noticeUrl=http://1.2.7.1:8080/payQuickApply/ReceiveNotice
	 */
}
