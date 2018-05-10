package com.dl.shop.payment.pay.yinhe.entity;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

/**
 * 银河二维码方式支付
 * @date 2018.05.10
 */
public class ReqQRPayEntity {
	public String orgNo;
	public String charset;
	public String termNo;
	public String termType;
	public String txtTime;
	public String signType;
	public String signValue;
	public String transNo;
	public String merId;
	public String amt;
	public String subject;
	public String backUrl;
	public String payType;
	
	public static final ReqQRPayEntity buildReqEntity(String amount,String orderNo) {
		ReqQRPayEntity reqEntity = new ReqQRPayEntity();
		reqEntity.orgNo = "2188";
		reqEntity.charset = "UTF-8";
		reqEntity.termNo = ConfigerPay.DEVICE_NO;  //设备号
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = ConfigerPay.getPayTime(); //交易时间
		reqEntity.signType = "MD5";		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.merId = ConfigerPay.MERCHANT_NO;//商户号
		reqEntity.amt = amount;			//交易金额
		reqEntity.backUrl = ConfigerPay.URL_PAY_CALLBACK;	//异步回调接口
		reqEntity.payType = ConfigerPay.PAY_TYPE_WECHAT;
		return reqEntity;
	}
	
	public ReqSignEntity buildSignEntity() {
		ReqSignEntity sEntity = new ReqSignEntity();
		sEntity.orgNo = orgNo;
		sEntity.amt = amt;
		sEntity.termNo = termNo;
		sEntity.merId = merId;
		sEntity.transNo = transNo;
		sEntity.txtTime = txtTime;
		return sEntity;
	}
}
