package com.dl.shop.payment.pay.jhpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
@Data
@Configuration
public class JhPayWXUtils {
	/**
	 * 服务端地址
	 */
	@Value("${jh.pay.wx.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${jh.pay.wx.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${jh.pay.wx.merchantno}")
	private String MERCHANT_NO;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${jh.pay.wx.merchantname}")
	private String MERCHANT_NAME;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${jh.pay.wx.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${jh.pay.wx.url.paymethod}")
	private String PAY_URL_METHOD;
}
