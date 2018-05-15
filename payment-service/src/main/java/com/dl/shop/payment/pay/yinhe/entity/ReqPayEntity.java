package com.dl.shop.payment.pay.yinhe.entity;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

import lombok.Data;

@Component
@Data
public class ReqPayEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Resource
	private ConfigerPay cfgPay;
	
	private String orgNo;//机构号
	private String charset;
	private String termNo;
	private String termType;
	private String txtTime;
	private String signType;
	private String transNo;
	private String merId;
	private String amt;
	private String subject;
	private String backUrl;
	private String payType;
	private String signValue;
	private String ip;
	private String inWechat;	//等于0外部浏览器打开支付  1微信内部打开支付链接
	
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
	
	public final ReqPayEntity buildReqEntity(String ip,String amount,String orderNo) {
		ReqPayEntity reqEntity = new ReqPayEntity();
		reqEntity.orgNo = cfgPay.getORG_NO();
		reqEntity.charset = cfgPay.getCHAR_SET();
		reqEntity.termNo = cfgPay.getDEVICE_NO();  //设备号
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = ConfigerPay.getPayTime(); //交易时间
		reqEntity.signType = cfgPay.getSIGN_TYPE();		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.merId = cfgPay.getMERCHANT_NO();//商户号
		reqEntity.amt = amount;			//交易金额
		reqEntity.backUrl = cfgPay.getURL_PAY_CALLBACK();	//异步回调接口
		reqEntity.payType = ConfigerPay.PAY_TYPE_WECHAT;
		reqEntity.ip = ip;
		reqEntity.inWechat = "0";
		return reqEntity;
	}
}
