package com.dl.shop.payment.pay.kuaijie.entity;

import lombok.Data;

@Data
public class KuaiJiePayOrderQueryResponse {
	private KuaiJiePayOrderQueryResponseData data;
	private String info;
	private String status;
	@Data
	public class KuaiJiePayOrderQueryResponseData{
		private String status;
		private String trade_no;
		private String amount;
		private String merchant_no;
		private String pay_channel;
		private String payment_time;
		private String sign;
	}
}
