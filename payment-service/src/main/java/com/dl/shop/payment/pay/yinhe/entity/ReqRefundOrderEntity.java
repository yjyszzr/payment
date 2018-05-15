package com.dl.shop.payment.pay.yinhe.entity;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

import lombok.Data;

@Component
@Data
public class ReqRefundOrderEntity {
	
	@Resource
	private ConfigerPay cfgPay;
	
	private String orgNo;
	private String charset;
	private String termNo;
	private String termType;
	private String txtTime;
	private String signType;
	private String signValue;
	private String transNo;
	private String merId;
	private String amt;
	private String payType;
	private String inWechat = "0";
	
	public ReqRefundOrderEntity buildReqQueryEntity(boolean isInWeChat,String orderNo,String amt){
		ReqRefundOrderEntity reqEntity = new ReqRefundOrderEntity();
		reqEntity.orgNo = cfgPay.getORG_NO();
		reqEntity.charset = cfgPay.getCHAR_SET();
		reqEntity.termNo = cfgPay.getDEVICE_NO();  //设备号 XA026454
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = ConfigerPay.getPayTime(); //交易时间，暂时写死
		reqEntity.signType = cfgPay.getSIGN_TYPE();		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.amt = amt;
		reqEntity.merId = cfgPay.getMERCHANT_NO();
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
