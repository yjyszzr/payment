package com.dl.shop.payment.pay.yifutong.entity;

import lombok.Data;

@Data
public class RespYFTnotifyEntity {
	private String mchNo;
	private String orderCode;
	private String price;
	private String realPrice;
	private String tradeNo;
	private String remarks;
	private String ts;
	private String sign;
}
