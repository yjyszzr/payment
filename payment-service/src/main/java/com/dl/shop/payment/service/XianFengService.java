package com.dl.shop.payment.service;

import java.math.BigDecimal;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserBankService;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
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
	@Resource
	private PayMentService paymentService;
	
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
	
	/***
	 * 订单查询
	 * @param payLog
	 * @param payOrderSn
	 * @return
	 */
	public BaseResult<String> query(PayLog payLog,String payOrderSn){
		try {
			RspApplyBaseEntity rEntity = xFPayUtil.queryPayByOrderNo(payOrderSn);
			if(rEntity.isSucc()) {
				int payType = payLog.getPayType();
				RspOrderQueryEntity response = new RspOrderQueryEntity();
				response.setResult_code("00000");
				response.setTrade_no(rEntity.tradeNo);
				response.setResult_msg(rEntity.resMessage);
				BaseResult<RspOrderQueryDTO> bResult = null;
				if(payType == 0) {
					bResult = paymentService.orderOptions("xFengQuery",payLog,response);
				}else {
					bResult = paymentService.rechargeOptions("xFengQuery",payLog,response);
				}
				if(bResult != null && bResult.getCode() == 0) {
					if(payType == 0) {
						return ResultGenerator.genSuccessResult("订单已支付");
					}else {
						return ResultGenerator.genSuccessResult("充值成功");
					}
				}else {
					return ResultGenerator.genFailResult("操作失败");
				}
			}else{
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_FAILURE.getcode(),PayEnums.PAY_XIANFENG_FAILURE.getMsg()+"[" + rEntity.resMessage+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
