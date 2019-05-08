package com.dl.shop.payment.pay.kuaijie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class KuaiJiePayConfig {
	@Value("${kauijie.pay.url}")
	private String apiUrl;
	@Value("${kauijie.pay.merchant}")
	private String merchant;
	@Value("${kauijie.pay.secret}")
	private String secret;
	@Value("${kauijie.pay.notifyUrl}")
	private String notifyUrl;
	@Value("${kauijie.pay.goodDesc}")
	private String goodDesc;
	@Value("${kauijie.pay.debug}")
	private String DEBUG;
	
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
    	config.merchant="2018575718";
    	return config;
    }
}
