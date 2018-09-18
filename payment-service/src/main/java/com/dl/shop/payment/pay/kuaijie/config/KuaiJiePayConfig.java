package com.dl.shop.payment.pay.kuaijie.config;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

@Data
public class KuaiJiePayConfig {
	@Value("${kauijie.pay.url}")
	private String apiUrl;
	@Value("${kauijie.pay.secret}")
	private String secret;
	private String qqWapPayUrl="qqpay/wap_pay";
	private String qqQueryPayUrl="qqpay/query_pay";
	private String jdPayUrl="jdpay/wap_pay";
	private String jdQueryPayUrl="jdpay/query_pay";
	/**
	 * 测试方法
	 * @return
	 */
    private static KuaiJiePayConfig testKuaiJiePayConfig(){
    	KuaiJiePayConfig config = new KuaiJiePayConfig();
    	config.apiUrl="http://api.kj-pay.com/";
    	config.secret="5a7081b84030d3f0e468c3425a60116b";
    	return config;
    }
}
