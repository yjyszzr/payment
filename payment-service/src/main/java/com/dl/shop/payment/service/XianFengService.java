package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
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
	private final static Map<String,String> xianfengErrorCodeAndMsgMap = new HashMap<String, String>();
	static{
		xianfengErrorCodeAndMsgMap.put("00000","成功");
		xianfengErrorCodeAndMsgMap.put("00021","代扣处理中");
		xianfengErrorCodeAndMsgMap.put("00022","代扣失败");
		xianfengErrorCodeAndMsgMap.put("00023","代发处理中");
		xianfengErrorCodeAndMsgMap.put("00024","代发失败");
		xianfengErrorCodeAndMsgMap.put("00025","退款处理中");
		xianfengErrorCodeAndMsgMap.put("00026","退款失败");
		xianfengErrorCodeAndMsgMap.put("10010","余额不足");
		xianfengErrorCodeAndMsgMap.put("10025","金额超限");
		xianfengErrorCodeAndMsgMap.put("30002","金额超过日累计限额");
		xianfengErrorCodeAndMsgMap.put("30001","金额超过单笔限额");
		xianfengErrorCodeAndMsgMap.put("10042","金额超过月累计限额");
		xianfengErrorCodeAndMsgMap.put("10043","支付金额小于最低限制");
		xianfengErrorCodeAndMsgMap.put("10052","支付金额不在可支付金额范围内，或者银行卡不支持");
		xianfengErrorCodeAndMsgMap.put("00009","支付金额不在规定范围内");
		xianfengErrorCodeAndMsgMap.put("10024","姓名、身份证、银行卡或手机号信息不一致");
		xianfengErrorCodeAndMsgMap.put("10046","户名有误");
		xianfengErrorCodeAndMsgMap.put("10047","账号有误");
		xianfengErrorCodeAndMsgMap.put("10014","卡bin查询无记录");
		xianfengErrorCodeAndMsgMap.put("10011","需开通银联无卡支付功能");
		xianfengErrorCodeAndMsgMap.put("10026","账户不存在");
		xianfengErrorCodeAndMsgMap.put("00041","暂不支持该银行");
		xianfengErrorCodeAndMsgMap.put("10040","暂不支持此类型银行卡，请更换其他银行卡");
		xianfengErrorCodeAndMsgMap.put("10035","商户状态异常");
		xianfengErrorCodeAndMsgMap.put("10028","账户状态异常");
		xianfengErrorCodeAndMsgMap.put("10053","支付次数过多，请24小时后再试");
		xianfengErrorCodeAndMsgMap.put("10039","支付次数超过发卡银行限制，本次支付失败");
		xianfengErrorCodeAndMsgMap.put("00101","交易超出限额/次数");
		xianfengErrorCodeAndMsgMap.put("10050","余额不足次数过多");
		xianfengErrorCodeAndMsgMap.put("10051","当日失败次数过多，请24小时后再试");
		xianfengErrorCodeAndMsgMap.put("20003","通讯异常");
		xianfengErrorCodeAndMsgMap.put("20004","短信校验次数超限");
		xianfengErrorCodeAndMsgMap.put("20005","短信校验失败");
		xianfengErrorCodeAndMsgMap.put("20006","短信发送次数超限");
		xianfengErrorCodeAndMsgMap.put("20007","短信发送失败");
		xianfengErrorCodeAndMsgMap.put("20009","风控校验不通过");
		xianfengErrorCodeAndMsgMap.put("00063","银行系统升级中，请您稍后再试");
		xianfengErrorCodeAndMsgMap.put("10054","交易超时，请重新提交");
		xianfengErrorCodeAndMsgMap.put("10055","交易提交数量过多,请降低提交频率");
		xianfengErrorCodeAndMsgMap.put("20000","系统异常，请稍后重试");
		xianfengErrorCodeAndMsgMap.put("10027","银行通讯异常");
		xianfengErrorCodeAndMsgMap.put("10048","交易要素不完整，请核实后再试");
		xianfengErrorCodeAndMsgMap.put("10000","参数不合法");
		xianfengErrorCodeAndMsgMap.put("10001","请求参数有误。");
		xianfengErrorCodeAndMsgMap.put("10031","未开通该产品");
		xianfengErrorCodeAndMsgMap.put("10007","商户编号不存在！");
		xianfengErrorCodeAndMsgMap.put("10038","支付失败，请稍后再试");
		xianfengErrorCodeAndMsgMap.put("10049","交易失败，详情请咨询发卡行");
		xianfengErrorCodeAndMsgMap.put("10009","订单不存在！");
		xianfengErrorCodeAndMsgMap.put("10005","您已重复提交该订单，请稍后再试");
		xianfengErrorCodeAndMsgMap.put("10006","订单已超时关闭");
		xianfengErrorCodeAndMsgMap.put("10037","支付渠道系统繁忙，请稍后再试");
		xianfengErrorCodeAndMsgMap.put("10003","没有查询到合适的支付渠道");
		xianfengErrorCodeAndMsgMap.put("10036","订单状态异常");
		xianfengErrorCodeAndMsgMap.put("99999","未定义类型");
		xianfengErrorCodeAndMsgMap.put("10013","累计退款金额超限");
		xianfengErrorCodeAndMsgMap.put("99024","服务调用异常");
		xianfengErrorCodeAndMsgMap.put("99020","签名校验失败");
		xianfengErrorCodeAndMsgMap.put("99011","未知的系统错误");
		xianfengErrorCodeAndMsgMap.put("99012","数据验证错误");
		xianfengErrorCodeAndMsgMap.put("99016","参数无效");
		xianfengErrorCodeAndMsgMap.put("99018","白名单验证失败");
		xianfengErrorCodeAndMsgMap.put("99019","白名单用户不存在");
		xianfengErrorCodeAndMsgMap.put("99021","service不存在");
		xianfengErrorCodeAndMsgMap.put("99022","sign key 不存在");
		xianfengErrorCodeAndMsgMap.put("99023","verify sign failure");
		xianfengErrorCodeAndMsgMap.put("99025","转发URL异常");
		xianfengErrorCodeAndMsgMap.put("99026","参数异常");
		xianfengErrorCodeAndMsgMap.put("99027","service为空");
		xianfengErrorCodeAndMsgMap.put("99028","merchantId 为空");
		xianfengErrorCodeAndMsgMap.put("99029","商户密钥不存在");
		xianfengErrorCodeAndMsgMap.put("99030","防重复请求码校验失败");
		xianfengErrorCodeAndMsgMap.put("99031","服务版本号version错误");
		xianfengErrorCodeAndMsgMap.put("99032","请求IP非法");
		xianfengErrorCodeAndMsgMap.put("99033","数据解密失败");
		xianfengErrorCodeAndMsgMap.put("99034","数据加密失败");
	}
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
			log.info("先锋支付确认响应信息验证码={},payOrderSn={},resEntity={}",code,payOrderSn,rspEntity);
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
				String errorMsg = xianfengErrorCodeAndMsgMap.get(rspEntity.resCode);
				if(StringUtils.isEmpty(errorMsg)){
					errorMsg = PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getMsg();
				}
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getcode(),errorMsg);
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
		if(Integer.valueOf(3).equals(payLog.getIsPaid())){
			logger.info("[getPaySms]" + "订单号pay_order_sn={},order_sn={}已失败,不能获取验证码",payLog.getPayOrderSn(),payLog.getOrderSn());
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_CANCEL_ERROR.getcode(),PayEnums.PAY_XIANFENG_CANCEL_ERROR.getMsg());
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
            if(StringUtils.isEmpty(param.getCvn2()) || StringUtils.isEmpty(param.getValidDate())){
                PayBankRecordModel payBankCard = payBankRecordMapper.selectByBankCardAndPaySuccess(accNo);
                if(payBankCard!=null){
                    cvn2 = payBankCard.getCvn2();
                    validDate = payBankCard.getValidDate();
                }else{                    
                    return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_CVVN_ERROR.getcode(),PayEnums.PAY_XIANFENG_CVVN_ERROR.getMsg());
                }
            }else {
                cvn2 = param.getCvn2();
                validDate = param.getValidDate();
            }
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
				String errorMsg = xianfengErrorCodeAndMsgMap.get(rspEntity.resCode);
				if(StringUtils.isEmpty(errorMsg)){
					errorMsg = PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getMsg();
				}
				return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANK_INFO_ERROR.getcode(),errorMsg);
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
	public void bankRemove(String recordId) {
		PayBankRecordModel queryBank = new PayBankRecordModel();
		queryBank.setId(Integer.parseInt(recordId));
		List<PayBankRecordModel> payBankList = payBankRecordMapper.queryPayBankRecordModelById(queryBank);
		if(!CollectionUtils.isEmpty(payBankList)){
			String bankCardNo = payBankList.get(0).getBankCardNo();
			log.info("移除客户卡号={}",bankCardNo);
			payBankRecordMapper.updateIspaidRemoveByCardNo(bankCardNo);
		}else{
			log.info("移除客户dl_pay_bank_record 表id={}",recordId);
		}
	}
}
