package com.dl.shop.payment.pay.yinhe.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;

import lombok.Data;

@Component
@Data
public class ReqQueryEntity {
	
	@Resource
	private ConfigerPay cfgPay;
	
	private String orgNo;//机构号
	private String charset;
	private String termNo;
	private String termType;
	private String txtTime;
	private String signType;
	private String signValue;
	private String transNo;
	private String payType;
	private String amt;
	private String merId;
	private String inWechat;	//等于0外部浏览器打开支付  1微信内部打开支付链接
	
	public static final String PAY_TYPE_WECHAT = "1";
	public static final String PAY_TYPE_ZHIFUBAO = "2";
	
	
	public ReqQueryEntity buildReqQueryEntity(boolean isZfb,boolean isWechat,String orderNo) {
		ReqQueryEntity reqEntity = new ReqQueryEntity();
		reqEntity.orgNo = cfgPay.getORG_NO();
		reqEntity.charset = cfgPay.getCHAR_SET();
		reqEntity.termNo = cfgPay.getDEVICE_NO();  //设备号 XA026454
		reqEntity.termType = "RQ";		//终端类型
		reqEntity.txtTime = getPayTime(); //交易时间，暂时写死
		reqEntity.signType = cfgPay.getSIGN_TYPE();		//签名信息
		reqEntity.transNo = orderNo;	//交易号 唯一
		reqEntity.payType = isZfb?PAY_TYPE_ZHIFUBAO:PAY_TYPE_WECHAT;
		reqEntity.amt = "";
		reqEntity.merId = cfgPay.getMERCHANT_NO();
		if(isZfb||isWechat) {
			reqEntity.inWechat = "1";
		}else {
			reqEntity.inWechat = "0";
		}
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
