package com.dl.shop.payment.configurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class WxpayConfig {

	@Value("${wxpay.app.app_id}")
	private String wxAppAppId;
	@Value("${wxpay.app.app_secret}")
	private String wxAppAppSecret;
	@Value("${wxpay.app.mch_id}")
	private String wxAppMchId;
	@Value("${wxpay.app.app_key}")
	private String wxAppAppKey;
	@Value("${wxpay.js.app_id}")
	private String wxJsAppId;
	@Value("${wxpay.js.app_secret}")
	private String wxJsAppSecret;
	@Value("${wxpay.js.mch_id}")
	private String wxJsMchId;
	@Value("${wxpay.js.app_key}")
	private String wxJsAppKey;
	@Value("${wxpay.unified_order_url}")
	private String wxUnifiedOrderUrl;
	@Value("${wxpay.refund_url}")
	private String wxRefundUrl;
	@Value("${wxpay.notify_url}")
	private String wxNotifyUrl;
}
