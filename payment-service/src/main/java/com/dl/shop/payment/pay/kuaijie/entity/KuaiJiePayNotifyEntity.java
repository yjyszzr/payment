package com.dl.shop.payment.pay.kuaijie.entity;

import lombok.Data;

@Data
public class KuaiJiePayNotifyEntity {
	private String status;
	private String msg;
	private String amount;
	private String merchant_order_no;
	private String trade_no;
	private String paymenty_time;
	private String pay_channel;
	private String pay_channel_name;
	private String attach;
	private String sign;
}

