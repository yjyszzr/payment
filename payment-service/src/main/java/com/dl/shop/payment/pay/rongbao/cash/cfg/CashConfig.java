package com.dl.shop.payment.pay.rongbao.cash.cfg;


public class CashConfig {
	public static String privateKey = "/usr/local/cert/itrus001.pfx";// 私钥
	public static String password = "123456";// 密码
	public static String key = "g0be2385657fa355af68b74e9913a1320af82gb7ae5f580g79bffd04a402ba8f";// 用户key
	public static String merchant_id = "100000000000147";// 商户ID
	public static String pubKeyUrl = "/usr/local/cert/itrus001.cer";// 公钥
	public static String url = "http://testagentpay.reapal.com/agentpay/";
	public static String notify_url = "http://localhost:9020/agent-client/test/rpresult";// 回调地址
	public static String version = "1.0";// 版本
	public static String charset = "UTF-8";// 编码
	public static String sign_type = "MD5";// 签名方式，暂时仅支持MD5
}
