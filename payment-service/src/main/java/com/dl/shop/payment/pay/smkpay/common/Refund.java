package com.dl.shop.payment.pay.smkpay.common;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.smkpay.util.HttpUtil;
import com.dl.shop.payment.pay.smkpay.util.RSAUtil;
import com.dl.shop.payment.pay.smkpay.util.SDKConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Function: ADD FUNCTION.
 * date: 2019/4/18 10:02
 *
 * @author yejqiiu
 * @version 1.0.0
 * @Copyright (c) 2019, 杭州市民卡有限公司  All Rights Reserved.
 **/
public class Refund {

	public static void main(String[] args) throws Exception {
		SDKConfig.getConfig().loadPropertiesFromPath("src/main/resources/sdk.properties");
		String reqSeq = System.currentTimeMillis() + "";
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		//请求接口实际数据
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put("version", "1.0.0");
		messageMap.put("transCode", "REF0001");
		messageMap.put("merCode", SDKConfig.getConfig().getMerCode());
		messageMap.put("chainNo", "004");
		messageMap.put("reqSeq", "201907161113009999");

		messageMap.put("tradeDate", "2019-07-16");
		messageMap.put("tradeTime", "11:13:00");
		messageMap.put("originalseq", "");
		messageMap.put("originalorder", "201907161112009999");
		messageMap.put("originaldate", "2019-07-16");
		messageMap.put("originaltime", "11:12:00");
		messageMap.put("amount", "0.50");
		messageMap.put("orderNo", "201907161113009999");


		String accSplitData="[{\"splitMerCode\":\"000012\",\"splitType\":\"2\",\"splitAmt\":\"20\",\"splitSort\":\"1\",\"splitFee\":\"0.3\" },{\"splitMerCode\":\"000042\",\"splitType\":\"2\",\"splitAmt\":\"20\",\"splitSort\":\"2\",\"splitFee\":\"0.7\" }]";


		//messageMap.put("accSplitData",accSplitData);
		messageMap.put("reserved","保留域");


		String messageMapStr = gson.toJson(messageMap);
		//请求开放平台数据
		Map<String, String> openMap = new HashMap<String, String>();
		openMap.put("reqSeq", reqSeq);
		openMap.put("appId", SDKConfig.getConfig().getAppId());
		openMap.put("method", "refund");
		openMap.put("bizContent", messageMapStr);
		openMap.put("sign_param", "appId,method,bizContent");
		//加签
		openMap.put("sign", RSAUtil.rsaSignByCert(openMap,SDKConfig.getConfig().getCertPath(),SDKConfig.getConfig().getCertPwd()));
		String message = gson.toJson(openMap);
		System.out.println(message);
		String respStr = HttpUtil.postReq(SDKConfig.getConfig().getRequestUrl(), message);
		JSONObject json = JSON.parseObject(respStr);
		System.out.println(json.toJSONString());
		//验签
		if ("true".equals(json.getString("success"))) {
			Map<String, String> checkMap = new HashMap<String, String>();
			checkMap.put("reqSeq", reqSeq);
			checkMap.put("sign_param", "success,value");
			checkMap.put("success", json.getString("success"));
			checkMap.put("value", json.getString("value"));
			checkMap.put("sign", json.getString("sign"));
			checkMap.put("publicKey", SDKConfig.getConfig().getVertifyPublicKey());
			System.out.println(RSAUtil.rsaCheck(checkMap));


		}
	}
}
