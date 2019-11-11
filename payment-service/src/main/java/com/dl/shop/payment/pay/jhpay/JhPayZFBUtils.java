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
	 * 支付接口
	 */
	@Value("${jh.pay.zfb.pay_url}")
	private String PAY_URL;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${jh.pay.zfb.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号 汇付圣和点
	 */
	@Value("${jh.pay.zfb.merchantno_a}")
	private String MERCHANT_NO_A;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥 汇付圣和点
	 */
	@Value("${jh.pay.zfb.secret_a}")
	private String SECRET_A;
	
	/**
	 * 商户号，正式上线需要修改为自己的商户号 汇付文水点
	 */
	@Value("${jh.pay.zfb.merchantno_b}")
	private String MERCHANT_NO_B;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥 汇付文水点
	 */
	@Value("${jh.pay.zfb.secret_b}")
	private String SECRET_B;
	/**
	 * 商户号，正式上线需要修改为自己的商户号 德式圣和点
	 */
	@Value("${jh.pay.zfb.merchantno_c}")
	private String MERCHANT_NO_C;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥 德式圣和点
	 */
	@Value("${jh.pay.zfb.secret_c}")
	private String SECRET_C;
	/**
	 * 商户号，正式上线需要修改为自己的商户号 德式文水点
	 */
	@Value("${jh.pay.zfb.merchantno_d}")
	private String MERCHANT_NO_D;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥 德式文水点
	 */
	@Value("${jh.pay.zfb.secret_d}")
	private String SECRET_D;
	
}
