package com.dl.shop.payment.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dl.base.result.BaseResult;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserAccountParamByType;
import com.dl.order.api.IOrderService;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.configurer.WxpayConfig;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.WxpayNotifyModel;
import com.dl.shop.payment.pay.common.PayConfig;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.UserRechargeService;
import com.dl.shop.payment.utils.XmlUtil;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/payment/wxpay")
public class WxpayNotifyController {
	//url /payment/wxpay/notify
	private final static Logger logger = LoggerFactory.getLogger(WxpayNotifyController.class);
	@Resource
	private WxpayConfig wxpayConfig;
	@Resource
	private PayLogService payLogService;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private UserRechargeService userRService;
	
	@ApiOperation(value="微信支付回调")
	@PostMapping("notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		String loggerId = "wxNotify_"+System.currentTimeMillis();
		logger.warn(loggerId + " in controller /payment/wxpay/notify");
		String val = request.getParameter("result");
		logger.info("微信回掉处理:" + val);
//		// 将微信的回调的参数转化为String并打印
//		StringBuffer strBuf1 = new StringBuffer();
//		String line = null;
//		try(BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))){
//			while ((line = reader.readLine()) != null) {
//				strBuf1.append(line).append("\n");
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		String requestStr = val;
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

			String payOrderSn = responseModel.getOut_trade_no();//prePayJo.getString("out_trade_no");
			logger.info(loggerId + " payOrderSn="+payOrderSn);
			String tradeNo = responseModel.getTransaction_id();//prePayJo.getString("transaction_id");
			int amount = responseModel.getTotal_fee();
			PayLog payLog = payLogService.findPayLogByOrderSign(payOrderSn);
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
			logger.info("实际交易金额:" + amount +" 订单金额:" + orderAmount);
			if ((PayConfig.isDebug() || amount == orderAmount) && ((appid.equals(wxpayConfig.getWxAppAppId()) && mchId.equals(wxpayConfig.getWxAppMchId())) || (appid.equals(wxpayConfig.getWxJsAppId()) && mchId.equals(wxpayConfig.getWxJsMchId())))) {
				logger.info(loggerId + " 订单金额或appid,mchId校验成功，前去回调订单服务！");
				try {
					int payType = payLog.getPayType();
					int currentTime = DateUtil.getCurrentTimeLong();
					boolean result = false;
					if(0 == payType) {
						//order
						UpdateOrderInfoParam param = new UpdateOrderInfoParam();
						param.setPayStatus(1);
						param.setOrderStatus(1);
						param.setPayTime(currentTime);
						param.setPaySn(payLog.getLogId()+"");
						param.setPayName(payLog.getPayName());
						param.setPayCode(payLog.getPayCode());
						param.setOrderSn(payLog.getOrderSn());
						BaseResult<String> baseResult = orderService.updateOrderInfo(param);
						logger.info(loggerId + " 订单回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
						if(0 == baseResult.getCode()) {
							result = true;
						}
					}else {
						String rechargeSn = payLog.getOrderSn();
						//更新账单信息
						UpdateUserRechargeParam updateRParams = new UpdateUserRechargeParam();
						updateRParams.setRechargeSn(rechargeSn);
						updateRParams.setStatus("1");
						updateRParams.setPaymentCode("app_weixin");
						updateRParams.setPaymentName("微信充值");
						updateRParams.setPayTime(currentTime);
						updateRParams.setPaymentId(payLog.getLogId()+"");
						BaseResult<String> baseResult = userRService.updateReCharege(updateRParams);
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
						
						UserAccountParamByType userAccountParamByType = new UserAccountParamByType();
						Integer accountType = ProjectConstant.BUY; 
						if(0 != payType) {
							accountType = ProjectConstant.RECHARGE;
						}
						userAccountParamByType.setAccountType(accountType);
						userAccountParamByType.setAmount(new BigDecimal(payLog.getOrderAmount().doubleValue()));
						userAccountParamByType.setBonusPrice(BigDecimal.ZERO);//暂无红包金额
						userAccountParamByType.setOrderSn(payLog.getOrderSn());
						userAccountParamByType.setPayId(payLog.getLogId());
						userAccountParamByType.setPaymentName("微信");
						userAccountParamByType.setThirdPartName("微信");
						userAccountParamByType.setThirdPartPaid(new BigDecimal(payLog.getOrderAmount().doubleValue()));
						userAccountParamByType.setUserId(payLog.getUserId());
						BaseResult<String> accountRst = userAccountService.insertUserAccount(userAccountParamByType);
						if(accountRst.getCode() != 0) {
							logger.info(loggerId + "生成账户流水异常");
						}
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
