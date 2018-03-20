package com.dl.shop.payment.web;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.pqc.math.linearalgebra.BigEndianConversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.api.IOrderService;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.dto.OrderDTO;
import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.dto.UserRechargeDTO;
import com.dl.member.param.AmountParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.param.OrderSnParam;
import com.dl.param.SubmitOrderParam;
import com.dl.param.SubmitOrderParam.TicketDetail;
import com.dl.param.UpdateOrderInfoParam;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.model.OrderQueryResponse;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.param.GoPayParam;
import com.dl.shop.payment.param.RechargeParam;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.utils.WxpayUtil;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/payment")
public class PaymentController extends AbstractBaseController{

	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private PayLogService payLogService;
	@Resource
	private PayMentService paymentService;
	@Resource
	private WxpayUtil wxpayUtil;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	
	@ApiOperation(value="app支付调用", notes="")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<Object> unifiedOrderForApp(@RequestBody GoPayParam param, HttpServletRequest request) {
		String loggerId = "payment_app_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/app, userId="+SessionUtil.getUserId()+" ,payCode="+param.getPayCode());
		String payToken = param.getPayToken();
		//校验payToken的有效性
		Integer userBonusId = null;//form paytoken
		BigDecimal ticketAmount = null;//from paytoken
		Integer orderFrom = null;//from paytoken
		BigDecimal moneyPaid = null;//from paytoken
		BigDecimal bonusAmount = null;//from paytoken
		List<TicketDetail> ticketDetails = null;//from paytoken
		BigDecimal surplus = null;//from paytoken
		BigDecimal thirdPartyPaid = new BigDecimal(moneyPaid.doubleValue()-surplus.doubleValue());
		//order生成
		SubmitOrderParam submitOrderParam = new SubmitOrderParam();
		submitOrderParam.setBonusAmount(bonusAmount);
		submitOrderParam.setMoneyPaid(moneyPaid);
		submitOrderParam.setOrderFrom(orderFrom);
		submitOrderParam.setTicketAmount(ticketAmount);
		submitOrderParam.setTicketDetails(ticketDetails);
		submitOrderParam.setUserBonusId(userBonusId);
		submitOrderParam.setSurplus(surplus);
		submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
		BaseResult<OrderDTO> createOrder = orderService.createOrder(submitOrderParam);
		if(createOrder == null || createOrder.getCode() != 0) {
			logger.info(loggerId + "订单创建失败！");
			return ResultGenerator.genFailResult("支付失败！");
		}
		String orderSn = createOrder.getData().getOrderSn();
		if(surplus != null && surplus.doubleValue() > 0) {
			//用户余额扣除
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay = userAccountService.changeUserAccountByPay(surplusPayParam);
			if(changeUserAccountByPay.getCode() != 0) {
				logger.info(loggerId + "用户余额扣减失败！");
				return ResultGenerator.genFailResult("支付失败！");
			}
			BigDecimal userSurplus = changeUserAccountByPay.getData().getUserSurplus();
			BigDecimal userSurplusLimit = changeUserAccountByPay.getData().getUserSurplusLimit();
			UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
			updateOrderInfoParam.setUserSurplus(userSurplus);
			updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
			BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(updateOrderInfoParam);
			if(updateOrderInfo.getCode() != 0) {
				logger.info(loggerId + "订单回写用户余额扣减详情失败！");
				return ResultGenerator.genFailResult("支付失败！");
			}
		}
		if(thirdPartyPaid == null || thirdPartyPaid.doubleValue() <= 0) {
			//回调order,更新支付状态
			logger.info(loggerId + "订单没有需要第三方支付金额，完全余额支付成功！");
			return ResultGenerator.genSuccessResult("支付成功！");
		}
		//支付方式
		String payCode = param.getPayCode();
		if(StringUtils.isBlank(payCode)) {
			logger.info(loggerId + "订单第三支付没有提供paycode！");
			return ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null);
		}
		BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(payCode);
		if(paymentResult.getCode() == 1) {
			logger.info(loggerId + "订单第三方支付提供paycode有误！");
			return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
		}
		String payName = paymentResult.getData().getPayName();
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(orderSn, thirdPartyPaid, 0, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}
		//更新订单号为paylogId
		UnifiedOrderParam unifiedOrderParam = new UnifiedOrderParam();
		unifiedOrderParam.setBody(null);
		unifiedOrderParam.setSubject(null);
		unifiedOrderParam.setTotalAmount(thirdPartyPaid.doubleValue());
		unifiedOrderParam.setIp(payIp);
		unifiedOrderParam.setOrderNo(savePayLog.getLogId());
		BaseResult payBaseResult = null;
		if("app_weixin".equals(payCode)) {
			payBaseResult = wxpayUtil.unifiedOrderForApp(unifiedOrderParam);
		}
		//处理支付失败的情况
		if(null == payBaseResult || payBaseResult.getCode() != 0) {
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
			if(rollbackUserAccountChangeByPay.getCode() == 1) {
				logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
			}
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(savePayLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(payBaseResult.getMsg());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + "paylogid="+savePayLog.getLogId()+" , paymsg="+payBaseResult.getMsg()+"保存失败记录时出错", e);
			}
		}
		logger.info(loggerId + " result: code="+payBaseResult.getCode()+" , msg="+payBaseResult.getMsg());
		return payBaseResult;
	}
	
	@ApiOperation(value="app充值调用", notes="")
	@PostMapping("/recharge")
	@ResponseBody
	public BaseResult<Object> rechargeForApp(@RequestBody RechargeParam param, HttpServletRequest request){
		String loggerId = "rechargeForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/app, userId="+SessionUtil.getUserId()+" ,payCode="+param.getPayCode());
		double totalAmount = param.getTotalAmount();
		if(totalAmount <= 0) {
			return ResultGenerator.genFailResult("对不起，请提供有效的充值金额！", null);
		}
		//支付方式
		String payCode = param.getPayCode();
		if(StringUtils.isBlank(payCode)) {
			logger.info(loggerId + "订单第三支付没有提供paycode！");
			return ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null);
		}
		BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(payCode);
		if(paymentResult.getCode() == 1) {
			logger.info(loggerId + "订单第三方支付提供paycode有误！");
			return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
		}
		//生成充值单
		AmountParam amountParam = new AmountParam();
		amountParam.setAmount(BigDecimal.valueOf(totalAmount));
		BaseResult<UserRechargeDTO> createReCharege = userAccountService.createReCharege(amountParam);
		if(createReCharege.getCode() != 0) {
			return ResultGenerator.genFailResult("充值失败！", null);
		}
		String orderSn = createReCharege.getData().getRechargeSn();
		//生成充值记录payLog
		String payName = paymentResult.getData().getPayName();
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(orderSn, BigDecimal.valueOf(totalAmount), 1, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}
		//更新订单号为paylogId
		UnifiedOrderParam unifiedOrderParam = new UnifiedOrderParam();
		unifiedOrderParam.setBody("余额充值");
		unifiedOrderParam.setSubject("余额充值");
		unifiedOrderParam.setTotalAmount(totalAmount);
		unifiedOrderParam.setIp(payIp);
		unifiedOrderParam.setOrderNo(savePayLog.getLogId());
		BaseResult payBaseResult = null;
		if("app_weixin".equals(payCode)) {
			payBaseResult = wxpayUtil.unifiedOrderForApp(unifiedOrderParam);
		}
		//处理支付失败的情况
		if(null == payBaseResult || payBaseResult.getCode() != 0) {
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(savePayLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(payBaseResult.getMsg());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + "paylogid="+savePayLog.getLogId()+" , paymsg="+payBaseResult.getMsg()+"保存失败记录时出错", e);
			}
		}
		logger.info(loggerId + " result: code="+payBaseResult.getCode()+" , msg="+payBaseResult.getMsg());
		return payBaseResult;
	}
	
	@ApiOperation(value="支付订单结果 查询 ", notes="")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<Object> orderquery(String payLogId) {
		String loggerId = "orderquery_" + System.currentTimeMillis();
		if(StringUtils.isBlank(payLogId) ) {
			return ResultGenerator.genFailResult("订单号不能为空！", null);
		}
		logger.info(loggerId+" payLogId="+payLogId);
		PayLog payLog = payLogService.findById(Integer.parseInt(payLogId));
		if(null == payLog) {
			logger.info(loggerId+" payLogId="+payLogId+" 没有查询到对应的订单号");
			return ResultGenerator.genFailResult("请提供有效的订单号！", null);
		}
		int isPaid = payLog.getIsPaid();
		if(1== isPaid) {
			logger.info(loggerId+" 订单已支付成功");
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}
		String payCode = payLog.getPayCode();
		BaseResult<OrderQueryResponse> baseResult = null;
		if("app_weixin".equals(payCode)) {
			baseResult = wxpayUtil.orderQuery(payLogId);
		}
		if(baseResult.getCode() != 0) {
			logger.info(loggerId+" 订单查询请求异常"+baseResult.getMsg());
			return ResultGenerator.genFailResult("请求异常！", null);
		}
		OrderQueryResponse response = baseResult.getData();
		Integer payType = payLog.getPayType();
		if(0 == payType) {
			return orderOptions(loggerId, payLog, response);
		}else if(1 == payType){
			return rechargeOptions(loggerId, payLog, response);
		}
		return ResultGenerator.genFailResult("请求失败！", null);
	}

	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 */
	private BaseResult<Object> rechargeOptions(String loggerId, PayLog payLog, OrderQueryResponse response) {
		Integer tradeState = response.getTradeState();
		if(1 == tradeState) {
			int currentTime = DateUtil.getCurrentTimeLong();
			//更新order
			UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
			updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
			updateUserRechargeParam.setPaymentId(payLog.getLogId()+"");
			updateUserRechargeParam.setPaymentName(payLog.getPayName());
			updateUserRechargeParam.setPayTime(currentTime);
			updateUserRechargeParam.setStatus("1");
			updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
			BaseResult<UserRechargeDTO> updateReCharege = userAccountService.updateReCharege(updateUserRechargeParam);
			if(updateReCharege.getCode() != 0) {
				logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新充值单成功状态失败");
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTradeNo());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("支付成功");
				payLogService.update(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId+" paylogid="+payLog.getLogId()+" , paymsg=支付成功，保存成功记录时出错", e);
			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else {
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(response.getTradeStateDesc());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getTradeStateDesc()+"，保存失败记录时出错", e);
			}
			return ResultGenerator.genFailResult("请求失败！", null);
		}
	}
	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 */
	private BaseResult<Object> orderOptions(String loggerId, PayLog payLog, OrderQueryResponse response) {
		Integer tradeState = response.getTradeState();
		if(1 == tradeState) {
			int currentTime = DateUtil.getCurrentTimeLong();
			//更新order
			UpdateOrderInfoParam param = new UpdateOrderInfoParam();
			param.setPayStatus(1);
			param.setPayTime(currentTime);
			param.setPaySn(payLog.getLogId()+"");
			param.setPayName(payLog.getPayName());
			param.setPayCode(payLog.getPayCode());
			param.setOrderSn(payLog.getOrderSn());
			BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(param);
			if(updateOrderInfo.getCode() != 0) {
				logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTradeNo());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("支付成功");
				payLogService.update(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId+" paylogid="+payLog.getLogId()+" , paymsg=支付成功，保存成功记录时出错", e);
			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else {
			//退回用户余额
			String orderSn = payLog.getOrderSn();
			OrderSnParam snParam = new OrderSnParam();
			snParam.setOrderSn(orderSn);
			BaseResult<OrderDTO> orderInfoByOrderSn = orderService.getOrderInfoByOrderSn(snParam);
			if(orderInfoByOrderSn.getCode() != 0 || orderInfoByOrderSn.getData() == null) {
				logger.info(loggerId+" 订单获取失败");
				return ResultGenerator.genFailResult("请求失败！", null);
			}
			BigDecimal userAccount = orderInfoByOrderSn.getData().getSurplus();
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(userAccount);
			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
			if(rollbackUserAccountChangeByPay.getCode() != 0) {
				logger.error(loggerId + " orderSn="+orderSn+" , Surplus="+userAccount.doubleValue()+" 在回滚用户余额时出错！");
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(response.getTradeStateDesc());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getTradeStateDesc()+"，保存失败记录时出错", e);
			}
			return ResultGenerator.genFailResult("请求失败！", null);
		}
	}
}
