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
 * Function: TODO ADD FUNCTION. <br/>
 * Date: 2018年8月7日 下午2:51:00 <br/>
 *
 * @author jianghm
 * @version 1.0
 * @Copyright (c) 2018, 杭州市民卡有限公司  All Rights Reserved.
 */
public class DldSettleTest {

	public static void main(String[] args) throws Exception {
		SDKConfig.getConfig().loadPropertiesFromPath("src/main/resources/sdk.properties");
		String reqSeq = System.currentTimeMillis() + "";
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        //请求接口实际数据
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");
        messageMap.put("merCode", SDKConfig.getConfig().getMerCode());
        messageMap.put("chainNo", "004");
        messageMap.put("reqSeq", reqSeq);
        messageMap.put("transCode", "DLD0001");
        messageMap.put("date", "20190614");
        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", SDKConfig.getConfig().getAppId());
        openMap.put("method", "dldSettle");
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
        	if(RSAUtil.rsaCheck(checkMap)){
				System.out.println(RSAUtil.rsaCheck(checkMap));
        		String downUrl=json.getString("value");
				JSONObject jsondownUrl = JSON.parseObject(downUrl);
				String bill = HttpUtil.postReq(jsondownUrl.getString("downloadUrl"), "");
				System.out.println(bill);
			}

        }
	}

}

