package com.dl.shop.payment.pay.xianfeng.cash.entity;

public class RspSingleCashEntity {
	public String transCur;
	public String tradeNo;
	public String status;
	public String resMessage;
	public String amount;
	public String resCode;
	public String merchantId;
	public String merchantNo;
	
	public boolean isSucc() {
		return "00000".equals(resCode);
	}

	//00001已受理    00002订单处理中
	public boolean isHandleing() {
		return "00001".equals(resCode) || "00002".equals(resCode);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "transCur:" + transCur + " tradeNo:" + tradeNo + " status:" + status + " resMessage:" + resMessage
				+" amount:" + amount + " resCode:" + resCode + " merchantId:" + merchantId + " merchantNo:" + merchantNo;
		return str;
	}
}
