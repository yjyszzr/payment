package com.dl.shop.payment.pay.yinhe.entity;

import org.apache.http.util.TextUtils;

public class RspNotifyWeChatEntity {
	public String code;
	public String transNo;
	public String amt;
	public String transTime;
	
	public boolean isSucc() {
		return "0000".equals(code) && !TextUtils.isEmpty(transNo);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "code:" + code +" transNo:" + transNo +" amt:" + amt +" transTime:" + transTime;
	}
}
 