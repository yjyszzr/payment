/*package com.dl.shop.payment.schedul;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.dl.base.result.BaseResult;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.PayManager.QueueCashItemEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleQueryEntity;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.dl.shop.payment.service.CashService;
import com.dl.shop.payment.service.UserWithdrawService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class CashSchedul {
	private final static Logger logger = LoggerFactory.getLogger(CashSchedul.class);
	
	@Autowired
	private UserWithdrawService userWithdrawService;
	
	@Autowired
	private CashService cashService;
	
	@Autowired
	private XianFengCashUtil xianFengUtil;
	
	*//**
	 * 提现状态轮询
	 *//*
	@Scheduled(fixedRate = 1000*20)
    public void timerCheckCashReq() {
//		logger.info("[timerCheckCashReq]" +" call...");
		List<QueueCashItemEntity> mVector = PayManager.getInstance().getCashList();
		if(mVector.size() > 0) {
			for(int i = 0;i < mVector.size();i++) {
				QueueCashItemEntity itemEntity = mVector.get(i);
				if(itemEntity != null) {
					if(itemEntity.cnt >= QueueCashItemEntity.MAX_CNT) {
						mVector.remove(itemEntity);
					}else {
						itemEntity.cnt++;
						boolean isSucc;
						try {
							isSucc = task(itemEntity);
							if(isSucc) {
								itemEntity.cnt = QueueCashItemEntity.MAX_CNT;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	
	private boolean task(QueueCashItemEntity itemEntity) throws Exception {
		boolean isSucc = false;
		String withDrawSn = itemEntity.withDrawSn;
		BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
		if(baseResult.getCode() == 0 && baseResult.getData() != null) {
			UserWithdraw userWithDraw = baseResult.getData();
			int userId = userWithDraw.getUserId();
			if(userWithDraw != null 
			  &&!ProjectConstant.STATUS_FAILURE.equals(userWithDraw.getStatus()) 
			  &&!ProjectConstant.STATUS_SUCC.equals(userWithDraw.getStatus())){
				//query订单状态
				RspSingleQueryEntity rspEntity = xianFengUtil.queryCash(withDrawSn);
				if(rspEntity != null && rspEntity.isSucc()) {
					cashService.operation(convert2RspSingleCashEntity(rspEntity),withDrawSn, userId,false,true,true);
				}
			}
		}
		return isSucc;
	}
	
	private RspSingleCashEntity convert2RspSingleCashEntity(RspSingleQueryEntity sEntity) {
		RspSingleCashEntity rspSingleCashEntity = new RspSingleCashEntity();
		if(sEntity != null) {
			rspSingleCashEntity.resCode = sEntity.resCode;
			rspSingleCashEntity.resMessage = sEntity.resMessage;
			rspSingleCashEntity.tradeNo = sEntity.tradeNo;
			rspSingleCashEntity.status = sEntity.status;
			rspSingleCashEntity.amount = sEntity.amount;
			rspSingleCashEntity.transCur = sEntity.transCur;
			rspSingleCashEntity.merchantId = sEntity.merchantId;
			rspSingleCashEntity.merchantNo = sEntity.merchantNo;
		}
		return rspSingleCashEntity;
	}
}
*/