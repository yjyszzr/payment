package com.dl.shop.payment.pay.yifutong.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 0 	交易成功
	1023 签名错误
	1	交易失败
 */
public class RspQueryYFTEntity {
	public String code;
	public String msg;
	public String success;
	public QueryYFTData data;
	
	public boolean isSucc() {
		return "0".equals(code);
	}

	public class QueryYFTData{
		public String account; //设备登录帐号
		public String expireTime;//支付过期时间
		public String extraCost;//手续费
		public String mchNo;//商户号
		public String notifyUrl;//支付成功回调地址
		public String orderCode;
		public String paidTime;//付款时间
		public String payUrl;
		public String price;
		public String realPrice;
		public String remarks;
		public String tradeNo;//支付宝交易订单号
		public String requestContent;//请求时的参数
		public String requestTime;
		public String scanTime;//用户打开支付链接的时间
		public String succPage;
		public String status;//0=失败；1=等待付款；2=交易成功；3=通知失败；注：status=2或3都是用户支付成功，3说明回调通知没有返回success
		public String type;
		public String sign;
	}
}
