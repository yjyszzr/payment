package com.dl.shop.payment.pay.youbei.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.dl.shop.payment.web.PaymentController;

@Component
public class PayUBeyUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Resource
	private ConfigerUBeyPay cfgPay;
	
	/**
	 * 请求支付
	 * @throws Exception 
	 */
	public final FormUBeyEntity getUBeyPayUrl(String amount,String orderId,String banktype)  {
		logger.info("getYBPayUrl调取优贝科技支付orderId={},amount={}",orderId,amount);
		if("true".equals(cfgPay.getDEBUG())) {
			amount = "0.01";
		}
		try {
			FormUBeyEntity rEntity = null;
			JSONObject jsonYB = new JSONObject(new TreeMap<String, Object>());
			jsonYB.put("account", cfgPay.getAPP_ACCOUNT());
			jsonYB.put("amount", amount);
			jsonYB.put("banktype", banktype);
			jsonYB.put("callback_url",cfgPay.getCallbackUrl());
			jsonYB.put("notify_url", cfgPay.getNotifyUrl());
			jsonYB.put("orderId", orderId);
			jsonYB.put("type", "kozl");
			//加密
			JSONObject dataJson = getDataAndSign(jsonYB.toString());
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
	 * 请求支付
	 * @throws Exception 
	 */
	public BaseResult<RspOrderQueryEntity> queryPayResult(String payCode, String orderNo) {
		logger.info("queryPayResult调取优贝查询订单支付结果orderNo={}",orderNo);
		RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
		RespUBeyEntity rEntity = null;
		RspHttpEntity rspHttpEntity = null;
		JSONObject treeMap = new JSONObject(new TreeMap<String, Object>());
		treeMap.put("account", cfgPay.getAPP_ACCOUNT());
		treeMap.put("orderId", orderNo);
		treeMap.put("type", "TenQuery");
		JSONObject dataJson = getDataAndSign(treeMap.toString());
		rspHttpEntity = HttpUtil.sendMsg(dataJson.toString(),cfgPay.getQUERY_URL(),false);
		logger.info("优贝查询请求返回信息:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			JSONObject resultJSON = JSON.parseObject(rspHttpEntity.msg);
			String data = checkDataSign(resultJSON);
			if(data==null) {
				return ResultGenerator.genFailResult("查询优贝支付返回数据验签失败[" + resultJSON + "]");
			}
			rEntity = JSONObject.parseObject(data, RespUBeyEntity.class);
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
			 jsonPost.put("date", Base64Utils.encode(data));
			 jsonPost.put("signature", Base64Utils.encode(sign));
			 return jsonPost;
		} catch (Exception e) {
			logger.info("优贝科技支付参数加密异常text={}",text,e);
			return null;
		}
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public String checkDataSign(JSONObject resultJSON) {
		
		try {
			String dataMi = resultJSON.getString("data");
			String signature = resultJSON.getString("signature");
			RSAPublicKey publicKey = (RSAPublicKey) RSAUtil.loadPublicKey(cfgPay.getPUBLIC_KEY());
			RSAPrivateKey privateKey=(RSAPrivateKey) RSAUtil.loadPrivateKey(cfgPay.getPRIVATE_KEY());
			byte[] dataMing = RSAUtil.decryptData(Base64Utils.decode(dataMi), privateKey);
			boolean sign = RSAUtil.publicsign( new String(dataMing), Base64.decode(signature),publicKey);
			if(sign) {
				return new String(dataMing);
			}
			return null;
		} catch (Exception e) {
			logger.info("优贝科技支付参数验签异常resultJSON={}",resultJSON,e);
			return null;
		}		
		
	}
	
//	public static void main(String[] args) throws Exception {
////		JSONObject jsonYB = new JSONObject(new TreeMap<String, Object>());
////		jsonYB.put("account", "DE18910216040");
////		jsonYB.put("amount", "1000");
////		jsonYB.put("banktype", "01000000");
////		jsonYB.put("callback_url",URLEncoder.encode("http://39.106.18.39:9805/static/payCallBack/app_payCallback.html","utf-8"));
////		jsonYB.put("notify_url", URLEncoder.encode("http://39.106.18.39:8765/api/payment/payment/notify/YFTNotify","utf-8"));
////		jsonYB.put("orderId", "1234512345123451234512345");
////		jsonYB.put("type", "kozl");
//		JSONObject treeMap = new JSONObject(new TreeMap<String, Object>());
//		treeMap.put("account", "DE18910216040");
//		treeMap.put("orderId", "hbpay1537447694567320965");
//		treeMap.put("type", "TenQuery");
//		String key1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmbMRYdd3Ob2c2FRdU04Pm0FGOxpCjAuJSM53FRryIuQMD4u54eALtHsDaOJ7sqpnEUT9vggsPoEXb5LEOYhWeaofeYvgOpYZIHYdgGA51zF6JevedcvV/YMeb3rXTuaZKpuiOS8rRfpJ3k5OmXy7G2oyjiv20jLzB5E+HvRtStu3PHpxPKUmMwqbVkWLI5sWhLQqps8UVvgMGf+mEL5UTLlZbJevB5x+au3lNDRdbfUCQ2Bf+1mhYkjeMtb/qTR2X+tONyvmNL0m78r27+r+RFBQuKCWkI20fPSi4bT7BDtshYoqC83K6IFMTDZoJ5n6yoq3mja0tvYiKu+fDN7ILwIDAQAB";
//		String key2 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCpjFLhiPm9AnSt4NaZB0T/uXny5cZGjb62EOSQ428WqK2pXh8KOcL1gK1JEUOn9WsbqzU3+nvy1LIroeAmvO5gi+Bz09deY4y7hSeX/LMYVzSF9BcvezYpwdJZbBy0YhvMqVQxdRTOKrkHDxuOINq5+zwhw3kTmhRvEffykstFMSGRv6wScvlXLDQf0RfcH8nz3OOjmimr+bRdH20hE7z1EbgMhvYc00/Dc9c+33Doqu9JVjlHq2peWEbiDwQaYnoonQnsXefGGnkk792CcnueSAlJJlleylTJHPg7xlUWLeMcHtRBARJTHv145Y/wJ8Dpmbi5zPy4eX5uj03f0+spAgMBAAECggEBAIx0PZiuk0x7w1LjxoLg3c0GxrpslNOPyuQinGZWsolP1QVQfmBHN3WPUn4MOkLicQdrzluAOrwQbUTuBbWxn7wFRDxPysNbrpMNnJ4w7eCFJXKuoaJo7JatBOelQyY/fA4QXDnYZxLfm4X4gvg7yVZ2hPrnZt7RLopQlVmzN4cp8QJ53P8NNu9ZGlrcZZxVh5LvlvnfIFDtb7/J999LMmMaRbFxdqz9wnC9JILzRKqwEKgPS70NJr3fWo6lGqQsIH0YJtLtLIQGf60IhwsMeVqi7wpF9SaF9ka15jKSYYsgDpwHJ11nk4lH+59NWLxAuwEhmqlV0lMGJjGNQUl3ePECgYEA4ZfxgrzKiPOED6/7htyVX+/7PYxuT++QReY5yXXkXUUkiKHSFeHrfYIu+B2t+cWaPDsI0VUg3xMomPvfNRBJ7euUGmUrajAoDekQNshz4WdhoCd0JSo/0k85d9dltPdwkUGFRWE4ekxa0TFmJyThOzNlsf+wWJTD6dSV6etHbnUCgYEAwGaL0IWRxvb96S8mh/cDw3oYEGoQ+htB1WwwmbyqGedUGl09I5KCdpdfeDeWXaacLWKVm66xXJfLt4v3GJtR2aAYpI8HhvCkPEMQRUuw647fYugKsczuOcgLYSfQ04KUQB6AcAwi4Ol2eWWtC0zkm3Wg7YTlqEVWOKic+IIjG2UCgYEA1BRO1JR9ZzBfR4wuz0MSHqXyMJOTpQxXOo9MZiuxq0l0JgrTAQVpqI05cMIeK0DsVZE+IeWAyJYvRVsMJMuSZNxiWEoh/WsWTLV9K7NY1V+mNLTCiKnmY+Vc5mAV2oIATl4lVusl+DtN6XoiScxu/YO9KBBzDWOVpn7XDJjGh30CgYA8XjCSTcaOqiLDDpNFaADbTazNQapv/ytp3pdlNWgd1pJx7z1msuqScS54VuCsI8GvD9anUWm/BrXZsmXmZ52H7g6tW19ePJUbf2NUispJLrvymaH5ZbZ9lMHVSxvfsYH4mFluTrCG51nP/1ILUKYODRtgSymP4amK/4S5CIQhbQKBgGTBrzkKEbwWd6ppVb1Uf6HsPRFMHDR1tGt2R9x6FtLDZTvqZPqptNI3+4KdXt5GmsC0CxXB6A3JdDMh+fAMBDhY49fv+8SXC0gXveOQYm//uTTHQgG0XUqajmgVdIegv0ANYaG5sgSCcHNvHb+oopdp56OPynWgPP7CNmRDy15C";
//		String textYBW = treeMap.toString();
//		RSAPublicKey publicKey = null;
//		RSAPrivateKey privateKey = null;
//		try {
//			 publicKey = (RSAPublicKey) RSAUtil.loadPublicKey(key1);
//			 privateKey=(RSAPrivateKey) RSAUtil.loadPrivateKey(key2);
//		} catch (Exception e) {
//			logger.info("调取优贝科技支付加载密钥失败publicKey={},privateKey={}",publicKey,privateKey);
//		}
//		byte[] data=RSAUtil.encryptData((textYBW).getBytes(), publicKey);
//		byte[] sign = RSAUtil.privatesign((textYBW),privateKey);
//		
//		JSONObject jsonPost = new JSONObject(new TreeMap<String, Object>());
//		jsonPost.put("data", Base64Utils.encode(data));
//		jsonPost.put("signature", Base64Utils.encode(sign));
//		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(jsonPost.toString(),"http://www.ddcxpay.com/Platform/query/tentoQuery.do",false);
//		if(rspHttpEntity.isSucc) {
//			JSONObject resultJSON = JSON.parseObject(rspHttpEntity.msg);
//			try {
//				String dataMi = resultJSON.getString("data");
//				String signature = resultJSON.getString("signature");
//				byte[] dataMing = RSAUtil.decryptData(Base64.decode(dataMi), privateKey);
//				boolean signs = RSAUtil.publicsign( new String(dataMing), Base64.decode(signature),publicKey);
//			} catch (Exception e) {
//				logger.info("优贝科技支付参数验签异常resultJSON={}",resultJSON,e);
//			}
//		}
//	}
}
