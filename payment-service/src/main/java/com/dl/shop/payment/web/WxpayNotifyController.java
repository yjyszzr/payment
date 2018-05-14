package com.dl.shop.payment.web;

import java.io.IOException;
import java.math.BigDecimal;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.apache.zookeeper.Op;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.dl.base.result.BaseResult;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.param.RecharegeParam;
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
	private UserRechargeService userRechargeService;
	
	@ApiOperation(value="微信支付回调")
	@PostMapping("notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		String loggerId = "wxNotify_"+System.currentTimeMillis();
		logger.warn(loggerId + " in controller /payment/wxpay/notify");
		String val = request.getParameter("result");
		if(TextUtils.isEmpty(val)) {
			val = request.getParameter("returnCode");
		}
		
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
			logger.info("=======isPaid:" + isPaid +" payLogId:" +payLog.getLogId() +"==========");
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
						result = orderOptionsSucc(tradeNo, payLog);
					}else {
						result = recharageOptionSucc(tradeNo, payLog);
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
						payLogService.update(updatePayLog);
						logger.info(loggerId + " 业务回调成功，payLog.对象状态回写结束");
						String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
						response.getWriter().write(xml);
						
						if(0 == payType) {
							//订单支付付款成功就要生成流水
							logger.info("订单支付付款成功就要生成流水...");
							UserAccountParamByType userAccountParamByType = new UserAccountParamByType();
							Integer accountType = ProjectConstant.BUY;
							logger.info("===========更新用户流水表=======:" + accountType);
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
							}else {
								logger.info("生成账户流水成功");
							}
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
	
	/***
	 * 订单支付，微信回调成功
	 */
	private boolean orderOptionsSucc(String tradeNo,PayLog payLog) {
		boolean isSucc = true;
		int currentTime = DateUtil.getCurrentTimeLong();
		//更新order
		UpdateOrderInfoParam param = new UpdateOrderInfoParam();
		param.setPayStatus(1);
		param.setOrderStatus(1);
		param.setPayTime(currentTime);
		param.setPaySn(payLog.getLogId()+"");
		param.setPayName(payLog.getPayName());
		param.setPayCode(payLog.getPayCode());
		param.setOrderSn(payLog.getOrderSn());
		BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(param);
		if(updateOrderInfo.getCode() != 0) {
			logger.error("ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
		}else {
			isSucc = true;
		}
		return isSucc;
	}

	/***
	 * 充值，微信回调
	 */
	private boolean recharageOptionSucc(String tradeNo,PayLog payLog) {
		boolean isSucc = false;
		int currentTime = DateUtil.getCurrentTimeLong();
		//更新充值单信息
		UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
		updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
		updateUserRechargeParam.setPaymentId(payLog.getLogId()+"");
		updateUserRechargeParam.setPaymentName(payLog.getPayName());
		updateUserRechargeParam.setPayTime(currentTime);
		updateUserRechargeParam.setStatus("1");
		updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
		userRechargeService.updateReCharege(updateUserRechargeParam);
		
		//给用户增加不可提现余额,注意:rechargeUserMoneyLimit 已經生成充值流水userAccountService
		RecharegeParam recharegeParam = new RecharegeParam();
		recharegeParam.setAmount(payLog.getOrderAmount());
		recharegeParam.setPayId(tradeNo);
		recharegeParam.setThirdPartName("微信");
		recharegeParam.setThirdPartPaid(payLog.getOrderAmount());
		recharegeParam.setUserId(payLog.getUserId());
		BaseResult<String>  rechargeRst = userAccountService.rechargeUserMoneyLimit(recharegeParam);
		if(rechargeRst.getCode() != 0) {
			logger.error("给个人用户充值：code"+rechargeRst.getCode() +"message:"+rechargeRst.getMsg());
		}else {
			isSucc = true;
		}
		return isSucc;
	}
}
