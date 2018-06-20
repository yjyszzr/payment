package com.dl.shop.payment.pay.rongbao.entity;

import lombok.Data;

@Data
public class RspRefundEntity {
	//融宝
	private String result_code;
	private String result_msg;
	
	//银河字段
	private String returnCode;
	private String returnMsg;
	
	public boolean isSucc() {
		return "0000".equals(result_code) || "0000".equals(returnCode);
	}

	public String toString() {
		return "result_code:" + result_code +" result_msg:" +result_msg +" returnCode:" + returnCode + " returnMsg:" +returnMsg;
	}
}
