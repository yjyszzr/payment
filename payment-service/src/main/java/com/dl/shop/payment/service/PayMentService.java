package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryPrintService;
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
import com.dl.member.param.UserBonusParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderCondtionParam;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.order.param.UpdateOrderPayStatusParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.PayBankRecordMapper;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dao.RollBackLogMapper;
import com.dl.shop.payment.dto.PayBankRecordDTO;
import com.dl.shop.payment.dto.PayFinishRedirectUrlTDTO;
import com.dl.shop.payment.dto.PayReturnDTO;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayBankRecordModel;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.PayMent;
import com.dl.shop.payment.model.RollBackLog;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.RollbackThirdOrderAmountParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRefundEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;
import com.dl.shop.payment.pay.yifutong.entity.RspYFTEntity;
import com.dl.shop.payment.pay.yifutong.util.PayYFTUtil;
import com.dl.shop.payment.pay.yinhe.util.PayUtil;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.web.PaymentController;

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
	private PayYFTUtil payYFTUtil;
	
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
		List<PaymentDTO> list = payments.stream().filter(payment->{
			Boolean isEnable = payment.getIsEnable() == 1;
			return isEnable;
		}).map(payment->{
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
	public BaseResult<RspOrderQueryDTO> rechargeOptions(PayLog payLog, RspOrderQueryEntity response) {
//		Integer tradeState = response.getTradeState();
		if(response.isSucc()) {
			int currentTime = DateUtil.getCurrentTimeLong();
			PayLog updatePayLog = new PayLog();
			updatePayLog.setPayTime(currentTime);
			payLog.setLastTime(currentTime);
			updatePayLog.setTradeNo(response.getTrade_no());
			updatePayLog.setLogId(payLog.getLogId());
			updatePayLog.setIsPaid(1);
			updatePayLog.setPayMsg("充值成功");
			int updateRow = payLogMapper.updatePayLogSuccess0To1(updatePayLog);
			logger.info("充值记录payOrderSn={},更新充值成功,updateRow={}",payLog.getPayOrderSn(),updateRow);
			if(updateRow>0){
				updatePaybankRecord(payLog.getLogId());
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
				if("app_zfb".equals(payCode)) {
					recharegeParam.setThirdPartName("支付宝");
				}else if("app_weixin".equals(payCode)) {
					recharegeParam.setThirdPartName("微信");
				}else if("app_rongbao".equals(payCode)){
					recharegeParam.setThirdPartName("银行卡");
				}
				recharegeParam.setThirdPartPaid(payLog.getOrderAmount());
				recharegeParam.setUserId(payLog.getUserId());
				BaseResult<String>  rechargeRst = userAccountService.rechargeUserMoneyLimit(recharegeParam);
				if(rechargeRst.getCode() != 0) {
					logger.error(payLog.getPayOrderSn()+" 给个人用户充值：code"+rechargeRst.getCode() +"message:"+rechargeRst.getMsg());
				}
				//更新paylog
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
						BaseResult<DonationPriceDTO> donationPriceRst = userBonusService.reiceiveBonusAfterRechargeNew(payLogIdParam);
						logger.info("充值赠送红包结果："+ JSON.toJSONString(donationPriceRst));
						if(donationPriceRst.getCode() == 0) {
							logger.info("结束执行充值赠送红包逻辑");
							rspOrderQueryDTO.setDonationPrice(donationPriceRst.getData().getDonationPrice());
						}
					}
				}
				log.info("放入redis："+String.valueOf(payLog.getLogId())+"-----------"+rspOrderQueryDTO.getDonationPrice());
				stringRedisTemplate.opsForValue().set(String.valueOf(payLog.getLogId()),rspOrderQueryDTO.getDonationPrice(),180, TimeUnit.SECONDS);
				logger.info("充值成功后返回的信息："+rspOrderQueryDTO.getIsHaveRechargeAct() +"-----"+rspOrderQueryDTO.getDonationPrice());
				return ResultGenerator.genSuccessResult("充值成功",rspOrderQueryDTO);	
			}
			logger.error("充值记录payOrderSn={}已变更，更新失败",payLog.getPayOrderSn());
			return ResultGenerator.genSuccessResult();
		}else if(response.isFail()){
			//更新paylog
			try {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setPayMsg("充值失败["+response.getResult_msg()+"]");
				updatePayLog.setLastTime(DateUtil.getCurrentTimeLong());
				payLogMapper.updatePayLogFail0To3(updatePayLog);
			} catch (Exception e) {
				logger.error("payOrderSn="+payLog.getPayOrderSn()+ ", paylogid="+payLog.getLogId()+" , paymsg="+response.getResult_msg()+"，保存失败记录时出错", e);
			}
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),PayEnums.PAY_RONGBAO_FAILURE.getMsg());
		}else {
			String payCode = payLog.getPayCode();
			logger.info("payOrderSn={},payCode={},retCode={},retMsg={}",payLog.getPayOrderSn(),payCode,response.getResult_code(),response.getResult_msg());
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
		}
	}
	
	
	/**
	 * 对支付结果的一个回写处理
	 * @param loggerId
	 * @param payLog
	 * @param response
	 * @return
	 */
	public BaseResult<RspOrderQueryDTO> orderOptions(PayLog payLog, RspOrderQueryEntity response) {
		int currentTime = DateUtil.getCurrentTimeLong();
		if(response.isSucc()) {
			//2018-07-04
			//更新order
			UpdateOrderPayStatusParam param = new UpdateOrderPayStatusParam();
			param.setPayStatus(1);
			param.setPayTime(currentTime);
//			param.setPayId(payId);
			param.setPaySn(payLog.getLogId()+"");
			param.setPayName(payLog.getPayName());
			param.setPayCode(payLog.getPayCode());
			param.setOrderSn(payLog.getOrderSn());
			BaseResult<Integer> updateOrderInfo = orderService.updateOrderPayStatus(param);
			
			logger.info("==============支付成功订单回调[orderService]==================");
			logger.info("payLogId:" + payLog.getLogId() + " payName:" + payLog.getPayName() 
			+ " payCode:" + payLog.getPayCode() + " payOrderSn:" + payLog.getPayOrderSn());
			logger.info("==================================");
			if(updateOrderInfo.getCode() == 0) {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTrade_no());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(1);
				updatePayLog.setPayMsg("支付成功");
				payLogService.update(updatePayLog);
				updatePaybankRecord(payLog.getLogId());
			}else {
				logger.error("payOrderSn={}"+payLog.getPayOrderSn()+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
			}
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else if(response.isFail()){
			//更新order
			UpdateOrderPayStatusParam param = new UpdateOrderPayStatusParam();
			param.setPayStatus(2);
			param.setPayTime(currentTime);
//				param.setPayId(payId);
			param.setPaySn(payLog.getLogId()+"");
			param.setPayName(payLog.getPayName());
			param.setPayCode(payLog.getPayCode());
			param.setOrderSn(payLog.getOrderSn());
			BaseResult<Integer> updateOrderInfo = orderService.updateOrderPayStatus(param);
			if(updateOrderInfo.getCode() == 0) {
				PayLog updatePayLog = new PayLog();
				updatePayLog.setPayTime(currentTime);
				payLog.setLastTime(currentTime);
				updatePayLog.setTradeNo(response.getTrade_no());
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setIsPaid(3);
				updatePayLog.setPayMsg("支付失败");
				payLogService.update(updatePayLog);
			}
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_FAILURE.getcode(),PayEnums.PAY_RONGBAO_FAILURE.getMsg());
		}else {
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
		}
	}
	private void updatePaybankRecord(Integer payLogId){
		//先锋支付银行卡回写支付成功，该银行卡已生效
		PayBankRecordModel payBankRecordModel = new PayBankRecordModel();
		payBankRecordModel.setPayLogId(payLogId);
		PayBankRecordModel cardPay = payBankRecordMapper.selectPayBankCardNoByPayLog(payBankRecordModel);
		int updateRow = payBankRecordMapper.updatePayBankCardNoByPayLog(cardPay);
		log.info("更新历史卡号={},更新行数={}",cardPay==null?"":cardPay.getBankCardNo(),updateRow);
		payBankRecordModel.setIsPaid(1);
		int cnt = payBankRecordMapper.updateIsPaidInfo(payBankRecordModel);
		logger.info("[payNotify]" + "先锋支付银行卡支付状态回写 cnt:" + cnt +" payLogId:" + payLogId);
	}
	/**
	 * 处理订单支付 轮询
	 */
    public void timerOrderQueryScheduled() {
    	List<PayLog> findUnPayOrderPayLogs = payLogMapper.findUnPayOrderPayLogs();
		if(findUnPayOrderPayLogs.size() > 0) {
			for(PayLog paylog: findUnPayOrderPayLogs) {
				try{
					log.info("check order 处理payuOrderSn={}开始",paylog.getPayOrderSn());
					boolean succ = this.task(paylog);
					log.info("check order  处理payuOrderSn={}结束",paylog.getPayOrderSn());
				}catch(Exception e){
					logger.error("订单支付payOrderSn={}异常",paylog.getPayOrderSn(),e);
				}
			}
		}
	}
    /**
	 * 处理充值订单轮询
	 */
    public void timerRechargeQueryScheduled() {
    	List<PayLog> findUnPayChargePayLogs = payLogMapper.findUnPayChargePayLogs();
		if(findUnPayChargePayLogs.size() > 0) {
			for(PayLog paylog: findUnPayChargePayLogs) {
				try{
					log.info("check recharge 处理payuOrderSn={}开始",paylog.getPayOrderSn());
					boolean succ = this.task(paylog);
					log.info("check recharge 处理payuOrderSn={}结束",paylog.getPayOrderSn());
				}catch(Exception e){
					logger.error("充值支付payOrderSn={}异常",paylog.getPayOrderSn(),e);
				}
			}
		}
	}
    
    //主动查询支付状态
	private boolean task(PayLog payLog) {
		boolean succ = false;
		BaseResult<RspOrderQueryEntity> baseResult = null;
		//http request
		if(payLog == null) {
			return succ;
		}
		int isPaid = payLog.getIsPaid();
		if(isPaid == 1) {
			logger.info("[task]" + "payLogId:" + payLog.getLogId() + " orderSn:"+ payLog.getOrderSn() +" 已支付...");
			succ = true;
			return succ;
		}
		String payCode = payLog.getPayCode();
		String payOrderSn = payLog.getPayOrderSn();
		if("app_zfb".equals(payCode)){
			baseResult = yinHeUtil.orderQuery(true,false,payOrderSn);
		}else if("app_rongbao".equals(payCode)) {
			baseResult = rongUtil.queryOrderInfo(payOrderSn);
		}else if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)) {
			boolean isInWeChat = "app_weixin_h5".equals(payCode);
			baseResult = yinHeUtil.orderQuery(false,isInWeChat,payOrderSn);
		}else if("app_xianfeng".equals(payCode)) {
			try {
				RspApplyBaseEntity rspBaseEntity = xFengPayUtil.queryPayByOrderNo(payOrderSn);
				if(rspBaseEntity != null) {
					RspOrderQueryEntity rspOrderQueryEntity = rspBaseEntity.buildRspOrderQueryEntity(payCode);	
					baseResult = ResultGenerator.genSuccessResult("succ",rspOrderQueryEntity);
				}
			} catch (Exception e) {
				logger.error("先锋支付报错payOrderSn={}",payOrderSn,e);
			}
		}else if("app_yifutong".equals(payCode)) {
			baseResult = payYFTUtil.queryPayResult(payCode, payOrderSn);
		}
		if(baseResult == null || baseResult.getCode() != 0) {
			if(baseResult != null) {
				logger.info("订单支付状态轮询第三方[" + baseResult.getMsg()+"]");
			}
			return succ;
		}
		RspOrderQueryEntity rspEntity = baseResult.getData();
		succ = rspEntity.isSucc();
		logger.info("支付查询 payOrderSn={},payCode={},retCode={},retMsg={},isSucc={}",payOrderSn,payCode,rspEntity.getResult_code(),rspEntity.getResult_msg(),rspEntity.isSucc());
		if(rspEntity != null) {
			logger.info("payType:" + payLog.getPayType() +" payCode:" + payCode + "第三方定时器查询订单 payordersn:" + payOrderSn +"succ..");
			Integer payType = payLog.getPayType();
			if(payType == 0) {
				this.orderOptions(payLog,rspEntity);
			}else if(payType == 1) {
				BaseResult<RspOrderQueryDTO> bResult = this.rechargeOptions(payLog, rspEntity);
			}
		}
		return succ;
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
				payBRDTO.setRecordId(payBankRModel.getId());
				payBRDTO.setLastTime(payBankRModel.getLastTime());
//				payBRDTO.setUserId(payBankRModel.getUserId());
//				payBRDTO.setBankCardNo(payBankRModel.getBankCardNo());
//				payBRDTO.setUserName(payBankRModel.getUserName());
//				payBRDTO.setCertNo(payBankRModel.getCertNo());
//				payBRDTO.setPhone(payBankRModel.getPhone());
//				payBRDTO.setBankType(payBankRModel.getBankType());
//				payBRDTO.setCvn2(payBankRModel.getCvn2());
//				payBRDTO.setVaildDate(payBankRModel.getValidDate());
//				payBRDTO.setBankName(payBankRModel.getBankName());
				String msg = getMsg(payBankRModel);
				payBRDTO.setMessage(msg);
				rList.add(payBRDTO);
			}
		}
		return rList;
	}
	
	private String getMsg(PayBankRecordModel entity) {
		String msg = null;
		if(entity != null) {
			String strType = "储蓄卡";	//产品需求名称变更
			if(entity.getBankType() == 1) {
				strType = "信用卡";
			}
			String tail = "";
			if(!StringUtils.isEmpty(entity.getBankCardNo()) && entity.getBankCardNo().length() > 4) {
				tail = entity.getBankCardNo().substring(entity.getBankCardNo().length()-4);
			}
			msg = entity.getBankName()+strType+ "(尾号" + tail +")";
		}
		return msg;
	}
	/**
	 * 间联是否打开
	 * @return
	 */
	public Boolean getJianLianIsOpen() {
		Integer jianlian = payMentMapper.selectJianLianConfig();
		logger.info("间联开关config={}",jianlian);
		if(Integer.valueOf(1).equals(jianlian)){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	/**
	 * 拼接支付完成跳转地址公共参数信息
	 * @param redirectUrl
	 * @param channelId
	 * @return
	 */
	public String payFinishRedirectUrlPlusParams(String redirectUrl){
		String channelId = "";
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if(userDevice!=null&&!"h5".equalsIgnoreCase(userDevice.getChannel())){
			channelId = userDevice.getChannel();
		}
		PayFinishRedirectUrlTDTO finishRedirectDto = payMentMapper.selectRedirectInfo(channelId);
		String payFinishRedirectUrl = redirectUrl+"appName="+finishRedirectDto.getAppName()+"&schemeUrl="+finishRedirectDto.getSchemeUrl();
		logger.info("支付完成跳转地址redirectUrl={},转换后地址payFinishRedirectUrl={}",redirectUrl,payFinishRedirectUrl);
		return payFinishRedirectUrl;
	}
	
	public BaseResult<?> getYFTPayUrl(PayLog savePayLog,String orderId, String lotteryClassifyId) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.setScale(2,RoundingMode.HALF_EVEN);
		String payOrderSn = savePayLog.getPayOrderSn();
		RspYFTEntity rspEntity = null;
		rspEntity = payYFTUtil.getYFTPayUrl(bigD.toString(),payOrderSn);
		if(rspEntity != null) {
			if(rspEntity.isSucc()) {
				PayReturnDTO rEntity = new PayReturnDTO();
				String url = rspEntity.data.payUrl;
				
				//生成二维码url
				
				if(!TextUtils.isEmpty(url)) {
					rEntity.setPayUrl(url);
					rEntity.setPayLogId(savePayLog.getLogId()+"");
					rEntity.setOrderId(orderId);
					rEntity.setLotteryClassifyId(lotteryClassifyId);
					logger.info("YFTclient jump url:" + url +" payLogId:" +savePayLog.getLogId() +" orderId:" + orderId );
					payBaseResult = ResultGenerator.genSuccessResult("succ",rEntity);
				}else {
					payBaseResult = ResultGenerator.genFailResult("url decode失败",null);
				}
			}else {
				payBaseResult = ResultGenerator.genResult(PayEnums.PAY_YIFUTONG_INNER_ERROR.getcode(),PayEnums.PAY_YIFUTONG_INNER_ERROR.getMsg()+"[" +rspEntity.msg +"]");	
			}
		}else {
			payBaseResult = ResultGenerator.genFailResult("易富通支付返回数据有误");
		}
		return payBaseResult;
	}
}
