package com.dl.shop.payment.pay.yifutong.entity;

import java.io.Serializable;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.yifutong.config.ConfigerYFTPay;

import lombok.Data;

@Component
@Data
public class ReqYFTPayEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Resource
	private ConfigerYFTPay cfgPay;

	private String price;//金额
	private String mchNo;//商户号
	private String type;//支付类型1=支付宝
	private String remarks;//交易备注
	private String account;//设备号
	private String orderCode;//商户订单号
	private String ts;//时间戳long 单位毫秒
	private String notifyUrl;//通知地址
	private String succPage;//交易成功跳转
	private String token;//交易成功跳转
	private String sign;//签名

	public ReqYFTSignEntity buildSignEntity() {
		ReqYFTSignEntity sEntity = new ReqYFTSignEntity();
		sEntity.mchNo = mchNo;
		sEntity.notifyUrl = notifyUrl;
		sEntity.orderCode = orderCode;
		sEntity.price = price;
		sEntity.ts = ts;
		sEntity.type = type;
		sEntity.token = token;
		return sEntity;
	}

	public final ReqYFTPayEntity buildReqEntity(String amount, String orderNo) {
		ReqYFTPayEntity reqEntity = new ReqYFTPayEntity();
		reqEntity.succPage = "https://www.baidu.com";
		reqEntity.ts = ConfigerYFTPay.getPayTime(); 
		reqEntity.orderCode = "1234368"; 
		reqEntity.mchNo = "1536317691tXgyQz";
		reqEntity.price = "10.00"; 
		reqEntity.notifyUrl = "http://39.106.18.39:7076/cash/notify"; 
		reqEntity.token = "afef76a5aed6ba63a0c010c40e104cd4d156cd6e"; 
		reqEntity.type = "1";
//		reqEntity.remarks = "";
//		reqEntity.succPage = cfgPay.getAPP_SUCCPAGE();
//		reqEntity.account = cfgPay.getAPP_ACCOUNT();
//		reqEntity.ts = ConfigerYFTPay.getPayTime(); 
//		reqEntity.orderCode = orderNo; 
//		reqEntity.mchNo = cfgPay.getAPP_MCHNO();
//		reqEntity.price = amount; 
//		reqEntity.notifyUrl = cfgPay.getAPP_NOTIFYURL(); 
//		reqEntity.token = cfgPay.getAPP_TOKEN(); 
//		reqEntity.type = ConfigerYFTPay.PAY_TYPE_ZHIFUBAO;
		return reqEntity;
	}
}
