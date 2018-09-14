package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanRequestPaidByOthersBalanceQuery extends TXScanRequestBaseEntity {

	/**
	 * 商户订单号 1..30 订单编号
	 */
	private String orderId;
	/**
	 * 交易日期yyyymmdd
	 */
	private String tranDate;

}
