package com.dl.shop.payment.service;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dto.PayLogDTO;
import com.dl.shop.payment.dto.PayLogDetailDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.ValidPayDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.param.SysConfigParam;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import javax.annotation.Resource;

@Service
@Transactional
public class PayLogService extends AbstractService<PayLog> {
	@Resource
	private PayLogMapper payLogMapper;
	@Resource
	private RkPayService rkPayService;
	@Resource
	private IUserAccountService userAccountService;
	
	public PayLog savePayLog(PayLog payLog) {
//		PayLog existPayLog = payLogMapper.existPayLog(payLog);
//		if(null != existPayLog && existPayLog.getLogId() != null) {
//			return existPayLog;
//		}
		//生成的方法
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
	
	/***
	 * 根据payOrderSn查找PayLog
	 * @param orderSign
	 * @return
	 */
	public PayLog findPayLogByOrderSign(String orderSign) {
		return payLogMapper.findPayLogByOrderSign(orderSign);
	}

	/***
	 * 根据OrderSn查找PayLog
	 * @param orderSn
	 * @return
	 */
	public PayLog findPayLogByOrderSn(String orderSn) {
		return payLogMapper.findPayLogByOrderSn(orderSn);
	}
	
	public int updatePayLog(PayLog payLog) {
		return payLogMapper.updatePayLog(payLog);
	}
	
	public int updatePayLogByOrderSn(PayLog payLog) {
		return payLogMapper.updatePayLogByOrderSn(payLog);
	}
	
	public BaseResult<PayLogDTO> queryPayLogByPayLogId(Integer payLogId) {
		Integer userId = SessionUtil.getUserId();
		SysConfigParam cfg = new SysConfigParam();
		cfg.setBusinessId(67);//读取财务账号id
		int cwuserId = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
		if(userId!=null && userId==cwuserId) {//财务账号
			com.dl.shop.payment.param.StrParam emptyParam = new com.dl.shop.payment.param.StrParam();
			String strMoney="0";
			BaseResult<RspOrderQueryDTO> ymoney = rkPayService.getShMoney(emptyParam);
			if(ymoney!=null && ymoney.getData()!=null) {
				strMoney=ymoney.getData().getDonationPrice()!=null?ymoney.getData().getDonationPrice():"0";//商户余额
			}
			PayLogDTO payLogDTO = new PayLogDTO();
			payLogDTO.setOrderAmount(BigDecimal.valueOf(Double.parseDouble(strMoney)));
			return ResultGenerator.genSuccessResult("success", payLogDTO);
		}
		PayLog payLog = payLogMapper.findPayLogByPayLogId(payLogId);
		if(null == payLog) {
			return ResultGenerator.genResult(PayEnums.PAY_DBDATA_IS_NOT_IN.getcode(),PayEnums.PAY_DBDATA_IS_NOT_IN.getMsg());
		}
		PayLogDTO payLogDTO = new PayLogDTO();
		BeanUtils.copyProperties(payLog, payLogDTO);
		return ResultGenerator.genSuccessResult("success", payLogDTO);
	}
	
	public BaseResult<PayLogDetailDTO> queryPayLogByOrderSn(String orderSn){
		PayLog payLog = payLogMapper.findPayLogByOrderSn(orderSn);
		if(null == payLog) {
			return ResultGenerator.genResult(PayEnums.PAY_DBDATA_IS_NOT_IN.getcode(),PayEnums.PAY_DBDATA_IS_NOT_IN.getMsg());
		}
		PayLogDetailDTO payLogDTO = new PayLogDetailDTO();
		BeanUtils.copyProperties(payLog, payLogDTO);
		return ResultGenerator.genSuccessResult("success", payLogDTO);
	}
	
	/**
	 * 查询用户是否有成功的交易
	 * @param userId
	 * @return
	 */
	public BaseResult<ValidPayDTO> validUserPay(Integer userId){
		ValidPayDTO validPayDTO= new ValidPayDTO();
		int rst = payLogMapper.countValidPayLogByUserId(userId);
		if(rst > 0) {
			validPayDTO.setHasPaid("1");
			return ResultGenerator.genSuccessResult("success", validPayDTO);
		}
		return ResultGenerator.genSuccessResult("success", validPayDTO);
	}
	
}

