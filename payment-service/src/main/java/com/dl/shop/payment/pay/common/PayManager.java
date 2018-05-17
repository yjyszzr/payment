package com.dl.shop.payment.pay.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayManager {
	private final static Logger logger = LoggerFactory.getLogger(PayManager.class);
	private static PayManager instance;
	private List<QueueItemEntity> mVector;
	
	private PayManager() {
		mVector = new java.util.Vector<QueueItemEntity>();
	}
	
	public static final PayManager getInstance() {
		if(instance == null) {
			instance = new PayManager();
		}
		return instance;
	}

	public List<QueueItemEntity> getList(){
		return this.mVector;
	}
	
	public void addReqQueue(String orderSn,String payOrderSn,String payCode) {
		QueueItemEntity entity = new QueueItemEntity();
		entity.orderSn = orderSn;
		entity.payCode = payCode;
		entity.payOrderSn = payOrderSn;
		entity.cnt = 0;
		mVector.add(entity);
	}
	
//	private void task(QueueItemEntity entity) {
//		//http request
//		String payCode = entity.payCode;
//		String orderSn = entity.orderSn;
//		if("app_weixin".equals(payCode)) {
//			YinHeUtil yinHeUtil = new YinHeUtil();
//			BaseResult<RspOrderQueryEntity> baseResult = yinHeUtil.orderQuery(false,orderSn);
//			if(baseResult != null && baseResult.getCode() == 0) {
//				RspOrderQueryEntity dataEntity = baseResult.getData();
//				if(dataEntity != null && dataEntity.isSucc()) {//内部timer query
//					entity.cnt = QueueItemEntity.MAX_CNT;
//					logger.info("内部timer查詢支付成功... orderNo:" + dataEntity.getOrder_no());
//					optionMoney(entity);
//				}else {
//					logger.info("内部timer查詢支付失敗...");
//				}
//			}else {
//				logger.info("内部timer查詢支付失敗...");
//			}
//		}
//	}
	
	private void optionMoney(QueueItemEntity entity) {
//		String payCode = entity.payCode;
//		String orderSn = entity.orderSn;
//		String payOrderSn = entity.payOrderSn;
//		OrderSnParam p = new OrderSnParam();
//		p.setOrderSn(orderSn);
//		BaseResult<OrderDTO> baseResult = orderService.getOrderInfoByOrderSn(p);
//		OrderDTO orderDTO = baseResult.getData();
//		//用户余额扣除
//		SurplusPayParam surplusPayParam = new SurplusPayParam();
//		surplusPayParam.setOrderSn(orderSn);
//		surplusPayParam.setSurplus(orderDTO.getSurplus());
//		surplusPayParam.setBonusMoney(orderDTO.getBonus());
//		int payType1 = 2;
//		if(hasThird) {
//			payType1 = 3;
//		}
//		surplusPayParam.setPayType(payType1);
//		surplusPayParam.setMoneyPaid(moneyPaid);
//		surplusPayParam.setThirdPartName(paymentDto!=null?paymentDto.getPayName():"");
//		surplusPayParam.setThirdPartPaid(thirdPartyPaid);
//		BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay = userAccountService.changeUserAccountByPay(surplusPayParam);
//		if(changeUserAccountByPay.getCode() != 0) {
//			logger.info(loggerId + "用户余额扣减失败！");
//			return ResultGenerator.genFailResult("支付失败！");
//		}
//		//更新余额支付信息到订单
//		BigDecimal userSurplus = changeUserAccountByPay.getData().getUserSurplus();
//		BigDecimal userSurplusLimit = changeUserAccountByPay.getData().getUserSurplusLimit();
//		UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
//		updateOrderInfoParam.setOrderSn(orderSn);
//		updateOrderInfoParam.setUserSurplus(userSurplus);
//		updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
//		BaseResult<String> updateOrderInfo = orderService.updateOrderInfo(updateOrderInfoParam);
//		if(updateOrderInfo.getCode() != 0) {
//			logger.info(loggerId + "订单回写用户余额扣减详情失败！");
//			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
//			logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
//			if(rollbackUserAccountChangeByPay.getCode() != 0) {
//				logger.info(loggerId + " orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" 在回滚用户余额时出错！");
//			}
//			return ResultGenerator.genFailResult("支付失败！");
//		}
	}
	
	public class QueueItemEntity{
		public String orderSn;
		public String payOrderSn;
		public String payCode;
		public int cnt;
		public static final int MAX_CNT = 20;
	}
	
	public static interface PayListener{
		public void orderQuerySucc(String orderId,String payCode);
	}
	
}
