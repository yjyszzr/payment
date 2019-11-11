package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import lombok.Data;

@Data
public class TXScanRequestBaseEntity {

	/**
	 * 机构号
	 */
	private String agtId;
	/**
	 * 商户号
	 */
	private String merId;
	/**
	 * 交易码2103
	 */
	private String tranCode;
	/**
	 * 随机数 1…32 随机数
	 */
	private String nonceStr;
	/**
	 * 签名 N 32 Body域所有字段 按照key=value的格式，并按照 参数名ASCII字典序排序， 最后加上加密key 如下：
	 */
	private String sign;
}
