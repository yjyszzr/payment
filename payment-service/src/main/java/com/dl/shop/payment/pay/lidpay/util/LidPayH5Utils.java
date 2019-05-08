package com.dl.shop.payment.pay.lidpay.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.dl.shop.payment.web.PaymentController;

import lombok.Data;
@Data
@Configuration
public class LidPayH5Utils {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	/**
	 * 服务端地址
	 */
	@Value("${lid.pay.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${lid.pay.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 订单支付同步URL地址,需要修改为自己的同步回调地址，公网可以访问的,本同步回调地址只在微信H5支付中使用payUrl时起作用
	 */
	@Value("${lid.pay.returnUrl}")
	private String RETURN_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${lid.pay.merchant}")
	private String MERCHANT_NO;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${lid.pay.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${lid.pay.url.paymethod}")
	private String PAY_URL_METHOD;
	/**
	 * 订单查询URL
	 */
	@Value("${lid.pay.url.querymethod}")
	private String QUERY_URL_METHOD;
	/**
	 * 订单退款URL
	 */
	@Value("${lid.pay.url.refundmethod}")
	private String REFUND_URL_METHOD;
	/**
	 * 订单退款URL
	 */
	@Value("${lid.pay.version}")
	private String VERSION;
}
