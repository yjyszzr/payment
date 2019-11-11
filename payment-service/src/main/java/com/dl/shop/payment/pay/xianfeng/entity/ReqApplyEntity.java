package com.dl.shop.payment.pay.xianfeng.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;


public class ReqApplyEntity {
	
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String data;
	public String sign;
	
	public static final ReqApplyEntity buildReqApplyEntity(String reqSn,String data,String sign,Constants xfConstants) {
		ReqApplyEntity reqEntity = new ReqApplyEntity();
		reqEntity.service = "REQ_PAY_QUICK_APPLY";
		reqEntity.secId = xfConstants.getSEC_ID();
		reqEntity.version = xfConstants.getVERSION();
		reqEntity.reqSn = reqSn;
		reqEntity.merchantId = xfConstants.getMER_ID();
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
