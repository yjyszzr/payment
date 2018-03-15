package com.dl.shop.payment.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dl.base.result.BaseResult;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.configurer.WxpayConfig;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.WxpayNotifyModel;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.utils.XmlUtil;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/payment/wxpay")
public class WxpayNotifyController {

	private final static Logger logger = LoggerFactory.getLogger(WxpayNotifyController.class);
	@Resource
	private WxpayConfig wxpayConfig;
	@Resource
	private PayLogService payLogService;


	@ApiOperation(value="微信支付回调")
	@PostMapping("notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		String loggerId = "wxNotify_"+System.currentTimeMillis();
		logger.warn(loggerId + " in controller /payment/wxpay/notify");
		// 将微信的回调的参数转化为String并打印
		StringBuffer strBuf1 = new StringBuffer();
		String line = null;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))){
			while ((line = reader.readLine()) != null) {
				strBuf1.append(line).append("\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		String requestStr = strBuf1.toString();
		logger.warn(loggerId + "  /payment/wxpay/notify requestStr 微信支付 验证信息: " + requestStr);
		WxpayNotifyModel responseModel = null;
		try {
			responseModel = XmlUtil.xmlToBean(requestStr, WxpayNotifyModel.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String resultCode = null;
		if(null != responseModel) {
			resultCode = responseModel.getResult_code();
		}
		if ("SUCCESS".equals(resultCode)) {
			// 获取回调的具体参数
			logger.debug(loggerId + " 开始回调接口处理");
			String appid = responseModel.getAppid();//prePayJo.getString("appid");
			String mchId = responseModel.getMch_id();//prePayJo.getString("mch_id");
			String bank_type = responseModel.getBank_type();//prePayJo.getString("bank_type");
			logger.debug(loggerId + " appid:", appid);
			logger.debug(loggerId + " bank_type:" + bank_type);
			logger.debug(loggerId + " cash_fee:" + responseModel.getCash_fee());
			logger.debug(loggerId + " mch_id:" + mchId);
			logger.debug(loggerId + " nonce_str:" + responseModel.getNonce_str());
			logger.debug(loggerId + " openid:" + responseModel.getOpenid());
			logger.debug(loggerId + " out_trade_no:" + responseModel.getOut_trade_no());
			logger.debug(loggerId + " transaction_id:" + responseModel.getTransaction_id());
			logger.debug(loggerId + " sign:" + responseModel.getSign());
			logger.debug(loggerId + " time_end:" + responseModel.getTime_end());
			logger.debug(loggerId + " total_fee:" + responseModel.getTotal_fee());
			logger.debug(loggerId + " trade_type:" + responseModel.getTrade_type());
			logger.debug(loggerId + " 解析完毕！！*********");

			String payLogId = responseModel.getOut_trade_no();//prePayJo.getString("out_trade_no");
			logger.info(loggerId + " payLogId="+payLogId);
			String tradeNo = responseModel.getTransaction_id();//prePayJo.getString("transaction_id");
			int amount = responseModel.getTotal_fee();
			PayLog payLog = payLogService.findById(Integer.parseInt(payLogId));
			if(null == payLog) {
				logger.info(loggerId + " payLog对象未查询到，返回失败！");
				//fail
				String xml = "<xml><return_code><![CDATA[FAIL]]></return_code> <return_msg><![CDATA[order no find]]></return_msg></xml>";
				try {
					response.getWriter().write(xml);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			int isPaid = payLog.getIsPaid();
			if(1== isPaid) {
				logger.info(loggerId + " paylog.ispaid=1,已支付成功，返回OK！");
				String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
				try {
					response.getWriter().write(xml);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			int orderAmount = (int)(payLog.getOrderAmount().doubleValue()*100);
			if (amount == orderAmount && ((appid.equals(wxpayConfig.getWxAppAppId()) && mchId.equals(wxpayConfig.getWxAppMchId())) || (appid.equals(wxpayConfig.getWxJsAppId()) && mchId.equals(wxpayConfig.getWxJsMchId())))) {
				logger.info(loggerId + " 订单金额或appid,mchId校验成功，前去回调订单服务！");
				try {
					int payType = payLog.getPayType();
					int currentTime = DateUtil.getCurrentTimeLong();
					boolean result = false;
					if(0 == payType) {
						//order
						/*PaymentCallbackParam paymentCallbackParam = new PaymentCallbackParam();
						paymentCallbackParam.setAmount(payLog.getOrderAmount().doubleValue()+"");
						paymentCallbackParam.setOrderSn(payLog.getOrderSn());
						paymentCallbackParam.setParentSn(payLog.getParentSn());
						paymentCallbackParam.setPayCode(payLog.getPayCode());
						paymentCallbackParam.setPayId("4");
						paymentCallbackParam.setPayName(payLog.getPayName());
						paymentCallbackParam.setPaySn(payLog.getLogId()+"");
						paymentCallbackParam.setPayTime(currentTime);
						paymentCallbackParam.setIp(payLog.getPayIp());*/
						BaseResult<String> baseResult = null;//orderService.payCallback(paymentCallbackParam);
						logger.info(loggerId + " 订单回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
						if(0 == baseResult.getCode()) {
							result = true;
						}
					}else {
						String rechargeSn = payLog.getOrderSn();
						/*UserCapitalSnParam userCapitalSnParam = new UserCapitalSnParam();
						userCapitalSnParam.setRechargeSn(rechargeSn);
						userCapitalSnParam.setTradeNo(payLog.getLogId()+"");*/
						BaseResult baseResult = null;//userCapitalService.completeRecharge(userCapitalSnParam);
						logger.info(loggerId + " 充值回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
						if(0 == baseResult.getCode()) {
							result = true;
						}
					}
					logger.info(loggerId + " 业务回调结果：result="+result);
					if(result) {
						//更新paylog状态为已支付
						PayLog updatePayLog = new PayLog();
						updatePayLog.setLogId(payLog.getLogId());
						updatePayLog.setTradeNo(tradeNo);
						updatePayLog.setIsPaid(1);
						updatePayLog.setLastTime(currentTime);
						updatePayLog.setPayTime(currentTime);
						payLogService.update(payLog);
						logger.info(loggerId + " 业务回调成功，payLog.对象状态回写结束");
						String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
						response.getWriter().write(xml);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else {
				logger.info(loggerId + " 订单金额或appid,mchId校验失败！");
			}
		}
	}
}
