package com.dl.shop.payment.pay.xianfeng.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dl.shop.payment.pay.xianfeng.config.XianFengPayCfg;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

public class ReqApplyCfgEntity {
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String data;
	public String sign;
	
	public static final ReqApplyCfgEntity buildReqApplyCfgEntity(String reqSn,String data,String sign) throws CoderException {
		ReqApplyCfgEntity reqEntity = new ReqApplyCfgEntity();
		reqEntity.service = "REQ_PAY_QUICK_CONFIRM";
		reqEntity.secId = XianFengPayCfg.SEC_ID;
		reqEntity.version = XianFengPayCfg.VERSION;
		reqEntity.merchantId = XianFengPayCfg.MERCHANT_NO;
		reqEntity.data = data;
		reqEntity.sign = sign;
		reqEntity.reqSn = reqSn;
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
