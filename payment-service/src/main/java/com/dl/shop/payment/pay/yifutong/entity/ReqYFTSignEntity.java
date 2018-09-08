package com.dl.shop.payment.pay.yifutong.entity;

public class ReqYFTSignEntity {
	public String mchNo;//商户号
	public String notifyUrl;//通知地址
	public String orderCode;//商户订单号
	public String price;//金额
	public String ts;//时间戳long
	public String type;//支付类型1=支付宝
	public String token;//支付类型1=支付宝
}
