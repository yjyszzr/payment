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
import org.apache.http.util.TextUtils;
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
import com.dl.member.dto.UserWithdrawDTO;
import com.dl.member.param.IDParam;
import com.dl.member.param.MessageAddParam;
import com.dl.member.param.RecharegeParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserAccountParamByType;
import com.dl.member.param.UserWithdrawParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.SubmitOrderParam;
import com.dl.order.param.SubmitOrderParam.TicketDetail;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dto.PayReturnDTO;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.AllPaymentInfoParam;
import com.dl.shop.payment.param.GoPayParam;
import com.dl.shop.payment.param.RechargeParam;
import com.dl.shop.payment.param.ReqOrderQueryParam;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRongEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.pay.yinhe.util.PayUtil;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.UserRechargeService;
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
	@Resource
	private YinHeUtil yinHeUtil;
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
	@Resource
	private UserRechargeService userRechargeService;
	@Resource
	private PayUtil payUtil;
	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private ReapalH5Config rongCfg;
	@Resource
	private RongUtil rongUtil;
	
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
	public BaseResult<?> rollbackOrderAmount(@RequestBody RollbackOrderAmountParam param) {
		return paymentService.rollbackOrderAmount(param);
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
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EMPTY.getcode(),PayEnums.PAY_TOKEN_EMPTY.getMsg());
		}
		//校验payToken的有效性
		String jsonData = stringRedisTemplate.opsForValue().get(payToken);
		if(StringUtils.isBlank(jsonData)) {
			logger.info(loggerId + "支付信息获取为空！");
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EXPRIED.getcode(),PayEnums.PAY_TOKEN_EXPRIED.getMsg());
		}
		//清除payToken
		stringRedisTemplate.delete(payToken);
		
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
			ticketDetail.setFixedodds(betCell.getFixedodds());
			return ticketDetail;
		}).collect(Collectors.toList());
		//余额支付
		boolean hasSurplus = false;
		if((surplus != null && surplus.doubleValue() > 0) || (bonusAmount != null && bonusAmount.doubleValue() > 0)) {
			hasSurplus = true;
		}
		//临时添加
		boolean isSurplus = false;
		if(surplus != null && surplus.doubleValue() > 0) {
			isSurplus = true;
		}
		//第三方支付
		boolean hasThird = false;
		if(thirdPartyPaid != null && thirdPartyPaid.doubleValue() > 0) {
			hasThird = true;
			String payCode = param.getPayCode();
			if(StringUtils.isBlank(payCode)) {
				logger.info(loggerId + "第三方支付，paycode为空~");
				return ResultGenerator.genResult(PayEnums.PAY_CODE_BLANK.getcode(),PayEnums.PAY_CODE_BLANK.getMsg());
			}
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
			surplusPayParam.setMoneyPaid(surplus);
			surplusPayParam.setThirdPartName("");
			surplusPayParam.setThirdPartPaid(BigDecimal.ZERO);
			if(isSurplus) {
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
				if(baseResult.getCode() != 0 && isSurplus) {
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
					if(isSurplus) {
						BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
						logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在预出票失败更新回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
						if(rollbackUserAccountChangeByPay.getCode() != 0) {
							logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在预出票失败更新回滚用户余额时出错！");
						}
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
		//payCode处理
		String payCode = paymentDto.getPayCode();
		if("app_weixin".equals(payCode)) {
			boolean isWechat = (param.getInnerWechat()==1);
			if(isWechat) {
				payCode = "app_weixin" + "_h5";
			}
		}
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(orderSn, thirdPartyPaid,0,payCode,paymentDto.getPayName(), payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}else {
			logger.info("paylog save succ:" + " payLogId:" + payLog.getPayIp() + " paycode:" + payLog.getPayCode() + " payname:" + payLog.getPayName());
		}
		//url下发后，服务器开始主动轮序订单状态
		PayManager.getInstance().addReqQueue(orderSn,savePayLog.getPayOrderSn(),paymentDto.getPayCode());
		BaseResult payBaseResult = null;
		if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)) {
			logger.info("生成微信支付url:" + "inWechat:" + (param.getInnerWechat()==1) + " payCode:" + savePayLog.getPayCode());
			payBaseResult = getWechatPayUrl(param.getInnerWechat()==1,param.getIsH5(),0,savePayLog, payIp, orderId);
			if(payBaseResult != null &&payBaseResult.getData() != null) {
				String str = payBaseResult.getData()+"";
				logger.info("生成支付url成功:" + str);
			}
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
				String url = rongCfg.getURL_PAY() + "?data="+data;
				PayReturnDTO rEntity = new PayReturnDTO();
				rEntity.setPayUrl(url);
				rEntity.setPayLogId(savePayLog.getLogId()+"");
				rEntity.setOrderId(orderId);
				payBaseResult = ResultGenerator.genSuccessResult("succ",rEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		logger.info(loggerId + " result: code="+payBaseResult.getCode()+" , msg="+payBaseResult.getMsg());
		return payBaseResult;
	}
	
	/***
	 * 根据savePayLog生成微信支付链接
	 * @param savePayLog
	 * @param payIp
	 * @param orderId
	 * @param payType 0->支付   1->充值  
	 * @return
	 */
	private BaseResult<?> getWechatPayUrl(boolean isInnerWeChat,String isH5,int payType,PayLog savePayLog,String payIp,String orderId) {
		BaseResult<?> payBaseResult = null;
		String strAmt = savePayLog.getOrderAmount().doubleValue()+"";
		BigDecimal bigD = new BigDecimal(strAmt);
		strAmt = bigD.movePointRight(2).toString();
		String payOrderSn = savePayLog.getPayOrderSn();
		String payLogId = savePayLog.getLogId()+"";
		RspYinHeEntity rYinHeEntity = null;
		if(isInnerWeChat) {
			//公共账号方式支付
//			rYinHeEntity = new RspYinHeEntity();
//			rYinHeEntity.returnCode = "0000";
//			try {
//				String redirect_uri = URLDecoder.decode("http://zf.caixiaomi.net/reapal-h5-api/wechat/reqcode.jsp","UTF-8");
//				rYinHeEntity.qrCode = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx50d353a8b7b77225&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			rYinHeEntity = payUtil.getWechatPayUrl(true,payIp,strAmt,payOrderSn);
		}else {
			rYinHeEntity = payUtil.getWechatPayUrl(false,payIp,strAmt,payOrderSn);
		}
		if(rYinHeEntity != null) {
			if(rYinHeEntity.isSucc() && !TextUtils.isEmpty(rYinHeEntity.qrCode)) {
				PayReturnDTO rEntity = new PayReturnDTO();
				String encodeUrl = null;
				String redirectUri = null;
				String url = null;
				if(!isInnerWeChat) {
					try {
						String qrCode = rYinHeEntity.qrCode;
						encodeUrl = URLEncoder.encode(qrCode,"UTF-8");
						if("1".equals(isH5)) {
							redirectUri = URLEncoder.encode(cfgPay.getURL_REDIRECT_H5()+"?payLogId="+payLogId,"UTF-8");
						}else {
							redirectUri = URLEncoder.encode(cfgPay.getURL_REDIRECT_APP()+"?payLogId="+payLogId,"UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error(e.getMessage());
					}
					if(!TextUtils.isEmpty(encodeUrl)) {
						if("1".equals(isH5)) {
							url = cfgPay.getURL_PAY_WECHAT_H5()+"?data="+encodeUrl+"&redirect_uri=" + redirectUri;
						}else {
							url = cfgPay.getURL_PAY_WECHAT_APP()+"?data="+encodeUrl+"&redirect_uri=" + redirectUri;	
						}
					}else {
						logger.info("encodeUrl失败~");
					}
				}else {
					url = rYinHeEntity.qrCode;
				}
				if(!TextUtils.isEmpty(url)) {
					rEntity.setPayUrl(url);
					rEntity.setPayLogId(savePayLog.getLogId()+"");
					rEntity.setOrderId(orderId);
					logger.info("client jump url:" + url +" payLogId:" +savePayLog.getLogId() +" orderId:" + orderId + " inWechat:" + isInnerWeChat);
					payBaseResult = ResultGenerator.genSuccessResult("succ",rEntity);
				}else {
					payBaseResult = ResultGenerator.genFailResult("url decode失败",null);
				}
			}else {
				payBaseResult = ResultGenerator.genResult(PayEnums.PAY_YINHE_INNER_ERROR.getcode(),PayEnums.PAY_YINHE_INNER_ERROR.getMsg()+"[" + rYinHeEntity.returnMsg+"]");
			}
		}else {
			payBaseResult = ResultGenerator.genFailResult("银河支付返回数据有误");
		}
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
			return ResultGenerator.genResult(PayEnums.RECHARGE_AMT_ERROR.getcode(),PayEnums.RECHARGE_AMT_ERROR.getMsg());
		}
		//当前支付方式限额10万/笔
		if(totalAmount > 100000) {
			logger.info(loggerId + "每笔限额10w");
			return ResultGenerator.genResult(PayEnums.PAY_RECHARGE_MAX.getcode(),PayEnums.PAY_RECHARGE_MAX.getMsg());
		}
		//支付方式
		String payCode = param.getPayCode();
		if(StringUtils.isBlank(payCode)) {
			logger.info(loggerId + "订单第三支付没有提供paycode！");
			return ResultGenerator.genResult(PayEnums.RECHARGE_PAY_STYLE_EMPTY.getcode(),PayEnums.RECHARGE_PAY_STYLE_EMPTY.getMsg());
		}
		BaseResult<PaymentDTO> paymentResult = paymentService.queryByCode(payCode);
		if(paymentResult.getCode() != 0) {
			logger.info(loggerId + "订单第三方支付提供paycode有误！");
			return ResultGenerator.genResult(PayEnums.RECHARGE_PAY_STYLE_EMPTY.getcode(), PayEnums.RECHARGE_PAY_STYLE_EMPTY.getMsg());
		}
		//生成充值单
		String rechargeSn = userRechargeService.saveReCharege(BigDecimal.valueOf(totalAmount));
		
		if(StringUtils.isEmpty(rechargeSn)) {
			logger.info(loggerId + "生成充值单失败");
			return ResultGenerator.genFailResult("充值失败！", null);
		}
		String orderSn = rechargeSn;
		//生成充值记录payLog
		String payName = paymentResult.getData().getPayName();
		String payIp = this.getIpAddr(request);
		//payCode处理
		if("app_weixin".equals(payCode)) {
			boolean isWechat = (param.getInnerWechat()==1);
			if(isWechat) {
				payCode = "app_weixin" + "_h5";
			}
		}
		PayLog payLog = super.newPayLog(orderSn, BigDecimal.valueOf(totalAmount), 1, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}else {
			logger.info("save paylog succ:" + " id:" + payLog.getPayIp() + " paycode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
		}
		//第三方支付调用
		UnifiedOrderParam unifiedOrderParam = new UnifiedOrderParam();
		unifiedOrderParam.setBody("余额充值");
		unifiedOrderParam.setSubject("余额充值");
		unifiedOrderParam.setTotalAmount(totalAmount);
		unifiedOrderParam.setIp(payIp);
		unifiedOrderParam.setOrderNo(savePayLog.getLogId());
		//url下发后，服务器开始主动轮序订单状态
		PayManager.getInstance().addReqQueue(orderSn,savePayLog.getPayOrderSn(),payCode);
		BaseResult payBaseResult = null;
		if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)){
			logger.info("微信支付url开始生成...isWechat:" + (param.getInnerWechat()==1) + " payOrderSn:" + savePayLog.getPayOrderSn());
			payBaseResult = getWechatPayUrl(param.getInnerWechat()==1,param.getIsH5(),1,savePayLog, payIp, orderSn);
			logger.info("微信支付url生成成功 code" + payBaseResult.getCode() +" data:" +payBaseResult.getData());
		}else if("app_rongbao".equals(payCode)) {
			//生成支付链接信息
			String payOrder = savePayLog.getPayOrderSn();
			ReqRongEntity reqEntity = new ReqRongEntity();
			reqEntity.setOrderId(payOrder);
			reqEntity.setUserId(savePayLog.getUserId().toString());
			reqEntity.setTotal(savePayLog.getOrderAmount().doubleValue());
			reqEntity.setPName("彩小秘");
			reqEntity.setPDesc("彩小秘充值支付");
			reqEntity.setTransTime(savePayLog.getAddTime()+"");
			String data = JSON.toJSONString(reqEntity);
			try {
				data = URLEncoder.encode(data,"UTF-8");
				String url = rongCfg.getURL_PAY() + "?data="+data;
				PayReturnDTO rEntity = new PayReturnDTO();
				rEntity.setPayUrl(url);
				rEntity.setPayLogId(savePayLog.getLogId()+"");
				rEntity.setOrderId(orderSn);
				payBaseResult = ResultGenerator.genSuccessResult("succ",rEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		//处理支付失败的情况
		if(null == payBaseResult || payBaseResult.getCode() != 0) {
			//充值失败逻辑
			//更改充值单状态
			UpdateUserRechargeParam updateUserParams = new UpdateUserRechargeParam();
			updateUserParams.setPaymentCode(payLog.getPayCode());
			updateUserParams.setPaymentId(payLog.getLogId()+"");
			updateUserParams.setPaymentName(payLog.getPayName());
			updateUserParams.setPayTime(DateUtil.getCurrentTimeLong());
			updateUserParams.setRechargeSn(rechargeSn);
			updateUserParams.setStatus("2");
			BaseResult<String> baseResult = userRechargeService.updateReCharege(updateUserParams);
			logger.info(loggerId + " 充值失败更改充值单返回信息：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
			if(baseResult.getCode() == 0) {
				//更改流水信息
				try {
					PayLog updatePayLog = new PayLog();
					updatePayLog.setLogId(savePayLog.getLogId());
					updatePayLog.setIsPaid(0);
					updatePayLog.setPayMsg(baseResult.getMsg());
					payLogService.updatePayMsg(updatePayLog);
				} catch (Exception e) {
					logger.error(loggerId + "paylogid="+savePayLog.getLogId()+" , paymsg="+baseResult.getMsg()+"保存失败记录时出错", e);
				}
			}
		}
		if(payBaseResult != null) {
			logger.info(loggerId + " result: code="+payBaseResult.getCode()+" , msg="+payBaseResult.getMsg());
			return payBaseResult;
		}else {
			return ResultGenerator.genFailResult("参数异常");
		}
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
//		double totalAmount = param.getTotalAmount();
		double totalAmount = 0;
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
		logger.info("查询订单:" + loggerId + " payCode:"  +payLog.getPayCode());
		int isPaid = payLog.getIsPaid();
		if(1 == isPaid) {
			logger.info(loggerId+" 订单已支付成功");
			return ResultGenerator.genSuccessResult("订单已支付成功！",null);
		}
		String payCode = payLog.getPayCode();
		BaseResult<RspOrderQueryEntity> baseResult = null;
		logger.info("调用第三方订单查询接口 payCode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
		
		if("app_rongbao".equals(payCode)) {
			baseResult = rongUtil.queryOrderInfo(payLog.getPayOrderSn());
		}else if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)) {
			boolean isInWeixin = "app_wexin_h5".equals(payCode);
//			baseResult = wxpayUtil.orderQuery(payLog.getPayOrderSn());
			baseResult = yinHeUtil.orderQuery(isInWeixin,payLog.getPayOrderSn());
		}
		
		if(baseResult != null) {
			if(baseResult.getCode() != 0) {
				logger.info(loggerId+" 订单查询请求异常"+baseResult.getMsg());
				return ResultGenerator.genFailResult("请求异常！",null);
			}
			
			Integer payType = payLog.getPayType();
			RspOrderQueryEntity response = baseResult.getData();
			logger.info("调用第三方订单查询接口 返回成功" + 
			response.getResult_msg() +" payType:" +payType + " isSucc:" + response.isSucc() + 
			"resultCode:"+response.getResult_code());
			if(0 == payType) {
				return orderOptions(paymentService,lotteryPrintService,orderService,payLogService,userAccountService,loggerId, payLog, response);
			}else if(1 == payType){
				return rechargeOptions(userRechargeService,userAccountService,payLogService,loggerId, payLog, response);
			}
		}else {
			return ResultGenerator.genFailResult("未获取到订单信息，开发中...", null);
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
	public static BaseResult<RspOrderQueryDTO> rechargeOptions(UserRechargeService userRechargeService,
			IUserAccountService userAccountService,PayLogService payLogService,String loggerId, PayLog payLog, RspOrderQueryEntity response) {
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
			userRechargeService.updateReCharege(updateUserRechargeParam);
			
			RecharegeParam recharegeParam = new RecharegeParam();
			recharegeParam.setAmount(payLog.getOrderAmount());
			recharegeParam.setPayId(response.getOrder_no());
			String payCode = response.getPayCode();
			if(payCode.equals("app_weixin")) {
				recharegeParam.setThirdPartName("微信");
			}else if(payCode.equals("app_rongbao")){
				recharegeParam.setThirdPartName("银行卡");
			}
			recharegeParam.setThirdPartPaid(payLog.getOrderAmount());
			recharegeParam.setUserId(payLog.getUserId());
			BaseResult<String>  rechargeRst = userAccountService.rechargeUserMoneyLimit(recharegeParam);
			if(rechargeRst.getCode() != 0) {
				logger.error(loggerId+" 给个人用户充值：code"+rechargeRst.getCode() +"message:"+rechargeRst.getMsg());
			}
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTrade_no());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("充值成功");
				payLogService.update(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId+" paylogid="+payLog.getLogId()+" , paymsg=支付成功，保存成功记录时出错", e);
			}
			return ResultGenerator.genSuccessResult("订单已支付成功");
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
			String payCode = response.getPayCode();
			if(RspOrderQueryEntity.PAY_CODE_RONGBAO.equals(payCode)) {
				String code = response.getResult_code();
				if(StringUtils.isBlank(code) || "3015".equals(code)) {//订单不存在
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
				}else {
					String tips = response.getResult_msg();
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"融宝服务返回[" + tips +"]");
				}
			}else {
				String code = response.getResult_code(); //104 -> 未支付  404 -> 订单不存在
				if(StringUtils.isBlank(code) || response.isYinHeWeChatNotPay()) {
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
				}else {
					String tips = response.getResult_msg();
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"微信支付失败["+tips+"]");	
				}
			}
		}
	}
	
	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 */
	public static BaseResult<RspOrderQueryDTO> orderOptions(PayMentService paymentService,ILotteryPrintService lotteryPrintService,IOrderService orderService, PayLogService payLogService,IUserAccountService userAccountService,String loggerId, PayLog payLog, RspOrderQueryEntity response) {
		if(response.isSucc()) {
			//预出票操作
			String orderSn = payLog.getOrderSn();
			int currentTime = DateUtil.getCurrentTimeLong();
			SaveLotteryPrintInfoParam saveLotteryPrintParam = new SaveLotteryPrintInfoParam();
			saveLotteryPrintParam.setOrderSn(orderSn);
			BaseResult<String> saveLotteryPrintInfo = lotteryPrintService.saveLotteryPrintInfo(saveLotteryPrintParam);
			boolean isLotteryPrintSucc = false;
			if(saveLotteryPrintInfo.getCode() != 0) {
				isLotteryPrintSucc = false;
			}else {
				isLotteryPrintSucc = true;
			}
			logger.info("查询已经支付成功，进行预出票操作...isLotteryPrintSucc:" + isLotteryPrintSucc);
			//更新order
			UpdateOrderInfoParam param = new UpdateOrderInfoParam();
			if(isLotteryPrintSucc) {
				param.setOrderStatus(1);	
			}else {
				param.setOrderStatus(2);//2->出票失败   1->待出票
			}
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
				updatePayLog.setTradeNo(response.getTrade_no());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("支付成功");
				payLogService.update(updatePayLog);
			} catch (Exception e) {
				logger.error(loggerId+" paylogid="+payLog.getLogId()+" , paymsg=支付成功，保存成功记录时出错", e);
			}
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
			String payCode = payLog.getPayCode();
			String payName;
			if(payCode.equals("app_weixin") || payCode.equals("app_weixin_h5")) {
				payName = "微信";
			}else {
				payName = "银行卡";
			}
			userAccountParamByType.setPaymentName(payName);
			userAccountParamByType.setThirdPartName(payName);
			userAccountParamByType.setThirdPartPaid(new BigDecimal(payLog.getOrderAmount().doubleValue()));
			userAccountParamByType.setUserId(payLog.getUserId());
			BaseResult<String> accountRst = userAccountService.insertUserAccount(userAccountParamByType);
			if(accountRst.getCode() != 0) {
				logger.info(loggerId + "生成账户流水异常");
			}else {
				logger.info("生成账户流水成功");
			}
			if(!isLotteryPrintSucc) {
				//资金回滚
				RollbackOrderAmountParam p = new RollbackOrderAmountParam();
				p.setOrderSn(orderSn);
				paymentService.rollbackOrderAmount(p);
			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else {
			//预扣款 的方案 这里什么也不做
//			String payCode = response.getPayCode();
//			String code = response.getResult_code();
//			if(StringUtils.isBlank(code) || "3015".equals(code) || response.isYinHeWeChatNotPay()) {//融宝和银河返回值  为 订单不存在和未支付
//				dealWithPayFailure(orderService, payLog,payLogService, response);
//			}
//			//融宝处理
//			if(RspOrderQueryEntity.PAY_CODE_RONGBAO.equals(payCode)) {
//				String code = response.getResult_code();
//				if(StringUtils.isBlank(code) || "3015".equals(code)) {//订单不存在
//					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
//				}else {
//					String tips = response.getResult_msg();
//					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"融宝服务返回[" + tips +"]");
//				}
//			//微信处理	
//			}else if(RspOrderQueryEntity.PAY_CODE_WECHAT.equals(payCode)){//wechat pay
//				String code = response.getResult_code();
//				String tips = response.getResult_msg();
//				if(StringUtils.isBlank(code) || response.isYinHeWeChatNotPay()) {
//					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
//				}else {
//					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"微信支付失败["+tips+"]");	
//				}
//			}
//			return null;
		}
		return null;
	}
	
	/**
	 * 第三方返回结果为非成功状态：包括不存在和失败
	 * @param payLog
	 * @param response
	 * @return
	 */
	public static BaseResult<String> dealWithPayFailure(IOrderService orderService,PayLog payLog,PayLogService payLogService,RspOrderQueryEntity response) {
		Integer loggerId = payLog.getLogId();
		int currentTime = DateUtil.getCurrentTimeLong();
//		//更新order
//		UpdateOrderInfoParam param = new UpdateOrderInfoParam();
//		param.setPayStatus(0);//支付失败
//		param.setOrderStatus(8);//订单支付失败
//		param.setPayTime(currentTime);
//		param.setPaySn(payLog.getLogId()+"");
//		param.setPayName(payLog.getPayName());
//		param.setPayCode(payLog.getPayCode());
//		param.setOrderSn(payLog.getOrderSn());
//		BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(param);
//		if(updateOrderInfo.getCode() != 0) {
//			logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
//			return ResultGenerator.genFailResult("更新订单成功状态失败！", "");
//		}
		
		//更新paylog
		try {
			PayLog updatePayLog = new PayLog();
			updatePayLog.setLogId(payLog.getLogId());
			updatePayLog.setIsPaid(0);
			updatePayLog.setPayMsg(response.getResult_msg());
			payLogService.updatePayMsg(updatePayLog);
		} catch (Exception e) {
			logger.error(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getResult_msg()+"，保存失败记录时出错", e);
			return ResultGenerator.genFailResult(loggerId + " paylogid="+payLog.getLogId()+" , paymsg="+response.getResult_msg()+"，保存失败记录时出错", "");
		}
		
		return ResultGenerator.genSuccessResult("更新订单成功状态成功！", "");
	}
}
