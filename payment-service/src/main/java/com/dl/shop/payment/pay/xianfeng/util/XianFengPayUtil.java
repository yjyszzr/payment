package com.dl.shop.payment.pay.xianfeng.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSnEntity;
import com.dl.shop.payment.pay.xianfeng.config.XianFengPayCfg;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplyCfgDataEntity;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplyCfgEntity;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplyDataEntity;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplyEntity;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplyQueryEntity;
import com.dl.shop.payment.pay.xianfeng.entity.ReqApplySmsEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.yinhe.util.PayKeyComparator;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.AESCoder;

@Component
public class XianFengPayUtil {
	
	public XianFengPayUtil() throws Exception {
		// TODO Auto-generated constructor stub
		//支付申请
		String userId = null;
		String amt = "1";
		String certNo = "420621199012133824";
		String accNo = "6222021001115704287";
		String accName = "工商银行";
		String mobileNo = "18100000000";
		String bankId = "ICBC";
		String pName = "测试产品";
		String pInfo = "测试产品描述";
		String orderNo = System.currentTimeMillis()+"";
		RspApplyBaseEntity rspEntity = reqApply(orderNo,userId,amt,certNo,accNo,accName,mobileNo,bankId,pName,pInfo);
		System.out.println("========="+rspEntity);
		
		//支付确认
//		String code = "909090";
//		String orderNo = System.currentTimeMillis()+"";
//		RspApplyBaseEntity rspEntity = reqApplyCfg(code,orderNo);
//		System.out.println(rspEntity);
		
//		String orderNo = System.currentTimeMillis() +"";
//		//短信重发
//		RspApplyBaseEntity rspEntity = reqApplySms(orderNo);
//		System.out.println(rspEntity);
		
		//支付订单查询
//		String orderNo = System.currentTimeMillis() +"";
//		RspApplyBaseEntity rspEntity = queryPayByOrderNo(orderNo);
//		System.out.println(rspEntity);
		
//		String strMsg = "UEW/n2BLXZCLe4TvpOpc/IZczQu94HN049zJL7ODVMkjlbWwo9DUbqjzPQRA7Qg3HVVavG0Drl1rf8qKCg12wQ==";
//		String dataResult = AESCoder.decrypt(strMsg,XianFengPayCfg.RSA_KEY);
//		System.out.println(dataResult);
	}

	public RspApplyBaseEntity queryPayByOrderNo(String orderNo) throws Exception {
		ReqApplyQueryEntity reqQueryEntity = ReqApplyQueryEntity.buildReqApplyQueryEntity(orderNo);
		//生成data
		String url = XianFengPayCfg.NET_GATE + "?" + reqQueryEntity.buildReqStr();
		System.out.println("请求参数:" + url);
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		RspApplyBaseEntity rEntity = null;
		if(rspHttpEntity.isSucc) {
			String str = rspHttpEntity.msg;
			String dataResult = AESCoder.decrypt(str,XianFengPayCfg.RSA_KEY);
			rEntity = JSON.parseObject(dataResult,RspApplyBaseEntity.class);
		}else {
			rEntity = new RspApplyBaseEntity();
			rEntity.resMessage = rspHttpEntity.msg;
		}
		System.out.println("dataResult:" + rEntity);
		return rEntity;
	}
	
	public RspApplyBaseEntity reqApplySms(String orderNo) throws Exception {
		RspApplyBaseEntity rEntity = null;
		ReqApplySmsEntity reqApplySmsEntity = ReqApplySmsEntity.buildApplySmsEntity(orderNo);
		//生成data
		String url = XianFengPayCfg.NET_GATE + "?" + reqApplySmsEntity.buildReqStr();
		System.out.println("请求参数:" + url);
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		if(rspHttpEntity.isSucc) {
			String str = rspHttpEntity.msg;
			String dataResult = AESCoder.decrypt(str,XianFengPayCfg.RSA_KEY);
			rEntity = JSON.parseObject(dataResult,RspApplyBaseEntity.class);
		}else {
			rEntity = new RspApplyBaseEntity();
			rEntity.resMessage = rspHttpEntity.msg;
		}
		return rEntity;
	}
	
	public RspApplyBaseEntity reqApplyCfg(String code,String orderNo) throws Exception {
		RspApplyBaseEntity rspEntity = null;
		ReqApplyCfgDataEntity reqCfgEntity = new ReqApplyCfgDataEntity();
		reqCfgEntity.checkCode = code;
		reqCfgEntity.merchantNo = orderNo;
		//生成data
		String jsonStr = JSON.toJSONString(reqCfgEntity);
		System.out.println("jsonStr:" + jsonStr);
		String data = AESCoder.encrypt(jsonStr,XianFengPayCfg.RSA_KEY);
		System.out.println("data:" + data);
		//sn
		ReqSnEntity reqSnEntity = reqCfgEntity.buildSnCashEntity(data);
		String strInfo = JSON.toJSONString(reqSnEntity);
		//sort key
		JSONObject jsonObj = JSONObject.parseObject(strInfo,JSONObject.class);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		Map<String,String> mMap = new TreeMap<>(new PayKeyComparator());;
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).toString();
			mMap.put(key,val);
		}
		//生成signVal
		String signValue = UcfForOnline.createSign(XianFengPayCfg.RSA_KEY,"sign", mMap, "RSA");
		ReqApplyCfgEntity reqApplyCfgEntity = ReqApplyCfgEntity.buildReqApplyCfgEntity(reqSnEntity.reqSn,data,signValue);
		String url = XianFengPayCfg.NET_GATE + "?" + reqApplyCfgEntity.buildReqStr();
		System.out.println("请求参数:" + url);
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		if(rspHttpEntity.isSucc) {
			String str = rspHttpEntity.msg;
			String dataResult = AESCoder.decrypt(str,XianFengPayCfg.RSA_KEY);
			System.out.println("dataResult:" + dataResult);
			rspEntity = JSON.parseObject(dataResult,RspApplyBaseEntity.class);
		}else {
			rspEntity = new RspApplyBaseEntity();
			rspEntity.resMessage = rspHttpEntity.msg;
		}
		return rspEntity;
	}
	
	//userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo
	public RspApplyBaseEntity reqApply(String orderNo,String userId,String amt,String certNo,String accNo,String accName,String mobileNo,String bankId,String pName,String pInfo) throws Exception {
		RspApplyBaseEntity rspEntity = null;
		ReqApplyDataEntity reqDataEntity = ReqApplyDataEntity.buildReqDataEntity(orderNo,userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo);
		String jsonStr = JSON.toJSONString(reqDataEntity);
//		System.out.println("jsonStr:" + jsonStr);
		//data生成
		String data = AESCoder.encrypt(jsonStr,XianFengPayCfg.RSA_KEY);
//		System.out.println("data:" + data);
		ReqSnEntity reqSnEntity = reqDataEntity.buildSnCashEntity(data);
		String strInfo = JSON.toJSONString(reqSnEntity);
		//sort key
		Map<String,String> mMap = JSON.parseObject(strInfo,HashMap.class);
		//生成signVal
		String signValue = UcfForOnline.createSign(XianFengPayCfg.RSA_KEY,"sign", mMap, "RSA");
		ReqApplyEntity reqApplyEntity = ReqApplyEntity.buildReqApplyEntity(reqSnEntity.reqSn,data,signValue);
		//生成请求链接
		String url = XianFengPayCfg.NET_GATE + "?" + reqApplyEntity.buildReqStr();
		System.out.println("请求参数:" + url);
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		if(rspHttpEntity.isSucc) {
			String strMsg = rspHttpEntity.msg;
			String dataResult = AESCoder.decrypt(strMsg,XianFengPayCfg.RSA_KEY);
			System.out.println("dataResult:" + dataResult);
			rspEntity = JSON.parseObject(dataResult,RspApplyBaseEntity.class);
		}else {
			rspEntity = new RspApplyBaseEntity();
			rspEntity.resMessage = rspHttpEntity.msg;
		}
		return rspEntity;
	}
	
	public static void main(String[] args) throws Exception {
		new XianFengPayUtil();
	}
}
