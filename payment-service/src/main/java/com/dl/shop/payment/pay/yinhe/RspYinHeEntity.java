package com.dl.shop.payment.pay.yinhe;

public class RspYinHeEntity {
	public String returnCode;
	public String returnMsg;
	public String qrCode;
	
	public boolean isSucc() {
		return "0000".equals(returnCode);
	}
}
