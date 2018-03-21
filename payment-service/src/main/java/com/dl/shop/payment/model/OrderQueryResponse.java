package com.dl.shop.payment.model;

import lombok.Data;

@Data
public class OrderQueryResponse {

	private Integer tradeState;
	private String tradeStateDesc;
	private String tradeNo;
	private Integer tradeEndTime;
}
