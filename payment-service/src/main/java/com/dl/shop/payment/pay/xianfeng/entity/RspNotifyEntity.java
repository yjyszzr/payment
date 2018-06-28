package com.dl.shop.payment.pay.xianfeng.entity;

/**
 * 先锋回调通知实体类
 */
public class RspNotifyEntity {
	public String sign;
	public String amount;
	public String transCur;
	public String memo;
	public String tradeNo;	//订单号
	public String status;
	public String tradeTime;
	public String resCode;
	public String resMessage;
	public String merchantId;
	public String merchantNo;
	
	public boolean isSucc() {
		return "00000".equals(resCode);
	}
}
