package com.dl.shop.payment.pay.tianxia.tianxiaScan.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TXPayConfig {
	@Value("${tianxiaqrpay.app.debug}")
	private String DEBUG;
	// 私钥
	@Value("${tianxiaqrpay.app.prvkey}")
	private String PRVKEY;
	// //平台公钥
	@Value("${tianxiaqrpay.app.txpubkey}")
	private String TXPUBKEY;
	// md5
	@Value("${tianxiaqrpay.app.md5key}")
	private String MD5KEY;
	// 机构号
	@Value("${tianxiaqrpay.app.agtid}")
	private String AGTID;
	// 商户编号
	@Value("${tianxiaqrpay.app.merid}")
	private String MERID;
	// 请求URL
	@Value("${tianxiaqrpay.app.req_url}")
	private String REQ_URL;
	// 回调函数
	@Value("${tianxiaqrpay.app.callback_url}")
	private String CALLBACK_URL;

}
