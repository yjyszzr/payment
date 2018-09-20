package com.dl.shop.payment.pay.kuaijie.entity;

import lombok.Data;

@Data
public class KuaiJieQqPayOrderCreateResponse {
	private KuaiJieQqPayOrderCreateResponseData data;
	private String info;
	private String status;
	@Data
	public class KuaiJieQqPayOrderCreateResponseData{
		private String trade_no;
		private String pay_url;
		private String sign;
	}
}
