package com.dl.shop.payment.service;

import java.math.BigDecimal;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserBankService;
import com.dl.member.dto.BankDTO;
import com.dl.member.param.BankCardParam;
import com.dl.member.param.BankCardSaveParam;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;

import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XianFengService {
	private final static Logger logger = LoggerFactory.getLogger(XianFengService.class);
	
	@Resource
	private PayLogService payLogService;
	@Resource
	private XianFengPayUtil xFPayUtil;
	@Resource
	private IUserBankService userBankService;
	@Resource
	private PayMentService paymentService;
	
	public BaseResult<Object> appPay(XianFengPayParam param){
		int payLogId = param.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null){
			logger.info("查询PayLog失败");
			return ResultGenerator.genFailResult("查询支付信息失败");
		}
		int payType = payLog.getPayType();
		int uid = payLog.getUserId();
		BigDecimal bigDecimal = payLog.getOrderAmount();
		String payOrderSn = payLog.getPayOrderSn();
		BigDecimal bigDec = bigDecimal.multiply(BigDecimal.valueOf(100));
		String amt = bigDec.intValue()+"";
		String certNo = param.getCertNo();
		String accNo = param.getAccNo();
		String phone = param.getPhone();
		String pName = null;
		String pInfo = null;
		if(payType == 0) {
			pName = "足彩订单支付";
			pInfo = "彩小秘支付服务";
		}else {
			pName = "充值支付";
			pInfo = "彩小秘充值服务";
		}
		//三要素校验
		
		//获取bankId
		
		//请求第三方申请接口
		
		//userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo
//		xFPayUtil.reqApply(payOrderSn,null,bigDec.intValue()+"",);
		return null;
	}
	
	/**
	 * 根据银行卡号查询银行信息
	 * @param bankCardNo
	 * @return
	 */
	public BaseResult<BankDTO> queryBankType(String bankCardNo){
		BankCardParam bankCardParams = new BankCardParam();
		bankCardParams.setBankCardNo(bankCardNo);
		BaseResult<BankDTO> baseResult = userBankService.queryUserBankType(bankCardParams);
		if(baseResult.getCode() == 0) {
			BankDTO bankDTO = baseResult.getData();
			if(bankDTO != null) {
				BankCardSaveParam param = new BankCardSaveParam();
				param.setCardNo(bankDTO.getBankcard());
				param.setBankLogo(bankDTO.getBanklogo());
				param.setBankName(bankDTO.getBankname());
				param.setCardType(bankDTO.getCardtype());
				param.setAbbreviation(bankDTO.getAbbreviation());
				int type = 0;
				String cardType = bankDTO.getCardtype();
				if("借记卡".equals(cardType)) {
					type = 0;
				}else if("贷记卡".equals(cardType)) {
					type = 1;
				}
				param.setType(type);
				param.setPurpose(1);
				if(type == 0 || type == 1) {
					logger.info("[queryBankType]" + " " + bankDTO.getBankcard() +" saveInfo...");
					userBankService.saveBankInfo(param);
				}
			}
		}
		return baseResult;
	}
	
	/**
	 * 查询信息
	 * @param payOrderSn
	 * @return
	 */
	public BaseResult<Object> getPaySms(String payOrderSn){
		try {
			RspApplyBaseEntity rspEntity = xFPayUtil.reqApplySms(payOrderSn);
			if(rspEntity.isSucc()) {
				return ResultGenerator.genSuccessResult("查询成功");
			}else {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_SMS_ERROR.getcode(),PayEnums.PAY_XIANFENG_SMS_ERROR.getMsg() +"[" + rspEntity.resMessage + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				logger.info("[query]" + "先锋查询成功...");
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
				logger.info("[query]" + "先锋查询异常...");
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_FAILURE.getcode(),PayEnums.PAY_XIANFENG_FAILURE.getMsg()+"[" + rEntity.resMessage+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
