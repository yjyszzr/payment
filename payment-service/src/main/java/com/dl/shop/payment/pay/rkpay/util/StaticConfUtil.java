package com.dl.shop.payment.pay.rkpay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class StaticConfUtil {
	@Value("${rk.pay.ds_id}")
    public String ds_id;//渠道号
	@Value("${rk.pay.secret}")
    public String secret;//渠道商密钥
	@Value("${rk.pay.serverRoot}")
    public String serverRoot;//接口地址
	@Value("${rk.pay.version}")
    public String version;//版本号
	@Value("${rk.pay.sign_type}")
    public String sign_type;//签名类型
	@Value("${rk.pay.expire_time}")
    public int expire_time;//付款有效期，单位分钟
	@Value("${rk.pay.mpid}")
    public String mpid;//商户池编号
	@Value("${rk.pay.mchid}")
    public String mchid;//商户号
    @Value("${rk.pay.notify_url}")
    public String notify_url;//交易通知
    @Value("${rk.pay.callback_url}")
    public String callback_url;//交易通知
    @Value("${rk.pay.fund_notify_url}")
    public String fund_notify_url;//代付通知
}
