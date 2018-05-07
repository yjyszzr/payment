package com.dl.shop.payment.pay.rongbao.cash.entity;

public class RspCashEntity {
	public String batch_no;
	public String charset;
	public String merchant_id;
	public String result_code;
	public String result_msg;
	
	public boolean isSucc() {
		return "0000".equals(result_code);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return batch_no+";"+charset+";"+merchant_id+";"+result_code+";"+result_msg+";";
	}
}
