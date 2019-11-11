package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanRequestPay extends TXScanRequestBaseEntity {

	/**
	 * 订单总金额 N 订单总金额 长度不大于12位 单位分
	 */
	private String orderAmt;

	/**
	 * 商户订单号 1..30 商户订单号不可重复, 最大长度 30位。
	 */
	private String orderId;

	/**
	 * 商品简单描述
	 * 
	 * 需转换成16进制字符串传输
	 */
	private String goodsName;

	/**
	 * URL地址1…100 支付成功后服务器回调该地址
	 */
	private String notifyUrl;

	/**
	 * 商品详情1…2000 产品名称1|单价1|数量 1#产品 名称2|单价2|数量2 例如： 可乐|300|1#雪碧|300|1
	 * 转换成16进制字符串后： E58FAFE4B9907C3330307C 3123E99BAAE7A2A77C3330 307C31
	 */
	private String goodsDetail;

	/**
	 * 结算类型:T0 结算至已入账账户 T1 结算至未结算账户
	 */
	private String stlType;

	/**
	 * 支付渠道: WXPAY 微信支付 ALIPAY 支付宝 ALIWAPPAY 支付宝 H5 QQPAY qq钱包 WXWAPPAY 微信H5
	 * CPPAY 银联二维码 CPPAYLARGE 银联二维码大额 JDPAY 京东支付 JDWAPPAY 京东wap支付
	 */
	private String payChannel;
	/**
	 * 交易ip N 填写正确客户交易ip，否则无法 支付
	 */
	private String termIp;

}
