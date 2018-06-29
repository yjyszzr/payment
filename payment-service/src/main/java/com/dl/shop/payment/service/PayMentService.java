package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.dl.member.param.MemRollParam;
import com.dl.member.param.RecharegeParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserAccountParamByType;
import com.dl.member.param.UserBonusParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderCondtionParam;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.PayBankRecordMapper;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dao.RollBackLogMapper;
import com.dl.shop.payment.dto.PayBankRecordDTO;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayBankRecordModel;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.PayMent;
import com.dl.shop.payment.model.RollBackLog;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.RollbackThirdOrderAmountParam;
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.PayManager.QueueItemEntity;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRefundEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;
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
    private RollBackLogMapper rollBackLogMapper;
    
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
	private XianFengPayUtil xFengPayUtil;
	
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
	
	@Resource
	private PayBankRecordMapper payBankRecordMapper;
	
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
    public void dealBeyondPayTimeOrderOut() {
		logger.info("开始执行混合支付超时订单任务");
		OrderCondtionParam orderQueryParam = new OrderCondtionParam();
    	orderQueryParam.setOrderStatus(0);
    	orderQueryParam.setPayStatus(0);
    	BaseResult<List<OrderDTO>> orderDTORst = orderService.queryOrderListByCondition(orderQueryParam);
    	    	
    	if(orderDTORst.getCode() != 0) {
    		log.error("查询混合支付超时订单失败"+orderDTORst.getMsg());
    		return;
    	}
    	
    	List<OrderDTO> orderDTOList = orderDTORst.getData();
    	logger.info("混合支付超时订单数："+orderDTOList.size());
    	if(orderDTOList.size() == 0) {
    		logger.info("没有混合支付超时订单,定时任务结束");
    		return;
    	}
    	
    	for(OrderDTO or:orderDTOList) {
    		this.dealBeyondPayTimeOrder(or);
    	}
    	
		log.info("结束执行支混合付超时订单任务");
    }
    
    
    /**
     * 处理支付超时订单
     */
    @Transactional
    public void dealBeyondPayTimeOrder(OrderDTO or) {
    	if(or.getSurplus().compareTo(BigDecimal.ZERO) > 0) {
	    	SurplusPayParam surplusPayParam = new SurplusPayParam();
	    	surplusPayParam.setOrderSn(or.getOrderSn());
	    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
	    	if(rollRst.getCode() != 0) {
	    		log.error(rollRst.getMsg());
	    		return;
	    	}
	    	
	    	if(rollRst.getCode() != 0) {
	    		log.error("支付超时订单回滚用户余额异常,code="+rollRst.getCode()+"  msg:"+rollRst.getMsg()+" 订单号："+or.getOrderSn());
	    	}else {
	    		log.info(JSON.toJSONString("用户"+or.getUserId()+"超时支付订单"+or.getOrderSn()+"已回滚账户余额"));
	    	} 
    	}
    	
    	Integer userBonusId = or.getUserBonusId();
    	if(null != userBonusId) {
    		UserBonusParam userbonusParam = new UserBonusParam();
    		userbonusParam.setUserBonusId(userBonusId);
    		userbonusParam.setOrderSn(or.getOrderSn());
    		userAccountService.rollbackChangeUserAccountByCreateOrder(userbonusParam);
    	}
   	
    	UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    	updateOrderInfoParam.setOrderSn(or.getOrderSn());
    	updateOrderInfoParam.setOrderStatus(8);//订单失败
    	updateOrderInfoParam.setPayStatus(2);//支付失败
    	updateOrderInfoParam.setPayTime(DateUtil.getCurrentTimeLong());
    	BaseResult<String> updateRst = orderService.updateOrderInfoStatus(updateOrderInfoParam);
    	if(updateRst.getCode() != 0) {
    		log.error("支付超时订单更新订单为出票失败 异常，返回，code="+updateRst.getCode()+"  msg:"+updateRst.getMsg()+" 订单号："+or.getOrderSn());
    		return;
    	}
    	
    	PayLog updatepayLog = new PayLog();
    	updatepayLog.setIsPaid(ProjectConstant.IS_PAID_FAILURE);
    	updatepayLog.setOrderSn(or.getOrderSn());
    	payLogService.updatePayLogByOrderSn(updatepayLog);

    }
    
    public BaseResult<?> rollbackAmountThird(RollbackThirdOrderAmountParam param) {
    	String amt = param.getAmt();
    	ReqRefundEntity reqEntity = new ReqRefundEntity();
		reqEntity.setAmount(amt);
		reqEntity.setNote("手动退款操作");
		reqEntity.setOrig_order_no(param.getOrderSn());
		String payCode = param.getPayCode();
		boolean isInWeChat = false;
		if(payCode.equals("app_weixin_h5")) {
			isInWeChat = true;
		}
		log.info("[rollbackAmountThird]" + " str:" + reqEntity.toString() + " isInWeChat:" + isInWeChat);
		try {
			RspRefundEntity rspRefundEntity = yinHeUtil.orderRefund(isInWeChat,reqEntity.getOrig_order_no(),reqEntity.getAmount());
			log.info("rEntity:" + rspRefundEntity.toString());
			RollBackLog rBackLog = new RollBackLog();
			rBackLog.setAmt(amt);
			rBackLog.setPayLogSn(param.getOrderSn());
			rBackLog.setReq(reqEntity.toString());
			rBackLog.setRsp(rspRefundEntity.toString());
			String strTime = DateUtil.getCurrentDateTime();
			rBackLog.setTime(strTime);
			int status = 0;
			if(rspRefundEntity.isSucc()) {
				status = 1;
			}
			rBackLog.setStatus(status);
			rollBackLogMapper.insert(rBackLog);
			return ResultGenerator.genSuccessResult("succ",rspRefundEntity);
		}catch(Exception ee) {
			logger.info("[rollbackAmountThird]" +"msg:" + ee.getMessage());
		}
    	return ResultGenerator.genFailResult("查询到第三方失败...");
    }
    
    /**
     * 资金回滚
     * @param param
     * @return
     */
	public BaseResult<?> rollbackOrderAmount(RollbackOrderAmountParam param) {
		log.info("[rollbackOrderAmount] ordersn=" + param.getOrderSn() + " amt:" + param.getAmt());
		String orderSn = param.getOrderSn();
		BigDecimal amt = param.getAmt();
		OrderSnParam snParam = new OrderSnParam();
		snParam.setOrderSn(orderSn);
		BaseResult<OrderDTO> orderRst = orderService.getOrderInfoByOrderSn(snParam);
		if(orderRst.getCode() != 0 || StringUtils.isEmpty(orderRst.getData().getOrderSn())) {
			log.info("[rollbackOrderAmount]orderService.getOrderInfoByOrderSn rst code="+orderRst.getCode()+" msg="+orderRst.getMsg());
			return ResultGenerator.genFailResult("[rollbackOrderAmount]" + " 查询订单失败");
		}
		OrderDTO order = orderRst.getData();
		BigDecimal bonusAmount = order.getBonus();
		BigDecimal totalAmt = order.getTicketAmount();
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		Integer userBonusId = order.getUserBonusId();
		Integer userId = order.getUserId();
		
		logger.info("[rollbackOrderAmount]" +" 实际回退金额:" + amt + " 总金额:" + totalAmt);
		if(amt.compareTo(totalAmt) > 0) {
			logger.info("[rollbackOrderAmount]" + "回退金额大于彩票总金额");
			return ResultGenerator.genFailResult("回退金额大于彩票总金额");
		}
		
		logger.info("[rollbackOrderAmount]" + "优惠券:" +  bonusAmount);
		//退回优惠券
		if(userBonusId != null && userBonusId > 0) {
			if(amt.compareTo(bonusAmount) >= 0) {
				amt = amt.subtract(bonusAmount);
				log.info("[rollbackOrderAmount] 优惠券退回操作 userBonusId:" + userBonusId + " 优惠券金额:" + bonusAmount +" 实际回退金额:" + amt);
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
		}
		
		if(amt.compareTo(BigDecimal.ZERO) <= 0) {
			logger.info("[rollbackOrderAmount]" + "用户回退金额无效");
			return ResultGenerator.genFailResult("用户回退金额无效");
		}
		
		MemRollParam mRollParam = new MemRollParam();
		mRollParam.setUserId(userId);
		mRollParam.setOrderSn(orderSn);
		mRollParam.setAmt(amt);
		BaseResult<SurplusPaymentCallbackDTO> baseResult = userAccountService.rollbackUserMoneyFailure(mRollParam);
		boolean isSucc;
		if(baseResult.getCode() != 0) {
			isSucc = false;
		}else {
			isSucc = true;
		}
		log.info("[rollbackOrderAmount]" + " 回退用户可提现余额结果 succ:" + isSucc);
		//第三方资金退回
		if(!isSucc) {
			log.info("第三方资金退回失败 payCode：" + " amt:" + thirdPartyPaid.toString());
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
			return ResultGenerator.genSuccessResult("充值成功",rspOrderQueryDTO);
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
			String payCode = payLog.getPayCode();
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
//			if(!isLotteryPrintSucc) {
//				//资金回滚
//				RollbackOrderAmountParam p = new RollbackOrderAmountParam();
//				p.setOrderSn(orderSn);
//				p.setAmt(amt);
//				this.rollbackOrderAmount(p);
//			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else {
			//预扣款 的方案 这里什么也不做
			String payCode = payLog.getPayCode();
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
			//先锋错误码处理
			}else if(RspOrderQueryEntity.PAY_CODE_XIANFENG.equals(payCode)){
				
			}
		}
		return null;
	}
	
	/**
	 * 处理订单支付超时的定时任务
	 */
    public void timerOrderQueryScheduled() {
		String loggerId = "timer_orderquery_" + System.currentTimeMillis();
		List<QueueItemEntity> mVector = PayManager.getInstance().getList();
		if(mVector.size() > 0) {
			for(int i = 0;i < mVector.size();i++) {
				QueueItemEntity entity = mVector.get(i);
				int cnt = entity.cnt;
				if(cnt >= QueueItemEntity.MAX_CNT) {
					mVector.remove(entity);
				}
				entity.cnt++;
				boolean succ = task(loggerId,entity);
				if(succ) {
					entity.cnt = QueueItemEntity.MAX_CNT;
				}
			}
		}
	}

	private boolean task(String loggerId,QueueItemEntity entity) {
		boolean succ = false;
		BaseResult<RspOrderQueryEntity> baseResult = null;
		//http request
		String payCode = entity.payCode;
		String payOrderSn = entity.payOrderSn;
		PayLog payLog = payLogService.findPayLogByOrderSign(payOrderSn);
		if(payLog == null) {
			log.info("查询到该订单不存在...payOrderSn:" + payOrderSn);
			return succ;
		}
		int isPaid = payLog.getIsPaid();
		if(isPaid == 1) {
			logger.info("[task]" + "payLogId:" + payLog.getLogId() + " orderSn:"+ payLog.getOrderSn() +" 已支付...");
			succ = true;
			return succ;
		}
		if("app_rongbao".equals(payCode)) {
			baseResult = rongUtil.queryOrderInfo(payOrderSn);
		}else if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)) {
			boolean isInWeChat = "app_weixin_h5".equals(payCode);
			baseResult = yinHeUtil.orderQuery(isInWeChat,payOrderSn);
		}else if("app_xianfeng".equals(payCode)) {
			try {
				RspApplyBaseEntity rspBaseEntity = xFengPayUtil.queryPayByOrderNo(payOrderSn);
				if(rspBaseEntity != null) {
					RspOrderQueryEntity rspOrderQueryEntity = rspBaseEntity.buildRspOrderQueryEntity(payCode);	
					baseResult = ResultGenerator.genSuccessResult("succ",rspOrderQueryEntity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(baseResult == null || baseResult.getCode() != 0) {
			if(baseResult != null) {
				logger.info("订单支付状态轮询第三方[" + baseResult.getMsg()+"]");
			}
			return succ;
		}
		RspOrderQueryEntity rspEntity = baseResult.getData();
		succ = rspEntity.isSucc();
		if(rspEntity != null && rspEntity.isSucc()) {
			logger.info("payType:" + payLog.getPayType() +" payCode:" + payCode + "第三方定时器查询订单 payordersn:" + payOrderSn +"succ..");
			Integer payType = payLog.getPayType();
			BaseResult<RspOrderQueryDTO> bResult = null;
			if(payType == 0) {
				bResult = this.orderOptions(loggerId, payLog,rspEntity);
			}else if(payType == 1) {
				bResult = this.rechargeOptions(loggerId, payLog, rspEntity);
			}
		}
		return succ;
	}
	
	
	//第三方返回成功，扣除钱包余额
	private BaseResult<String> optionMoney(QueueItemEntity entity) {
		String payCode = entity.payCode;
		String orderSn = entity.orderSn;
		String payOrderSn = entity.payOrderSn;
		OrderSnParam p = new OrderSnParam();
		p.setOrderSn(orderSn);
		BaseResult<OrderDTO> baseResult = orderService.getOrderInfoByOrderSn(p);
		OrderDTO orderDTO = baseResult.getData();
		if(orderDTO.getThirdPartyPaid().doubleValue() > 0) {
			//用户余额扣除
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(orderDTO.getSurplus());
			surplusPayParam.setBonusMoney(orderDTO.getBonus());
			surplusPayParam.setPayType(1);
			surplusPayParam.setMoneyPaid(orderDTO.getSurplus());
			surplusPayParam.setThirdPartName(orderDTO.getPayName());
			surplusPayParam.setThirdPartPaid(orderDTO.getThirdPartyPaid());
			BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay = userAccountService.changeUserAccountByPay(surplusPayParam);
			if(changeUserAccountByPay.getCode() != 0) {
				logger.info(orderSn + "用户余额扣减失败！");
				return ResultGenerator.genFailResult("支付失败！");
			}
			BigDecimal surplus = changeUserAccountByPay.getData().getSurplus();
			//更新余额支付信息到订单
			BigDecimal userSurplus = changeUserAccountByPay.getData().getUserSurplus();
			BigDecimal userSurplusLimit = changeUserAccountByPay.getData().getUserSurplusLimit();
			UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
			updateOrderInfoParam.setOrderSn(orderSn);
			updateOrderInfoParam.setUserSurplus(userSurplus);
			updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
			BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(updateOrderInfoParam);
			if(updateOrderInfo.getCode() != 0) {
				logger.info(orderSn + "订单回写用户余额扣减详情失败！");
				BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
				logger.info(orderSn + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
				if(rollbackUserAccountChangeByPay.getCode() != 0) {
					logger.info(orderSn + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
				}
				return ResultGenerator.genFailResult("支付失败！");
			}
		}
		return ResultGenerator.genSuccessResult();
	}
	
	
	public List<PayBankRecordDTO> listUserBanks(int userId) {
		List<PayBankRecordDTO> rList = new ArrayList<>();
		PayBankRecordModel p = new PayBankRecordModel();
		p.setUserId(userId);
		p.setIsPaid(1);
		List<PayBankRecordModel> mList = payBankRecordMapper.listUserBank(p);
		if(mList != null) {
			for(int i = 0;i < mList.size();i++) {
				PayBankRecordModel payBankRModel = mList.get(i);
				PayBankRecordDTO payBRDTO = new PayBankRecordDTO();
				payBRDTO.setId(payBankRModel.getId());
				payBRDTO.setUserId(payBankRModel.getUserId());
				payBRDTO.setBankCardNo(payBankRModel.getBankCardNo());
				payBRDTO.setUserName(payBankRModel.getUserName());
				payBRDTO.setCertNo(payBankRModel.getCertNo());
				payBRDTO.setPhone(payBankRModel.getPhone());
				payBRDTO.setBankType(payBankRModel.getBankType());
				payBRDTO.setCvn2(payBankRModel.getCvn2());
				payBRDTO.setVaildDate(payBankRModel.getValidDate());
				rList.add(payBRDTO);
			}
		}
		return rList;
	}
}
