package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanResponseOrderQuery extends TXScanResponseBaseEntity {

	/**
	 * 机构号
	 */
	private String agtId;

	/**
	 * 下单时间yyyyMMddHHmmss
	 */
	private String orderTime;

	/**
	 * 订单状态
	 * 
	 * 订单状态：00-未支付 01-成功 02 失败
	 */
	private String orderState;

	/**
	 * 清算状态 NN 未处理 00 成功 02 清算中 01失败 03已入账 04入账失败
	 */
	private String settleState;
	/**
	 * 清算信息 HEX2STR
	 */
	private String settleMsg;
	/**
	 * 商户订单号 成功时返回。
	 */
	private String orderId;
	/**
	 * 交易流水号
	 */
	private String tranSeqId;
	/**
	 * 订单金额
	 */
	private String orderAmt;
	/**
	 * 签名 成功时返回
	 */
	private String sign;

}
