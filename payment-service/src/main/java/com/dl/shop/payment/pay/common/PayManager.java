package com.dl.shop.payment.pay.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayManager {
	private final static Logger logger = LoggerFactory.getLogger(PayManager.class);
	
	private static PayManager instance;
	private List<QueueItemEntity> mVector;
	private List<QueueCashItemEntity> mCashVector;
	
	private PayManager() {
		mVector = new java.util.Vector<QueueItemEntity>();
		mCashVector = new java.util.Vector<PayManager.QueueCashItemEntity>();
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

	public List<QueueCashItemEntity> getCashList(){
		return this.mCashVector;
	}
	
	public void addReqQueue(String orderSn,String payOrderSn,String payCode) {
		QueueItemEntity entity = new QueueItemEntity();
		entity.orderSn = orderSn;
		entity.payCode = payCode;
		entity.payOrderSn = payOrderSn;
		entity.cnt = 0;
		mVector.add(entity);
	}
	
	public void addReq2CashQueue(String withDrawSn) {
		QueueCashItemEntity entity = new QueueCashItemEntity();
		entity.withDrawSn = withDrawSn;
		entity.cnt = 0;
		mCashVector.add(entity);
	}
	
	public class QueueItemEntity{
		public String orderSn;
		public String payOrderSn;
		public String payCode;
		public int cnt;
		public static final int MAX_CNT = 20;
	}
	
	public class QueueCashItemEntity{
		public String withDrawSn;
		public int cnt;
		public static final int MAX_CNT = 6;
	}
}
