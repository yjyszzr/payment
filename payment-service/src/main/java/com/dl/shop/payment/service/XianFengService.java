package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.druid.util.StringUtils;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
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
import com.dl.shop.payment.pay.common.PayManager;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspApplyBaseEntity;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;
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
	@Resource
	private PayBankRecordMapper payBankRecordMapper;
	
	public BaseResult<Object> appPayCfm(XianFengPayConfirmParam param){
		int payLogId = param.getPayLogId();
		String code = param.getCode();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			logger.info("[appPayCfm]" + "查询PayLog失败");
			return ResultGenerator.genFailResult("查询支付信息失败");
		}
		String payCode = payLog.getPayCode();
		String orderSn = payLog.getOrderSn();
		if(StringUtils.isEmpty(code)) {
			logger.info("[appPayCfm]" + " code:" + code);
			return ResultGenerator.genFailResult("请输入验证码");
		}
		String payOrderSn = payLog.getPayOrderSn();
		RspApplyBaseEntity rspEntity = null;
		try {
			rspEntity = xFPayUtil.reqApplyCfg(code,payOrderSn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(rspEntity != null) {
			if(rspEntity.isSucc()) {
				//主动query接口
				PayManager.getInstance().addReqQueue(orderSn, payOrderSn, payCode);
				return ResultGenerator.genSuccessResult("支付申请成功");
			}else {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg() + "[" + rspEntity.resMessage +"]");
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
		BigDecimal bigDec = bigDecimal.multiply(BigDecimal.valueOf(100));
		String amt = bigDec.intValue()+"";
		String certNo = param.getCertNo();
		String accNo = param.getAccNo();
		String mobileNo = param.getPhone();
		String pName = null;
		String pInfo = null;
		String accName = param.getName();
		if(payType == 0) {
			pName = "足彩订单支付";
			pInfo = "彩小秘支付服务";
		}else {
			pName = "充值支付";
			pInfo = "彩小秘充值服务";
		}
		//获取bankId
		UserBankPurposeQueryParam queryParams = new UserBankPurposeQueryParam();
		queryParams.setUserId(userId);
		queryParams.setPurpose(1);
		queryParams.setBankCardCode(accNo);
		BaseResult<UserBankDTO> baseResult = userBankService.queryBankByPurpose(queryParams);
		if(baseResult.getCode() != 0 || baseResult.getData() == null) {
			log.info("[appPay]" + "查询银行卡为空...");
			return ResultGenerator.genFailResult("查询银行卡为空");
		}
		UserBankDTO userBankDTO = baseResult.getData();
		String bankId = userBankDTO.getAbbreviation();
		RspApplyBaseEntity rspEntity = null;
		//请求第三方申请接口
		//userId, amt, certNo, accNo, accName, mobileNo, bankId, pName, pInfo
		logger.info("===================请求先锋支付==========================");
		logger.info("[appPay]" +" userId:" + userId +" amt:" + amt +" certNo:" + certNo 
				+" accName:" + accName +" mobileNo:" + mobileNo + " bankId:" + bankId +" pName:" 
				+ pName +" pInfo:" + pInfo + " payOrderSn:" + payOrderSn);
		logger.info("===================请求先锋支付==========================");
		try {
			rspEntity = xFPayUtil.reqApply(payOrderSn,null,amt+"",certNo,accNo,accName,mobileNo,bankId,pName,pInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rspEntity != null) {
			logger.info("[appPay]" + " rsp:" + rspEntity);
			if(rspEntity.isSucc()) {
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
				Integer bankType = userBankDTO.getType();
				logger.info("[appPay saveBankInfo]" + " userId:" + userId + " bankType:" + bankType 
						+ " accNo:" + accNo + " certNo:" + certNo + " mobileNo:" + mobileNo + " accName:" + accName);
				saveBankInfo(userId,bankType,accNo,certNo,mobileNo,accName);
				return ResultGenerator.genSuccessResult("验证码发送成功",xFApplyDTO);	
			}else {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg()+"["+rspEntity.resMessage+"]");
			}
		}
		return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_PAY_ERROR.getcode(),PayEnums.PAY_XIANFENG_PAY_ERROR.getMsg()+"[先锋请求异常]");
	}
	
	private void saveBankInfo(int userId,int bankType,String accNo,String certNo,String mobileNo,String accName) {
		PayBankRecordModel payBankRecordModel = new PayBankRecordModel();
		payBankRecordModel.setUserId(userId);
		List<PayBankRecordModel> sList = payBankRecordMapper.listUserBank(payBankRecordModel);
		PayBankRecordModel findModel = null;
		for(int i = 0;i < sList.size();i++) {
			PayBankRecordModel payBRModel = sList.get(i);
			if(payBRModel.getBankCardNo().equals(accNo)) {
				findModel = payBankRecordModel;
				break;
			}
		}
		if(findModel == null) {
			payBankRecordModel = new PayBankRecordModel();
			payBankRecordModel.setUserId(userId);
			payBankRecordModel.setBankCardNo(accNo);
			payBankRecordModel.setCertNo(certNo);
			payBankRecordModel.setPhone(mobileNo);
			payBankRecordModel.setUserName(accName);
			payBankRecordModel.setBankType(bankType);
			int cnt = payBankRecordMapper.insert(payBankRecordModel);
			logger.info("[appPay]" + " payBankRecordMapper.insert cnt:" + cnt);
		}else {
			findModel.setUserId(userId);
			findModel.setBankCardNo(accNo);
			findModel.setCertNo(certNo);
			findModel.setPhone(mobileNo);
			findModel.setUserName(accName);
			int cnt = payBankRecordMapper.updateInfo(findModel);
			logger.info("[appPay]" + " payBankRecordMapper.updateInfo cnt:" + cnt);
		}
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
	public BaseResult<XianFengApplyDTO> getPaySms(String payOrderSn,String token){
		try {
			RspApplyBaseEntity rspEntity = xFPayUtil.reqApplySms(payOrderSn);
			if(rspEntity.isSucc()) {
				XianFengApplyDTO applyDTO = new XianFengApplyDTO();
				applyDTO.setToken(token);
				return ResultGenerator.genSuccessResult("验证码发送成功",applyDTO);
			}else {
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_SMS_ERROR.getcode(),PayEnums.PAY_XIANFENG_SMS_ERROR.getMsg() +"[" + rspEntity.resMessage + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/***
	 * 支付配置相关
	 * @param params
	 * @return
	 */
	public BaseResult<XianFengApplyCfgDTO> appPayCfg(int userId){
		List<PayBankRecordDTO> mBankList = paymentService.listUserBanks(userId);
		XianFengApplyCfgDTO xFApplyCfgDTO = new XianFengApplyCfgDTO();
		xFApplyCfgDTO.setBankList(mBankList);
		return ResultGenerator.genSuccessResult("succ",xFApplyCfgDTO);
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
