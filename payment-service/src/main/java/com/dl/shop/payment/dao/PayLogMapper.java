package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.PayLog;

import tk.mybatis.mapper.provider.base.BaseInsertProvider;

public interface PayLogMapper extends Mapper<PayLog> {
	
	/**
	 * 查找订单的状态
	 * @param payLog
	 * @return
	 */
	Integer findPayStatus(PayLog payLog);
	/**
	 *  查找同一订单在同一支付方式下是否存在记录
	 * @param payLog
	 * @return
	 */
	PayLog existPayLog(PayLog payLog);
	
	@Options(useGeneratedKeys = true, keyProperty = "logId")
	@InsertProvider(type = BaseInsertProvider.class, method = "dynamicSQL")
    int insert(PayLog payLog);
	
	/**
	 * 更新支付状态及信息
	 * @param payLog
	 * @return
	 */
	int updatePayMsg(PayLog payLog);

	/***
	 * 根据PayOrderSign查找该数据
	 * @param orderSign
	 * @return
	 */
	PayLog findPayLogByOrderSign(String orderSign);
	
	
	/***
	 * 根据orderSign查找该数据
	 * @param orderSign
	 * @return
	 */
	PayLog findPayLogByOrderSn(String orderSign);
	
	
	/**
	 * 更新订单信息
	 * @param payLog
	 * @return
	 */
	int updatePayLog(PayLog payLog);
	
	/**
	 * 根据订单号更新
	 * @param payLog
	 * @return
	 */
	int updatePayLogByOrderSn(PayLog payLog);
	
	/***
	 * 根据logId查找该数据
	 * @param payLogId
	 * @return
	 */
	PayLog findPayLogByPayLogId(@Param("logId")Integer logId);
	
	//获取没有支付的订单支付信息
	List<PayLog> findUnPayOrderPayLogs();
	
	List<PayLog> findUnPayChargePayLogs();
	
	/**
	 * 
	 * @param 根据userId查询有效的支付记录
	 * @return
	 */
	int countValidPayLogByUserId(@Param("userId")Integer userId);
	int updatePayLogFail0To3(PayLog updatePayLog);
	int updatePayLogSuccess0To1(PayLog updatePayLog);
	int updatePayLogTradeNoByPayOrderSn(@Param("payOrderSn") String payOrderSn, @Param("tradeNo") String tradeNo);
	int shutDownBetValue();
	
}