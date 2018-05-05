package com.dl.shop.payment.pay.rongbao.entity;

import lombok.Data;

@Data
public class RspRefundEntity {
	private String result_code;
	private String result_msg;
	
	public boolean isSucc() {
		return "0000".equals(result_code);
	}

	public String toString() {
		return "result_code:" + result_code +" result_msg:" +result_msg;
	}
}
