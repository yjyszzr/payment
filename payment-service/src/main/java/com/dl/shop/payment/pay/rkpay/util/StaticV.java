package com.dl.shop.payment.pay.rkpay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaticV {
//    public static String ds_id="DS1904231609145999";//渠道号
//    public static String secret="44d8a9ec12b47e59a13d490e2a42c7a3";//渠道商密钥
//    public static String serverRoot="https://openapi.haodiana.cn";//接口地址
//    public static String version="1.0";//版本号
//    public static String sign_type="MD5";//签名类型
//    public static int expire_time=30;//付款有效期，单位分钟
//    public static String mpid="MP1904241125194799";//商户池编号
//    public static String mchid="MC1905051517214236";//商户号
//    public static String notify_url="http://94.191.113.169:8765/api/payment/payment/notify/RkPayNotify";//交易通知
//    public static String callback_url="http://94.191.113.169:8765/api/payment/payment/notify/RkPayNotify";//交易通知
//    public static String fund_notify_url="http://94.191.113.169:8765/api/payment/payment/notify/RkFundNotify";//代付通知
    
    @Value("${rk.pay.ds_id}")
    public static String ds_id;//渠道号
	@Value("${rk.pay.secret}")
    public static String secret;//渠道商密钥
	@Value("${rk.pay.serverRoot}")
    public static String serverRoot;//接口地址
	@Value("${rk.pay.version}")
    public static String version;//版本号
	@Value("${rk.pay.sign_type}")
    public static String sign_type;//签名类型
	@Value("${rk.pay.expire_time}")
    public static int expire_time;//付款有效期，单位分钟
	@Value("${rk.pay.mpid}")
    public static String mpid;//商户池编号
	@Value("${rk.pay.mchid}")
    public static String mchid;//商户号
    @Value("${rk.pay.notify_url}")
    public static String notify_url;//交易通知
    @Value("${rk.pay.callback_url}")
    public static String callback_url;//交易通知
    @Value("${rk.pay.fund_notify_url}")
    public static String fund_notify_url;//代付通知
}
