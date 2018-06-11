package com.dl.shop.payment.pay.xianfeng.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSnEntity;
import com.dl.shop.payment.pay.xianfeng.config.XianFengPayCfg;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

public class ReqApplyCfgDataEntity {
	public String merchantNo;
	public String checkCode;
	
	
	
	public ReqSnEntity buildSnCashEntity(String data) throws CoderException {
		ReqSnEntity reqEntity = new ReqSnEntity();
		reqEntity.service = "REQ_PAY_QUICK_CONFIRM";
		reqEntity.version = XianFengPayCfg.VERSION;
		reqEntity.merchantId = XianFengPayCfg.MERCHANT_NO;
		reqEntity.data = data;
		reqEntity.secId = XianFengPayCfg.SEC_ID;
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
		return reqEntity;
	}
	
}
