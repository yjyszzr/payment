package com.dl.shop.payment.pay.rongbao.entity;

import java.io.Serializable;

public class PayResultEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	public String merchant_id;
	public String notify_id;
	public String order_no;
	public String sign;
	public String status;
	public String total_fee;
	public String trade_no;
}
