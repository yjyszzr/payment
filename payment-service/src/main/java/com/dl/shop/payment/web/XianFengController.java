package com.dl.shop.payment.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.shop.payment.dto.PayLogDTO;
import com.dl.shop.payment.dto.PayXianFengApplyDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.XianFengService;

import io.swagger.annotations.ApiOperation;

/**
 * 先锋支付
 * @date 2018.06.08
 */
@Controller
@RequestMapping("/payment/xianfeng")
public class XianFengController {
	private final static Logger logger = LoggerFactory.getLogger(XianFengController.class);
	
	@Resource
	private PayLogService payLogService;
	@Resource
	private XianFengService xianFengService;
	
	@ApiOperation(value="先锋支付回调")
	@PostMapping("/notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		
	}
	
	@ApiOperation(value="先锋支付请求")
	@PostMapping("/app")
	public BaseResult<Object> appPay(XianFengPayParam payParam) {
		int userId = SessionUtil.getUserId();
		if(userId <= 0) {
			return ResultGenerator.genFailResult("请登录");
		}
		return xianFengService.appPay(payParam.getPayLogId());
	}
	
	
	/***
	 * 先锋订单查询
	 * @param payParam
	 * @return
	 */
	@ApiOperation(value="先锋支付信息查询")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<String> query(@RequestBody XianFengPayParam payParam) {
		logger.info("[query]" +" payParams:" + payParam.getPayLogId());
		int payLogId = payParam.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			return ResultGenerator.genFailResult("查询支付信息失败");
		}
		int isPaid = payLog.getIsPaid();
		int payType = payLog.getPayType();
		String payOrderSn = payLog.getPayOrderSn();
		if(isPaid == 1) {
			logger.info("[query]" + " 订单:" + payLogId +" 已支付" + " payType:" + payType + " payOrderSn:" + payOrderSn);
			if(payType == 0) {
				return ResultGenerator.genSuccessResult("订单已支付");
			}else {
				return ResultGenerator.genSuccessResult("充值成功");				
			}
		}
		BaseResult<String> baseResult = xianFengService.query(payLog,payOrderSn);
		if(baseResult == null) {
			return ResultGenerator.genFailResult("先锋查询异常");
		}
		return baseResult;
	}
}
