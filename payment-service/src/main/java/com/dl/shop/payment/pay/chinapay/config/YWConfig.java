package com.dl.shop.payment.pay.chinapay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
public class YWConfig {
	
	@Value("${chinapay.frontRequestUrl}")
	private String frontRequestUrl;
	
	@Value("${chinapay.queryRequestUrl}")
	private String queryRequestUrl;
	
	@Value("${chinapay.merId}")
	private String merId;

}
