package com.dl.shop.payment.pay.tianxia.tianxiaScan.config;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
// @ConfigurationProperties(prefix = "pay.tianxia")
public class TXPayConfig {
	@Value("${pay.tianxia}")
	private Map<String, String> merchant = new HashMap<String, String>();

	private String DEBUG;
	// 私钥
	private String PRVKEY;
	// 平台公钥
	private String TXPUBKEY;
	// md5
	private String MD5KEY;
	// 机构号
	private String AGTID;
	// 商户编号
	private String MERID;
	// 请求URL
	private String REQ_URL;
	// 回调函数
	private String CALLBACK_URL;

	public void setMerchant(Map<String, String> merchant) {
		this.merchant = merchant;
	}

	public String getDEBUG(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "debug");
	}

	public String getPRVKEY(String merchentNo) {
		return merchant.get(merchentNo + "prvkey");
	}

	public String getTXPUBKEY(String merchentNo) {
		return merchant.get(merchentNo + "txpubkey");
	}

	public String getMD5KEY(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "md5key");
	}

	public String getAGTID(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "agtid");
	}

	public String getMERID(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "merid");
	}

	public String getREQ_URL(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "req_url");
	}

	public String getCALLBACK_URL(String merchentNo) {
		log.info("天下支付配置信息打印:={}", merchant);
		return merchant.get(merchentNo + "callback_url");
	}

}
