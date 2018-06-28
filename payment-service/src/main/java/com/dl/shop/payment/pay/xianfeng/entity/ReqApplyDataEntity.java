package com.dl.shop.payment.pay.xianfeng.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSnEntity;
import com.dl.shop.payment.pay.xianfeng.config.XianFengPayCfg;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

import lombok.val;

public class ReqApplyDataEntity {
	public String merchantNo;
	public String payerId;		//付款方ID，应为先锋支付合法用户ID
	public String amount;		//金额	
	public String transCur;		//币种
	public String certificateType;//证件类型
	public String certificateNo;	//证件号码
	public String accountNo;	//银行卡号
	public String accountName;	//持卡人姓名
	public String mobileNo;		//对应银行预留手机号
	public String bankId;		//银行编码
	public String productName;	//商品名称
	public String productInfo;	//商品信息
	public String noticeUrl;	//后台通知地址
	public String cvn2;			//信用卡专用码
	public String validDate;	//信用卡有效期
	
	public static final ReqApplyDataEntity buildReqDataEntity(String orderNo,String userId,String amt,String certNo,
			String accNo,String accName,String mobileNo,String bankId,String pName,String pInfo,String cvn2,String validDate) {
		ReqApplyDataEntity reqEntity = new ReqApplyDataEntity();
		reqEntity.merchantNo = orderNo;
		reqEntity.payerId = userId;
		reqEntity.amount = amt;
		reqEntity.transCur = XianFengPayCfg.TRANSCUR;
		reqEntity.certificateType = XianFengPayCfg.CERTIFICATETYPE;
		reqEntity.certificateNo = certNo;
		reqEntity.accountNo = accNo;
		reqEntity.accountName = accName;
		reqEntity.mobileNo = mobileNo;
		reqEntity.bankId = bankId;
		reqEntity.productName = pName;
		reqEntity.productInfo = pInfo;
		reqEntity.noticeUrl = XianFengPayCfg.NOTIFY_URL;
		reqEntity.cvn2 = cvn2;
		reqEntity.validDate = validDate;
		return reqEntity;
	}
	
	public ReqSnEntity buildSnCashEntity(String data) throws CoderException {
		ReqSnEntity reqEntity = new ReqSnEntity();
		reqEntity.service = "REQ_PAY_QUICK_APPLY";
		reqEntity.version = XianFengPayCfg.VERSION;
		reqEntity.merchantId = XianFengPayCfg.MERCHANT_NO;
		reqEntity.data = data;
		reqEntity.secId = XianFengPayCfg.SEC_ID;
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
		return reqEntity;
	}
}
