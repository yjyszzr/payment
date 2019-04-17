package com.dl.shop.payment.pay.apay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
@Data
@Configuration
public class APayH5Utils {
	/**
	 * 服务端地址
	 */
	@Value("${a.pay.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${a.pay.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 订单支付同步URL地址,需要修改为自己的同步回调地址，公网可以访问的,本同步回调地址只在微信H5支付中使用payUrl时起作用
	 */
	@Value("${a.pay.returnUrl}")
	private String RETURN_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${a.pay.merchant}")
	private String MERCHANT_NO;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${a.pay.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${a.pay.url.paymethod}")
	private String PAY_URL_METHOD;
	/**
	 * 订单查询URL
	 */
	@Value("${a.pay.url.querymethod}")
	private String QUERY_URL_METHOD;
	/**
	 * 订单退款URL
	 */
	@Value("${a.pay.url.refundmethod}")
	private String REFUND_URL_METHOD;
}
