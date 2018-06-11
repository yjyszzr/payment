package com.dl.shop.payment.pay.xianfeng.cash.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.yinhe.util.PayKeyComparator;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.AESCoder;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

public class ReqSingleCashEntity {
	public String service;		//接口名称
	public String secId;		//签名算法
	public String version;		//接口版
	public String reqSn;		//序列
	public String merchantId;	//商户号
	public String data;			//加密业务数据
	public String sign;			//订单签名数
	
	
	public String merchantNo;	//商户订单号
	public String source;		//来源
	public String amount;		//金额
	public String transCur;		//币种
	public String userType;		//用户类型
	public String accountNo;	//卡号
	public String accountName;	//持卡人姓名
	public String accountType;	//账户类型
	public String mobileNo;		//手机号码
	public String bankNo;		//银行编码
	public String noticeUrl;	//结果通知地址
	
	public static ReqSingleCashEntity buildReqSingleCashEntity(String orderNo,String amt,
			String accNo,String accName,String phone,String bankNo,
			String secID,String version,String merID,String notifyURL,String mer_rsakey) throws Exception {
		ReqSingleCashEntity reqEntity = new ReqSingleCashEntity();
		reqEntity.service = "REQ_WITHDRAW";
		reqEntity.secId = secID;
		reqEntity.version = version;
		reqEntity.merchantId = merID;
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
		reqEntity.merchantNo = orderNo;
		reqEntity.source = "1";
		reqEntity.amount = amt;
		reqEntity.transCur = "156";
		reqEntity.userType = "1";
		reqEntity.accountNo = accNo;
		reqEntity.accountName = accName;
		reqEntity.accountType = "1";
		reqEntity.mobileNo = phone;
		reqEntity.bankNo = bankNo;
		reqEntity.noticeUrl = notifyURL;
		//data
		ReqEncodeCashEntity reqCodeEntity = reqEntity.buildEncodeCashEntity();
		String jsonStr = JSON.toJSONString(reqCodeEntity);
//		System.out.println("参与data加密原串:" + jsonStr);
		//AESCoder.encrypt
		String val = AESCoder.encrypt(jsonStr,mer_rsakey);
		reqEntity.data = val;
//		System.out.println("加密后data:" + val);
//		String tempStr = AESCoder.decrypt(val,mer_rsakey);
//		System.out.println("解密data后:" + tempStr);
		
		//sign value
		ReqSnEntity reqSnEntity = reqEntity.buildSnCashEntity();
		jsonStr = JSON.toJSONString(reqSnEntity);
		JSONObject jsonObj = JSONObject.parseObject(jsonStr);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		//sort key
		Map<String,String> mMap = new TreeMap<>(new PayKeyComparator());;
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			val = jsonObj.get(key).toString();
			mMap.put(key,val);
		}
		//UcfForOnline.createSign
		String signValue = UcfForOnline.createSign(mer_rsakey,"sign", mMap, "RSA");
		reqEntity.sign = signValue;
//		System.out.println("签名原串:" + jsonStr);
//		System.out.println("签名结果:" + reqEntity.sign);
		return reqEntity;
	}

	
	public ReqEncodeCashEntity buildEncodeCashEntity() {
		ReqEncodeCashEntity reqEntity = new ReqEncodeCashEntity();
		reqEntity.merchantNo = merchantNo;
		reqEntity.source = source;
		reqEntity.amount = amount;
		reqEntity.transCur = transCur;
		reqEntity.userType = userType;
		reqEntity.accountNo = accountNo;
		reqEntity.accountName = accountName;
		reqEntity.accountType = accountType;
		reqEntity.mobileNo = mobileNo;
		reqEntity.bankNo = bankNo;
		reqEntity.noticeUrl = noticeUrl;
		return reqEntity;
	}
	
	public ReqSnEntity buildSnCashEntity() {
		ReqSnEntity reqEntity = new ReqSnEntity();
		reqEntity.service = this.service;
		reqEntity.version = this.version;
		reqEntity.merchantId = this.merchantId;
		reqEntity.data = this.data;
		reqEntity.secId = this.secId;
		reqEntity.reqSn = this.reqSn;
		return reqEntity;
	}
	
	public ReqRealCashEntity buildRealReqCashEntity() {
		ReqRealCashEntity reqEntity = new ReqRealCashEntity();
		reqEntity.service = this.service;
		reqEntity.secId = this.secId;
		reqEntity.version = this.version;
		reqEntity.reqSn = this.reqSn;
		reqEntity.merchantId = this.merchantId;
		reqEntity.data = this.data;
		reqEntity.sign = this.sign;
		return reqEntity;
	}
}
