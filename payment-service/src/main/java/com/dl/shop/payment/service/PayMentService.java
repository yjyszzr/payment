package com.dl.shop.payment.service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.lottery.api.ILotteryPrintService;
import com.dl.lottery.param.SaveLotteryPrintInfoParam;
import com.dl.member.api.IActivityService;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBonusService;
import com.dl.member.dto.DonationPriceDTO;
import com.dl.member.dto.RechargeDataActivityDTO;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.param.RecharegeParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserAccountParamByType;
import com.dl.member.param.UserBonusParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.PayMent;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRefundEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PayMentService extends AbstractService<PayMent> {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
    @Resource
    private PayMentMapper payMentMapper;
    
    @Resource
    private IOrderService orderService;
    
    @Resource
    private IUserAccountService userAccountService;
    
    @Resource
    private PayLogService payLogService;
    
	@Resource
	private PayLogMapper payLogMapper;
	
	@Resource
	private YinHeUtil yinHeUtil;

	@Resource
	private RongUtil rongUtil;
	
	@Resource
	private  IActivityService activityService;

	@Resource
	private IUserBonusService userBonusService;
	
	@Resource
	private UserRechargeService userRechargeService;
	
	@Resource
	private ILotteryPrintService lotteryPrintService;
	
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	
    /**
     * 查询所有可用的支付方式
     * @return
     */
    public List<PaymentDTO> findAllDto() {
		List<PayMent> payments = super.findAll();
		if(CollectionUtils.isEmpty(payments)) {
			return new ArrayList<PaymentDTO>();
		}
		List<PaymentDTO> list = payments.stream().filter(payment->payment.getIsEnable() == 1).map(payment->{
			PaymentDTO paymentDTO = new PaymentDTO();
			paymentDTO.setPayCode(payment.getPayCode());
			paymentDTO.setPayDesc(payment.getPayDesc());
			paymentDTO.setPayId(payment.getPayId());
			paymentDTO.setPayName(payment.getPayName());
			paymentDTO.setPaySort(payment.getPaySort());
			paymentDTO.setPayType(payment.getPayType());
			paymentDTO.setPayTitle(payment.getPayTitle());
			paymentDTO.setPayImg(payment.getPayImg());
			return paymentDTO;
		}).collect(Collectors.toList());
		return list;
	}
    /**
     * 通过payCode读取可用支付方式
     * @param payCode
     * @return
     */
    public BaseResult<PaymentDTO> queryByCode(String payCode) {
		List<PaymentDTO> paymentDTOs = this.findAllDto();
		Optional<PaymentDTO> optional = paymentDTOs.stream().filter(dto-> dto.getPayCode().equals(payCode)).findFirst();
		return optional.isPresent()?ResultGenerator.genSuccessResult("success", optional.get()):ResultGenerator.genFailResult("没有匹配的记录！");
	}
    
    /**
     * 处理支付超时订单
     */
    @Transactional
    public void dealBeyondPayTimeOrder(OrderDTO or) {
    	SurplusPayParam surplusPayParam = new SurplusPayParam();
    	surplusPayParam.setOrderSn(or.getOrderSn());
    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
    	if(rollRst.getCode() != 0) {
    		log.error(rollRst.getMsg());
    		return;
    	}
    	
    	Integer userBonusId = or.getUserBonusId();
    	if(null != userBonusId) {
    		UserBonusParam userbonusParam = new UserBonusParam();
    		userbonusParam.setUserBonusId(userBonusId);
    		userbonusParam.setOrderSn(or.getOrderSn());
    		userAccountService.rollbackChangeUserAccountByCreateOrder(userbonusParam);
    	}
    	if(rollRst.getCode() != 0) {
    		log.error("-------------------支付超时订单回滚用户余额异常,code="+rollRst.getCode()+"  msg:"+rollRst.getMsg()+" 订单号："+or.getOrderSn());
    	}else {
    		log.info(JSON.toJSONString("用户"+or.getUserId()+"超时支付订单"+or.getOrderSn()+"已回滚账户余额"));
    	}    	
    	
    	UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    	updateOrderInfoParam.setOrderSn(or.getOrderSn());
    	updateOrderInfoParam.setOrderStatus(8);//订单失败
    	updateOrderInfoParam.setPayStatus(2);//支付失败
    	updateOrderInfoParam.setPayTime(DateUtil.getCurrentTimeLong());
    	BaseResult<String> updateRst = orderService.updateOrderInfoStatus(updateOrderInfoParam);
    	if(updateRst.getCode() != 0) {
    		log.error("-------------------支付超时订单更新订单为出票失败 异常，返回，code="+updateRst.getCode()+"  msg:"+updateRst.getMsg()+" 订单号："+or.getOrderSn());
    		return;
    	}

		//logger.info("调用第三方订单查询接口 payCode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
//    	BaseResult<RspOrderQueryEntity>  baseResult = null;
//    	String payCode = or.getPayCode();
//    	PayLog payLog = new PayLog();
//    	payLog.setOrderSn(or.getOrderSn());
//    	payLog.setPayCode(or.getPayCode());
//    	payLog.setPayType(0);
//    	PayLog payLogDelay = payLogMapper.existPayLog(payLog);
//		if("app_rongbao".equals(payCode)) {
//			baseResult = RongUtil.queryOrderInfo(payLogDelay.getPayOrderSn());
//		}else if("app_weixin".equals(payCode)) {
//			baseResult = yinHeUtil.orderQuery(false,payLog.getPayOrderSn());
//		}
//		if(baseResult.getCode() != 0) {
//			log.error("查询第三方"+payCode+"异常:"+baseResult.getMsg());
//		}
//		RspOrderQueryEntity rspEntity = baseResult.getData();
//		if(rspEntity.isSucc()) {
//			return;//第三方付款成功，就不再回退余额
//		}
    	
    	PayLog updatepayLog = new PayLog();
    	updatepayLog.setIsPaid(ProjectConstant.IS_PAID_FAILURE);
    	updatepayLog.setOrderSn(or.getOrderSn());
    	payLogService.updatePayLogByOrderSn(updatepayLog);

    }
    
    /**
     * 资金回滚
     * @param param
     * @return
     */
	public BaseResult<?> rollbackOrderAmount(RollbackOrderAmountParam param) {
		log.info("in rollbackOrderAmount ordersn=" + param.getOrderSn());
		String orderSn = param.getOrderSn();
		OrderSnParam snParam = new OrderSnParam();
		snParam.setOrderSn(orderSn);
		BaseResult<OrderDTO> orderRst = orderService.getOrderInfoByOrderSn(snParam);
		if(orderRst.getCode() != 0) {
			log.info("orderService.getOrderInfoByOrderSn rst code="+orderRst.getCode()+" msg="+orderRst.getMsg());
			return ResultGenerator.genFailResult();
		}
		OrderDTO order = orderRst.getData();
		BigDecimal surplus = order.getSurplus();
		BigDecimal bonusAmount = order.getBonus();
		BigDecimal moneyPaid = order.getMoneyPaid();
		String payName = order.getPayName();
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		Integer userBonusId = order.getUserBonusId();
		//退回优惠券
		log.info("优惠券退回操作 userBonusId:" + userBonusId);
		if(userBonusId != null && userBonusId > 0) {
			UserBonusParam userBP = new UserBonusParam();
			userBP.setUserBonusId(userBonusId);
			userBP.setOrderSn(orderSn);
			BaseResult<String> baseResult = userAccountService.rollbackChangeUserAccountByCreateOrder(userBP);
			if(baseResult.getCode() == 0) {
				log.info("优惠券退回成功...");
			}else {
				log.info("优惠券退回失败...");
			}
		}
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
		boolean succThird = false;
		log.info("出票失败含有第三方支付:" + hasThird);
		if(hasThird) {
			orderSn = order.getOrderSn();
			log.info("出票失败含有第三方支付 订单orderSn:" + orderSn);
			if(!TextUtils.isEmpty(orderSn)) {
				PayLog payLog = payLogService.findPayLogByOrderSn(orderSn);
				if(payLog == null) {
					return ResultGenerator.genFailResult("回滚订单不存在 orderSn:" + orderSn);
				}
				String payCode = payLog.getPayCode();
				log.info("回滚查询PayLog信息:" + " payCode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
				RspRefundEntity rspRefundEntity = null;
				if(payLog != null) {
					if(payCode.equals("app_rongbao")) {
						ReqRefundEntity reqEntity = new ReqRefundEntity();
						reqEntity.setAmount(thirdPartyPaid.toString());
						reqEntity.setNote("出票失败退款操作");
						reqEntity.setOrig_order_no(payLog.getPayOrderSn());
						try {
							rspRefundEntity = rongUtil.refundOrderInfo(reqEntity);
							log.info("rEntity:" + rspRefundEntity.toString());
							if(rspRefundEntity != null && rspRefundEntity.isSucc()) {
								succThird = true;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if(payCode.equals("app_weixin") || "app_weixin_h5".equals(payCode)){
						boolean isInWeChat = "app_weixin_h5".equals(payCode);
						String amt = thirdPartyPaid.toString();
						BigDecimal bigDec = new BigDecimal(amt);
						String amtFen = bigDec.movePointRight(2).intValue()+"";
						log.info("=========================");
						log.info("进入到了微信订单回滚 isInWeChat：" + isInWeChat + " amtFen" + amtFen + "payOrderSn:" + payLog.getPayOrderSn());
						rspRefundEntity = yinHeUtil.orderRefund(isInWeChat,payLog.getPayOrderSn(),amtFen);
						if(rspRefundEntity.isSucc()) {
							succThird = true;
						}
						log.info("微信订单回滚 isSucc:" + rspRefundEntity.isSucc() + " msg:" + rspRefundEntity.toString());
						log.info("=========================");
					}
					//第三方资金退回
					if(succThird) {
						log.info("第三方资金退回成功 payCode：" + payCode + " amt:" + thirdPartyPaid.toString());
						//===========记录退款流水==========
						UserAccountParamByType userAccountParamByType = new UserAccountParamByType();
						Integer accountType = ProjectConstant.ACCOUNT_ROLLBACK;
						log.info("===========更新用户流水表=======:" + accountType);
						userAccountParamByType.setAccountType(accountType);
						userAccountParamByType.setAmount(BigDecimal.ZERO.subtract(payLog.getOrderAmount()));
						userAccountParamByType.setBonusPrice(BigDecimal.ZERO);//暂无红包金额
						userAccountParamByType.setOrderSn(payLog.getOrderSn());
						userAccountParamByType.setPayId(payLog.getLogId());
						userAccountParamByType.setThirdPartPaid(thirdPartyPaid);
						if(payCode.equals("app_weixin") || payCode.equals("app_weixin_h5")) {
							payName = "微信";
						}else {
							payName = "银行卡";
						}
						userAccountParamByType.setPaymentName(payName);
						userAccountParamByType.setThirdPartName(payName);
						userAccountParamByType.setThirdPartPaid(payLog.getOrderAmount());
						userAccountParamByType.setUserId(payLog.getUserId());
						BaseResult<String> accountRst = userAccountService.insertUserAccount(userAccountParamByType);
						if(accountRst.getCode() == 0) {
							log.info("退款成功记录流水成功...");
						}
					}else {
						payLog.setPayMsg("第三方资金退回失败");
						payLogService.update(payLog);
						log.info("第三方资金退回失败 payCode：" + payCode + " amt:" + thirdPartyPaid.toString());
					}
				}
			}
		}else {	//无第三方支付，默认第三支付成功
			succThird = true;
		}
		if(succThird && (payType ==2 || payType == 3)) {
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			surplusPayParam.setBonusMoney(bonusAmount);
			surplusPayParam.setPayType(payType);
			surplusPayParam.setMoneyPaid(moneyPaid);
			surplusPayParam.setThirdPartName(payName);
			surplusPayParam.setThirdPartPaid(new BigDecimal(0));
			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
			log.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
			if(rollbackUserAccountChangeByPay.getCode() != 0) {
				log.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额时出错！");
			}
		}
		return ResultGenerator.genSuccessResult();
	}
	
	
	
	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 * 
	 */
	public BaseResult<RspOrderQueryDTO> rechargeOptions(String loggerId, PayLog payLog, RspOrderQueryEntity response) {
//		Integer tradeState = response.getTradeState();
		RspOrderQueryDTO rspEntity = new RspOrderQueryDTO();
		if(response.isSucc()) {
			int currentTime = DateUtil.getCurrentTimeLong();
			UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
			updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
			updateUserRechargeParam.setPaymentId(payLog.getPayOrderSn());
			updateUserRechargeParam.setPaymentName(payLog.getPayName());
			updateUserRechargeParam.setPayTime(currentTime);
			updateUserRechargeParam.setStatus("1");
			updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
			userRechargeService.updateReCharege(updateUserRechargeParam);
			
			RecharegeParam recharegeParam = new RecharegeParam();
			recharegeParam.setAmount(payLog.getOrderAmount());
			recharegeParam.setPayId(payLog.getPayOrderSn());//解决充值两次问题
			String payCode = payLog.getPayCode();
			if("app_weixin".equals(payCode)) {
				recharegeParam.setThirdPartName("微信");
			}else if("app_rongbao".equals(payCode)){
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
			
			
			RspOrderQueryDTO rspOrderQueryDTO = new RspOrderQueryDTO();
			rspOrderQueryDTO.setIsHaveRechargeAct(0);
			rspOrderQueryDTO.setDonationPrice("");
			StrParam strParam = new StrParam();
			strParam.setStr("");
			BaseResult<RechargeDataActivityDTO> rechargeDataAct = activityService.queryValidRechargeActivity(strParam);
			if(rechargeDataAct.getCode() == 0) {
				RechargeDataActivityDTO  rechargeDataActivityDTO  = rechargeDataAct.getData();
				rspOrderQueryDTO.setIsHaveRechargeAct(rechargeDataActivityDTO.getIsHaveRechargeAct());
				if(1 == rechargeDataActivityDTO.getIsHaveRechargeAct()) {
					logger.info("开始执行充值赠送红包逻辑");
					com.dl.member.param.PayLogIdParam payLogIdParam = new com.dl.member.param.PayLogIdParam();
					payLogIdParam.setPayLogId(String.valueOf(payLog.getLogId()));
					BaseResult<DonationPriceDTO> donationPriceRst = userBonusService.reiceiveBonusAfterRecharge(payLogIdParam);
					logger.info("充值赠送红包结果："+ JSON.toJSONString(donationPriceRst));
					if(donationPriceRst.getCode() == 0) {
						logger.info("结束执行充值赠送红包逻辑");
						rspOrderQueryDTO.setDonationPrice(donationPriceRst.getData().getDonationPrice());
					}
				}
			}
			
			log.info("放入redis："+String.valueOf(payLog.getLogId())+"-----------"+rspOrderQueryDTO.getDonationPrice());
			stringRedisTemplate.opsForValue().set(String.valueOf(payLog.getLogId()),rspOrderQueryDTO.getDonationPrice());
			logger.info("充值成功后返回的信息："+rspOrderQueryDTO.getIsHaveRechargeAct() +"-----"+rspOrderQueryDTO.getDonationPrice());
			return ResultGenerator.genSuccessResult("订单已支付成功",rspOrderQueryDTO);
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
	public BaseResult<RspOrderQueryDTO> orderOptions(String loggerId, PayLog payLog, RspOrderQueryEntity response) {
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
			
			logger.info("==============支付成功订单回调[orderService]==================");
			logger.info("payLogId:" + payLog.getLogId() + " payName:" + payLog.getPayName() 
			+ " payCode:" + payLog.getPayCode() + " payOrderSn:" + payLog.getPayOrderSn());
			logger.info("==================================");
			
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
			userAccountParamByType.setAmount(payLog.getOrderAmount());
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
			userAccountParamByType.setThirdPartPaid(payLog.getOrderAmount());
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
				this.rollbackOrderAmount(p);
			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else {
			//预扣款 的方案 这里什么也不做
			String payCode = response.getPayCode();
//			String code = response.getResult_code();
//			if(StringUtils.isBlank(code) || "3015".equals(code) || response.isYinHeWeChatNotPay()) {//融宝和银河返回值  为 订单不存在和未支付
//				dealWithPayFailure(orderService, payLog,payLogService, response);
//			}
			//融宝处理
			if(RspOrderQueryEntity.PAY_CODE_RONGBAO.equals(payCode)) {
				String code = response.getResult_code();
				if(StringUtils.isBlank(code) || "3015".equals(code)) {//订单不存在
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
				}else {
					String tips = response.getResult_msg();
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"融宝服务返回[" + tips +"]");
				}
			//微信处理	
			}else if(RspOrderQueryEntity.PAY_CODE_WECHAT.startsWith(payCode)){//wechat pay
				String code = response.getResult_code();
				String tips = response.getResult_msg();
				if(StringUtils.isBlank(code) || response.isYinHeWeChatNotPay()) {
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
				}else {
					return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),"微信支付失败["+tips+"]");	
				}
			}
		}
		return null;
	}
	
	
}
