package com.dl.shop.payment.web;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SessionUtil;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.jhpay.util.XmlUtils;
import com.dl.shop.payment.pay.kuaijie.util.KuaiJiePayUtil;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback.TXCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.util.TXScanPay;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.yifutong.entity.RespYFTnotifyEntity;
import com.dl.shop.payment.pay.yifutong.util.PayYFTUtil;
import com.dl.shop.payment.pay.youbei.entity.RespUBeyNotifyEntity;
import com.dl.shop.payment.pay.youbei.entity.RespUBeyRSAEntity;
import com.dl.shop.payment.pay.youbei.util.PayUBeyUtil;
import com.dl.shop.payment.service.APayService;
import com.dl.shop.payment.service.CashService;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONUtils;

@Controller
@RequestMapping("/payment/notify")
@Slf4j
public class PayNotifyController {
	@Resource
	private PayMentService paymentService;

	@Resource
	private PayLogService payLogService;
	
	@Resource
	private APayService aPayService;
	
	@Resource
	private PayYFTUtil payYFTUtil;
	
	@Resource
	private PayUBeyUtil payUBeyUtil;

	@Resource
	private KuaiJiePayUtil kuaiJiePayUtil;
	@Resource
	private TXScanPay txScanPay;
	@Resource
	private CashService cashService;
	
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
			writeUppercaseSuccess(response);
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
			writeUppercaseSuccess(response);
			return;
		}
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeUppercaseSuccess(response);
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
		writeUppercaseSuccess(response);
		return;
	}

	@ApiOperation(value = "快接支付支付回调")
	@PostMapping("/KJPayNotify")
	@ResponseBody
	public void KJPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("快接支付通知消息yftNotify={}", parameters);
		Map<String,String> realMap = new HashMap<String, String>();
		// 打印先锋支付返回值
		log.info("快接支付服务器端通知-接收到快接支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info(key + "-------------" + values[0]);
			realMap.put(key, values[0]);
		}
		String payOrderfSn = realMap.get("merchant_order_no");
		String status=realMap.get("status");
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.error("快接支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
//		Boolean checkSignIsTure = kuaiJiePayUtil.booleanCheckSign(kuaiJiePayNotifyEntity);
//		if (!checkSignIsTure) {
//			log.error("快接支付回调通知验签失败payOrderSn={}", payOrderfSn);
//			return;
//		}
		PayLog payLog = payLogService.findPayLogByOrderSign(payOrderfSn);
		if (payLog == null) {
			log.info("快接支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("快接支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
//		rspOrderEntikty.setTrade_no(kuaiJiePayNotifyEntity.getTrade_no());
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_KUAIJIE_PAY);
		rspOrderEntikty.setTrade_status(status);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}

	private void writeUppercaseSuccess(HttpServletResponse response) {
		// 通知先锋成功
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write("SUCCESS");
			writer.flush();
			log.error("回调响应成功,返回信息成功,返回信息为'SUCCESS'");
		} catch (Exception e) {
			log.error("回调通知响应异常", e);
		}

	}

	/**
	 * 回写小写字母success
	 * 
	 * @param response
	 */
	private void writeLowerSuccess(HttpServletResponse response) {
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.write("success");
			writer.flush();
			log.error("回调响应成功,返回信息成功,返回信息为'success'");
		} catch (Exception e) {
			log.error("支付回调响应异常", e);
		}

	}

	@ApiOperation(value = "天下支付回调")
	@PostMapping("/TXCallBack")
	@ResponseBody
	public void TXPayCallBack(@RequestBody TXScanRequestCallback callback, HttpServletResponse response) throws UnsupportedEncodingException {
		log.info("接收到天下支付返回报文：={}", callback);
		// 验证签名
		TXCallback txcallbackBody = callback.getREP_BODY();
		String payOrderId = txcallbackBody.getOrderId();
		if (StringUtils.isEmpty(payOrderId)) {
			log.error("天下支付返回订单号为空");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSign(payOrderId);
		if (payLog == null) {
			log.info("[payNotify]" + "该支付订单查询失败 payOrderSn:" + payOrderId);
			writeLowerSuccess(response);
			return;
		}
		String[] merchentArr = payLog.getPayCode().split("_");
		Boolean checkSignIsTure = txScanPay.checkSign(callback, merchentArr[merchentArr.length - 1]);
		if (!checkSignIsTure) {
			log.error("天下支付回调通知验签失败payOrderSn={}", payOrderId);
			return;
		}
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("[payNotify] payOrderSn={}订单已支付...", payOrderId);
			writeLowerSuccess(response);
			return;
		}

		String payCode = payLog.getPayCode();
		int payType = payLog.getPayType();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(txcallbackBody.getOrderState());
		rspOrderEntikty.setTrade_no(payOrderId);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_TIANXIA_SCAN);
		rspOrderEntikty.setTrade_status(txcallbackBody.getOrderState());
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	
	@ApiOperation(value = "优贝支付回调")
	@PostMapping("/UbeyCallBack")
	@ResponseBody
	public void payNotifyUbey(RespUBeyRSAEntity rsa, HttpServletRequest request, HttpServletResponse response) {
		log.info("Ubey支付回调返回报文：={}", rsa);
		String data = payUBeyUtil.checkDataSign(rsa);
		log.info("Ubey支付回调解密报文：={}", data);
		if(data==null) {
			log.error("优贝支付回调通知验签失败RespUBeyRSAEntity={}", rsa);
		}
		RespUBeyNotifyEntity respEntity = new RespUBeyNotifyEntity();
		respEntity = JSONObject.parseObject(data, RespUBeyNotifyEntity.class);
		if(!respEntity.getRespCode().equals("0000")) {
			String payOrderfSn = respEntity.getOrderId();
			if (StringUtils.isEmpty(payOrderfSn)) {
				log.error("优贝支付返回payOrderSn is null");
				writeUppercaseSuccess(response);
				return;
			}
			PayLog payLog = payLogService.findPayLogByOrderSign(payOrderfSn);
			if (payLog == null) {
				log.info("[payNotify]" + "该支付订单查询失败 payLogSn:" + payOrderfSn);
				writeUppercaseSuccess(response);
				return;
			}
			int isPaid = payLog.getIsPaid();
			if (isPaid == 1) {
				log.info("[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
				writeUppercaseSuccess(response);
				return;
			}
			int payType = payLog.getPayType();
			String payCode = payLog.getPayCode();
			RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
			rspOrderEntikty.setResult_code("3");
			rspOrderEntikty.setTrade_no("");
			rspOrderEntikty.setPayCode(payCode);
			rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_UBEY);
			rspOrderEntikty.setTrade_status("3");
			if (payType == 0) {
				paymentService.orderOptions(payLog, rspOrderEntikty);
			} else {
				paymentService.rechargeOptions(payLog, rspOrderEntikty);
			}
			writeUppercaseSuccess(response);
		}
	}
	
	@ApiOperation(value = "Lid支付回调")
	@PostMapping("/LidPayNotify")
	@ResponseBody
	public void LidPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("LidPayNotify()华移支付通知消息LidPayNotify={}", parameters);
		Map<String,String> realMap = new HashMap<String, String>();
		// 打印华移支付返回值
		log.info("LidPayNotify()华移支付服务器端通知-接收到华移支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info("LidPayNotify()*********"+key + "-------------" + values[0]);
			realMap.put(key, values[0]);
		}
		log.info("LidPayNotify()返回报文*********"+JSONUtils.valueToString(realMap));
		String payOrderfSn = realMap.get("orderNo");
		String status=realMap.get("code");
		log.info("LidPayNotify()返回报文*********"+payOrderfSn+"&&&"+status);
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.info("LidPayNotify()华移支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
		if (payLog == null) {
			log.info("LidPayNotify()华移支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("LidPayNotify()华移支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_LID);
		rspOrderEntikty.setTrade_status(status);
		log.info("LidPayNotify()返回报文*********"+payType);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	@ApiOperation(value = "艾支付回调")
	@PostMapping("/APayNotify")
	@ResponseBody
	public void APayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("APayNotify()艾支付通知消息LidPayNotify={}", parameters);
		Map<String,Object> realMap = new HashMap<String, Object>();
		// 打印华移支付返回值
		log.info("APayNotify()艾支付服务器端通知-接收到华移支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info("APayNotify()*********"+key + "-------------" + values[0]);
			realMap.put(key, values[0]);
		}
//		aPayService.getSign(realMap); ///签名校验
		log.info("APayNotify()返回报文*********"+JSONUtils.valueToString(realMap));
		String payOrderfSn = realMap.get("partner_order")==null?"":realMap.get("partner_order").toString();
		String status=realMap.get("code")==null?"":realMap.get("code").toString();
		log.info("APayNotify()返回报文*********"+payOrderfSn+"&&&"+status);
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.info("APayNotify()艾支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
		if (payLog == null) {
			log.info("APayNotify()艾支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("APayNotify()艾支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_APAY);
		rspOrderEntikty.setTrade_status(status);
		log.info("APayNotify()返回报文*********"+payType);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	
	@ApiOperation(value = "Q多多支付回调")
	@PostMapping("/RkPayNotify")
	@ResponseBody
	public void RkPayNotify(@RequestBody JSONObject jsonStr,HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<String,Object> realMap = new HashMap<>();
		if(jsonStr!=null) {
			realMap = (Map<String, Object>) com.alibaba.druid.support.json.JSONUtils.parse(jsonStr.toJSONString());
		}
		
		log.info("RkPayNotify()返回报文*********"+jsonStr);
		String payOrderfSn = realMap.get("ds_trade_no")==null?"":realMap.get("ds_trade_no").toString();
		String status=realMap.get("status")==null?"":realMap.get("status").toString();
		log.info("RkPayNotify()返回报文*********"+payOrderfSn+"&&&"+status);
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.info("RkPayNotify()Q多多支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
		if (payLog == null) {
			log.info("RkPayNotify()Q多多支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("RkPayNotify()Q多多支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_RKPAY);
		rspOrderEntikty.setTrade_status(status);
		log.info("RkPayNotify()返回报文*********"+payType);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	
	
	@ApiOperation(value = "Q多多支付完成回调")
	@GetMapping("/getRkPayNotify")
	@ResponseBody
	public void getRkPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		log.info("getRkPayNotify返回成功");
		writeLowerSuccess(response);
		return;
	}
	
	@ApiOperation(value = "聚合支付回调")
	@PostMapping("/JhPayNotify")
	@ResponseBody
	public void JhPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		log.info("ShPayNotify()返回报文*********");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<String,String> realMap = new HashMap<>();
		
		realMap=XmlUtils.toMap(request);
		
//		log.info("ShPayNotify()返回报文*********"+jsonStr);
		String payOrderfSn = realMap.get("out_trade_no")==null?"":realMap.get("out_trade_no").toString();
//		String status=realMap.get("status")==null?"":realMap.get("status").toString();
//		String result_code=realMap.get("result_code")==null?"":realMap.get("result_code").toString();
		String pay_result=realMap.get("pay_result")==null?"":realMap.get("pay_result").toString();
		log.info("ShPayNotify()返回报文***realMap******"+JSONUtils.valueToString(realMap));
		log.info("ShPayNotify()返回报文*********"+payOrderfSn+"&&&"+pay_result);
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.info("ShPayNotify()Q多多支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
		if (payLog == null) {
			log.info("ShPayNotify()Q多多支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("ShPayNotify()Q多多支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(pay_result);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_JHPAY);
		rspOrderEntikty.setTrade_status(pay_result);
		log.info("ShPayNotify()返回报文*********"+payType);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	
	@ApiOperation(value = "云闪付支付回调")
	@PostMapping("/YunPayNotify")
	@ResponseBody
	public void YunPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("YunPayNotify()云闪付支付通知消息LidPayNotify={}", parameters);
		Map<String,Object> realMap = new HashMap<String, Object>();
		// 打印华移支付返回值
		log.info("YunPayNotify()云闪付支付服务器端通知-接收到云闪付支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info("YunPayNotify()*********"+key + "-------------" + values[0]);
			realMap.put(key, values[0]);
		}
//		aPayService.getSign(realMap); ///签名校验
		log.info("YunPayNotify()返回报文*********"+JSONUtils.valueToString(realMap));
		String payOrderfSn = realMap.get("sp_billno")==null?"":realMap.get("sp_billno").toString();
		String status=realMap.get("tran_state")==null?"":realMap.get("tran_state").toString();
		log.info("YunPayNotify()返回报文*********"+payOrderfSn+"&&&"+status);
		if (StringUtils.isEmpty(payOrderfSn)) {
			log.info("YunPayNotify()云闪付支付返回payOrderSn is null");
			writeLowerSuccess(response);
			return;
		}
		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
		if (payLog == null) {
			log.info("YunPayNotify()云闪付支付[payNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
			writeLowerSuccess(response);
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			log.info("YunPayNotify()云闪付支付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
			writeLowerSuccess(response);
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_YUNPAY);
		rspOrderEntikty.setTrade_status(status);
		log.info("YunPayNotify()返回报文*********"+payType);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
		writeLowerSuccess(response);
		return;
	}
	
	@ApiOperation(value = "惠民代付完成回调")
	@PostMapping("/SmkPayNotify")
	@ResponseBody
	public String SmkPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		log.info("SMKPayNotify()惠民代付通知消息begin");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
		log.info("SMKPayNotify()*********惠民代付通知消息LidPayNotify={}", parameters);
		Map<String,String> realMap = new HashMap<String, String>();
		// 打印惠民代付回调结果
		log.info("SMKPayNotify()*********惠民代付服务器端通知-接收到云闪付支付返回报文：");
		Iterator<?> paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info("SMKPayNotify()*********"+key + "-------------" + values[0]);
			realMap.put(key, values[0]);
		}
		log.info("SMKPayNotify()*********返回成功realMap="+realMap);
//		writeLowerSuccess(response);
		String widthDrawSn = realMap.get("orderNo");
		String status = realMap.get("status");
		RspSingleCashEntity rspEntity = new RspSingleCashEntity();
		if("01".equals(status)) {
			rspEntity.status = "S";
			rspEntity.resCode = realMap.get("respCode");
			rspEntity.resMessage = "提现成功！";
		}else {
			rspEntity.status = "F";
			rspEntity.resCode = realMap.get("respCode");
			rspEntity.resMessage = realMap.get("respDesc");
		}
		cashService.operation(rspEntity, widthDrawSn, SessionUtil.getUserId(), Boolean.TRUE);
		Map<String,String> resultMap = new HashMap<String,String>();
		resultMap.put("respSeq", realMap.get("respSeq"));
		resultMap.put("merCode", realMap.get("merCode"));
		resultMap.put("respCode", "00");
		resultMap.put("respDesc", "OK");
		return JSONUtils.valueToString(resultMap);
	}
	
	
//	@ApiOperation(value = "Q多多代付回调")
//	@PostMapping("/RkFundNotify")
//	@ResponseBody
//	public void RkFundNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
//		request.setCharacterEncoding("UTF-8");
//		response.setCharacterEncoding("UTF-8");
//		response.setHeader("Content-type", "text/html;charset=UTF-8");
//		Map<?, ?> parameters = request.getParameterMap();// 保存request请求参数的临时变量
//		log.info("RkFundNotify()Q多多代付通知消息RkFundNotify={}", parameters);
//		Map<String,Object> realMap = new HashMap<String, Object>();
//		// 打印华移支付返回值
//		log.info("RkFundNotify()Q多多代付服务器端通知-接收到支付返回报文：");
//		Iterator<?> paiter = parameters.keySet().iterator();
//		while (paiter.hasNext()) {
//			String key = paiter.next().toString();
//			String[] values = (String[]) parameters.get(key);
//			log.info("RkFundNotify()*********"+key + "-------------" + values[0]);
//			realMap.put(key, values[0]);
//		}
////		aPayService.getSign(realMap); ///签名校验
//		log.info("RkFundNotify()返回报文*********"+JSONUtils.valueToString(realMap));
//		String payOrderfSn = realMap.get("ds_trade_no")==null?"":realMap.get("ds_trade_no").toString();
//		String status=realMap.get("status")==null?"":realMap.get("status").toString();
//		log.info("RkFundNotify()返回报文*********"+payOrderfSn+"&&&"+status);
//		if (StringUtils.isEmpty(payOrderfSn)) {
//			log.info("RkFundNotify()Q多多代付返回payOrderSn is null");
//			writeLowerSuccess(response);
//			return;
//		}
//		PayLog payLog = payLogService.findPayLogByOrderSn(payOrderfSn);
//		if (payLog == null) {
//			log.info("RkFundNotify()Q多多代付[RkFundNotify]该支付订单查询失败 payLogSn:" + payOrderfSn);
//			writeLowerSuccess(response);
//			return;
//		} 
//		int isPaid = payLog.getIsPaid();
//		if (isPaid == 1) {
//			log.info("RkFundNotify()Q多多代付[payNotify] payOrderSn={}订单已支付...", payOrderfSn);
//			writeLowerSuccess(response);
//			return;
//		}
//		int payType = payLog.getPayType();
//		String payCode = payLog.getPayCode();
//		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
//		rspOrderEntikty.setResult_code(status);
//		rspOrderEntikty.setPayCode(payCode);
//		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_RKPAY);
//		rspOrderEntikty.setTrade_status(status);
//		log.info("RkFundNotify()返回报文*********"+payType);
//		if (payType == 0) {
//			paymentService.orderOptions(payLog, rspOrderEntikty);
//		} else {
//			paymentService.rechargeOptions(payLog, rspOrderEntikty);
//		}
//		writeLowerSuccess(response);
//		return;
//	}
}
