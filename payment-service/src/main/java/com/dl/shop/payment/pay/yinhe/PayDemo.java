package com.dl.shop.payment.pay.yinhe;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.ReqPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqQueryEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqSignEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.pay.yinhe.util.PayKeyComparator;
import com.dl.shop.payment.pay.yinhe.util.PayUtil;

/****
 * 支付代码
 */
public class PayDemo {

	public PayDemo() {
//		testQuery();
		testPay();
	}
	
	private void testQuery() {
		String orderNo = "123456";
		ReqQueryEntity reqEntity = ReqQueryEntity.buildReqQueryEntity(orderNo);
		ReqSignEntity signEntity = reqEntity.buildSignEntity();
		String str = JSON.toJSONString(signEntity);
		JSONObject jsonObj = JSON.parseObject(str,JSONObject.class);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		//sort key
		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).toString();
			treeMap.put(key,val);
		}
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
		System.out.println(reqStr);//查询queryPayInfo.action
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/queryPayInfo.action",true);
		System.out.println(rspEntity);
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
	
	
	private void testPay() {
		String amt = "10.0";
		BigDecimal bigD = new BigDecimal(amt);
		amt = bigD.movePointRight(2).toString();
		System.out.println(amt);
		ReqPayEntity reqEntity = ReqPayEntity.buildReqEntity("127.0.0.1",amt, ""+System.currentTimeMillis());
		ReqSignEntity signEntity = reqEntity.buildSignEntity();
		String str = JSON.toJSONString(signEntity);
		JSONObject jsonObj = JSON.parseObject(str,JSONObject.class);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		//sort key
		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).toString();
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
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/yinHePayH5.action",true);
		System.out.println(rspEntity);
		if(rspEntity.isSucc) {
			RspYinHeEntity rEntity = JSON.parseObject(rspEntity.msg,RspYinHeEntity.class);
			System.out.println("isSucc:" +rEntity.isSucc());
		}else {
			System.out.println(rspEntity);
		}
	}
}
