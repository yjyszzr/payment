package com.dl.shop.payment.web;

import java.math.BigDecimal;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.druid.util.StringUtils;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserDTO;
import com.dl.member.dto.WithdrawalSnDTO;
import com.dl.member.param.IDParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.WithDrawParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.CashGetParam;
import com.dl.shop.payment.param.UpdateUserWithdrawParam;
import com.dl.shop.payment.param.UserWithdrawParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.rongbao.cash.CashUtil;
import com.dl.shop.payment.pay.rongbao.cash.entity.CashResultEntity;
import com.dl.shop.payment.pay.rongbao.cash.entity.ReqCashContentEntity;
import com.dl.shop.payment.pay.rongbao.cash.entity.ReqCashEntity;
import com.dl.shop.payment.pay.rongbao.cash.entity.RspCashEntity;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.UserWithdrawService;
import io.swagger.annotations.ApiOperation;

/**
 * 代支付
 * @date 2018.05.05
 */
@Controller
@RequestMapping("/cash")
public class CashController {
	private final static Logger logger = LoggerFactory.getLogger(CashController.class);
	@Resource
	private IUserService userService;
	@Autowired
	private IUserBankService userBankService;
	@Autowired
	private IUserAccountService userAccountService;
	
	@Autowired
	private UserWithdrawService userWithdrawService;
	
	@Resource
	private UserWithdrawLogService userWithdrawLogService;
	@Resource
	private IUserMessageService userMessageService;
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		String loggerId = "withdrawForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/withdraw, userId="+SessionUtil.getUserId()+", totalAmount="+param.getTotalAmount()+",userBankId="+param.getUserBankId());
		BaseResult<UserDTO> userInfoExceptPass = userService.userInfoExceptPass(new StrParam());
		if(userInfoExceptPass == null) {
			return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
		}
		UserDTO userDTO = userInfoExceptPass.getData();
		if(userDTO == null) {
			return ResultGenerator.genFailResult("未查询到该用户信息");
		}
		String mobile = userDTO.getMobile();
		String strTotalAmount = param.getTotalAmount();
		//长度超过1000000 -> 7位数
		if(StringUtils.isEmpty(strTotalAmount) || strTotalAmount.length() > 10) {
			logger.info(loggerId+"输入金额超出有效范围~");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		Double totalAmount = null;
		try {
			totalAmount = Double.valueOf(strTotalAmount);
		}catch(Exception ee) {
			ee.printStackTrace();
		}
		if(totalAmount == null || totalAmount <= 0) {
			logger.info(loggerId+"提现金额提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(),PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		String strMoney = userDTO.getUserMoney();
		Double dMoney = null;
		logger.info("钱包金额:" + strMoney + " 用户提现金额:" + strMoney);
		if(!TextUtils.isEmpty(strMoney)) {
			try {
				dMoney = Double.valueOf(strMoney);
			}catch(Exception ee) {
				ee.printStackTrace();
			}
		}
		if(dMoney == null) {
			logger.info(loggerId+"金额转换失败！");
			return ResultGenerator.genFailResult("用户钱包金额转换失败！",null);
		}
		
		//提现金额大于钱包数据
		if(totalAmount > dMoney) {
			logger.info(loggerId+"提现金额超出用户钱包数值~");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_NOT_ENOUGH.getcode(),PayEnums.PAY_RONGBAO_NOT_ENOUGH.getMsg()); 
		}
		//支付方式
		int userBankId = param.getUserBankId();
		if(userBankId < 1) {
			logger.info(loggerId + "用户很行卡信息id提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(),PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		IDParam idParam = new IDParam();
		idParam.setId(userBankId);
		BaseResult<UserBankDTO> queryUserBank = userBankService.queryUserBank(idParam);
		if(queryUserBank.getCode() != 0) {
			logger.info(loggerId+"用户银行卡信息获取有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(),PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		UserBankDTO userBankDTO = queryUserBank.getData();
		String realName = userBankDTO.getRealName();
		String cardNo = userBankDTO.getCardNo();
		boolean inReview = false;
		//如果提现金额大于阈值
	    if(totalAmount > 1000) {
	    	inReview = true;
	    }
		//生成提现单
		UserWithdrawParam userWithdrawParam = new UserWithdrawParam();
		userWithdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
		userWithdrawParam.setCardNo(cardNo);
		userWithdrawParam.setRealName(realName);
		userWithdrawParam.setStatus(ProjectConstant.STATUS_UNCOMPLETE);
		WithdrawalSnDTO withdrawalSnDTO = userWithdrawService.saveWithdraw(userWithdrawParam);
		if(StringUtils.isEmpty(withdrawalSnDTO.getWithdrawalSn())) {
			logger.info(loggerId+" 生成提现单失败");
			return ResultGenerator.genFailResult("提现失败！", null);
		}
		//
		String orderSn = withdrawalSnDTO.getWithdrawalSn();
		//保存提现进度
		UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
		userWithdrawLog.setLogCode(1);
		userWithdrawLog.setLogName("提现申请");
		userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
		userWithdrawLog.setWithdrawSn(orderSn);
		userWithdrawLogService.save(userWithdrawLog);
		//生成提现记录payLog,该操作在提现暂时不需要
		/*String payName = "第三方接口";
		String payIp = this.getIpAddr(request);
		String payCode = "withdraw_api";
		PayLog payLog = super.newPayLog(orderSn, BigDecimal.valueOf(totalAmount), 2, payCode, payName, payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}*/
		

		//消息
//		MessageAddParam messageAddParam = new MessageAddParam();
//		messageAddParam.setTitle("申请提现");
//		messageAddParam.setContent("提现"+totalAmount+"元");
//		messageAddParam.setContentDesc("提交申请");
//		messageAddParam.setSender(SessionUtil.getUserId());
//		messageAddParam.setMsgType(1);
//		messageAddParam.setReceiver(SessionUtil.getUserId());
//		messageAddParam.setReceiveMobile(mobile);
//		messageAddParam.setObjectType(2);
//		messageAddParam.setMsgUrl("");
//		messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
//		Integer addTime = withdrawalSnDTO.getAddTime();
//		LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.UTC);
//		StringBuilder msgDesc = new StringBuilder();
//		msgDesc.append("申请时间：").append(loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:dd"))).append("\n")
//		.append("审核时间：").append("\n")
//		.append("提现成功时间：");
//		messageAddParam.setMsgDesc(msgDesc.toString());
//		userMessageService.add(messageAddParam);
		if(inReview) {
			logger.info("单号:"+orderSn+"超出提现阈值,进入审核通道");
//			return ResultGenerator.genResult(PayEnums.CASH_REVIEWING.getcode(),PayEnums.CASH_REVIEWING.getMsg());
			return ResultGenerator.genSuccessResult("提现成功");
		}else {
			logger.info("进入第三方提现流程...");
			CashResultEntity rEntity = callThirdGetCash(orderSn,totalAmount);
			if(rEntity.isSucc) {
				logger.info("单号:"+orderSn+"第三方提现成功，扣除用户余额");
				//减少用户钱包余额
				WithDrawParam withdrawParam = new WithDrawParam();
				withdrawParam.setAmount(new BigDecimal(totalAmount));
				withdrawParam.setPayId(orderSn);
				withdrawParam.setThirdPartName("融宝");
				withdrawParam.setThirdPartPaid(new BigDecimal(totalAmount));
				withdrawParam.setUserId(SessionUtil.getUserId());
				BaseResult<String> withdrawRst = userAccountService.withdrawUserMoney(withdrawParam);
				if(withdrawRst.getCode() != 0) {
					logger.info(loggerId+"用户可提现余额提现失败");
				}
				//更新提现单
				logger.info("提现单号:"+orderSn+"更新提现单位成功状态");
				UpdateUserWithdrawParam updateParams = new UpdateUserWithdrawParam();
				updateParams.setWithdrawalSn(withdrawalSnDTO.getWithdrawalSn());
				updateParams.setStatus(ProjectConstant.STATUS_SUCC);
				updateParams.setPayTime(DateUtil.getCurrentTimeLong());
				updateParams.setPaymentId(orderSn);
				updateParams.setPaymentName("融宝提现");
				userWithdrawService.updateWithdraw(updateParams);
				
				return ResultGenerator.genSuccessResult("提现成功");
			}else {
				return ResultGenerator.genFailResult("提现失败[" +rEntity.msg +"]");
			}	
		}
	}
	
	/**
	 * 调用第三方扣款流程
	 * @param orderSn
	 * @param totalAmount
	 * @return
	 */
	private CashResultEntity callThirdGetCash(String orderSn,double totalAmount) {
		CashResultEntity rEntity = new CashResultEntity();
		//第三方提现接口
		ReqCashEntity reqCashEntity = new ReqCashEntity();
		//提现序号
		reqCashEntity.setBatch_no(orderSn);
		reqCashEntity.setBatch_count("1");
		reqCashEntity.setBatch_amount(totalAmount+"");
		reqCashEntity.setPay_type("1");
		ReqCashContentEntity reqCashContentEntity = ReqCashContentEntity.buildTestReqCashEntity("1",""+totalAmount,"18910116131");
		reqCashEntity.setContent(reqCashContentEntity.buildContent());
		logger.info(reqCashContentEntity.buildContent());
		boolean isSucc = false;
		String tips = null;
		try {
			RspCashEntity rspEntity = CashUtil.sendGetCashInfo(reqCashEntity);
			logger.info("RspCashEntity->"+rspEntity);
			if(rspEntity != null && rspEntity.isSucc()) {
				isSucc = true;
				rEntity.isSucc = true;
			}else {
				if(rspEntity != null) {
					tips = rspEntity.result_msg;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			tips = e.getMessage();
			rEntity.msg = tips;
		}
		return rEntity;
	}
	
	@ApiOperation(value="后台管理提现调用", notes="")
	@PostMapping("/getcash")
	@ResponseBody
	public BaseResult<Object> getCash(@RequestBody CashGetParam param, HttpServletRequest request){
		String sn = param.getWithdrawSn();
		if(StringUtils.isEmpty(sn)) {
			logger.info("提现单号不能为空");
			return ResultGenerator.genFailResult("提现单号不能为空",null);
		}
		//查询该用户的提现金额
		BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(sn);
		if(baseResult.getCode() != 0 || baseResult.getData() == null) {
			logger.info("查询提现单失败");
			return ResultGenerator.genFailResult("提现单号不能为空",null);
		}
		UserWithdraw userEntity = baseResult.getData();
		BigDecimal amt = userEntity.getAmount();
		logger.info("进入到第三方提现流程，金额:" + amt.doubleValue() +" 用户名:" +userEntity.getUserId() +" sn:" + sn);
		CashResultEntity cashREntity = callThirdGetCash(sn,amt.doubleValue());
		if(cashREntity.isSucc) {
			//更改提现单状态
			logger.info("更改提现单流程为成功...");
			UpdateUserWithdrawParam updateParams = new UpdateUserWithdrawParam();
			updateParams.setWithdrawalSn(sn);
			updateParams.setStatus(ProjectConstant.STATUS_SUCC);
			updateParams.setPayTime(DateUtil.getCurrentTimeLong());
			updateParams.setPaymentId(userEntity.getPaymentId());
			updateParams.setPaymentName("彩小秘管理后台发起提现");
			userWithdrawService.updateWithdraw(updateParams);
			return ResultGenerator.genSuccessResult("第三方发起提现成功");
		}else {
			return ResultGenerator.genFailResult("提现失败[" +cashREntity.msg+"]");
		}
	}
}