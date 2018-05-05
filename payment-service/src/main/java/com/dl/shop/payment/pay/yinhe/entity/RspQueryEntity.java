package com.dl.shop.payment.pay.yinhe.entity;

import lombok.Data;

@Data
public class RspQueryEntity {
	private String returnCode;
	private String returnMsg;
	private String txtAmt;
	private String channelOrderNum;
	private String transTime;
	private String sign;
	private String payStyle;
	private String orderNum;
	
	private boolean isSucc() {
		return "0000".equals(returnCode);
	}
}
