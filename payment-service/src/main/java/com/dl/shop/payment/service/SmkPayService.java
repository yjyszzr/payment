package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.smkpay.common.SmkAgent;
import com.dl.shop.payment.pay.smkpay.common.SmkPay;
import com.dl.shop.payment.pay.smkpay.util.SmkParam;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmkPayService {
	private final static Logger logger = LoggerFactory.getLogger(SmkPayService.class);
	@Resource
	private SmkParam smkParam;
	@Resource
	private SmkPay smkPay;
	@Resource
	private SmkAgent smkAgent;
	/**
	 * 银行卡签约--暂时不用
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> bqpSign() throws Exception {
		Map<String,String> requestMap = new HashMap<String,String>();
//		requestMap.put("merCustId", value);
//		requestMap.put("name", value);
//		requestMap.put("certType", value);
//		requestMap.put("certNo", value);
//		requestMap.put("phone", value);
//		requestMap.put("cardType", value);
//		requestMap.put("cardNo", value);
//		requestMap.put("verCode", value);
//		requestMap.put("token", value);
//		requestMap.put("phoneToken", value);
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		return smkPay.bqpSign(requestMap);
	}
	
	/**
	 * 银行卡解约--暂时不用
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> bqpUnSign() throws Exception{
		Map<String,String> requestMap = new HashMap<String,String>();
//		requestMap.put("merCustId", value);
//		requestMap.put("shortCardNo", value);
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		return smkPay.bqpUnSign(requestMap);
	}
	/**
	 * 银行卡签约并支付
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> bqpSignAndPay(Map<String,String> requestMap) throws Exception {
		requestMap.put("merCustId", requestMap.get("merCustId"));//用户ID
		requestMap.put("name", requestMap.get("name"));
		requestMap.put("certType", requestMap.get("certType"));
		requestMap.put("certNo", requestMap.get("certNo"));
		requestMap.put("phone", requestMap.get("phone"));
		requestMap.put("cardType", requestMap.get("cardType"));
		requestMap.put("cardNo", requestMap.get("cardNo"));
		requestMap.put("orderNo", requestMap.get("orderNo"));
		requestMap.put("amount", requestMap.get("amount"));
		requestMap.put("verCode", requestMap.get("verCode"));
		requestMap.put("token", requestMap.get("token"));
		requestMap.put("phoneToken", requestMap.get("phoneToken"));
		requestMap.put("goods", "充值");
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		return smkPay.bqpSignAndPay(requestMap);
	}
	
	/**
	 * 支付
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> bqpPay(Map<String,String> requestMap) throws Exception{
		requestMap.put("merCustId", requestMap.get("merCustId"));//用户ID
		requestMap.put("orderNo", requestMap.get("orderNo"));
		requestMap.put("shortCardNo", requestMap.get("shortCardNo"));
		requestMap.put("amount", requestMap.get("amount"));
		requestMap.put("verCode", requestMap.get("verCode"));
		requestMap.put("phoneToken", requestMap.get("phoneToken"));
		requestMap.put("goods", "充值");
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		Map<String,String> result = smkPay.bqpPay(requestMap);
		return result;
	}
	
	
	/**
	 * 支付结果查询
	 * @param orderNo-订单编号
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> bqpPayQuery(String orderNo) throws Exception {
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put("orderNo", orderNo);
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		return smkPay.bqpPayQuery(requestMap);
	}
	/**
	 * 单笔实时代付
	 * @param orderNo-订单号
	 * @param amount-提现金额
	 * @param accName-收款人
	 * @param accNo-收款账户
	 * @param count-重调接口次数
	 * @return
	 * @throws Exception
	 */
	public RspSingleCashEntity agentSinglePay(String orderNo,String amount,String accName,String accNo,int count) throws Exception {
		RspSingleCashEntity rspEntity = new RspSingleCashEntity();
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put("orderNo", orderNo);
		requestMap.put("actacn", accNo);
		requestMap.put("toname", accName);
		requestMap.put("amount", amount);
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		requestMap.put("notifyUrl", smkParam.getNotifyUrl());
		Map<String,String> resultMap = smkAgent.agentSinglePay(requestMap);
		if(resultMap!=null && resultMap.get("status")!=null) {
			rspEntity.resMessage = resultMap.get("statusDesc");
			String status = resultMap.get("status");
			if(Integer.valueOf(status)==1) {
				rspEntity.status = "S";
			}else if(Integer.valueOf(status)==2) {
				rspEntity.status = "F";
			}else if(Integer.valueOf(status)==4) {
				if(count<3) {
					agentSinglePay(orderNo,amount,accName,accNo,count++);
				}else {
					rspEntity.status = "S";
					rspEntity.resMessage = "提现处理中！";
				}
			}
			
			
		}else {
			rspEntity.status = "F";
			rspEntity.resMessage = "惠民代付接口内部错误！";
		}
		return rspEntity;
	}

	/**
	 * 账户余额查询
	 * @return
	 * @throws Exception
	 */
	public double agentQueryBalance() throws Exception{
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put("merCode", smkParam.getMerCode());
		requestMap.put("appId", smkParam.getAppId());
		requestMap.put("certPath", smkParam.getCertPath());
		requestMap.put("certPwd", smkParam.getCertPwd());
		requestMap.put("requestUrl", smkParam.getRequestUrl());
		requestMap.put("vertifyPublicKey", smkParam.getVertifyPublicKey());
		Map<String,String> resultMap = smkAgent.agentQueryBalance(requestMap);
		if(resultMap!=null && resultMap.get("accBalance")!=null) {
			BigDecimal accBalance = BigDecimal.valueOf(Double.valueOf(resultMap.get("accBalance")));
			double accBalanceStr = accBalance.divide(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN).doubleValue();// 金额分转换成元
			return accBalanceStr;
		}
		return 0d;

	}
	//校验最小金额
	public boolean checkMinAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("6".equals(paytype) && paid<500) {
			return true;
		}
		if("7".equals(paytype) && paid<10) {
			return true;
		}
		if(paid<1) {
			return true;
		}
		return false;
	}
	//校验最大金额
	public boolean checkMaxAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("7".equals(paytype) && paid>300) {
			return true;
		}
		if(paid>10000) {
			return true;
		}
		return false;
	}
}
