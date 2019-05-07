package com.dl.shop.payment.pay.rkpay.util;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.dl.shop.payment.pay.apay.util.APayH5Utils;

import lombok.Data;


public class StaticV {
	
	@Resource
	private StaticConfUtil scutil;
//	
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
//    
    public static String ds_id;//渠道号
    public static String secret;//渠道商密钥
    public static String serverRoot;//接口地址
    public static String version;//版本号
    public static String sign_type;//签名类型
    public static int expire_time=30;//付款有效期，单位分钟
    public static String mpid;//商户池编号
    public static String mchid;//商户号
    public static String notify_url;//交易通知
    public static String callback_url;//交易通知
    public static String fund_notify_url;//代付通知
    
    {
    	ds_id = scutil.getDs_id();
    	secret = scutil.getSecret();
    	serverRoot = scutil.getServerRoot();
    	version = scutil.getVersion();
    	sign_type = scutil.getSign_type();
    	expire_time = scutil.getExpire_time();
    	mpid = scutil.getMpid();
    	mchid = scutil.getMchid();
    	notify_url = scutil.getNotify_url();
    	callback_url = scutil.getCallback_url();
    	fund_notify_url = scutil.getFund_notify_url();
    	
    }
}
