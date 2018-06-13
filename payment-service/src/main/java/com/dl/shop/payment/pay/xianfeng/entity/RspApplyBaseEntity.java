package com.dl.shop.payment.pay.xianfeng.entity;


public class RspApplyBaseEntity{
	public String resCode;
	public String resMessage;
	public String merchantId;
	public String merchantNo;
	public String tradeNo;
	public String status;
	public String tradeTime;
	public String memo;

	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "resCode:" + resCode + " resMessage:" + resMessage + " merchantId:" + merchantId + 
				" merchantNo:" + merchantNo + " tradeNo:" + tradeNo + " status:" + status + " tradeTime:" + tradeTime;
	}
	
	public boolean isSucc() {
		return "00000".equals(resCode);
	}
}
