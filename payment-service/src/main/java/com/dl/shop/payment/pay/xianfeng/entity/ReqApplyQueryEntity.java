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

/**
 * 订单查询
 * @date 20180611
 */
public class ReqApplyQueryEntity {
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String merchantNo;
	public String sign;

	
	public static final ReqApplyQueryEntity buildReqApplyQueryEntity(String orderNo,Constants xfConstants) throws Exception {
		ReqApplyQueryEntity reqEntity = new ReqApplyQueryEntity();
		reqEntity.service = "REQ_QUICK_QUERY_BY_ID";
		reqEntity.secId = xfConstants.getSEC_ID();
		reqEntity.version = xfConstants.getVERSION();
		reqEntity.merchantId = xfConstants.getMER_ID();
		reqEntity.merchantNo = orderNo;
		
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
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
		String signValue = UcfForOnline.createSign(xfConstants.getMER_RSAKEY(),"sign", mMap, "RSA");
		reqEntity.sign = signValue;
		return reqEntity;
	}
	
	
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
