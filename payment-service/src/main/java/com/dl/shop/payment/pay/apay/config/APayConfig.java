package com.dl.shop.payment.pay.apay.config;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "pay.apay")
public class APayConfig {

	private Map<String, String> merchant = new HashMap<String, String>();

	public Map<String, String> getMerchant() {
		return merchant;
	}

	public void setMerchant(Map<String, String> merchant) {
		this.merchant = merchant;
	}

	public String getDEBUG(String merchentNo) {
		return merchant.get(merchentNo + "debug");
	}

	public String getPRVKEY(String merchentNo) {
		return merchant.get(merchentNo + "prvkey");
	}

	public String getTXPUBKEY(String merchentNo) {
		return merchant.get(merchentNo + "txpubkey");
	}

	public String getMD5KEY(String merchentNo) {
		return merchant.get(merchentNo + "md5key");
	}

	public String getAGTID(String merchentNo) {
		return merchant.get(merchentNo + "agtid");
	}

	public String getMERID(String merchentNo) {
		return merchant.get(merchentNo + "merid");
	}

	public String getREQ_URL(String merchentNo) {
		return merchant.get(merchentNo + "req_url");
	}

	public String getCALLBACK_URL(String merchentNo) {
		return merchant.get(merchentNo + "callback_url");
	}

}
