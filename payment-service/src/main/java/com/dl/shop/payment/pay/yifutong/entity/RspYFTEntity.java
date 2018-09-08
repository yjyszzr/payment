package com.dl.shop.payment.pay.yifutong.entity;

/**
 * 0 	交易成功
	1023 签名错误
	1	交易失败
 */
public class RspYFTEntity {
	public String code;
	public String msg;
	public String success;
	public ResultYFTData data;
	
	public boolean isSucc() {
		return "0".equals(code);
	}

	public boolean isFailure() {
		return Boolean.FALSE;
//		return "104".equals(returnCode);
	}
	
	public boolean isWaitPay() {
		return "106".equals(code);
	}
	
	public class ResultYFTData{
		public String account;
		public String mchNo;
		public String orderCode;
		public String payUrl;
		public String price;
		public String realPrice;
		public String sign;
	}
}
