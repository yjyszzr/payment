package com.dl.shop.payment.pay.yinhe.entity;

import org.hamcrest.text.IsEqualIgnoringWhiteSpace;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

public class ReqRefundOrderEntity {
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
	public String payType;
	public String inWechat = "0";
	
	public static ReqRefundOrderEntity buildReqQueryEntity(boolean isInWeChat,String orderNo,String amt){
		ReqRefundOrderEntity reqEntity = new ReqRefundOrderEntity();
		reqEntity.orgNo = ConfigerPay.ORG_NO;
		reqEntity.charset = ConfigerPay.CHAR_SET;
		reqEntity.termNo = ConfigerPay.DEVICE_NO;  //设备号 XA026454
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = ConfigerPay.getPayTime(); //交易时间，暂时写死
		reqEntity.signType = ConfigerPay.SIGN_TYPE;		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.amt = amt;
		reqEntity.merId = ConfigerPay.MERCHANT_NO;
		reqEntity.payType = ConfigerPay.PAY_TYPE_WECHAT;
		if(isInWeChat) {
			reqEntity.inWechat = "1";
		}else {
			reqEntity.inWechat = "0";
		}
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
