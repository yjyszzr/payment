package com.dl.shop.payment.pay.smkpay.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.smkpay.util.AESUtil;
import com.dl.shop.payment.pay.smkpay.util.HttpUtil;
import com.dl.shop.payment.pay.smkpay.util.RSAUtil;
import com.dl.shop.payment.pay.smkpay.util.TimeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Function: 代付接口
 */
@Service
public class SmkAgent {
	private final static Logger logger = LoggerFactory.getLogger(SmkAgent.class);
	/**
	 * 单笔实时代付
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> agentSinglePay(Map<String,String> requestMap) throws Exception {
		String reqSeq = setReqSeq();
		String randomKey = setRandomKey();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put("version", "1.0.1");//版本号	必输	1.0.0 为同步接口/1.0.1和1.0.2为异步接口
		messageMap.put("tradeCode", "AP0001");//交易代码	string(8)	必输	AP0001
		messageMap.put("reqSeq", reqSeq);//请求流水号	string(32)	必输	请求流水（唯一）
		messageMap.put("merCode", requestMap.get("merCode"));//商户号	string (12)	必输	商户在市民卡签约的商户代码
		messageMap.put("randomKey", randomKey);//加密随机数	String(32)	必输	对称密钥串
		messageMap.put("channelNo", "004");//渠道号	string (4)	必输	默认004
		messageMap.put("tradeDate", TimeUtil.dateToString(new Date(), TimeUtil.FORMAT_STRING6));//交易日期	string (10)	必输	YYYY-MM-DD
		messageMap.put("tradeTime", TimeUtil.dateToString(new Date(), TimeUtil.FORMAT_STRING7));//交易时间	string (8)	必输	HH:MM:SS
		messageMap.put("orderNo",requestMap.get("orderNo"));//订单号	string (32)	必输	订单号，查询时使用，不超过32位
		messageMap.put("busType", "TOPRV");//业务类型	string (8)	必输	TOPUB：对公，TOPRV：对私
		messageMap.put("toibkn", "");//收款方联行号	string (12)	非必输 对公必输,对私避免填错导致入账失败,不填
		messageMap.put("actacn",  AESUtil.encryptToHex(requestMap.get("actacn"), randomKey));//收款方账号	string (35)	必输	需加密
		messageMap.put("toname",AESUtil.encryptToHex(requestMap.get("toname"), randomKey));//收款方姓名	string (70)	必输	需加密
		messageMap.put("toaddr", "");//非必输    收款方地址
		messageMap.put("tobknm", "");//非必输   收款方开户行名称
		messageMap.put("amount", requestMap.get("amount"));//交易金额	string (12)	必输	单位：元
//		messageMap.put("amount", "1");//交易金额	string (12)	必输	单位：元
		messageMap.put("currency", "CNY");//币种	string (3)	必输	非空3位大写字母、数字，只支持 001或者 CNY
		messageMap.put("remark", "");//备注	string (20)	非必输	格式为{"mobile":"手机号(需加密)","useage":"用途"}
    	messageMap.put("notifyUrl", requestMap.get("notifyUrl"));//服务端通知地址	string	非必输
    	logger.info("SMK提现messageMap="+messageMap);
		String messageMapStr = gson.toJson(messageMap);
		// 请求开放平台数据
		Map<String, String> openMap = new HashMap<String, String>();
		openMap.put("reqSeq", reqSeq);
		openMap.put("appId", requestMap.get("appId"));
		openMap.put("method", "singleAgentPay");
		openMap.put("bizContent", messageMapStr);
		openMap.put("sign_param", "appId,method,bizContent");
		// 加签
		openMap.put("sign", RSAUtil.rsaSignByCert(openMap,requestMap.get("certPath"),requestMap.get("certPwd")));
		String message = gson.toJson(openMap);
		String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
		logger.info("SMK提现resultStr="+respStr);
		JSONObject json = JSON.parseObject(respStr);
		// 验签
		if ("true".equals(json.getString("success"))) {
			Map<String, String> checkMap = new HashMap<String, String>();
			checkMap.put("reqSeq", reqSeq);
			checkMap.put("sign_param", "success,value");
			checkMap.put("success", json.getString("success"));
			checkMap.put("value", json.getString("value"));
			checkMap.put("sign", json.getString("sign"));
			checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
			if(RSAUtil.rsaCheck(checkMap)) {
				Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
				return resultMap;
			}
		}
		return null;
	}

	/**
	 * 账户余额查询
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> agentQueryBalance(Map<String,String> requestMap) throws Exception{
		String reqSeq = setReqSeq();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
		messageMap.put("tradeCode", "MI0001");//交易代码	String(8)	必输	MI0001
		messageMap.put("reqSeq", reqSeq);//请求流水号	String(32)	必输	数字+字母（唯一）
		messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
		messageMap.put("channelNo", "004");//渠道号	String(4)	必输	默认004
		messageMap.put("tradeDate", TimeUtil.dateToString(new Date(), TimeUtil.FORMAT_STRING6));//交易日期	String(10)	必输	YYYY-MM-DD
		messageMap.put("tradeTime", TimeUtil.dateToString(new Date(), TimeUtil.FORMAT_STRING7));//交易时间	String(8)	必输	HH:MM:SS
		String messageMapStr = gson.toJson(messageMap);
		// 请求开放平台数据
		Map<String, String> openMap = new HashMap<String, String>();
		openMap.put("reqSeq", reqSeq);
		openMap.put("appId", requestMap.get("appId"));
		openMap.put("method", "agentQueryBalance");
		openMap.put("bizContent", messageMapStr);
		openMap.put("sign_param", "appId,method,bizContent");
		// 加签
		openMap.put("sign", RSAUtil.rsaSignByCert(openMap,requestMap.get("certPath"),requestMap.get("certPwd")));
		String message = gson.toJson(openMap);
		String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
		JSONObject json = JSON.parseObject(respStr);
		// 验签
		if ("true".equals(json.getString("success"))) {
			Map<String, String> checkMap = new HashMap<String, String>();
			checkMap.put("reqSeq", reqSeq);
			checkMap.put("sign_param", "success,value");
			checkMap.put("success", json.getString("success"));
			checkMap.put("value", json.getString("value"));
			checkMap.put("sign", json.getString("sign"));
			checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
			if(RSAUtil.rsaCheck(checkMap)) {
				Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
				return resultMap;
			}
		}
		return null;
	}
	public String setRandomKey() {
	    int random = (int) (Math.random()*10000);
	    if(random < 10000){
	    	random =random +1000000000;
	    }
	    String randomKey =new SimpleDateFormat("yyyyMMddHHmmssSSSSSSSS").format(new Date()) + random;
	    return randomKey;
	}
	public String setReqSeq() {
		int random = (int) (Math.random()*10000);
	    if(random < 10000){
	    	random =random +1000000;
	    }
	    String reqSeq ="D"+new SimpleDateFormat("yyyyMMddHHmmssSSSSSSSS").format(new Date()) + random;
	    return reqSeq;
	}
}
