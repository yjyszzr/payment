package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;

@Data
public class TXScanRequestCallback {

	private TXSign REP_HEAD;

	private TXCallback REP_BODY;

	@Data
	public class TXCallback {

		/**
		 * 订单状态 N 2 成功时返回 订单状态：00-未支付 01-成功 02-失败
		 */
		private String orderState;

		/**
		 * 交易流水号
		 */
		private String tranSeqId;

		/**
		 * 商户订单号 成功时返回。 查询订单时使用。
		 */
		private String orderId;

		/**
		 * 支付完成时间yyyyMMddHHmmss
		 */
		private String payTime;

		/**
		 * 订单金额 单位分 元订单金额
		 */
		private String orderAmt;
	}

	@Data
	public class TXSign {
		/**
		 * 签名 成功时返回
		 */
		private String sign;
	}
}
