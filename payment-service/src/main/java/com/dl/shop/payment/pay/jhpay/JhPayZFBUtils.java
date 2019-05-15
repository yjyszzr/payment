package com.dl.shop.payment.pay.jhpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
@Data
@Configuration
public class JhPayZFBUtils {
	/**
	 * 服务端地址
	 */
	@Value("${jh.pay.zfb.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${jh.pay.zfb.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${jh.pay.zfb.merchantno}")
	private String MERCHANT_NO;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${jh.pay.zfb.merchantname}")
	private String MERCHANT_NAME;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${jh.pay.zfb.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${jh.pay.zfb.url.paymethod}")
	private String PAY_URL_METHOD;
	/**
	 * 支付生成URL
	 */
	@Value("${jh.pay.zfb.adduid}")
	private String ADDUID;
}
