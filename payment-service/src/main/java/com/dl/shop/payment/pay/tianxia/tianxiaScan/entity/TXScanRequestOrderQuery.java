package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanRequestOrderQuery extends TXScanRequestBaseEntity {

	/**
	 * 交易流水号 Y 交易流水号 和商户订单号2选
	 */
	private String tranSeqId;

	/**
	 * 商户订单号 交易流水号 和商户订单号2选
	 */
	private String orderId;

	/**
	 * 交易日期 N 8 如: 20170101
	 */
	private String tranDate;

}
