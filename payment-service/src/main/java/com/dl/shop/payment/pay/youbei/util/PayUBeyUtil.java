package com.dl.shop.payment.pay.youbei.util;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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
import com.dl.shop.payment.pay.youbei.config.ConfigerUBeyPay;
import com.dl.shop.payment.pay.youbei.entity.FormUBeyEntity;
import com.dl.shop.payment.pay.youbei.entity.RespUBeyEntity;
import com.dl.shop.payment.pay.youbei.entity.RespUBeyRSAEntity;
import com.dl.shop.payment.web.PaymentController;

@Component
public class PayUBeyUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Resource
	private ConfigerUBeyPay cfgPay;
	
	private final String PAY_TYPE="kkwwo";
	/**
	 * 请求支付
	 * @throws Exception 
	 */
	public final FormUBeyEntity getUBeyPayUrl(String amount,String orderId)  {
		logger.info("getUBeyPayUrl调取优贝科技支付orderId={},amount={}",orderId,amount);
		if("true".equals(cfgPay.getDEBUG())) {
			amount = "100";//单位（分）
		}
		try {
			FormUBeyEntity rEntity = new FormUBeyEntity();
			JSONObject jsonYB = new JSONObject(new TreeMap<String, Object>());
			jsonYB.put("account", cfgPay.getAPP_ACCOUNT());
			jsonYB.put("amount", amount);
			jsonYB.put("banktype", "");
			jsonYB.put("callback_url",cfgPay.getCallbackUrl());
			jsonYB.put("notify_url", cfgPay.getNotifyUrl());
			jsonYB.put("orderId", orderId);
			jsonYB.put("type", PAY_TYPE);
 
			logger.info("getUBeyPayUrl加密数据jsonYB={}",jsonYB);
 
			//加密
			JSONObject dataJson = this.getDataAndSign(jsonYB.toString());
			if(dataJson==null) {
				logger.info("调取优贝科技加密参数异常dataJson==null");
				return null;
			}
			rEntity.setUrl(cfgPay.getPAY_URL());
			rEntity.setData(dataJson.getString("data"));
			rEntity.setSignature(dataJson.getString("signature"));
			return rEntity;
		} catch (Exception e) {
			logger.info("优贝科技支付异常",e);
			return null;
		}
	}
	
	/**
	 * 支付查询
	 * @throws Exception 
	 */
	public BaseResult<RspOrderQueryEntity> queryPayResult(String payCode, String orderId) {
		logger.info("queryPayResult调取优贝查询订单支付结果orderId={}",orderId);
		RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
		RespUBeyEntity rEntity = null;
		RspHttpEntity rspHttpEntity = null;
		JSONObject treeMap = new JSONObject(new TreeMap<String, Object>());
		treeMap.put("account", cfgPay.getAPP_ACCOUNT());
		treeMap.put("orderId", orderId);
		treeMap.put("type", "TenQuery");
		JSONObject dataJson = this.getDataAndSign(treeMap.toString());
		rspHttpEntity = HttpUtil.sendMsg(dataJson.toString(),cfgPay.getQUERY_URL(),false);
		logger.info("优贝查询请求返回信息:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			RespUBeyRSAEntity rsa = JSON.parseObject(rspHttpEntity.msg,RespUBeyRSAEntity.class);
			String data = this.checkDataSign(rsa);
			if(data==null) {
				return ResultGenerator.genFailResult("查询优贝支付返回数据验签失败[" + rEntity.getMessage() + "]");
			}
			rEntity = JSONObject.parseObject(data, RespUBeyEntity.class);
			logger.info("优贝查询详细参数RespUBeyEntity={}" ,rEntity);
			if(!rEntity.getState().equals("61")) {
				return ResultGenerator.genFailResult("查询优贝支付查询失败[" + rEntity.getMessage() + "]");
			}
			rspOrderQueryEntity.setResult_code(rEntity.getOrderId_state());
			rspOrderQueryEntity.setPayCode(payCode);
			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_UBEY);
			rspOrderQueryEntity.setTrade_no("");
			return ResultGenerator.genSuccessResult("succ",rspOrderQueryEntity);
		}else {
			return ResultGenerator.genFailResult("查询优贝支付失败[" + rspHttpEntity.msg + "]");
		}
	}
	
	/**
	 * 参数加密与签名
	 * @param treeMap
	 * @return
	 */
	public JSONObject getDataAndSign(String text) {
		try {
			RSAPublicKey publicKey = null;
			RSAPrivateKey privateKey = null;
			 publicKey = (RSAPublicKey) RSAUtil.loadPublicKey(cfgPay.getPUBLIC_KEY());
			 privateKey=(RSAPrivateKey) RSAUtil.loadPrivateKey(cfgPay.getPRIVATE_KEY());
			 byte[] data=RSAUtil.encryptData(text.getBytes(), publicKey);
			 byte[] sign = RSAUtil.privatesign((text),privateKey);
			 JSONObject jsonPost = new JSONObject();
			 jsonPost.put("data", Base64Utils.encode(data));
			 jsonPost.put("signature", Base64Utils.encode(sign));
			 return jsonPost;
		} catch (Exception e) {
			logger.info("优贝科技支付参数加密异常text={}",text,e);
			return null;
		}
	}
	
	/**
	 * 签名校验
	 * @param args
	 * @throws Exception
	 */
	public String checkDataSign(RespUBeyRSAEntity rsa) {
		
		try {
			String dataMi = rsa.getData();
			String signature = rsa.getSignature();
			RSAPublicKey publicKey = (RSAPublicKey) RSAUtil.loadPublicKey(cfgPay.getPUBLIC_KEY());
			RSAPrivateKey privateKey=(RSAPrivateKey) RSAUtil.loadPrivateKey(cfgPay.getPRIVATE_KEY());
			byte[] dataMing = RSAUtil.decryptData(Base64Utils.decode(dataMi), privateKey);
			boolean sign = RSAUtil.publicsign( new String(dataMing), Base64.decode(signature),publicKey);
			if(sign) {
				return new String(dataMing);
			}
			return null;
		} catch (Exception e) {
			logger.info("优贝科技支付参数验签异常",e);
			return null;
		}		
		
	}
	
}
