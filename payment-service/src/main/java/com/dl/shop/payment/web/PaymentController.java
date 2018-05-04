package com.dl.shop.payment.web;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryPrintService;
import com.dl.lottery.dto.DIZQUserBetCellInfoDTO;
import com.dl.lottery.dto.DIZQUserBetInfoDTO;
import com.dl.lottery.param.SaveLotteryPrintInfoParam;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserDTO;
import com.dl.member.dto.UserRechargeDTO;
import com.dl.member.dto.UserWithdrawDTO;
import com.dl.member.param.AmountParam;
import com.dl.member.param.IDParam;
import com.dl.member.param.MessageAddParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserWithdrawParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.SubmitOrderParam;
import com.dl.order.param.SubmitOrderParam.TicketDetail;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.dto.PayReturnDTO;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RongbaoPayResultDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.YinHeResultDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.AllPaymentInfoParam;
import com.dl.shop.payment.param.GoPayParam;
import com.dl.shop.payment.param.RechargeParam;
import com.dl.shop.payment.param.ReqOrderQueryParam;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRongEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspOrderQueryEntity;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.UserWithdrawLogService;
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
	@Autowired
	private IUserBankService userBankService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private UserWithdrawLogService userWithdrawLogService;
	@Resource
	private IUserMessageService userMessageService;
	@Resource
	private IUserService userService;
	@Resource
	private ILotteryPrintService lotteryPrintService;
	
	@ApiOperation(value="系统可用第三方支付方式", notes="系统可用第三方支付方式")
	@PostMapping("/allPayment")
	@ResponseBody
	public BaseResult<List<PaymentDTO>> allPaymentInfo(@RequestBody AllPaymentInfoParam param) {
		BaseResult<List<PaymentDTO>> findAllDto = paymentService.findAllDto();
		return findAllDto;
	}
	@ApiOperation(value="用户支付回退接口", notes="")
	@PostMapping("/rollbackOrderAmount")
	@ResponseBody
	public BaseResult rollbackOrderAmount(@RequestBody RollbackOrderAmountParam param) {
		logger.info("in rollbackOrderAmount ordersn=" + param.getOrderSn());
		String orderSn = param.getOrderSn();
		OrderSnParam snParam = new OrderSnParam();
		snParam.setOrderSn(orderSn);
		BaseResult<OrderDTO> orderRst = orderService.getOrderInfoByOrderSn(snParam);
		if(orderRst.getCode() != 0) {
			logger.info("orderService.getOrderInfoByOrderSn rst code="+orderRst.getCode()+" msg="+orderRst.getMsg());
			return ResultGenerator.genFailResult();
		}
		OrderDTO order = orderRst.getData();
		BigDecimal surplus = order.getSurplus();
		BigDecimal bonusAmount = order.getBonus();
		BigDecimal moneyPaid = order.getMoneyPaid();
		String payName = order.getPayName();
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		//第三方支付
		int payType = 1;//默认微信
		if(surplus != null && surplus.doubleValue() > 0) {
			payType = 2;
		}
		boolean hasThird = false;
		if(thirdPartyPaid != null && thirdPartyPaid.doubleValue() > 0) {
			hasThird = true;
		}
		if(hasThird && payType == 2) {
			payType = 3;
		}
		if(payType ==2 || payType == 3) {
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			surplusPayParam.setBonusMoney(bonusAmount);
			surplusPayParam.setPayType(payType);
			surplusPayParam.setMoneyPaid(moneyPaid);
			surplusPayParam.setThirdPartName(payName);
			surplusPayParam.setThirdPartPaid(thirdPartyPaid);
			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
			logger.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
			if(rollbackUserAccountChangeByPay.getCode() != 0) {
				logger.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额时出错！");
			}
		}
		return ResultGenerator.genSuccessResult();
	}
	@ApiOperation(value="app支付调用", notes="payToken:商品中心购买信息保存后的返回值 ，payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<PayReturnDTO> unifiedOrderForApp(@RequestBody GoPayParam param, HttpServletRequest request) {
		String loggerId = "payment_app_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/app, userId="+SessionUtil.getUserId()+" ,payCode="+param.getPayCode());
		String payToken = param.getPayToken();
		if(StringUtils.isBlank(payToken)) {
			logger.info(loggerId + "payToken值为空！");
			return ResultGenerator.genFailResult("商品信息有误，支付失败！");
		}
		//校验payToken的有效性
		String jsonData = stringRedisTemplate.opsForValue().get(payToken);
		if(StringUtils.isBlank(jsonData)) {
			logger.info(loggerId + "支付信息获取为空！");
			return ResultGenerator.genFailResult("支付信息不存在或已失效，支付失败！");
		}
		DIZQUserBetInfoDTO dto = null;
		try {
			dto = JSONHelper.getSingleBean(jsonData, DIZQUserBetInfoDTO.class);
		} catch (Exception e1) {
			logger.error(loggerId + "支付信息转DIZQUserBetInfoDTO对象失败！", e1);
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		if(null == dto) {
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		Integer userId = dto.getUserId();
		Integer currentId = SessionUtil.getUserId();
		if(!userId.equals(currentId)) {
			logger.info(loggerId + "支付信息不是当前用户的待支付彩票！");
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		Integer userBonusId = StringUtils.isBlank(dto.getBonusId())?0:Integer.valueOf(dto.getBonusId());//form paytoken
		BigDecimal ticketAmount = BigDecimal.valueOf(dto.getMoney());//from paytoken
		BigDecimal bonusAmount = BigDecimal.valueOf(dto.getBonusAmount());//from paytoken
		BigDecimal moneyPaid = BigDecimal.valueOf(dto.getMoney() - dto.getBonusAmount());;//from paytoken
		BigDecimal surplus = BigDecimal.valueOf(dto.getSurplus());//from paytoken
		BigDecimal thirdPartyPaid = BigDecimal.valueOf(dto.getThirdPartyPaid());
		Integer orderFrom = dto.getRequestFrom();//from paytoken
		List<DIZQUserBetCellInfoDTO> userBetCellInfos = dto.getUserBetCellInfos();
		List<TicketDetail> ticketDetails = userBetCellInfos.stream().map(betCell->{
			TicketDetail ticketDetail = new TicketDetail();
			ticketDetail.setMatch_id(betCell.getMatchId());
			ticketDetail.setChangci(betCell.getChangci());
			ticketDetail.setMatchTime(Date.from(Instant.ofEpochSecond(betCell.getMatchTime())));
			ticketDetail.setMatchTeam(betCell.getMatchTeam());
			ticketDetail.setLotteryClassifyId(betCell.getLotteryClassifyId());
			ticketDetail.setLotteryPlayClassifyId(betCell.getLotteryPlayClassifyId());
			ticketDetail.setTicketData(betCell.getTicketData());
			ticketDetail.setIsDan(betCell.getIsDan());
			ticketDetail.setIssue(betCell.getPlayCode());
			return ticketDetail;
		}).collect(Collectors.toList());
		//余额支付
		boolean hasSurplus = false;
		if(surplus != null && surplus.doubleValue() > 0) {
			hasSurplus = true;
		}
		//第三方支付
		boolean hasThird = false;
		if(thirdPartyPaid != null && thirdPartyPaid.doubleValue() > 0) {
			hasThird = true;
		}
		PaymentDTO paymentDto = null;
		String payName = null;
		if(hasThird) {
			//支付方式校验
			String payCode = param.getPayCode();
			if(StringUtils.isBlank(payCode)) {
				logger.info(loggerId + "订单第三支付没有提供paycode！");
				return ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null);
			}
			BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(payCode);
			if(paymentResult.getCode() != 0) {
				logger.info(loggerId + "订单第三方支付提供paycode有误！payCode="+payCode);
				return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
			}
			paymentDto = paymentResult.getData();
			payName = paymentDto.getPayName();
		}
		//order生成
		SubmitOrderParam submitOrderParam = new SubmitOrderParam();
		submitOrderParam.setTicketNum(dto.getTicketNum());
		submitOrderParam.setMoneyPaid(moneyPaid);
		submitOrderParam.setTicketAmount(ticketAmount);
		submitOrderParam.setSurplus(surplus);
		submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
		submitOrderParam.setUserBonusId(userBonusId);
		submitOrderParam.setBonusAmount(bonusAmount);
		submitOrderParam.setOrderFrom(orderFrom);
		submitOrderParam.setLotteryClassifyId(dto.getLotteryClassifyId());
		submitOrderParam.setLotteryPlayClassifyId(dto.getLotteryPlayClassifyId());
		submitOrderParam.setPassType(dto.getBetType());
		submitOrderParam.setPlayType("0"+dto.getPlayType());
		submitOrderParam.setBetNum(dto.getBetNum());
		submitOrderParam.setCathectic(dto.getTimes());
		if(ticketDetails.size() > 1) {
			Optional<TicketDetail> max = ticketDetails.stream().max((detail1, detail2)->detail1.getMatchTime().compareTo(detail2.getMatchTime()));
			submitOrderParam.setMatchTime(max.get().getMatchTime());
		}else {
			submitOrderParam.setMatchTime(ticketDetails.get(0).getMatchTime());
		}
		submitOrderParam.setForecastMoney(dto.getForecastMoney());
		
		submitOrderParam.setIssue(dto.getIssue());
		submitOrderParam.setTicketDetails(ticketDetails);
		BaseResult<OrderDTO> createOrder = orderService.createOrder(submitOrderParam);
		if(createOrder.getCode() != 0) {
			logger.info(loggerId + "订单创建失败！");
			return ResultGenerator.genFailResult("支付失败！");
		}
		String orderId = createOrder.getData().getOrderId().toString();
		String orderSn = createOrder.getData().getOrderSn();
		
		
		if(hasSurplus) {
			//用户余额扣除
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			surplusPayParam.setBonusMoney(bonusAmount);
			int payType1 = 2;
			if(hasThird) {
				payType1 = 3;
			}
			surplusPayParam.setPayType(payType1);
			surplusPayParam.setMoneyPaid(moneyPaid);
			surplusPayParam.setThirdPartName(paymentDto!=null?paymentDto.getPayName():"");
			surplusPayParam.setThirdPartPaid(thirdPartyPaid);
			BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay = userAccountService.changeUserAccountByPay(surplusPayParam);
			if(changeUserAccountByPay.getCode() != 0) {
				logger.info(loggerId + "用户余额扣减失败！");
				return ResultGenerator.genFailResult("支付失败！");
			}
			//更新余额支付信息到订单
			BigDecimal userSurplus = changeUserAccountByPay.getData().getUserSurplus();
			BigDecimal userSurplusLimit = changeUserAccountByPay.getData().getUserSurplusLimit();
			UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
			updateOrderInfoParam.setOrderSn(orderSn);
			updateOrderInfoParam.setUserSurplus(userSurplus);
			updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
			BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(updateOrderInfoParam);
			if(updateOrderInfo.getCode() != 0) {
				logger.info(loggerId + "订单回写用户余额扣减详情失败！");
				BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
				logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
				if(rollbackUserAccountChangeByPay.getCode() != 0) {
					logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
				}
				return ResultGenerator.genFailResult("支付失败！");
			}
			if(!hasThird) {
				//回调order,更新支付状态,余额支付成功
				UpdateOrderInfoParam param1 = new UpdateOrderInfoParam();
				param1.setPayStatus(1);
				int currentTime = DateUtil.getCurrentTimeLong();
				param1.setPayTime(currentTime);
				param1.setOrderStatus(1);
				param1.setOrderSn(orderSn);
				BaseResult<String> baseResult = orderService.updateOrderInfo(param1);
				logger.info(loggerId + " 订单成功状态更新回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
				if(baseResult.getCode() != 0) {
					BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
					logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在订单成功状态更新回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
					if(rollbackUserAccountChangeByPay.getCode() != 0) {
						logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在订单成功状态更新回滚用户余额时出错！");
					}
					return ResultGenerator.genFailResult("支付失败！");
				}
				SaveLotteryPrintInfoParam saveLotteryPrintParam = new SaveLotteryPrintInfoParam();
				saveLotteryPrintParam.setOrderSn(orderSn);
				BaseResult<String> saveLotteryPrintInfo = lotteryPrintService.saveLotteryPrintInfo(saveLotteryPrintParam);
				if(saveLotteryPrintInfo.getCode() != 0) {
					UpdateOrderInfoParam updateOrderInfoParam1 = new UpdateOrderInfoParam();
					updateOrderInfoParam1.setOrderStatus(2);
					updateOrderInfoParam1.setOrderSn(orderSn);
					BaseResult<String> baseResult1 = orderService.updateOrderInfo(updateOrderInfoParam1);
					if(baseResult1.getCode() != 0) {
						logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在预出票失败后，更改订单状态为出票失败时出错！订单回调返回结果：status=" + baseResult1.getCode()+" , message="+baseResult1.getMsg());
					}
					BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
					logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在预出票失败更新回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
					if(rollbackUserAccountChangeByPay.getCode() != 0) {
						logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在预出票失败更新回滚用户余额时出错！");
					}
					return ResultGenerator.genFailResult("支付失败！");
				}
				logger.info(loggerId + "订单没有需要第三方支付金额，完全余额支付成功！");
				PayReturnDTO payReturnDTO = new PayReturnDTO();
				payReturnDTO.setOrderId(orderId);
				return ResultGenerator.genSuccessResult("支付成功！", payReturnDTO);
			}
		}
		
//		String orderSn = "2018050211202161310026";
//		BigDecimal thirdPartyPaid = BigDecimal.valueOf(10);
//		BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(param.getPayCode());
//		if(paymentResult.getCode() != 0) {
//			logger.info(loggerId + "订单第三方支付提供paycode有误！payCode="+payCode);
//			return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
//		}
//		PaymentDTO paymentDto = paymentResult.getData();
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(orderSn, thirdPartyPaid, 0, paymentDto.getPayCode(), paymentDto.getPayName(), payIp);
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
		if("app_weixin".equals(paymentDto.getPayCode())) {
//			payBaseResult = wxpayUtil.unifiedOrderForApp(unifiedOrderParam);
			//http://wxpay.wxutil.com/mch/pay/h5.v2.php
			YinHeResultDTO yinHeRDTO = new YinHeResultDTO();
			yinHeRDTO.setPayUrl("http://wxpay.wxutil.com/mch/pay/h5.v2.php");
			yinHeRDTO.setPayLogId(savePayLog.getLogId()+"");
			payBaseResult = ResultGenerator.genSuccessResult("succ",yinHeRDTO);
		}else if("app_rongbao".equals(paymentDto.getPayCode())) {
			//生成支付链接信息
			String payOrder = savePayLog.getPayOrderSn();
			ReqRongEntity reqEntity = new ReqRongEntity();
			reqEntity.setOrderId(payOrder);
			reqEntity.setUserId(savePayLog.getUserId().toString());
			reqEntity.setTotal(savePayLog.getOrderAmount().doubleValue());
			reqEntity.setPName("彩小秘");
			reqEntity.setPDesc("彩小秘足彩支付");
			reqEntity.setTransTime(savePayLog.getAddTime()+"");
			String data = JSON.toJSONString(reqEntity);
			try {
				data = URLEncoder.encode(data,"UTF-8");
				String url = ReapalH5Config.URL_PAY + "?data="+data;
				RongbaoPayResultDTO rongBaoREntity = new RongbaoPayResultDTO();
				rongBaoREntity.setPayUrl(url);
				rongBaoREntity.setPayLogId(savePayLog.getLogId()+"");
				payBaseResult = ResultGenerator.genSuccessResult("succ",rongBaoREntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		//处理支付失败的情况
		/*if(null == payBaseResult || payBaseResult.getCode() != 0) {
			if(surplus != null && surplus.doubleValue() > 0){
				//余额回滚
				SurplusPayParam surplusPayParam = new SurplusPayParam();
				surplusPayParam.setOrderSn(orderSn);
				surplusPayParam.setSurplus(surplus);
				surplusPayParam.setBonusMoney(bonusAmount);
				int payType1 = 0;
				surplusPayParam.setPayType(payType1);
				surplusPayParam.setThirdPartName(payName);
				surplusPayParam.setThirdPartPaid(thirdPartyPaid);
				BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
				if(rollbackUserAccountChangeByPay.getCode() != 0) {
					logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
				}
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
		}*/
		logger.info(loggerId + " result: code="+payBaseResult.getCode()+" , msg="+payBaseResult.getMsg());
		return payBaseResult;
	}
	
	@ApiOperation(value="app充值调用", notes="payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/recharge")
	@ResponseBody
	public BaseResult<Object> rechargeForApp(@RequestBody RechargeParam param, HttpServletRequest request){
		String loggerId = "rechargeForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/recharge, userId="+SessionUtil.getUserId()+" ,payCode="+param.getPayCode()+" , totalAmount="+param.getTotalAmount());
		double totalAmount = param.getTotalAmount();
		if(totalAmount <= 0) {
			logger.info(loggerId + "充值金额有误！totalAmount="+totalAmount);
			return ResultGenerator.genFailResult("对不起，请提供有效的充值金额！", null);
		}
		//支付方式
		String payCode = param.getPayCode();
		if(StringUtils.isBlank(payCode)) {
			logger.info(loggerId + "订单第三支付没有提供paycode！");
			return ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null);
		}
		BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(payCode);
		if(paymentResult.getCode() != 0) {
			logger.info(loggerId + "订单第三方支付提供paycode有误！");
			return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
		}
		//生成充值单
		AmountParam amountParam = new AmountParam();
		amountParam.setAmount(BigDecimal.valueOf(totalAmount));
		BaseResult<UserRechargeDTO> createReCharege = userAccountService.createReCharege(amountParam);
		if(createReCharege.getCode() != 0) {
			logger.info(loggerId + "生成充值单：code="+createReCharege.getCode()+" , msg="+createReCharege.getMsg());
			return ResultGenerator.genFailResult("充值失败！", null);
		}
		String orderSn = createReCharege.getData().getRechargeSn();
//		String orderSn = "test01";
		//生成充值记录payLog
		String payName = paymentResult.getData().getPayName();
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(orderSn, BigDecimal.valueOf(totalAmount), 1, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}
		//第三方支付调用
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
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		String loggerId = "withdrawForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/withdraw, userId="+SessionUtil.getUserId()+", totalAmount="+param.getTotalAmount()+",userBankId="+param.getUserBankId());
		BaseResult<UserDTO> userInfoExceptPass = userService.userInfoExceptPass(new StrParam());
		if(userInfoExceptPass == null) {
			return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
		}
		String mobile = userInfoExceptPass.getData().getMobile();
		double totalAmount = param.getTotalAmount();
		if(totalAmount <= 0) {
			logger.info(loggerId+"提现金额提供有误！");
			return ResultGenerator.genFailResult("对不起，请提供有效的提现金额！", null);
		}
		//支付方式
		int userBankId = param.getUserBankId();
		if(userBankId < 1) {
			logger.info(loggerId + "用户很行卡信息id提供有误！");
			return ResultGenerator.genFailResult("对不起，请选择有效的很行卡！", null);
		}
		IDParam idParam = new IDParam();
		idParam.setId(userBankId);
		BaseResult<UserBankDTO> queryUserBank = userBankService.queryUserBank(idParam);
		if(queryUserBank.getCode() != 0) {
			logger.info(loggerId+"用户银行卡信息获取有误！");
			return ResultGenerator.genFailResult("对不起，请提供有效的银行卡！", null);
		}
		UserBankDTO userBankDTO = queryUserBank.getData();
		String realName = userBankDTO.getRealName();
		String cardNo = userBankDTO.getCardNo();
		//生成提现单
		UserWithdrawParam userWithdrawParam = new UserWithdrawParam();
		userWithdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
		userWithdrawParam.setCardNo(cardNo);
		userWithdrawParam.setRealName(realName);
		BaseResult<UserWithdrawDTO> createUserWithdraw = userAccountService.createUserWithdraw(userWithdrawParam);
		if(createUserWithdraw.getCode() != 0) {
			logger.info(loggerId+" 生成提现单，code="+createUserWithdraw.getCode()+" , msg="+createUserWithdraw.getMsg());
			return ResultGenerator.genFailResult("提现失败！", null);
		}
		String orderSn = createUserWithdraw.getData().getWithdrawalSn();
		//保存提现进度
		UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
		userWithdrawLog.setLogCode(1);
		userWithdrawLog.setLogName("提现申请");
		userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
		userWithdrawLog.setWithdrawSn(orderSn);
		userWithdrawLogService.save(userWithdrawLog);
		//生成提现记录payLog,该操作在提现暂时不需要
		/*String payName = "第三方接口";
		String payIp = this.getIpAddr(request);
		String payCode = "withdraw_api";
		PayLog payLog = super.newPayLog(orderSn, BigDecimal.valueOf(totalAmount), 2, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}*/
		//消息
		MessageAddParam messageAddParam = new MessageAddParam();
		messageAddParam.setTitle("申请提现");
		messageAddParam.setContent("提现"+totalAmount+"元");
		messageAddParam.setContentDesc("提交申请");
		messageAddParam.setMsgType(1);
		messageAddParam.setReceiver(SessionUtil.getUserId());
		messageAddParam.setReceiveMobile(mobile);
		messageAddParam.setObjectType(2);
		messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
		Integer addTime = createUserWithdraw.getData().getAddTime();
		LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.UTC);
		StringBuilder msgDesc = new StringBuilder();
		msgDesc.append("申请时间：").append(loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:dd"))).append("\n")
		.append("审核时间：").append("\n")
		.append("提现成功时间：");
		messageAddParam.setMsgDesc(msgDesc.toString());
		userMessageService.add(messageAddParam);
		return ResultGenerator.genSuccessResult("请求成功！");
	}
	
	@ApiOperation(value="支付订单结果 查询 ", notes="")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<RspOrderQueryDTO> orderquery(@RequestBody ReqOrderQueryParam p) {
		String loggerId = "orderquery_" + System.currentTimeMillis();
		String payLogId = p.getPayLogId();
		if(StringUtils.isBlank(payLogId) ) {
			return ResultGenerator.genFailResult("订单号不能为空！",null);
		}
		logger.info(loggerId+" payLogId="+payLogId);
		PayLog payLog = payLogService.findById(Integer.parseInt(payLogId));
		if(null == payLog) {
			logger.info(loggerId+" payLogId="+payLogId+" 没有查询到对应的订单号");
			return ResultGenerator.genFailResult("请提供有效的订单号！", null);
		}
		int isPaid = payLog.getIsPaid();
		if(1 == isPaid) {
			logger.info(loggerId+" 订单已支付成功");
			return ResultGenerator.genSuccessResult("订单已支付成功！",null);
		}
		String payCode = payLog.getPayCode();
		BaseResult<RspOrderQueryEntity> baseResult = null;
		if("app_rongbao".equals(payCode)) {
			baseResult = RongUtil.queryOrderInfo(payLog.getPayOrderSn());
		}
		if(baseResult != null) {
			if(baseResult.getCode() != 0) {
				logger.info(loggerId+" 订单查询请求异常"+baseResult.getMsg());
				return ResultGenerator.genFailResult("请求异常！",null);
			}
			RspOrderQueryEntity response = baseResult.getData();
			Integer payType = payLog.getPayType();
			if(0 == payType) {
				return orderOptions(loggerId, payLog, response);
			}else if(1 == payType){
				return rechargeOptions(loggerId, payLog, response);
			}
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
	private BaseResult<RspOrderQueryDTO> rechargeOptions(String loggerId, PayLog payLog, RspOrderQueryEntity response) {
//		Integer tradeState = response.getTradeState();
		RspOrderQueryDTO rspEntity = new RspOrderQueryDTO();
		if(response.isSucc()) {
			int currentTime = DateUtil.getCurrentTimeLong();
			//更新order
			UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
			updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
			updateUserRechargeParam.setPaymentId(payLog.getLogId()+"");
			updateUserRechargeParam.setPaymentName(payLog.getPayName());
			updateUserRechargeParam.setPayTime(currentTime);
			updateUserRechargeParam.setStatus("1");
			updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
			BaseResult<String> updateReCharege = userAccountService.updateReCharege(updateUserRechargeParam);
			if(updateReCharege.getCode() != 0) {
				logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新充值单成功状态失败");
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTrade_no());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("支付成功");
				payLogService.update(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId+" paylogid="+payLog.getLogId()+" , paymsg=支付成功，保存成功记录时出错", e);
			}
			rspEntity.setCode(0);
			rspEntity.setMsg("订单已支付成功");
			return ResultGenerator.genSuccessResult("订单已支付成功！", rspEntity);
		}else {
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(response.getResult_msg());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getResult_msg()+"，保存失败记录时出错", e);
			}
			rspEntity.setCode(-1);
			rspEntity.setMsg("请求失败");
			return ResultGenerator.genFailResult("请求失败！", rspEntity);
		}
	}
	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 */
	private BaseResult<RspOrderQueryDTO> orderOptions(String loggerId, PayLog payLog, RspOrderQueryEntity response) {
		if(response.isSucc()) {
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
				logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTrade_no());
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
				return ResultGenerator.genFailResult("订单获取失败！", null);
			}
			BigDecimal surplus = orderInfoByOrderSn.getData().getSurplus();
			BigDecimal bonusAmount = orderInfoByOrderSn.getData().getBonus();
			if(surplus != null && surplus.doubleValue() > 0){
				SurplusPayParam surplusPayParam = new SurplusPayParam();
				surplusPayParam.setOrderSn(orderSn);
				surplusPayParam.setSurplus(surplus);
				surplusPayParam.setBonusMoney(bonusAmount);
				int payType1 = 0;
				surplusPayParam.setPayType(payType1);
				surplusPayParam.setThirdPartName(payLog.getPayName());
				surplusPayParam.setThirdPartPaid(payLog.getOrderAmount());
				BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
				if(rollbackUserAccountChangeByPay.getCode() != 0) {
					logger.error(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
				}
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(0);
				updatePayLog.setPayMsg(response.getResult_msg());
				payLogService.updatePayMsg(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getResult_msg()+"，保存失败记录时出错", e);
			}
			return ResultGenerator.genFailResult("请求失败！", null);
		}
	}
	
	
}
