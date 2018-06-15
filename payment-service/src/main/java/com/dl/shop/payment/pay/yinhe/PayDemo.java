package com.dl.shop.payment.pay.yinhe;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.ReqPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqQRPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqSignEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.pay.yinhe.util.PayKeyComparator;

/***
 * 支付代码
 */
public class PayDemo {

	public PayDemo() {
//		testQuery();
//		testPay();
//		testQRBarPay();
//		testUtil();
//		testRefund();
//		testPayManager();
//		testListRemove();
	}

//	private void testListRemove() {
//		List<Integer> mList = new ArrayList<Integer>();
//		mList.add(1);
//		mList.add(2);
//		mList.add(6);
//		mList.add(5);
//		mList.add(6);
//		mList.add(7);
//		int size = mList.size();
//		System.out.println("size:" + size);
//		showList(mList);
//		for(int i = 0;i < mList.size();i++) {
//			int data = mList.get(i);
//			if(data == 6 || data == 7) {
//				int rData = mList.remove(i);
//				System.out.println("remove data:" + rData);
//				showList(mList);
//			}
//		}
//		size = mList.size();
//		showList(mList);
//		System.out.println("size:" + size);
//		
//	}
//	
//	private void showList(List<Integer> mList) {
//		for(int i = 0;i < mList.size();i++) {
//			System.out.print(mList.get(i) + "\t");
//		}
//		System.out.println();
//	}
//	
//	private void testPayManager() {
//		PayManager.getInstance().addReqQueue("123456",null,"app_weixin");
//	}
//	
//	private void testRefund(){
//		String orderNo = "20180514170530910360015";
//		String amt = "1";
//		boolean isInWeChat = true;
//		ReqRefundOrderEntity reqEntity = ReqRefundOrderEntity.buildReqQueryEntity(isInWeChat,orderNo,amt);
//		ReqSignEntity signEntity = reqEntity.buildSignEntity();
//		String str = JSON.toJSONString(signEntity);
//		JSONObject jsonObj = JSON.parseObject(str,JSONObject.class);
//		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
//		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
//		//sort key
//		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
//		while(iterator.hasNext()) {
//			java.util.Map.Entry<String, Object> entry = iterator.next();
//			String key = entry.getKey();
//			String val = jsonObj.get(key).toString();
//			treeMap.put(key,val);
//		}
//		showTreeMap(treeMap);
//		//获取sign code 参数
//		String paraStr = PayUtil.getPayParams(treeMap);
//		System.out.println("sign code params:" + paraStr + " secret:" +ConfigerPay.SECRET);
//		//生成signCode
//		String signCode = PayUtil.getSignCode(paraStr,ConfigerPay.SECRET);
//		signCode = signCode.toUpperCase();
//		System.out.println("sign code:" + signCode);
//		//赋值signCode
//		reqEntity.signValue = signCode;
//		//signCode添加到请求参数中
//		String reqStr = JSON.toJSONString(reqEntity);
//		System.out.println(reqStr);//查询queryPayInfo.action
//		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/refundOrder.action",true);
//		System.out.println(rspEntity);
//	}
//	
//	private void testUtil() {
//		RspYinHeEntity rspEntity = PayUtil.getWechatPayUrl(true,"39.155.221.148","2",System.currentTimeMillis()+"");
//		System.out.println("rspEntity:" + rspEntity);
//	}
//	
//	private void testQRBarPay() {
//		String orderNo = "123456";
//		String URL_PAY = "http://zfyun.com.cn:8080/YinHeLoan/yinHe";
//		String amt = "1";
//		String SECRET = "b4df7b2d0cb5a90659afcb165b701d5e";
//		ReqQRPayEntity reqEntity = new ReqQRPayEntity();
//		reqEntity.setOrgNo("2188");
//		reqEntity.setAmt(amt);
//		reqEntity.setBackUrl("http://api.caixiaomi.net/api/payment/payment/wxpay/notify");
//		reqEntity.setCharset("UTF-8");
//		reqEntity.setTermType("RQ");
//		reqEntity.setTxtTime(ConfigerPay.getPayTime());
//		reqEntity.setSignType("MD5");
//		reqEntity.setTransNo(orderNo);
//		reqEntity.setMerId("1503174711");
//		reqEntity.setPayType("1");
//		reqEntity.setInWechat("0");
//		
//		ReqSignEntity signEntity = reqEntity.buildSignEntity();
//		String str = JSON.toJSONString(signEntity);
//		JSONObject jsonObj = JSON.parseObject(str,JSONObject.class);
//		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
//		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
//		//sort key
//		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
//		while(iterator.hasNext()) {
//			java.util.Map.Entry<String, Object> entry = iterator.next();
//			String key = entry.getKey();
//			String val = jsonObj.get(key).toString();
//			treeMap.put(key,val);
//		}
//		showTreeMap(treeMap);
//		//获取sign code 参数
//		String paraStr = getPayParams(treeMap);
//		System.out.println("sign code params:" + paraStr + " secret:" + SECRET);
//		//生成signCode
//		String signCode = getSignCode(paraStr,SECRET);
//		signCode = signCode.toUpperCase();
//		System.out.println("sign code:" + signCode);
//		//赋值signCode
//		reqEntity.setSignValue(signCode);;
//		//signCode添加到请求参数中
//		String reqStr = JSON.toJSONString(reqEntity);
//		System.out.println(reqStr);//查询queryPayInfo.action
//		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,URL_PAY+"/yinHePay.action",true);
//		System.out.println(rspEntity);
//	}
//	
//	private void testQuery() {//20180514160177010290042
//		String orderNo = "20180514170530910360015";
//		ReqQueryEntity reqEntity = ReqQueryEntity.buildReqQueryEntity(true,orderNo);
//		ReqSignEntity signEntity = reqEntity.buildSignEntity();
//		String str = JSON.toJSONString(signEntity);
//		JSONObject jsonObj = JSON.parseObject(str,JSONObject.class);
//		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
//		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
//		//sort key
//		TreeMap<String,Object> treeMap = new TreeMap<>(new PayKeyComparator());
//		while(iterator.hasNext()) {
//			java.util.Map.Entry<String, Object> entry = iterator.next();
//			String key = entry.getKey();
//			String val = jsonObj.get(key).toString();
//			treeMap.put(key,val);
//		}
//		showTreeMap(treeMap);
//		//获取sign code 参数
//		String paraStr = PayUtil.getPayParams(treeMap);
//		System.out.println("sign code params:" + paraStr + " secret:" +ConfigerPay.SECRET);
//		//生成signCode
//		String signCode = PayUtil.getSignCode(paraStr,ConfigerPay.SECRET);
//		signCode = signCode.toUpperCase();
//		System.out.println("sign code:" + signCode);
//		//赋值signCode
//		reqEntity.signValue = signCode;
//		//signCode添加到请求参数中
//		String reqStr = JSON.toJSONString(reqEntity);
//		System.out.println(reqStr);//查询queryPayInfo.action
//		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/queryPayInfo.action",true);
//		System.out.println(rspEntity);
//	}
//	
	public static void main(String[] args) {
		new PayDemo();
	}
//	
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
//	
	/**
	 * yinhe.app.app_id=wx50d353a8b7b77225
	   yinhe.app_secret=b4df7b2d0cb5a90659afcb165b701d5e
	   yinhe.app_mch_id=1503174711
	   yinhe.app_org_no=2188
	   yinhe.app_charset=UTF-8
	   yinhe.app_sign=MD5
	   yinhe.app_url_pay=http://zfyun.com.cn:8080/YinHeLoan/yinHe
	   yinhe.app_notify=http://api.caixiaomi.net/api/payment/payment/wxpay/notify
	   yinhe.app_redirect=http://m.caixiaomi.net/static/payCallBack/payCallBack.html
	   yinhe.app_device=kdt1070605
	   yinhe_app_screct=b4df7b2d0cb5a90659afcb165b701d5e
	   yinhe.app_wechat_jump=http://zf.caixiaomi.net/reapal-h5-api/wechat/payMatched.html
	 */
	
	private void testPay() {
//		String URL_PAY = "http://zfyun.com.cn:8080/YinHeLoan/yinHe";
		String URL_PAY = "http://yhyr.com.cn/YinHeLoan/yinHe";
		String amt = "1";
		String SECRET = "b4df7b2d0cb5a90659afcb165b701d5e";
		BigDecimal bigD = new BigDecimal(amt);
		amt = bigD.movePointRight(2).toString();
		System.out.println(amt);
		ReqPayEntity reqEntity = new ReqPayEntity();
		reqEntity.setOrgNo("2188");
		reqEntity.setAmt(amt);
		reqEntity.setBackUrl("http://api.caixiaomi.net/api/payment/payment/wxpay/notify");
		reqEntity.setCharset("UTF-8");
		reqEntity.setTermType("RQ");
		reqEntity.setTxtTime(ConfigerPay.getPayTime());
		reqEntity.setSignType("MD5");
		reqEntity.setTransNo("201805160000000001");
		reqEntity.setMerId("1503174711");
		reqEntity.setPayType("1");
		reqEntity.setInWechat("0");
		reqEntity.setIp("123.117.164.47");
		reqEntity.setTermNo("kdt1070605");
		
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
		String paraStr = getPayParams(treeMap);
		System.out.println("sign code params:" + paraStr + " secret:" + SECRET);
		//生成signCode
		String signCode = getSignCode(paraStr,SECRET);
		signCode = signCode.toUpperCase();
		System.out.println("sign code:" + signCode);
		//赋值signCode
		reqEntity.setSignValue(signCode);
		//signCode添加到请求参数中
		String reqStr = JSON.toJSONString(reqEntity);
		System.out.println(reqStr);
		//发送  yinHePay.action  -> yinHePayH5.action
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,URL_PAY+"/yinHePayH5.action",true);
		System.out.println(rspEntity);
		if(rspEntity.isSucc) {
			RspYinHeEntity rEntity = JSON.parseObject(rspEntity.msg,RspYinHeEntity.class);
			System.out.println("isSucc:" +rEntity.isSucc());
		}else {
			System.out.println(rspEntity);
		}
	}
	
	/*
	 * 获取signcode
	 * @param paraUrl
	 * @param s
	 * @return
	 */
	public final String getSignCode(String paraUrl,String s) {
		String str = paraUrl + s;
		String strMD5 = MD5Utils.MD5(str);
		return strMD5;
	}
	 
	public final String getPayParams(TreeMap<String, Object> treeMap) {
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
}
