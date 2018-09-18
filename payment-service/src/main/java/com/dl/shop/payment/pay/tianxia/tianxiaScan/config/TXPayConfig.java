package com.dl.shop.payment.pay.tianxia.tianxiaScan.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TXPayConfig {
	@Value("${pay_tianxia_merchent}")
	private Map<String, String> merchents;

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

	public void setMerchents(Map<String, String> merchents) {
		this.merchents = merchents;
	}

	public String getDEBUG(String merchentNo) {
		return merchents.get(merchentNo + "debug");
	}

	public String getPRVKEY(String merchentNo) {
		return merchents.get(merchentNo + "prvkey");
	}

	public String getTXPUBKEY(String merchentNo) {
		return merchents.get(merchentNo + "txpubkey");
	}

	public String getMD5KEY(String merchentNo) {
		return merchents.get(merchentNo + "md5key");
	}

	public String getAGTID(String merchentNo) {
		return merchents.get(merchentNo + "agtid");
	}

	public String getMERID(String merchentNo) {
		return merchents.get(merchentNo + "merid");
	}

	public String getREQ_URL(String merchentNo) {
		return merchents.get(merchentNo + "req_url");
	}

	public String getCALLBACK_URL(String merchentNo) {
		return merchents.get(merchentNo + "callback_url");
	}

}
