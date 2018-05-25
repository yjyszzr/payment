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
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "transCur:" + transCur + " tradeNo:" + tradeNo + " status:" + status + " resMessage:" + resMessage
				+" amount:" + amount + " resCode:" + resCode + " merchantId:" + merchantId + " merchantNo:" + merchantNo;
		return str;
	}
}
