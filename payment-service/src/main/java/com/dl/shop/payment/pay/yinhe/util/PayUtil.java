package com.dl.shop.payment.pay.yinhe.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
import com.dl.shop.payment.web.PaymentController;

@Component
public class PayUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private ReqQRPayEntity reqQRPayEntity;
	@Resource 
	private ReqPayEntity reqPayEntity;
	
	/**
	 * 获取请求参数
	 * @param treeMap
	 * @return
	 */
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
	
	/**
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
	
	
	/**
	 * 获取微信支付url
	 * @return
	 */
	public final RspYinHeEntity getWechatPayUrl(boolean isInnerWechat,String ip,String amount,String orderNo){
		/*if("true".equals(cfgPay.getDEBUG())) {
			amount = "1";
		}*/
		RspYinHeEntity rEntity = null;
		ReqQRPayEntity reqQREntity = null;
		ReqPayEntity reqH5Entity = null;
		ReqSignEntity signEntity = null;
		if(isInnerWechat) {
			reqQREntity = reqQRPayEntity.buildReqEntity(amount,orderNo);
			signEntity = reqQREntity.buildSignEntity();
		}else {
			reqH5Entity = reqPayEntity.buildReqEntity(ip, amount, orderNo);			
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
		String paraStr = getPayParams(treeMap);
		String secret = null;
		if(isInnerWechat) {
			secret = cfgPay.getSECRET_PUBLIC();
		}else {
			secret = cfgPay.getSECRET_PUBLIC();
		}
		//生成signCode
		String signCode = getSignCode(paraStr,secret);
		signCode = signCode.toUpperCase();
		logger.info("sign code:" + signCode);
		String reqStr = null;
		//赋值signCode
		if(isInnerWechat) {
			reqQREntity.setSignValue(signCode);
			reqStr = JSON.toJSONString(reqQREntity);
		}else {
			reqH5Entity.setSignValue(signCode);
			reqStr = JSON.toJSONString(reqH5Entity);
		}
		//signCode添加到请求参数中
		logger.info(reqStr);
		RspHttpEntity rspHttpEntity = null;
		logger.info("sign code params:" + paraStr + " secret:" + secret + " inwechat:" + isInnerWechat);
		if(isInnerWechat) {	//yinHePay->动态二维码方式   yinHePayPublic
			rspHttpEntity = HttpUtil.sendMsg(reqStr,cfgPay.getURL_PAY()+"/yinHePayPublic.action",true);
		}else {
			//发送  yinHePay.action  -> yinHePayH5.action
			rspHttpEntity = HttpUtil.sendMsg(reqStr,cfgPay.getURL_PAY()+"/yinHePayH5.action",true);			
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
