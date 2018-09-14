package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanResponsePaidByOthers extends TXScanResponseBaseEntity {

	/**
	 * 交易结果 成功时返回
	 * 
	 * 0000交易受理 T010交易失败，退回余额 T011 清算结果未知 T101 无此订单
	 */
	private String subcode;
	/**
	 * 交易信息 成功时返回，HEX2STR
	 */
	private String submsg;
	/**
	 * 商户订单编号 成功时返回
	 */
	private String orderId;
	/**
	 * 交易流水号 成功时返回，交易流水号
	 */
	private String tranId;
}
