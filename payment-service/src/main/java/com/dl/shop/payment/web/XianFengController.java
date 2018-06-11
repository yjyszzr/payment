package com.dl.shop.payment.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiOperation;

/**
 * 先锋支付
 * @date 2018.06.08
 */
@Controller
@RequestMapping("/payment/xianfeng")
public class XianFengController {
	
	@ApiOperation(value="先锋支付回调")
	@PostMapping("notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		
	}
}
