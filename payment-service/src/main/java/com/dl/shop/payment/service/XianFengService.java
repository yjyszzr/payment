package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.RegexUtil;
import com.dl.member.api.IUserBankService;
import com.dl.member.dto.BankDTO;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.param.BankCardParam;
import com.dl.member.param.BankCardSaveParam;
import com.dl.member.param.UserBankPurposeQueryParam;
import com.dl.shop.payment.dao.PayBankRecordMapper;
import com.dl.shop.payment.dto.PayBankRecordDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.XianFengApplyCfgDTO;
import com.dl.shop.payment.dto.XianFengApplyDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayBankRecordModel;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.XianFengPayConfirmParam;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspNotifyEntity;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;

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
	@Resource
	private PayBankRecordMapper payBankRecordMapper;
	@Resource
	private Constants xfConstants;
	
	public BaseResult<Object> appPayCfm(PayLog payLog,String code){
		String payOrderSn = payLog.getPayOrderSn();
		RspApplyBaseEntity rspEntity = null;
		try {
			rspEntity = xFPayUtil.reqApplyCfg(code,payOrderSn);
			RspOrderQueryEntity rspOrderQueryEntity = rspEntity.buildRspOrderQueryEntity("app_xianfeng");
			int payType = payLog.getPayType();
			if(0 == payType) {
				paymentService.orderOptions(payLog,rspOrderQueryEntity);
			} else if(1 == payType){
				paymentService.rechargeOptions(payLog,rspOrderQueryEntity);
			}
		} catch (Exception e) {
			logger.error("先锋支付失败",e);
		}
		if(rspEntity != null) {
			log.error("支付处理响应pay_orderSn={},retCode={},retMessage={},",rspEntity.merchantNo,rspEntity.resCode,rspEntity.resMessage);
			if(rspEntity.isSucc()) {
				return ResultGenerator.genSuccessResult("支付申请成功");
			}else if(rspEntity.isVerfyCodeWrong()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getMsg());
			}else if(rspEntity.isVerifyCodeInValid()) {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getMsg());
			}else if(rspEntity.isDoing()) {
				return ResultGenerator.genSuccessResult("支付处理中");
//				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_PAY_DOING.getcode(),PayEnums.PAY_XIANFENG_BANK_PAY_DOING.getMsg());
			}else if(rspEntity.isFail()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getcode(),PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getMsg());
			}else{
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg());
			}
		}
		return ResultGenerator.genFailResult("请求失败");
	}
	public BaseResult<XianFengApplyDTO> appPay(XianFengPayParam param,String token){
		int payLogId = param.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null){
			logger.info("查询PayLog失败");
			return ResultGenerator.genFailResult("查询支付信息失败");
		}
		int payType = payLog.getPayType();
		int userId = payLog.getUserId();
		BigDecimal bigDecimal = payLog.getOrderAmount();
		String payOrderSn = payLog.getPayOrderSn();
		BigDecimal bigDec = bigDecimal.multiply(BigDecimal.valueOf(100)).setScale(0,RoundingMode.HALF_EVEN);
		String certNo = param.getCertNo();
		String accNo = param.getAccNo();
		String mobileNo = param.getPhone();
		String pName = null;
		String pInfo = null;
		String accName = param.getName();
		String cvn2 = null;
		String validDate = null;
		if(payType == 0) {
			pName = "足彩订单支付";
			pInfo = "彩小秘支付服务";
		}else {
			pName = "充值支付";
			pInfo = "彩小秘充值服务";
		}
		BankCardParam bankCardParams = new BankCardParam();
		bankCardParams.setBankCardNo(accNo);
		BaseResult<BankDTO> baseResult = userBankService.queryUserBankType(bankCardParams);
		if(baseResult==null||baseResult.getCode() != 0) {
			logger.info("查询PayLog失败cardNo={},retCode={},retMsg={}",accNo,baseResult==null?"":baseResult.getCode(),baseResult==null?"":baseResult.getMsg());
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANKTYPE_FAILURE.getcode(),PayEnums.PAY_XIANFENG_BANKTYPE_FAILURE.getMsg());
		}
		BankDTO bankDTO = baseResult.getData();
		String bankId = bankDTO.getAbbreviation();
		String bankName = bankDTO.getBankname();
		String cardType = bankDTO.getCardtype();
		Integer bankType = Integer.valueOf(0);
		if("贷记卡".equals(cardType)) {
			bankType = Integer.valueOf(1);
		}
		RspApplyBaseEntity rspEntity = null;
		//请求第三方申请接口
		//userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo
		logger.info("===================请求先锋支付==========================");
		logger.info("[appPay]" +" userId:" + userId +" amt:" + bigDec.toString() +" certNo:" + certNo 
				+" accName:" + accName +" mobileNo:" + mobileNo + " bankId:" + bankId +" pName:" 
				+ pName +" pInfo:" + pInfo + " payOrderSn:" + payOrderSn + " cvn2:" + cvn2 + " validDate:" + validDate);
		logger.info("===================请求先锋支付==========================");
		try {
			rspEntity = xFPayUtil.reqApply(payOrderSn,null,bigDec.toString(),certNo,accNo,accName,mobileNo,bankId,pName,pInfo,cvn2,validDate);
		} catch (Exception e) {
			logger.error("appPay支付报错payOrderSn={}",payOrderSn,e);
		}
		logger.info("[appPay]" + " rsp:" + rspEntity);
		if(rspEntity != null) {
			if(rspEntity.isDoing()) {
				//更新payLog信息
				String tradeNo = rspEntity.tradeNo;
				PayLog updatePayLog = new PayLog();
				updatePayLog.setOrderSn(payLog.getOrderSn());
				updatePayLog.setTradeNo(tradeNo);
				updatePayLog.setIsPaid(0);
				payLogService.updatePayLogByOrderSn(updatePayLog);
				XianFengApplyDTO xFApplyDTO = new XianFengApplyDTO();
				xFApplyDTO.setToken(token);
				//验证码发送成功，bank信息入库
				logger.info("[appPay saveBankInfo]" + " userId:" + userId + " bankType:" + bankType 
						+ " accNo:" + accNo + " certNo:" + certNo + " mobileNo:" + mobileNo + " accName:" + accName);
                saveBankInfo(userId,bankType,accNo,certNo,mobileNo,accName,cvn2,validDate,bankName,payLogId);
				return ResultGenerator.genSuccessResult("验证码发送成功",xFApplyDTO);	
			}else if(rspEntity.isVerfyCodeWrong()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getMsg());
			}else if(rspEntity.isVerifyCodeInValid()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getMsg());
			}else if(rspEntity.isFail()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getcode(),PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getMsg());
			}else {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg()+"["+rspEntity.resMessage+"]");
			}
		}
		return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg());
	}
	
	private void saveBankInfo(int userId,int bankType,String accNo,String certNo,String mobileNo,String accName,String cvn2,String vaildDate,String bankName,int payLogId) {
		PayBankRecordModel payBankRecordModel = new PayBankRecordModel();
			payBankRecordModel = new PayBankRecordModel();
			payBankRecordModel.setUserId(userId);
			payBankRecordModel.setBankCardNo(accNo);
			payBankRecordModel.setCertNo(certNo);
			payBankRecordModel.setPhone(mobileNo);
			payBankRecordModel.setUserName(accName);
			payBankRecordModel.setBankType(bankType);
			payBankRecordModel.setCvn2(cvn2);
			payBankRecordModel.setValidDate(vaildDate);
			payBankRecordModel.setLastTime(DateUtil.getCurrentTimeLong());
			payBankRecordModel.setBankName(bankName);
			payBankRecordModel.setPayLogId(payLogId);
			int cnt = payBankRecordMapper.insert(payBankRecordModel);
			logger.info("[appPay] payLogId="+payLogId+"bankCardNo=" +accNo+" payBankRecordMapper.insert cnt:" + cnt);
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
		return baseResult;
	}
	
	/**
	 * 查询信息
	 * @param payOrderSn
	 * @return
	 */
	public BaseResult<XianFengApplyDTO> getPaySms(String payOrderSn,String token){
		try {
			RspApplyBaseEntity rspEntity = xFPayUtil.reqApplySms(payOrderSn);
			if(rspEntity.isSucc()||rspEntity.isDoing()) {
				XianFengApplyDTO applyDTO = new XianFengApplyDTO();
				applyDTO.setToken(token);
				return ResultGenerator.genSuccessResult("验证码发送成功",applyDTO);
			}else if(rspEntity.isFail()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getcode(),PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getMsg());
			}else if(rspEntity.isVerfyCodeWrong()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getMsg());
			}else if(rspEntity.isVerifyCodeInValid()){
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_INVALID.getMsg());
			}else{
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_SMS_ERROR.getcode(),PayEnums.PAY_XIANFENG_SMS_ERROR.getMsg() +"[" + rspEntity.resMessage + "]");
			}
		} catch (Exception e) {
			log.error("先锋支付重发验证码失败payOrderSn={}",payOrderSn,e);
		}
		return null;
	}
	
	/***
	 * 支付配置相关
	 * @param params
	 * @return
	 */
	public BaseResult<XianFengApplyCfgDTO> appPayCfg(int payLogId){
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			return ResultGenerator.genFailResult("订单查询失败 payLogId:" + payLogId);
		}
		BigDecimal bigAmt = payLog.getOrderAmount();
		List<PayBankRecordDTO> mBankList = paymentService.listUserBanks(payLog.getUserId());
		//按照时间先后排序
		sortBankList(mBankList);
		XianFengApplyCfgDTO xFApplyCfgDTO = new XianFengApplyCfgDTO();
		xFApplyCfgDTO.setBankList(mBankList);
		xFApplyCfgDTO.setAmt(bigAmt+"");
		return ResultGenerator.genSuccessResult("succ",xFApplyCfgDTO);
	}
	
	private void sortBankList(List<PayBankRecordDTO> mList) {
		Collections.sort(mList,new Comparator<PayBankRecordDTO>() {
			@Override
			public int compare(PayBankRecordDTO left, PayBankRecordDTO right) {
				if(left != null && right != null) {
					return left.getLastTime() - right.getLastTime();
				}
				return 0;
			}
		});
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
					bResult = paymentService.orderOptions(payLog,response);
				}else {
					bResult = paymentService.rechargeOptions(payLog,response);
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
	
	/***
	 * 先锋支付Notify通知
	 */
	public boolean payNotify(RspNotifyEntity rspEntity) {
		boolean isSucc = false;
		logger.info("[XianFengService.payNotify]");
		String payLogSn = rspEntity.merchantNo;
		PayLog payLog = payLogService.findPayLogByOrderSign(payLogSn);
		if(payLog == null) {
			logger.info("[payNotify]"+"该支付订单查询失败 payLogSn:" + payLogSn);
			return isSucc;
		}
		int isPaid = payLog.getIsPaid();
		if(isPaid == 1) {
			logger.info("[payNotify]" +" 该订单已支付...");
			return isSucc;
		}
		BigDecimal bigAmt = payLog.getOrderAmount();
		bigAmt = bigAmt.movePointRight(2);
		int amt = bigAmt.intValue();
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		if(xfConstants.getMER_ID().equals(rspEntity.merchantId) && (rspEntity.amount.equals(amt+""))) {
			logger.info("[payNotify]" +" 商户号，交易金额校验成功, amt:" + rspEntity.amount +" merchantId:" + rspEntity.merchantId);
			BaseResult<RspOrderQueryDTO> bResult = null;
			if(rspEntity.isSucc()) {
				RspOrderQueryEntity response = new RspOrderQueryEntity();
				response.setResult_code("00000");
				response.setTrade_no(rspEntity.tradeNo);
				response.setPayCode(payCode);
				response.setType(RspOrderQueryEntity.TYPE_XIANFENG);
				response.setTrade_status(rspEntity.status);
				if(payType == 0) {
					bResult = paymentService.orderOptions(payLog,response);
				}else {
					bResult = paymentService.rechargeOptions(payLog,response);
				}
			}
			if(bResult != null && bResult.getCode() == 0) {
				isSucc = true;
			}
		}else {
			logger.error("[payNotify] payOrderSn=" +rspEntity.merchantNo+" 商户号，交易金额校验失败, amt:" + rspEntity.amount +" merchantId:" + rspEntity.merchantId);
		}
		return isSucc;
	}
}
