package com.dl.shop.payment.pay.yinhe.entity;

/**
 * 0000	交易成功
	500	系统内部错误
	101	机构号不存在
	102	设备号不正确
	103	签名错误
	104	交易失败
	105	商户信息有误
	106	等待支付
 */
public class RspYinHeEntity {
	public String returnCode;
	public String returnMsg;
	public String qrCode;
	public String qrcode;
	
	public boolean isSucc() {
		return "0000".equals(returnCode);
	}

	public boolean isFailure() {
		return Boolean.FALSE;
//		return "104".equals(returnCode);
	}
	
	public boolean isWaitPay() {
		return "106".equals(returnCode);
	}
	
	//支付
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "returnCode:" + returnCode + " returnMsg:" + returnMsg +" qrCode:" + qrCode;
	}
}
