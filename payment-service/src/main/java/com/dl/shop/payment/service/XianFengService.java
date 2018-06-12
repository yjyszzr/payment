package com.dl.shop.payment.service;

import java.math.BigDecimal;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserBankService;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XianFengService {
	private final static Logger logger = LoggerFactory.getLogger(CashService.class);
	
	@Resource
	private PayLogService payLogService;
	@Resource
	private XianFengPayUtil xFPayUtil;
	@Resource
	private IUserBankService userBankService;
	
	
	public BaseResult<Object> appPay(int payLogId){
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null){
			logger.info("查询PayLog失败");
			return ResultGenerator.genFailResult("查询支付信息失败");
		}
		int uid = payLog.getUserId();
		BigDecimal bigDecimal = payLog.getOrderAmount();
		String payOrderSn = payLog.getPayOrderSn();
		//userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo
//		xFPayUtil.reqApply(payOrderSn,null,);
		return null;
	}
}
