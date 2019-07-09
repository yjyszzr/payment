package com.dl.shop.payment.pay.rkpay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class StaticV {
	
//    public  String ds_id="DS1904231609145999";//渠道号
//    public  String secret="44d8a9ec12b47e59a13d490e2a42c7a3";//渠道商密钥
//    public  String serverRoot="https://openapi.haodiana.cn";//接口地址
//    public  String version="1.0";//版本号
//    public  String sign_type="MD5";//签名类型
//    public  int expire_time=30;//付款有效期，单位分钟
//    public  String mpid="MP1907091634078161";//商户池编号MP1904241125194799
//    public  String mchid="MC1905051517214236";//商户号
//    public  String notify_url="http://94.191.113.169:8765/api/payment/payment/notify/RkPayNotify";//交易通知
//    public  String callback_url="http://94.191.113.169:8765/api/payment/payment/notify/RkPayNotify";//交易通知
//    public  String fund_notify_url="http://94.191.113.169:8765/api/payment/payment/notify/RkFundNotify";//代付通知

	@Value("${rk.pay.ds_id}")
    public String ds_id;//渠道号
	@Value("${rk.pay.secret}")
    public String secret;//渠道商密钥
	@Value("${rk.pay.serverRoot}")
    public String serverRoot;//接口地址
	@Value("${rk.pay.version}")
    public String version;//版本号
	@Value("${rk.pay.sign_type}")
    public String sign_type;//签名类型
	@Value("${rk.pay.expire_time}")
    public int expire_time;//付款有效期，单位分钟
	@Value("${rk.pay.mpid}")
    public String mpid;//商户池编号
	@Value("${rk.pay.mchid}")
    public String mchid;//商户号
    @Value("${rk.pay.notify_url}")
    public String notify_url;//交易通知
    @Value("${rk.pay.callback_url}")
    public String callback_url;//交易通知
    @Value("${rk.pay.fund_notify_url}")
    public String fund_notify_url;//代付通知
    
}
