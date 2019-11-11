package com.dl.shop.payment.pay.xianfeng.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSnEntity;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

public class ReqApplyCfgDataEntity {
	public String merchantNo;
	public String checkCode;
	
	
	
	public ReqSnEntity buildSnCashEntity(String data,Constants xfConstants) throws CoderException {
		ReqSnEntity reqEntity = new ReqSnEntity();
		reqEntity.service = "REQ_PAY_QUICK_CONFIRM";
		reqEntity.version = xfConstants.getVERSION();
		reqEntity.merchantId = xfConstants.getMER_ID();
		reqEntity.data = data;
		reqEntity.secId = xfConstants.getSEC_ID();
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
		return reqEntity;
	}
	
}
