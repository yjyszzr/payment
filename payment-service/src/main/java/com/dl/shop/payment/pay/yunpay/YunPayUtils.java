package com.dl.shop.payment.pay.yunpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
@Data
@Configuration
public class YunPayUtils {
	/**
	 * 服务端地址
	 */
	@Value("${yun.pay.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${yun.pay.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${yun.pay.merchant}")
	private String MERCHANT_NO;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${yun.pay.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${yun.pay.url.paymethod}")
	private String PAY_URL_METHOD;
	/**
	 * 订单查询URL
	 */
	@Value("${yun.pay.url.querymethod}")
	private String QUERY_URL_METHOD;
}
