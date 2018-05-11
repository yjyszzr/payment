package com.dl.shop.payment.pay.yinhe.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.PayConfig;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.ReqPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqQRPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqSignEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.web.PaymentController;

public class PayUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
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
	public static final RspYinHeEntity getWechatPayUrl(boolean isInnerWechat,String ip,String amount,String orderNo){
		if(PayConfig.isDebug()) {
			amount = "1";
		}
		RspYinHeEntity rEntity = null;
		ReqQRPayEntity reqQRPayEntity = null;
		ReqPayEntity reqH5Entity = null;
		ReqSignEntity signEntity = null;
		if(isInnerWechat) {
			reqQRPayEntity = ReqQRPayEntity.buildReqEntity(amount,orderNo);
			signEntity = reqQRPayEntity.buildSignEntity();
		}else {
			reqH5Entity = ReqPayEntity.buildReqEntity(ip, amount, orderNo);			
			signEntity = reqH5Entity.buildSignEntity();
		}
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
		//获取sign code 参数
		String paraStr = PayUtil.getPayParams(treeMap);
		String secret = null;
		if(isInnerWechat) {
			secret = ConfigerPay.SECRET_PUBLIC;
		}else {
			secret = ConfigerPay.SECRET_PUBLIC;
		}
		logger.info("sign code params:" + paraStr + " secret:" + secret);
		//生成signCode
		String signCode = PayUtil.getSignCode(paraStr,secret);
		signCode = signCode.toUpperCase();
		logger.info("sign code:" + signCode);
		String reqStr = null;
		//赋值signCode
		if(isInnerWechat) {
			reqQRPayEntity.signValue = signCode;
			reqStr = JSON.toJSONString(reqQRPayEntity);
		}else {
			reqH5Entity.signValue = signCode;
			reqStr = JSON.toJSONString(reqH5Entity);
		}
		//signCode添加到请求参数中
		logger.info(reqStr);
		RspHttpEntity rspHttpEntity = null;
		if(isInnerWechat) {	//yinHePay->动态二维码方式   yinHePayPublic
			rspHttpEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/yinHePublicWxPay.action",true);
		}else {
			//发送  yinHePay.action  -> yinHePayH5.action
			rspHttpEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/yinHePayH5.action",true);			
		}
		logger.info("resultStr:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = JSON.parseObject(rspHttpEntity.msg,RspYinHeEntity.class);
		}else {
			rEntity = new RspYinHeEntity();
			rEntity.returnMsg = rspHttpEntity.msg;
		}
		return rEntity;
	}
}
