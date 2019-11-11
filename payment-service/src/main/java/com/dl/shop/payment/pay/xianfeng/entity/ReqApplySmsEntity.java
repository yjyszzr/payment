package com.dl.shop.payment.pay.xianfeng.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.yinhe.util.PayKeyComparator;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

public class ReqApplySmsEntity {
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String merchantNo;
	public String sign;
	
	public static final ReqApplySmsEntity buildApplySmsEntity(String orderNo,Constants xFConstants) throws Exception {
		ReqApplySmsEntity reqEntity = new ReqApplySmsEntity();
		reqEntity.service = "REQ_PAY_QUICK_RESEND";
		reqEntity.secId = xFConstants.getSEC_ID();
		reqEntity.version = xFConstants.getVERSION();
		reqEntity.merchantId = xFConstants.getMER_ID();
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));;
		reqEntity.reqSn = reqSn;
		reqEntity.merchantNo = orderNo;
		String jsonStr = JSON.toJSONString(reqEntity);
		System.out.println("json str:" + jsonStr);
		JSONObject jsonObj = JSON.parseObject(jsonStr,JSONObject.class);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		Map<String,String> mMap = new TreeMap<>(new PayKeyComparator());;
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).toString();
			mMap.put(key,val);
		}
		System.out.println("map.size:" + mMap.size());
		String signValue = UcfForOnline.createSign(xFConstants.getMER_RSAKEY(),"sign", mMap, "RSA");
		reqEntity.sign = signValue;
		System.out.println("sign:" + signValue);
		return reqEntity;
	}
	
	
//	public ReqSnEntity buildSnEntity() {
//	}
	
	public String buildReqStr() throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		builder.append("service="+service);
		builder.append("&");
		builder.append("secId="+secId);
		builder.append("&");
		builder.append("version="+version);
		builder.append("&");
		builder.append("reqSn="+reqSn);
		builder.append("&");
		builder.append("merchantId="+merchantId);
		builder.append("&");
		builder.append("sign="+URLEncoder.encode(sign,"UTF-8"));
		builder.append("&");
		builder.append("merchantNo="+merchantNo);
		return builder.toString();
	}
}
