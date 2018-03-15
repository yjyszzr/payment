package com.dl.shop.payment.service;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class PayLogService extends AbstractService<PayLog> {
	@Resource
	private PayLogMapper payLogMapper;


	public PayLog savePayLog(PayLog payLog) {
		PayLog existPayLog = payLogMapper.existPayLog(payLog);
		if(null != existPayLog && existPayLog.getLogId() != null) {
			return existPayLog;
		}
		int rst = payLogMapper.insert(payLog);
		if(rst == 1) {
			return payLog;
		}
		return null;
	}

	public int findPayStatus(PayLog payLog) {
		Integer status = payLogMapper.findPayStatus(payLog);
		return null==status?0:status;
	}

	public int updatePayMsg(PayLog payLog) {
		payLog.setLastTime(DateUtil.getCurrentTimeLong());
		return payLogMapper.updatePayMsg(payLog);
	}
}
