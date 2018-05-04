package com.dl.shop.payment.pay.yinhe;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.common.HttpUtil;

/****
 * 支付代码
 */
public class PayDemo {

	public PayDemo() {
		ReqPayEntity reqEntity = null;
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
		//展示treemap
		showTreeMap(treeMap);
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
		System.out.println(resultStr);
	}
	
	public static void main(String[] args) {
		new PayDemo();
	}
	
	private static void showTreeMap(Map<String,Object> treeMap) {
		System.out.println("==========================================");
		Iterator<?> iterator = treeMap.entrySet().iterator();  
		while(iterator.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			System.out.print(key + "=" +val + "\t");
		}
		System.out.println();
	}
	
}
