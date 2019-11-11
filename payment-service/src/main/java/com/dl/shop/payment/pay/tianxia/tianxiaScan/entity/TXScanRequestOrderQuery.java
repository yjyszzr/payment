package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanRequestOrderQuery extends TXScanRequestBaseEntity {

	/**
	 * 交易流水号;
	 * 
	 * 交易流水号和商户订单号2选1
	 */
	private String tranSeqId;

	/**
	 * 商户订单号;
	 * 
	 * 交易流水号和商户订单号2选1
	 */
	private String orderId;

	/**
	 * 交易日期yyyymmdd 如: 20170101
	 */
	private String tranDate;

}
