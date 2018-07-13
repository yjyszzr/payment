package com.dl.shop.payment.pay.yinhe.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.ReqQueryEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqRefundOrderEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqSignEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspQueryEntity;
import com.dl.shop.payment.web.PaymentController;

/****
 * 银河支付订单操作
 */
@Component
public class YinHeUtil {
	private final static Logger logger = LoggerFactory.getLogger(YinHeUtil.class);
	
	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private PayUtil payUtil;
	@Resource
	private ReqRefundOrderEntity reqRefundOrderEntity;
	@Resource
	private ReqQueryEntity reqQueryEntity;
	
	/***
	 * 资金回退接口
	 * @param orderNo
	 * @param amt
	 * @return
	 */
	public RspRefundEntity orderRefund(boolean isInWeChat,String orderNo,String amt){
		RspRefundEntity rEntity = new RspRefundEntity();
		/*if("true".equals(cfgPay.getDEBUG())) {
			amt = "1";
		}*/
		ReqRefundOrderEntity reqEntity = reqRefundOrderEntity.buildReqQueryEntity(isInWeChat,orderNo,amt);
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
		String paraStr = payUtil.getPayParams(treeMap);
		System.out.println("sign code params:" + paraStr + " secret:" + cfgPay.getSECRET_PUBLIC());
		//生成signCode
		String signCode = payUtil.getSignCode(paraStr,cfgPay.getSECRET_PUBLIC());
		signCode = signCode.toUpperCase();
		System.out.println("sign code:" + signCode);
		//赋值signCode
		reqEntity.setSignValue(signCode);
		//signCode添加到请求参数中
		String reqStr = JSON.toJSONString(reqEntity);
		System.out.println(reqStr);//退款refundOrder.action
		logger.info("==================================================");
		logger.info("[orderRefund]" +" url:" + cfgPay.getURL_PAY() + "/refundOrder.action");
		logger.info("[orderRefund]" +" req:" + reqStr);
		logger.info("==================================================");
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,cfgPay.getURL_PAY()+"/refundOrder.action",true);
		if(rspEntity != null && rspEntity.isSucc) {
			String msg = rspEntity.msg;
			rEntity = JSON.parseObject(msg,RspRefundEntity.class);
		}else {
			rEntity.setResult_msg(rspEntity.msg);
		}
		return rEntity;
	}
	
	/**
	 * 银河订单查询
	 * @param orderNo
	 * @return
	 */
	public BaseResult<RspOrderQueryEntity> orderQuery(boolean isInWechat,String orderNo){
		ReqQueryEntity reqEntity = reqQueryEntity.buildReqQueryEntity(isInWechat,orderNo);
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
		String paraStr = payUtil.getPayParams(treeMap);
//		System.out.println("sign code params:" + paraStr + " secret:" +cfgPay.getSECRET());
		//生成signCode
		String signCode = payUtil.getSignCode(paraStr,cfgPay.getSECRET_PUBLIC());
		signCode = signCode.toUpperCase();
//		System.out.println("sign code:" + signCode);
		//赋值signCode
		reqEntity.setSignValue(signCode);
		//signCode添加到请求参数中
		String reqStr = JSON.toJSONString(reqEntity);
		System.out.println(reqStr);
		RspHttpEntity rspEntity = HttpUtil.sendMsg(reqStr,cfgPay.getURL_PAY()+"/queryPayInfo.action",true);
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
	
	
	public static final String getPayCode() {
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx50d353a8b7b77225&redirect_uri=http://yhyr.com.cn/YinHeLoan/getaibangAuth.jsp&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
		RspHttpEntity rspEntity = HttpUtil.sendMsg("",url,true);
		System.out.println(rspEntity);
		return "";
	}
}
