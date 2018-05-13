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
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.common.PayManager.QueueItemEntity;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;

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
	
	@Scheduled(cron = "0 0/1 * * * ?")
    public void dealBeyondPayTimeOrder() {
		logger.info("开始执行支付超时订单任务");
		payMentService.dealBeyondPayTimeOrder();
		log.info("结束执行支付超时订单任务");
	}
	
	
	/**
	 * 处理订单支付超时的定时任务
	 */
//	@Scheduled(cron = "0 0/1 * * * ?")
//	@Scheduled(fixedRate = 1000*20)
//    public void dealWithNotPayAndBeyondTimeOrder() {
//		logger.info("查询第三方订单信息定时器...");
////		log.info("结束执行处理订单支付超时的定时任务");
//		List<QueueItemEntity> mVector = PayManager.getInstance().getList();
//		if(mVector.size() > 0) {
//			for(int i = 0;i < mVector.size();i++) {
//				QueueItemEntity entity = mVector.get(i);
//				int cnt = entity.cnt;
//				if(cnt >= QueueItemEntity.MAX_CNT) {
//					mVector.remove(entity);
//				}
//				entity.cnt++;
//				task(entity);
//			}	
//		}
//	}

	private void task(QueueItemEntity entity) {
		//http request
		String payCode = entity.payCode;
		String payOrderSn = entity.payOrderSn;
		if("app_weixin".equals(payCode)) {
			YinHeUtil yinHeUtil = new YinHeUtil();
			BaseResult<RspOrderQueryEntity> baseResult = yinHeUtil.orderQuery(false,payOrderSn);
			if(baseResult != null && baseResult.getCode() == 0) {
				RspOrderQueryEntity dataEntity = baseResult.getData();
				if(dataEntity != null && dataEntity.isSucc()) {//内部timer query
					entity.cnt = QueueItemEntity.MAX_CNT;
					logger.info("内部timer查詢支付成功... orderNo:" + dataEntity.getOrder_no());
					BaseResult<String> bResult = optionMoney(entity);
					if(bResult != null && bResult.getCode() == 0) {
						logger.info("混合支付扣除余额成功...");
					}else {
						logger.info("混合支付扣除余额失败...");
					}
				}else {
					logger.info("内部timer查詢支付失敗...");
				}
			}else {
				logger.info("内部timer查詢支付失敗...");
			}
		}
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
