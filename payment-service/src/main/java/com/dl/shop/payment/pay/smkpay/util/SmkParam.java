package com.dl.shop.payment.pay.smkpay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
/**
 * 接口属性封装
 * @author DELL
 *
 */
@Data
@Configuration
public class SmkParam {
	/**
	 * 接口地址
	 */
	@Value("${smk.pay.requestUrl}")
	private String requestUrl;
	/**
	 * 商户号
	 */
	@Value("${smk.pay.merCode}")
	private String merCode;
	/**
	 * 应用ID
	 */
	@Value("${smk.pay.appId}")
	private String appId;
	/**
	 * 公钥
	 */
	@Value("${smk.pay.vertifyPublicKey}")
	private String vertifyPublicKey;
	/**
	 * 私钥
	 */
	@Value("${smk.pay.signPrivateKey}")
	private String signPrivateKey;
	/**
	 * 证书路劲
	 */
	@Value("${smk.pay.certPath}")
	private String certPath;
	/**
	 * 证书密钥
	 */
	@Value("${smk.pay.certPwd}")
	private String certPwd;
	/**
	 *
	 */
	@Value("${smk.pay.asynVertifySignCertPath}")
	private String asynVertifySignCertPath;
	/**
	 * 回调接口
	 */
	@Value("${smk.pay.notifyUrl}")
	private String notifyUrl;
}
