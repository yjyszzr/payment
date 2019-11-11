package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanResponseBalanceQuery extends TXScanResponseBaseEntity {

	/**
	 * 账户总余额
	 */
	private String acBal;
	/**
	 * T0账户
	 */
	private String acT0;
	/**
	 * T1账户
	 */
	private String acT1;
}
