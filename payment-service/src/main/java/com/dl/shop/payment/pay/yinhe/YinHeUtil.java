package com.dl.shop.payment.pay.yinhe;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;

/****
 * 银河支付订单查询
 */
public class YinHeUtil {
	
	/**
	 * 银河订单查询
	 * @param orderNo
	 * @return
	 */
	public BaseResult<RspOrderQueryEntity> orderQuery(String orderNo){
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
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,ConfigerPay.URL_PAY+"/queryPayInfo.action",true);
		if(rspEntity.isSucc) {
			String contents = rspEntity.msg;
			RspQueryEntity rspQEntity = JSON.parseObject(contents,RspQueryEntity.class);
			RspOrderQueryEntity rEntity = convertEntity(rspQEntity);
			return ResultGenerator.genSuccessResult("succ",rEntity);
		}else {
			return ResultGenerator.genFailResult("请求银河支付回调失败[" + rspEntity.msg + "]");
		}
	}
	
	/***
	 * 银河实体类 -> 其他实体类
	 * @param entity
	 * @return
	 */
	private RspOrderQueryEntity convertEntity(RspQueryEntity entity) {
		RspOrderQueryEntity rEntity = new RspOrderQueryEntity();
		rEntity.setResult_code(entity.getReturnCode());
		rEntity.setResult_msg(entity.getReturnMsg());
		rEntity.setPayCode("app_weixin");
		rEntity.setTrade_no(entity.getOrderNum());
		return rEntity;
	}
}
