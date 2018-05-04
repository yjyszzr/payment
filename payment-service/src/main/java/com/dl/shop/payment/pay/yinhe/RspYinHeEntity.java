package com.dl.shop.payment.pay.yinhe;

public class RspYinHeEntity {
	public String returnCode;
	public String returnMsg;
	public String qrCode;
	
	public boolean isSucc() {
		return "0000".equals(returnCode);
	}

	//支付
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "returnCode:" + returnCode + " returnMsg:" + returnMsg +" qrCode:" + qrCode;
	}
}
