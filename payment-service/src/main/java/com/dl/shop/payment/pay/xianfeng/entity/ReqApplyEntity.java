package com.dl.shop.payment.pay.xianfeng.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.dl.shop.payment.pay.xianfeng.config.XianFengPayCfg;

public class ReqApplyEntity {
	
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String data;
	public String sign;
	
	public static final ReqApplyEntity buildReqApplyEntity(String reqSn,String data,String sign) {
		ReqApplyEntity reqEntity = new ReqApplyEntity();
		reqEntity.service = "REQ_PAY_QUICK_APPLY";
		reqEntity.secId = XianFengPayCfg.SEC_ID;
		reqEntity.version = XianFengPayCfg.VERSION;
		reqEntity.reqSn = reqSn;
		reqEntity.merchantId = XianFengPayCfg.MERCHANT_NO;
		reqEntity.data = data;
		reqEntity.sign = sign;
		return reqEntity;
	}
	
	
	public String buildReqStr() throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		builder.append("service="+service);
		builder.append("&");
		builder.append("secId="+secId);
		builder.append("&");
		builder.append("version="+version);
		builder.append("&");
		builder.append("reqSn="+reqSn);
		builder.append("&");
		builder.append("merchantId="+merchantId);
		builder.append("&");
		builder.append("data="+URLEncoder.encode(data,"UTF-8"));
		builder.append("&");
		builder.append("sign="+URLEncoder.encode(sign,"UTF-8"));
		return builder.toString();
	}
	
	
}
