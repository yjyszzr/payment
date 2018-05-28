package com.dl.shop.payment.pay.xianfeng.cash.entity;

public class RspSingleQueryEntity {
	public String merchantId;
	public String merchantNo;
	public String amount;
	public String transCur;
	public String tradeNo;
	public String tradeTime;
	public String status;
	public String memo;
	public String resCode;
	public String resMessage;

	public boolean isSucc() {
		return "00000".equals(resCode);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "merchantId:" + merchantId + " merchantNo:" + merchantNo + " amount:" + amount
				+ " transCur:" + transCur + " tradeNo:" + tradeNo + " tradeTime:" + tradeTime
				+ " status:" + status + " resCode:" + resCode + " resMessage:" + resMessage;
	}
}
