package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanResponsePaidByOthersBalanceQuery extends TXScanResponseBaseEntity {
	/**
	 * 响应码 000000成功,其他则失败
	 */
	private String rspcode;

	/**
	 * 响应信息
	 */
	private String rspmsg;

	/**
	 * 交易结果 成功时返回 0000交易成功 T000未处理 T006清算中 T010交易失败 T011交易结果未知
	 */
	private String subcode;

	/**
	 * 交易信息 N 成功时返回，urlDecode
	 */
	private String submsg;

	/**
	 * 商户订单编号 N 成功时返回
	 */
	private String orderId;

	/**
	 * 交易流水号 成功时返回，交易流水号
	 */
	private String tranId;

}
