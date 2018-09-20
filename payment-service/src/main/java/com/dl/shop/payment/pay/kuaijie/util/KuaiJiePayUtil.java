package com.dl.shop.payment.pay.kuaijie.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.kuaijie.config.KuaiJiePayConfig;
import com.dl.shop.payment.pay.kuaijie.entity.KuaiJieJdPayOrderCreateResponse;
import com.dl.shop.payment.pay.kuaijie.entity.KuaiJiePayNotifyEntity;
import com.dl.shop.payment.pay.kuaijie.entity.KuaiJiePayOrderQueryResponse;
import com.dl.shop.payment.pay.kuaijie.entity.KuaiJieQqPayOrderCreateResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KuaiJiePayUtil {
	@Resource
	private KuaiJiePayConfig kuaiJiePayConfig;

	public Boolean booleanCheckSign(KuaiJiePayNotifyEntity kuaiJiePayNotifyEntity) {
		StringBuffer params = new StringBuffer();
		TreeMap<String, String> paramsTreeMap = new TreeMap<String, String>();
		paramsTreeMap.put("status",kuaiJiePayNotifyEntity.getStatus());
		paramsTreeMap.put("msg",kuaiJiePayNotifyEntity.getMsg());
		paramsTreeMap.put("amount",kuaiJiePayNotifyEntity.getAmount());
		paramsTreeMap.put("merchant_order_no",kuaiJiePayNotifyEntity.getMerchant_order_no());
		paramsTreeMap.put("trade_no",kuaiJiePayNotifyEntity.getTrade_no());
		paramsTreeMap.put("payment_time",kuaiJiePayNotifyEntity.getPayment_time());
		paramsTreeMap.put("pay_channel",kuaiJiePayNotifyEntity.getPay_channel());
		paramsTreeMap.put("pay_channel_name",kuaiJiePayNotifyEntity.getPay_channel_name());
		for(String key:paramsTreeMap.keySet()){
			params.append(key+"="+paramsTreeMap.get(key)+"&");
		}
		params.deleteCharAt(params.length()-1);
		log.info("快接回调返回的参数字符串={}",params.toString());
		return checkParamSign(params.toString(),kuaiJiePayNotifyEntity.getSign());
	}

	public KuaiJieQqPayOrderCreateResponse getKuaijiePayQqQianBaoUrl(String amount, String payOrderSn) {
		log.info("调取快接支付qq钱包支付payOrderSn={},amount={}",payOrderSn,amount);
		if("true".equals(kuaiJiePayConfig.getDEBUG())) {
			amount = "0.01";
		}
		KuaiJieQqPayOrderCreateResponse rEntity = null;
		//获取拼接参数
		String paramStr = getKuaiJiePayCreateParams(amount,payOrderSn);
		RspHttpEntity rspHttpEntity = null;
		rspHttpEntity = HttpUtil.sendMsg(paramStr,kuaiJiePayConfig.getApiUrl()+kuaiJiePayConfig.getQqWapPayUrl(),false);
		log.info("快接QQ钱包支付请求返回信息:{}",rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = (KuaiJieQqPayOrderCreateResponse) JSON.parseObject(rspHttpEntity.msg,KuaiJieQqPayOrderCreateResponse.class);
			String responseParamStr="pay_url="+rEntity.getData().getPay_url()+"&trade_no="+rEntity.getData().getTrade_no();
			String sign=rEntity.getData().getSign();
			boolean rspSign = checkParamSign(responseParamStr,sign);
			if(!rspSign) {
				rspHttpEntity.isSucc = false;
				rEntity = new KuaiJieQqPayOrderCreateResponse();
				rEntity.setInfo("QQ钱包验证响应签名失败");
			}
		}else {
			log.info("qq钱包支付请求失败code={},msgDetail={}" ,rspHttpEntity.isSucc, rspHttpEntity.msg);
			rEntity = new KuaiJieQqPayOrderCreateResponse();
			rEntity.setInfo(rspHttpEntity.msg);
		}
		return rEntity;
	}

	/**
	 * 验证签名
	 * @param signParam
	 * @param sign
	 * @return
	 */
	private boolean checkParamSign(String signParam,String sign) {
		String signNew = MD5Utils.MD5(signParam+"&key="+kuaiJiePayConfig.getSecret());
		if(signNew.equals(sign)){
			return true;
		}
		return false;
	}

	public KuaiJieJdPayOrderCreateResponse getKuaijiePayJingDongUrl(String amount, String payOrderSn) {
		log.info("调取快接支付qq钱包支付payOrderSn={},amount={}",payOrderSn,amount);
		if("true".equals(kuaiJiePayConfig.getDEBUG())) {
			amount = "0.01";
		}
		KuaiJieJdPayOrderCreateResponse rEntity = null;
		//获取拼接参数
		String paramStr = getKuaiJiePayCreateParams(amount,payOrderSn);
		RspHttpEntity rspHttpEntity = null;
		rspHttpEntity = HttpUtil.sendMsg(paramStr,kuaiJiePayConfig.getApiUrl()+kuaiJiePayConfig.getJdPayUrl(),false);
		log.info("快接QQ钱包支付请求返回信息:{}",rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = (KuaiJieJdPayOrderCreateResponse) JSON.parseObject(rspHttpEntity.msg,KuaiJieJdPayOrderCreateResponse.class);
			String responseParamStr="pay_url="+rEntity.getData().getPay_url()+"&trade_no="+rEntity.getData().getTrade_no();
			String sign=rEntity.getData().getSign();
			boolean rspSign = checkParamSign(responseParamStr,sign);
			if(!rspSign) {
				rspHttpEntity.isSucc = false;
				rEntity = new KuaiJieJdPayOrderCreateResponse();
				rEntity.setInfo("QQ钱包验证响应签名失败");
			}
		}else {
			log.info("qq钱包支付请求失败code={},msgDetail={}" ,rspHttpEntity.isSucc, rspHttpEntity.msg);
			rEntity = new KuaiJieJdPayOrderCreateResponse();
			rEntity.setInfo(rspHttpEntity.msg);
		}
		return rEntity;
	}
	/**
	 * QQ钱包参数
	 * @param amount
	 * @param payOrderSn
	 * @return
	 */
	private String getKuaiJiePayCreateParams(String amount, String payOrderSn) {
		StringBuffer params = new StringBuffer();
		TreeMap<String, String> paramsTreeMap = new TreeMap<String, String>();
		paramsTreeMap.put("merchant_no", kuaiJiePayConfig.getMerchant());
		paramsTreeMap.put("merchant_order_no",payOrderSn);
		paramsTreeMap.put("notify_url",kuaiJiePayConfig.getNotifyUrl());
		 SimpleDateFormat sdf =   new SimpleDateFormat("yyyyMMddHHmmss"); 
		paramsTreeMap.put("start_time",sdf.format(new Date()));
		paramsTreeMap.put("trade_amount",amount);
		paramsTreeMap.put("goods_name",payOrderSn);
		paramsTreeMap.put("goods_desc",kuaiJiePayConfig.getGoodDesc());
		paramsTreeMap.put("sign_type", "1");
		for(String key:paramsTreeMap.keySet()){
			params.append(key+"="+paramsTreeMap.get(key)+"&");
		}
		params.deleteCharAt(params.length()-1);
		return addParamSignInfo(params.toString());
	}
	
	public BaseResult<RspOrderQueryEntity> queryOrderStatusQQqianBao(String qqQianBaoTradeNo) {
		return commonOrderQueryKuaiJie(qqQianBaoTradeNo,kuaiJiePayConfig.getApiUrl()+kuaiJiePayConfig.getQqQueryPayUrl());
	}

	public BaseResult<RspOrderQueryEntity> queryOrderStatusJd(String jingdongTradeNo) {
		return commonOrderQueryKuaiJie(jingdongTradeNo,kuaiJiePayConfig.getApiUrl()+kuaiJiePayConfig.getJdQueryPayUrl());
	}
	private BaseResult<RspOrderQueryEntity> commonOrderQueryKuaiJie(String tradeNo,String queryUrl){
		log.info("调取快接支付 订单插叙 TradeNo={},queryUrl={}",tradeNo,queryUrl);
		KuaiJiePayOrderQueryResponse rEntity = null;
		//获取拼接参数
		String paramStr = getKuaiJiePayQueryParams(tradeNo);
		RspHttpEntity rspHttpEntity = null;
		rspHttpEntity = HttpUtil.sendMsg(paramStr,queryUrl,false);
		log.info("快接订单查询请求返回信息:{}",rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = (KuaiJiePayOrderQueryResponse) JSON.parseObject(rspHttpEntity.msg,KuaiJiePayOrderQueryResponse.class);
			String responseParamStr=getOrderQueryResponseParamsStr(rEntity);
			String sign=rEntity.getData().getSign();
			boolean rspSign = checkParamSign(responseParamStr,sign);
			if(!rspSign) {
				rspHttpEntity.isSucc = false;
				rEntity = new KuaiJiePayOrderQueryResponse();
				rEntity.setInfo("QQ钱包验证响应签名失败");
			}
			String resultyCode="Process";
			if("1".equals(rEntity.getData().getStatus())){
				resultyCode="Success";
			}else if("2".equals(rEntity.getData().getStatus())){
				resultyCode="Fail";
			}
			RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
			rspOrderQueryEntity.setResult_code(resultyCode);
			rspOrderQueryEntity.setTrade_no(rEntity.getData().getTrade_no());
			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_KUAIJIE_PAY);
			return ResultGenerator.genSuccessResult("succ",rspOrderQueryEntity);
		}else {
			log.info("qq钱包支付请求失败code={},msgDetail={}" ,rspHttpEntity.isSucc, rspHttpEntity.msg);
			rEntity = new KuaiJiePayOrderQueryResponse();
			rEntity.setInfo(rspHttpEntity.msg);
		}
		return ResultGenerator.genFailResult();
	}

	private String getOrderQueryResponseParamsStr(KuaiJiePayOrderQueryResponse rEntity) {
		StringBuffer params = new StringBuffer();
		TreeMap<String, String> paramsTreeMap = new TreeMap<String, String>();
		paramsTreeMap.put("status", rEntity.getData().getStatus());
		paramsTreeMap.put("trade_no",rEntity.getData().getTrade_no());
		paramsTreeMap.put("amount",rEntity.getData().getAmount());
		paramsTreeMap.put("merchant_no",rEntity.getData().getMerchant_no());
		paramsTreeMap.put("pay_channel",rEntity.getData().getPay_channel());
		paramsTreeMap.put("payment_time",rEntity.getData().getPayment_time());
		for(String key:paramsTreeMap.keySet()){
			params.append(key+"="+paramsTreeMap.get(key)+"&");
		}
		params.deleteCharAt(params.length()-1);
		log.info("快接查询返回的参数字符串={}",params.toString());
		return params.toString();
	}

	private String getKuaiJiePayQueryParams(String tradeNo) {
		StringBuffer params = new StringBuffer();
		TreeMap<String, String> paramsTreeMap = new TreeMap<String, String>();
		paramsTreeMap.put("merchant_no", kuaiJiePayConfig.getMerchant());
		paramsTreeMap.put("trade_no",tradeNo);
		paramsTreeMap.put("sign_type", "1");
		for(String key:paramsTreeMap.keySet()){
			params.append(key+"="+paramsTreeMap.get(key)+"&");
		}
		params.deleteCharAt(params.length()-1);
		return addParamSignInfo(params.toString());
	}
	private String addParamSignInfo(String paramsStr){
		StringBuffer signParams = new StringBuffer();
		signParams.append(paramsStr+"&key="+kuaiJiePayConfig.getSecret());
		String sign = MD5Utils.MD5(signParams.toString());
		paramsStr = paramsStr+"&sign="+sign;
		log.info("快接支付 signParam={},sign={}",signParams.toString(),sign);
		return paramsStr;
	}
}
