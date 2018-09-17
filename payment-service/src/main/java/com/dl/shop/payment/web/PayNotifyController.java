package com.dl.shop.payment.web;

import io.swagger.annotations.ApiOperation;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.base.util.JSONHelper;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.util.TXScanPay;
import com.dl.shop.payment.pay.yifutong.entity.RespYFTnotifyEntity;
import com.dl.shop.payment.pay.yifutong.util.PayYFTUtil;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;

@Controller
@RequestMapping("/payment/notify")
@Slf4j
public class PayNotifyController {
	@Resource
	private PayMentService paymentService;

	@Resource
	private PayLogService payLogService;

	@Resource
	private PayYFTUtil payYFTUtil;

	@Resource
	private TXScanPay txScanPay;

	@ApiOperation(value = "易富通支付回调")
	@PostMapping("/YFTNotify")
	@ResponseBody
	public void payNotify(RespYFTnotifyEntity yftNotify, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("易富通支付通知消息yftNotify={}", JSONHelper.bean2json(yftNotify));
		// 打印先锋支付返回值
		log.info("服务器端通知-接收到易富通支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info(key + "-------------" + values[0]);
		}
		String payOrderfSn = yftNotify.getOrderCode();
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.error("易富通支付返回payOrderSn is null");
			writeSuccess(response);
			return;
		}
		Boolean checkSignIsTure = payYFTUtil.booleanCheckSign(yftNotify);
		if (!checkSignIsTure) {
			log.error("易富通支付回调通知验签失败payOrderSn={}", payOrderfSn);
			// writeSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSign(payOrderfSn);
		if (payLog == null) {
			log.info("[payNotify]" + "该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeSuccess(response);
			return;
		}
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code("2");
		rspOrderEntikty.setTrade_no(yftNotify.getOrderCode());
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_YIFUTONG);
		rspOrderEntikty.setTrade_status("2");
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeSuccess(response);
		return;
	}

	private void writeSuccess(HttpServletResponse response) {
		// 通知先锋成功
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write("SUCCESS");
			writer.flush();
			log.error("易富通支付回调写成功内容");
		} catch (Exception e) {
			log.error("易富通支付回调通知响应异常", e);
		}

	}

	private void successCallback(HttpServletResponse response) {
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write("success");
			writer.flush();
			log.error("天下支付回调响应成功,返回信息成功,返回信息为'success'");
		} catch (Exception e) {
			log.error("天下通支付回调响应异常", e);
		}

	}

	@ApiOperation(value = "天下支付回调")
	@PostMapping("/TXCallback")
	@ResponseBody
	public void TXPayCallback(@RequestBody TXScanRequestCallback callback, HttpServletResponse response) throws UnsupportedEncodingException {
		log.info("接收到天下支付返回报文：={}", callback);
		// 验证签名
		String payOrderId = callback.getOrderId();
		if (StringUtils.isEmpty(payOrderId)) {
			log.error("天下支付返回订单号为空");
			successCallback(response);
			return;
		}
		Boolean checkSignIsTure = txScanPay.checkSign(callback);
		if (!checkSignIsTure) {
			log.error("天下支付回调通知验签失败payOrderSn={}", payOrderId);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSign(payOrderId);
		if (payLog == null) {
			log.info("[payNotify]" + "该支付订单查询失败 payOrderSn:" + payOrderId);
			successCallback(response);
			return;
		}
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("[payNotify] payOrderSn={}订单已支付...", payOrderId);
			successCallback(response);
			return;
		}
		String payCode = payLog.getPayCode();
		int payType = payLog.getPayType();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code("01");
		rspOrderEntikty.setTrade_no(payOrderId);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_TIANXIA_SCAN);
		rspOrderEntikty.setTrade_status("01");
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		successCallback(response);
		return;
	}

}
