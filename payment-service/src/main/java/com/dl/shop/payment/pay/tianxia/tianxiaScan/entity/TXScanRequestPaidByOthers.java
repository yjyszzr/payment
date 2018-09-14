package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TXScanRequestPaidByOthers extends TXScanRequestBaseEntity {

	/**
	 * 商户订单号1..30 订单编号
	 */
	private String orderId;

	/**
	 * 交易日期 yyyymmdd
	 */
	private String tranDate;

	/**
	 * 订单金额 1…12 单位分
	 */
	private String txnAmt;

	/**
	 * 账户
	 */
	private String accountNo;

	/**
	 * 证件号码 可不关联,但必须过校验
	 */
	private String certNum;

	/**
	 * 银行编码
	 */
	private String bankCode;

	/**
	 * 银行名称 N 需转换成16进制字符串传输
	 */
	private String bankName;

	/**
	 * 账户名 N 需转换成16进制字符串传输
	 */
	private String accountName;

	/**
	 * 开户省 Y 省 如: 110000
	 */
	private String bankProv;

	/**
	 * 开户市 市 如: 110100
	 */
	private String bankCity;

	/**
	 * 联行号
	 */
	private String cnaps;

	/**
	 * 开户支行 Y 需转换成16进制字符串传输
	 */
	private String bankBranch;

	/**
	 * 手机号 N 可不关联,但必须过校验
	 */
	private String mobile;

	/**
	 * 账户类型 N 结算账户类型 (暂时只支持对私) 0对公 1对私
	 */
	private String accountType;
	/**
	 * 结算类型 T0 结算至已入账账户 T1 结算至未结算账户 (不传默认T1)
	 */
	private String stlType;

}
