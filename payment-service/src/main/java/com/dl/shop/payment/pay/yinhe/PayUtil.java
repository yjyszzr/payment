package com.dl.shop.payment.pay.yinhe;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.apache.http.util.TextUtils;

import com.alibaba.fastjson.JSON;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PayUtil {

	/**
	 * 获取请求参数
	 * @param treeMap
	 * @return
	 */
	public static final String getPayParams(TreeMap<String, Object> treeMap) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iterator = treeMap.entrySet().iterator();  
		while(iterator.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String key = entry.getKey();
			String val = (String) entry.getValue();
			builder.append(key + "=" + val + "&");
		}
		System.out.println("原串:" +builder.toString());
		//remove the last item &
		if(builder.length() > 0) {
			builder.delete(builder.length()-1,builder.length());
		}
		System.out.println("结果:" + builder.toString());
		return builder.toString();
	}
	
	/**
	 * 获取signcode
	 * @param paraUrl
	 * @param s
	 * @return
	 */
	public static final String getSignCode(String paraUrl,String s) {
		String str = paraUrl + s;
		String strMD5 = MD5Utils.MD5(str);
		return strMD5;
	}
	
	
	/**
	 * 获取微信支付url
	 * @return
	 */
	public static final RspYinHeEntity getWechatPayUrl(String ip,String amount,String orderNo){
		RspYinHeEntity rEntity = null;
		ReqPayEntity reqEntity = ReqPayEntity.buildReqEntity(ip, amount, orderNo);
		ReqSignEntity signEntity = reqEntity.buildSignEntity();
		String str = JSON.toJSONString(signEntity);
		JsonObject jsonObj = JSON.parseObject(str,JsonObject.class);
		Set<java.util.Map.Entry<String, JsonElement>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, JsonElement>> iterator = mSet.iterator();
		//sort key
		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, JsonElement> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).getAsString();
			treeMap.put(key,val);
		}
		//获取sign code 参数
		String paraStr = PayUtil.getPayParams(treeMap);
		System.out.println("sign code params:" + paraStr + " secret:" +ConfigerPay.SECRET);
		//生成signCode
		String signCode = PayUtil.getSignCode(paraStr,ConfigerPay.SECRET);
		signCode = signCode.toUpperCase();
		System.out.println("sign code:" + signCode);
		//赋值signCode
		reqEntity.signValue = signCode;
		//signCode添加到请求参数中
		String reqStr = JSON.toJSONString(reqEntity);
		System.out.println(reqStr);
		//发送  yinHePay.action  -> yinHePayH5.action
		String resultStr = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/yinHePayH5.action",true);
		if(!TextUtils.isEmpty(resultStr)) {
			rEntity = JSON.parseObject(resultStr,RspYinHeEntity.class);
		}
		return rEntity;
	}
}
