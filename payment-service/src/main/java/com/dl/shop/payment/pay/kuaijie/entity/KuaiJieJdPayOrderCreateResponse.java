package com.dl.shop.payment.pay.kuaijie.entity;

import lombok.Data;

@Data
public class KuaiJieJdPayOrderCreateResponse {
	private KuaiJieJdPayOrderCreateResponseData data;
	private String info;
	private String status;
	@Data
	public class KuaiJieJdPayOrderCreateResponseData{
		private String trade_no;
		private String pay_url;
		private String sign;
	}
}
