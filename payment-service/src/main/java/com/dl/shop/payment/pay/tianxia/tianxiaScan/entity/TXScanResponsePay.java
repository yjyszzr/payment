package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanResponsePay extends TXScanResponseBaseEntity {

	/**
	 * 订单状态 成功时返回 00未支付
	 */
	private String orderState;
	/**
	 * 交易日期 成功时返回
	 */
	private String tranDate;
	/**
	 * 二维码地址 成功时返回
	 */
	private String codeUrl;
	/**
	 * 交易流水号
	 */
	private String tranSeqId;
	/**
	 * 商户订单号 成功时返回。 查询订单时使用。
	 */
	private String orderId;
	/**
	 * 签名 成功时返回
	 */
	private String sign;

}
