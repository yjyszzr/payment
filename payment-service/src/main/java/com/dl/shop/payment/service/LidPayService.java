package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.lidpay.util.LidPayH5Utils;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class LidPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private LidPayH5Utils lidutil;
	/**
	 * 支付宝企业收款
	 * @param savePayLog
	 * @param orderSn
	 * @return
	 */
	public BaseResult<?> getLidPayUrl(PayLog savePayLog, String orderSn,String paytype) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,String> param = new HashMap<>();
		param.put("orderNo", orderSn);
		param.put("total", bigD.toString());
		param.put("payMethod", "6023");//支付业务编号： 6015 微信个人收款； 6023 支付宝企业收款
		param.put("name", paytype);//商品名称
		try {
			param = lidutil.pay(param);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (param != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		} else {
			payBaseResult = ResultGenerator.genFailResult("华移支付返回数据有误");
		}
		return payBaseResult;
	}
	/**
	 * 查询订单状态
	 * @param orderSn
	 * @return
	 */
	public BaseResult<RspOrderQueryEntity> commonOrderQueryLid(String orderSn){
		BaseResult<RspOrderQueryEntity> payBaseResult = null;
		Map<String,String> param = new HashMap<>();
		try {
			param = lidutil.orderQuery(orderSn);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (param != null) {
			RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
			rspOrderQueryEntity.setResult_code(param.get("status"));
			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_LID);
			payBaseResult = ResultGenerator.genSuccessResult("succ", rspOrderQueryEntity);
		} else {
			payBaseResult = ResultGenerator.genFailResult("华移支付返回数据有误");
		} 
		return payBaseResult;
	}
	
	public boolean checkAmount(String payToken) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if(paid<1) {
			return true;
		}
		return false;
	}
}
