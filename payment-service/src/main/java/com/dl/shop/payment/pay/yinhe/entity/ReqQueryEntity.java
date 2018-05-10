package com.dl.shop.payment.pay.yinhe.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

public class ReqQueryEntity {
	public String orgNo;//机构号
	public String charset;
	public String termNo;
	public String termType;
	public String txtTime;
	public String signType;
	public String signValue;
	public String transNo;
	public String payType;
	public String amt;
	public String merId;
	
	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "2";
	
	
	public static ReqQueryEntity buildReqQueryEntity(String orderNo) {
		ReqQueryEntity reqEntity = new ReqQueryEntity();
		reqEntity.orgNo = "2188";
		reqEntity.charset = "UTF-8";
		reqEntity.termNo = ConfigerPay.DEVICE_NO;  //设备号 XA026454
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = getPayTime(); //交易时间，暂时写死
		reqEntity.signType = "MD5";		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.payType = PAY_TYPE_WECHAT;
		reqEntity.amt = "";
		reqEntity.merId = ConfigerPay.MERCHANT_NO;
		return reqEntity;
	}
	
	public ReqSignEntity buildSignEntity() {
		ReqSignEntity sEntity = new ReqSignEntity();
		sEntity.orgNo = orgNo;
		sEntity.termNo = termNo;
		sEntity.transNo = transNo;
		sEntity.txtTime = txtTime;
		sEntity.merId = merId;
		sEntity.amt = "";
		return sEntity;
	}
	
	public static String getPayTime() {
		SimpleDateFormat sdfTime = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String str = sdfTime.format(date);
		return str;
	}
	
}