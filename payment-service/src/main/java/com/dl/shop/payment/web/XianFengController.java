package com.dl.shop.payment.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.shop.payment.dto.PayXianFengApplyDTO;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.dl.shop.payment.service.XianFengService;

import io.swagger.annotations.ApiOperation;

/**
 * 先锋支付
 * @date 2018.06.08
 */
@Controller
@RequestMapping("/payment/xianfeng")
public class XianFengController {
	
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
}
