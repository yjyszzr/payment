package com.dl.shop.payment.pay.rongbao.entity;

import lombok.Data;

@Data
public class ReqRefundEntity {
	private String orig_order_no;
	private String amount;
	private String note;

	
	@Override
	public String toString(){
		return "orig_order_no:" + orig_order_no +" amount:" + amount;
	}
}
