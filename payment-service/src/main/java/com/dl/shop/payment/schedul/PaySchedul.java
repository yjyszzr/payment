package com.dl.shop.payment.schedul;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.param.SurplusPayParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderCondtionParam;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.common.PayManager.QueueItemEntity;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.UserRechargeService;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class PaySchedul {
	private final static Logger logger = LoggerFactory.getLogger(PaySchedul.class);
	
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	@Resource
	private PayLogService payLogService;
	@Resource
	private PayMentService payMentService;
	@Resource
	private UserRechargeService userRechargeService;
	@Resource
	private YinHeUtil yinHeUtil;
	@Resource
	private RongUtil rongUtil;
	
	@Scheduled(cron = "0 0/2 * * * ?")
    public void dealBeyondPayTimeOrder() {
		logger.info("开始执行混合支付超时订单任务");
		OrderCondtionParam orderQueryParam = new OrderCondtionParam();
    	orderQueryParam.setOrderStatus(0);
    	orderQueryParam.setPayStatus(0);
    	BaseResult<List<OrderDTO>> orderDTORst = orderService.queryOrderListByCondition(orderQueryParam);
    	    	
    	if(orderDTORst.getCode() != 0) {
    		log.error("-------------------查询混合支付超时订单失败"+orderDTORst.getMsg());
    		return;
    	}
    	List<OrderDTO> orderDTOList = orderDTORst.getData();
    	
    	logger.info("---------混合支付超时订单数："+orderDTOList.size());
    	if(orderDTOList.size() == 0) {
    		logger.info("---------没有混合支付超时订单,定时任务结束");
    		return;
    	}
    	
    	for(OrderDTO or:orderDTOList) {
    		payMentService.dealBeyondPayTimeOrder(or);
    	}
		log.info("结束执行支混合付超时订单任务");
	}
	
	
	/**
	 * 处理订单支付超时的定时任务
	 */
	@Scheduled(fixedRate = 1000*5)
    public void timerOrderQueryScheduled() {
		String loggerId = "timer_orderquery_" + System.currentTimeMillis();
		log.info("订单支付Query任务...");
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
			return succ;
		}
		int isPaid = payLog.getIsPaid();
		if(isPaid == 1) {
			logger.info("[task]" + "payLogId:" + payLog.getPayIp() + " 已支付...");
			succ = true;
			return succ;
		}
		if("app_rongbao".equals(payCode)) {
			baseResult = rongUtil.queryOrderInfo(payOrderSn);
		}else if("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode)) {
			boolean isInWeChat = "app_weixin_h5".equals(payCode);
			baseResult = yinHeUtil.orderQuery(isInWeChat,payOrderSn);
		}
		if(baseResult == null || baseResult.getCode() != 0) {
			logger.info("订单支付状态轮询第三方[" + baseResult.getMsg()+"]");
			return succ;
		}
		RspOrderQueryEntity rspEntity = baseResult.getData();
		succ = rspEntity.isSucc();
		if(rspEntity != null && rspEntity.isSucc()) {
			logger.info("payType:" + payLog.getPayType() +"payCode:" + payCode + "第三方定时器查询订单 payordersn:" + rspEntity.getOrder_no() + " succ..");
			Integer payType = payLog.getPayType();
			BaseResult<RspOrderQueryDTO> bResult = null;
			if(payType == 0) {
				bResult = PaymentController.orderOptions(orderService, payLogService, userAccountService, loggerId, payLog,rspEntity);
			}else if(payType == 1) {
				bResult = PaymentController.rechargeOptions(userRechargeService,userAccountService, payLogService, loggerId, payLog, rspEntity);
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
}
