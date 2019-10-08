package com.dl.shop.payment.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.MD5Util;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserDTO;
import com.dl.member.param.AddMessageParam;
import com.dl.member.param.IDParam;
import com.dl.member.param.MessageAddParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SysConfigParam;
import com.dl.member.param.UserBankQueryParam;
import com.dl.member.param.UserIdParam;
import com.dl.member.param.UserIdRealParam;
import com.dl.member.param.WithDrawParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.GetUserMoneyDTO;
import com.dl.order.param.GetUserMoneyPayParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.UserWithdrawLogMapper;
import com.dl.shop.payment.dao.UserWithdrawMapper;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.WithdrawalSnDTO;
import com.dl.shop.payment.enums.CashEnums;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.enums.PayForCompanyEnum;
import com.dl.shop.payment.model.DlUserReal;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.UpdateUserWithdrawParam;
import com.dl.shop.payment.param.UserWithdrawParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthers;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthersBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.util.TXScanPay;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.util.TdExpBasicFunctions;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.ucf.sdk.util.AESCoder;

import lombok.extern.slf4j.Slf4j;

/**
 * 代支付
 * 
 * @date 2018.05.05
 */
@Service
@Slf4j
public class CashService {
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	@Resource
	private IUserService userService;
	@Resource
	private IOrderService orderService;
	@Autowired
	private IUserBankService userBankService;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private UserWithdrawService userWithdrawService;
	@Resource
	private UserWithdrawLogService userWithdrawLogService;
	@Resource
	private UserWithdrawLogMapper userWithdrawLogMapper;
	@Resource
	private IUserMessageService userMessageService;
	@Resource
	private XianFengCashUtil xianfengUtil;

	@Resource
	private TXScanPay txScanPay;

	@Resource
	private RkPayService rkPayService;
	@Resource
	private JhPayService jhpayService;
	@Resource
	private SmkPayService smkPayService;
	@Resource
	private Constants xFConstants;

	@Resource
	private DlUserRealService dlUserRealService;

	@Resource
	private UserWithdrawMapper userWithdrawMapper;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request) {
//		rwl.readLock().lock();
		SysConfigParam cfg = new SysConfigParam();
		Integer userId = SessionUtil.getUserId();
		try {
			long time1 = System.currentTimeMillis();
			log.info("time1:" + System.currentTimeMillis());
//			Long mTime = System.currentTimeMillis();
//			String userIdInRedis = stringRedisTemplate.opsForValue().get("WS:"+String.valueOf(userId));
//			if(!StringUtils.isEmpty(userIdInRedis)) {
//				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_REPEAT.getcode(),PayEnums.PAY_WITHDRAW_REPEAT.getMsg());
//			}
//			stringRedisTemplate.opsForValue().set("WS:"+String.valueOf(userId),String.valueOf(mTime));

			Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent("WS:"+String.valueOf(userId), "on");
			stringRedisTemplate.expire("WS:"+String.valueOf(userId), 7200, TimeUnit.SECONDS);
			log.info("withdrawForApp: "+absent);
			if(!absent) {
				log.info("withdrawForApp:"+absent);
				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_REPEAT.getcode(),PayEnums.PAY_WITHDRAW_REPEAT.getMsg());
			}
			String loggerId = "withdrawForApp_" + System.currentTimeMillis();
			log.info(loggerId + " int /payment/withdraw, userId=" + SessionUtil.getUserId() + ", totalAmount=" + param.getTotalAmount() + ",userBankId=" + param.getUserBankId());
			// bank判断
			int userBankId = param.getUserBankId();
			if (userBankId < 1) {
				log.info(loggerId + "用户很行卡信息id提供有误！");
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(), PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
			}
			IDParam idParam = new IDParam();
			idParam.setId(userBankId);
			BaseResult<UserBankDTO> queryUserBank = userBankService.queryUserBank(idParam);
			if (queryUserBank.getCode() != 0) {
				log.info(loggerId + "用户银行卡信息获取有误！");
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(), PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
			}
			String strTotalAmount = param.getTotalAmount();
			// 长度超过1000000 -> 7位数
			if (StringUtils.isEmpty(strTotalAmount) || strTotalAmount.length() > 10) {
				log.info(loggerId + "输入金额超出有效范围");
				return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
			}
			Double totalAmount = null;
			try {
				totalAmount = Double.valueOf(strTotalAmount);
			} catch (Exception ee) {
				log.error("提现金额转换异常", ee);
			}
			if (totalAmount == null || totalAmount <= 0) {
				log.info(loggerId + "提现金额提供有误！");
				return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
			}
			cfg.setBusinessId(64);//读取最低提现金额
			int minTxMoney = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
			// 是否小于3元钱
			if (totalAmount < minTxMoney) {
				log.info(loggerId + "单笔最低提现金额大于"+minTxMoney+"元~");
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_LOW_LIMIT.getcode(),"单笔提现金额不能低于"+minTxMoney+"元");
			}
			cfg.setBusinessId(65);//读取最高提现金额
			int maxTxMoney = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
			// 是否小于3元钱
			if (totalAmount > maxTxMoney) {
				log.info(loggerId + "单笔最高提现金额小于"+minTxMoney+"元~");
				return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(),"单笔提现金额不能高于"+maxTxMoney+"元");
			}
			UserDeviceInfo userDevice = SessionUtil.getUserDevice();
			cfg.setBusinessId(67);//读取财务账号id
			int cwuserId = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
			if(userId!=cwuserId) {//非财务账号--财务账号不限制提现次数
				int countUserWithdraw = userWithdrawService.countUserWithdraw(userId);
				log.info(userId + "当天已提现次数:" + countUserWithdraw);
				cfg.setBusinessId(63);//读取提现次数
				int conuntTx = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
				if (countUserWithdraw >= conuntTx) {
					return ResultGenerator.genResult(PayEnums.PAY_THREE_COUNT_WITHDRAW.getcode(),"每日提现次数不能超过"+conuntTx+"次");
				}
			}
			
			UserBankDTO userBankDTO = queryUserBank.getData();
			String bankCode = userBankDTO.getAbbreviation();
			String realName = userBankDTO.getRealName();
			String cardNo = userBankDTO.getCardNo();
			String bankName = userBankDTO.getBankName();
			if(userBankDTO.getPassword()==null || !userBankDTO.getPassword().equalsIgnoreCase(MD5Util.cryptForUTF("*"+userId+"#@"+realName+"$%"+cardNo+"^&"+bankName+"*"))) {
				log.info(loggerId + "用户银行卡信息获取有误！");
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_ERROR.getcode(), PayEnums.PAY_RONGBAO_BANK_ERROR.getMsg());
			}
			cfg.setBusinessId(8);// 提现
			
			StrParam strParam = new StrParam();
			strParam.setStr("");
			BaseResult<UserDTO> userInfoExceptPass = userService.userInfoExceptPassReal(strParam);
			if (userInfoExceptPass.getCode() != 0) {
				return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
			}
			UserDTO userDTO = userInfoExceptPass.getData();
			String mobile = userDTO.getMobile();
			String strMoney = userDTO.getUserMoney();
			if(userId!=null && userId==cwuserId) {// 财务账号--财务账号提现金额为商户余额
				com.dl.shop.payment.param.StrParam emptyParam = new com.dl.shop.payment.param.StrParam();
				BaseResult<RspOrderQueryDTO> ymoney = rkPayService.getShMoney(emptyParam);
				if(ymoney!=null && ymoney.getData()!=null) {
					strMoney=ymoney.getData().getDonationPrice()!=null?ymoney.getData().getDonationPrice():"0";//商户余额
				}
			}
			Double dMoney = null;
			log.info("用户提现金额:" + strMoney);
			if (!TextUtils.isEmpty(strMoney)) {
				try {
					dMoney = Double.valueOf(strMoney);
				} catch (Exception ee) {
					log.error("金额转换异常", ee);
				}
			}
			if (dMoney == null) {
				log.info(loggerId + "金额转换失败！");
				return ResultGenerator.genFailResult("用户钱包金额转换失败！", null);
			}
			// 提现金额大于可提现金额
			if (totalAmount > dMoney) {
				log.info(loggerId + "提现金额超出用户可提现金额数值~");
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_NOT_ENOUGH.getcode(), PayEnums.PAY_RONGBAO_NOT_ENOUGH.getMsg());
			}
			
			String withdrawalSn = SNGenerator.nextSN(SNBusinessCodeEnum.WITHDRAW_SN.getCode());
			// 生成提现单
			UserWithdrawParam userWithdrawParam = new UserWithdrawParam();
			userWithdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
			userWithdrawParam.setCardNo(cardNo);
			userWithdrawParam.setRealName(realName);
			userWithdrawParam.setStatus(ProjectConstant.STATUS_UNCOMPLETE);
			userWithdrawParam.setWithDrawSn(withdrawalSn);
			WithdrawalSnDTO withdrawalSnDTO = userWithdrawService.saveWithdraw(userWithdrawParam);
			if (StringUtils.isEmpty(withdrawalSnDTO.getWithdrawalSn())) {
				log.info(loggerId + " 生成提现单失败");
				return ResultGenerator.genFailResult("网络错误，提现失败，请联系客服！", null);
			}
			log.info("[withdrawForApp]" + "提现单号:" + withdrawalSn + "生成提现单成功");
			// stringRedisTemplate.delete("WS:"+String.valueOf(userId));
			String widthDrawSn = withdrawalSnDTO.getWithdrawalSn();
			// 保存提现 进度
			UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
			userWithdrawLog.setLogCode(CashEnums.CASH_APPLY.getcode());
			userWithdrawLog.setLogName(CashEnums.CASH_APPLY.getMsg());
			userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
			userWithdrawLog.setWithdrawSn(widthDrawSn);
			userWithdrawLogService.save(userWithdrawLog);
			log.info("[withdrawForApp]进入提现流程 userId:" + SessionUtil.getUserId() + " 扣除金额:" + totalAmount);
			WithDrawParam withdrawParam = new WithDrawParam();
			withdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
			withdrawParam.setPayId(withdrawalSn);
			withdrawParam.setThirdPartName("银行卡");
			withdrawParam.setThirdPartPaid(BigDecimal.valueOf(totalAmount));
			withdrawParam.setUserId(SessionUtil.getUserId());
			BaseResult<String> withdrawRst = userAccountService.withdrawUserMoney(withdrawParam);
			if (withdrawRst == null || withdrawRst.getCode() != 0) {
				log.info(loggerId + "用户可提现余额提现失败,用户资金钱包未变化");
				log.info("userId={}提现扣款失败，设置提现单withdrawsn={}失败", SessionUtil.getUserId(), withdrawalSn);
				UserWithdraw userWithdraw = new UserWithdraw();
				userWithdraw.setWithdrawalSn(widthDrawSn);
				userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
				userWithdrawMapper.updateUserWithdrawStatus0To2(userWithdraw);
				userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_FAILURE.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_FAILURE.getMsg());
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(widthDrawSn);
				userWithdrawLogService.save(userWithdrawLog);
				log.info("扣除用户余额返回={}", withdrawRst == null ? "" : withdrawRst.getCode() + ":" + withdrawRst.getMsg() + ":" + withdrawRst.getData());
				return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_NOT_ENOUGH.getcode(), PayEnums.PAY_RONGBAO_NOT_ENOUGH.getMsg());
			}
			userWithdrawLog = new UserWithdrawLog();
			userWithdrawLog.setLogCode(CashEnums.CASH_REVIEWING.getcode());
			userWithdrawLog.setLogName(CashEnums.CASH_REVIEWING.getMsg());
			userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
			userWithdrawLog.setWithdrawSn(widthDrawSn);
			userWithdrawLogService.save(userWithdrawLog);
			BaseResult<SysConfigDTO> baseResult = userAccountService.queryBusinessLimit(cfg);
			double limit = 100; // 默认100提现阈值数
			if (baseResult.getData() != null) {
				limit = baseResult.getData().getValue().doubleValue();
			}
			boolean isCheck = false;
			if(userId!=cwuserId) {//非财务账号--财务账号不设阈值和人工审核
				log.info("提现审请信息：userId=" + userId + " totalAmount=" + totalAmount + " limit=" + limit);
				if (totalAmount > limit) {
					double maxLimit = this.getMaxNoCheckMoney();
					log.info("提现审请信息：userId=" + userId + " totalAmount=" + totalAmount + " limit=" + limit + " getMaxNoCheckMoney=" + maxLimit);
					if (totalAmount >= maxLimit) {
						isCheck = true;
					} else {
						// 判断用户是否是购彩超过指定额度用户
						GetUserMoneyPayParam getUserMoneyPayParam = new GetUserMoneyPayParam();
						getUserMoneyPayParam.setUserId(userId);
						BaseResult<GetUserMoneyDTO> getUserMoneyPayRst = orderService.getUserMoneyPay(getUserMoneyPayParam);
						GetUserMoneyDTO data = getUserMoneyPayRst.getData();
						Double userMoneyPaid = data != null && data.getMoneyPaid() != null ? data.getMoneyPaid() : 0.0;
						Double userMoneyPaidForNoCheck = this.getUserMoneyPaidForNoCheck();
						log.info("提现审请信息：userId=" + userId + " totalAmount=" + totalAmount + " limit=" + limit + " getUserMoneyPaidForNoCheck=" + userMoneyPaidForNoCheck + " userMoneyPaid=" + userMoneyPaid);
						if (userMoneyPaidForNoCheck > userMoneyPaid) {
							isCheck = true;
						}
					}
				}
				Boolean withDrawByPersonOprateOpen = userWithdrawService.queryWithDrawPersonOpen();
				if (withDrawByPersonOprateOpen) {
					isCheck = true;// 人工打款打开时所有提现均走人工提现
				}
			}
			log.info("提现审请信息：userId=" + userId + " totalAmount=" + totalAmount + " limit=" + limit + " isCheck=" + isCheck);
			if (isCheck) {
				log.info("单号:" + widthDrawSn + "超出提现阈值,进入审核通道  系统阈值:" + limit);
				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_APPLY_SUC.getcode(), PayEnums.PAY_WITHDRAW_APPLY_SUC.getMsg());
			} else {
				log.info("进入第三方提现流程...系统阈值:" + limit + " widthDrawSn:" + widthDrawSn);
				UserWithdraw userWithdraw = new UserWithdraw();
				userWithdraw.setWithdrawalSn(withdrawalSn);
				Integer thirdPayForType = userWithdrawMapper.getThirdPayForType();
				userWithdraw.setPayForCode(thirdPayForType);
				userWithdrawMapper.updateUserWithdrawStatus0To3(userWithdraw);
				RspSingleCashEntity rEntity = callThirdGetCash(widthDrawSn, totalAmount, cardNo, bankName, realName, mobile, bankCode, userId);
				long time3 = System.currentTimeMillis();
				log.info("time3为：" + time3);
				log.info("提现所用时间为：" + (time3 - time1));
				return operation(rEntity, widthDrawSn, userId, Boolean.TRUE);
			}
		} finally {
			log.info("withdrawForApp:finally"+userId);
			stringRedisTemplate.delete("WS:"+String.valueOf(userId));
//			rwl.readLock().unlock();
		}
	}

	
	public BaseResult<Object> withdrawForAppCw(@RequestBody WithdrawParam param, HttpServletRequest request) {
		Integer userId = SessionUtil.getUserId();
		long time1 = System.currentTimeMillis();
		log.info("time1:" + System.currentTimeMillis());

		String loggerId = "withdrawForApp_" + System.currentTimeMillis();
		log.info(loggerId + " int /payment/withdraw, userId=" + SessionUtil.getUserId() + ", totalAmount=" + param.getTotalAmount() + ",userBankId=" + param.getUserBankId());
		SysConfigParam cfg = new SysConfigParam();
		// bank判断
		int userBankId = param.getUserBankId();
		if (userBankId < 1) {
			log.info(loggerId + "用户很行卡信息id提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(), PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		IDParam idParam = new IDParam();
		idParam.setId(userBankId);
		BaseResult<UserBankDTO> queryUserBank = userBankService.queryUserBank(idParam);
		if (queryUserBank.getCode() != 0) {
			log.info(loggerId + "用户银行卡信息获取有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(), PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		String strTotalAmount = param.getTotalAmount();
		// 长度超过1000000 -> 7位数
		if (StringUtils.isEmpty(strTotalAmount) || strTotalAmount.length() > 10) {
			log.info(loggerId + "输入金额超出有效范围");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		Double totalAmount = null;
		try {
			totalAmount = Double.valueOf(strTotalAmount);
		} catch (Exception ee) {
			log.error("提现金额转换异常", ee);
		}
		if (totalAmount == null || totalAmount <= 0) {
			log.info(loggerId + "提现金额提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		cfg.setBusinessId(64);//读取最低提现金额
		int minTxMoney = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
		// 是否小于3元钱
		if (totalAmount < minTxMoney) {
			log.info(loggerId + "单笔最低提现金额大于"+minTxMoney+"元~");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_LOW_LIMIT.getcode(),"单笔提现金额不能低于"+minTxMoney+"元");
		}
		cfg.setBusinessId(65);//读取最高提现金额
		int maxTxMoney = userAccountService.queryBusinessLimit(cfg).getData()!=null?userAccountService.queryBusinessLimit(cfg).getData().getValue().intValue():0;
		// 是否小于3元钱
		if (totalAmount > maxTxMoney) {
			log.info(loggerId + "单笔最高提现金额小于"+minTxMoney+"元~");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(),"单笔提现金额不能高于"+maxTxMoney+"元");
		}

		UserBankDTO userBankDTO = queryUserBank.getData();
		String bankCode = userBankDTO.getAbbreviation();
		String realName = userBankDTO.getRealName();
		String cardNo = userBankDTO.getCardNo();
		String bankName = userBankDTO.getBankName();
		cfg.setBusinessId(8);// 提现
		log.info("[withdrawForApp]" + " 扣除用户余额成功:" + totalAmount);
		StrParam strParam = new StrParam();
		strParam.setStr("");
		BaseResult<UserDTO> userInfoExceptPass = userService.userInfoExceptPassReal(strParam);
		if (userInfoExceptPass.getCode() != 0) {
			return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
		}
		UserDTO userDTO = userInfoExceptPass.getData();
		String mobile = userDTO.getMobile();
		String strMoney = userDTO.getUserMoney();
		
		String withdrawalSn = SNGenerator.nextSN(SNBusinessCodeEnum.WITHDRAW_SN.getCode());
		RspSingleCashEntity rEntity = callThirdGetCash(withdrawalSn, totalAmount, cardNo, bankName, realName, mobile, bankCode, userId);
		
		long time3 = System.currentTimeMillis();
		log.info("time3为：" + time3);
		log.info("提现所用时间为：" + (time3 - time1));
		if("S".equalsIgnoreCase(rEntity.status)) {
			return ResultGenerator.genSuccessResult("提现成功");
		}else {
			return ResultGenerator.genFailResult("提现失败");
		}
		
	}

	
	private double getMaxNoCheckMoney() {
		double maxNoCheckMoney = userWithdrawMapper.getMaxNoCheckMoney();
		if (maxNoCheckMoney <= 0) {
			return 10000;
		}
		return maxNoCheckMoney;
	}

	private double getUserMoneyPaidForNoCheck() {
		double maxNoCheckMoney = userWithdrawMapper.getUserMoneyPaidForNoCheck();
		if (maxNoCheckMoney <= 0) {
			return 3000;
		}
		return maxNoCheckMoney;
	}

	/**
	 * 调用第三方扣款流程
	 * 
	 * @param orderSn
	 * @param totalAmount
	 * @return
	 */
	private RspSingleCashEntity callThirdGetCash(String orderSn, double totalAmount, String accNo, String bankName, String accName, String phone, String bankNo, Integer userId) {
		DlUserReal userReal = dlUserRealService.findByUserId(userId);
		log.info("=====callThirdGetCash======");
		log.info("orderSn:" + orderSn + " total:" + totalAmount + " accNo:" + accNo + " accName:" + accName + " phone:" + phone + " bankNo:" + bankNo);
		// // test code
		// // ========================
		// accNo = "6222021001115704287";
		// accName = "王泽武";
		// phone = "18100000000";
		// bankNo = "CCB";
		// // ======================
		BigDecimal bigDec = BigDecimal.valueOf(totalAmount);
		BigDecimal bigFen = bigDec.multiply(new BigDecimal(100));
		RspSingleCashEntity rEntity = new RspSingleCashEntity();
		String tips = null;
		try {
			Integer thirdPayForType = userWithdrawMapper.getThirdPayForType();
			log.info("第三方支付公司Code=============={}", thirdPayForType);
			TXScanRequestPaidByOthers txScanRequestPaidByOthers = new TXScanRequestPaidByOthers();
			txScanRequestPaidByOthers.setAccountName(accName);
			txScanRequestPaidByOthers.setAccountNo(accNo);
			txScanRequestPaidByOthers.setMobile(phone);
			txScanRequestPaidByOthers.setCnaps("000000000000");
			txScanRequestPaidByOthers.setCertNum(userReal.getIdCode());
			txScanRequestPaidByOthers.setTxnAmt(bigFen.toString());
			txScanRequestPaidByOthers.setBankProv("000000");
			txScanRequestPaidByOthers.setBankCity("000000");
			txScanRequestPaidByOthers.setBankName(bankName);
			txScanRequestPaidByOthers.setTxnAmt(bigFen.toString());
			txScanRequestPaidByOthers.setStlType("T0");
			txScanRequestPaidByOthers.setAccountType("1");
			txScanRequestPaidByOthers.setOrderId(orderSn);
			if (null == thirdPayForType || PayForCompanyEnum.XF_PAYFOR.getCode().equals(thirdPayForType)) {
				log.info("走先锋通道提现============================");
				rEntity = xianfengUtil.reqCash(orderSn, bigFen.intValue() + "", accNo, accName, phone, bankNo);
			} else if (PayForCompanyEnum.TX_PAYFOR1.getCode().equals(thirdPayForType) || PayForCompanyEnum.TX_PAYFOR2.getCode().equals(thirdPayForType)) {// 天下支付代付
				log.info("走天下支付通道提现============================");
				String merchantStr = thirdPayForType.toString();
				rEntity = txScanPay.txScanPayFor1(txScanRequestPaidByOthers, merchantStr);
			} else if (PayForCompanyEnum.TX_PAYQDD.getCode().equals(thirdPayForType)) {// Q多多支付代付
				log.info("走Q多多支付代付通道提现============================");
				rEntity = rkPayService.fundApply(txScanRequestPaidByOthers);
			} else if (PayForCompanyEnum.JH_PAYQDD.getCode().equals(thirdPayForType)) {// 聚合支付代付
				log.info("走聚合付代付通道提现============================");
				rEntity = jhpayService.fundApply(txScanRequestPaidByOthers);
			} else if (PayForCompanyEnum.TX_PAYSMK.getCode().equals(thirdPayForType)) {// Q多多支付代付
				log.info("走Q多多支付代付通道提现============================");
				rEntity = smkPayService.agentSinglePay(orderSn, totalAmount+"",1);
			} else {
				log.info("空通道,未匹配到提现通道============================");
			}
			log.info("RspCashEntity->" + rEntity);
		} catch (Exception e) {
			log.error("提现异常, ordersn=" + orderSn, e);
			tips = e.getMessage();
			rEntity.resMessage = tips;
		}
		return rEntity;
	}

	public BaseResult<Object> operationSucc(RspSingleCashEntity rEntity, String withDrawSn) {
		log.info("单号:" + withDrawSn + "第三方提现成功，扣除用户余额");
		// 更新提现单
		log.info("提现单号:" + withDrawSn + "更新提现单位成功状态");
		UpdateUserWithdrawParam updateParams = new UpdateUserWithdrawParam();
		// updateParams.setWithdrawalSn(withdrawalSnDTO.getWithdrawalSn());
		updateParams.setWithdrawalSn(withDrawSn);
		updateParams.setStatus(ProjectConstant.STATUS_SUCC);
		updateParams.setPayTime(DateUtil.getCurrentTimeLong());
		updateParams.setPaymentId(withDrawSn);
		updateParams.setPaymentName("管理后台发起提现");
		userWithdrawService.updateWithdraw(updateParams);
		this.goWithdrawMessageSuccess(withDrawSn);

		// 提现中，提现成功两条记录到 withdraw_log中
		UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
		userWithdrawLog.setLogCode(CashEnums.CASH_SUCC.getcode());
		userWithdrawLog.setLogName(CashEnums.CASH_SUCC.getMsg());
		userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
		userWithdrawLog.setWithdrawSn(withDrawSn);
		userWithdrawLogService.save(userWithdrawLog);
		return ResultGenerator.genSuccessResult("提现成功");
	}

	/**
	 * 提现第三方结果解析处理
	 * 
	 * @param rEntity
	 * @param widthDrawSn
	 * @param userId
	 * @param isApply
	 *            是否申请响应信息 true 申请接口返回 false 是主动查询或者异步通知接口响应
	 * @return
	 */
	public BaseResult<Object> operation(RspSingleCashEntity rEntity, String widthDrawSn, Integer userId, Boolean isApply) {
		if (rEntity.isTradeSucc()) {
			log.info("提现单号:" + widthDrawSn + "更新提现单为成功状态");
			UserWithdraw userWithdraw = new UserWithdraw();
			userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
			userWithdraw.setWithdrawalSn(widthDrawSn);
			int row = userWithdrawMapper.updateUserWithdrawStatus3To1(userWithdraw);
			if (row == 1) {
				this.goWithdrawMessageSuccess(widthDrawSn);
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_SUCC.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_SUCC.getMsg());
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(widthDrawSn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genSuccessResult("提现成功");
			} else {
				log.warn("withdrawSn={},更新数据状态为1（成功），更新失败,", widthDrawSn);
				return ResultGenerator.genSuccessResult("提现成功");
			}
		} else if (rEntity.isTradeFail(isApply)) {
			log.info("提现订单号={}，提现失败信息={}", widthDrawSn, rEntity.resMessage);
			UserWithdraw userWithdraw = new UserWithdraw();
			userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
			userWithdraw.setWithdrawalSn(widthDrawSn);
			int updateRowNum = userWithdrawMapper.updateUserWithdrawStatus3To4(userWithdraw);
			if (updateRowNum == 1) {
				// 保存提现中状态记录位失败到数据库中...
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_FAILURE.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_FAILURE.getMsg() + "[" + rEntity.resCode + ":" + rEntity.resMessage + "]");
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(widthDrawSn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genResult(PayEnums.CASH_FAILURE.getcode(), "提现审核中！");
			} else {
				log.warn("withdrawSn={},更新提现状态3to4失败", widthDrawSn);
				return ResultGenerator.genResult(PayEnums.CASH_FAILURE.getcode(), "提现审核中！");
			}
		} else if (rEntity.isTradeDoing()) {
			return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_APPLY_SUC.getcode(), PayEnums.PAY_WITHDRAW_APPLY_SUC.getMsg());
		}
		return null;
	}

	public BaseResult<Object> getCash(UserWithdraw userWithDraw, Boolean approvePass) {
		String sn = userWithDraw.getWithdrawalSn();
		if (approvePass) {
			int userId = userWithDraw.getUserId();
			String realName = userWithDraw.getRealName();
			String cardNo = userWithDraw.getCardNo();
			String bankName = userWithDraw.getBankName();
			UserIdRealParam params = new UserIdRealParam();
			params.setUserId(userId);
			// 通过UserService查询到手机号码
			BaseResult<UserDTO> bR = userService.queryUserInfoReal(params);
			UserDTO userDTO = null;
			String phone = "";
			if (bR.getCode() == 0 && bR.getData() != null) {
				userDTO = bR.getData();
				phone = userDTO.getMobile();
			}
			if (StringUtils.isEmpty(phone)) {
				return ResultGenerator.genFailResult("手机号码查询失败", null);
			}
			// 银行信息
			String bankCode = "";
			UserBankQueryParam userBQP = new UserBankQueryParam();
			userBQP.setUserId(userId);
			userBQP.setBankCardCode(cardNo);
			BaseResult<UserBankDTO> base = userBankService.queryUserBankByCondition(userBQP);
			if (base.getCode() != 0 || base.getData() == null) {
				return ResultGenerator.genFailResult("查询银行信息失败", null);
			}
			UserBankDTO userBankDTO = base.getData();
			bankCode = userBankDTO.getAbbreviation();
			log.info("[queryUserBankByCondition]" + " bankAcc:" + userBankDTO.getCardNo() + " bankName:" + userBankDTO.getBankName() + " bankCode:" + userBankDTO.getAbbreviation());
			if (StringUtils.isEmpty(bankCode)) {
				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_BIND_CARD_RETRY.getcode(), PayEnums.PAY_WITHDRAW_BIND_CARD_RETRY.getMsg());
			}
			BigDecimal amt = userWithDraw.getAmount();
			log.info("=================后台管理审核通过====================");
			log.info("进入到第三方提现流程，金额:" + amt.doubleValue() + " 用户名:" + userWithDraw.getUserId() + " sn:" + sn + " realName:" + realName + " phone:" + phone + " amt:" + amt + " bankCode:" + bankCode);
			log.info("=================后台管理审核通过====================");
			RspSingleCashEntity rspSCashEntity = callThirdGetCash(sn, amt.doubleValue(), cardNo, bankName, realName, phone, bankCode, userId);
			// 后台点击的都变为提现审核中
			UserWithdraw userWithdraw = new UserWithdraw();
			userWithdraw.setWithdrawalSn(sn);
			Integer thirdPayForType = userWithdrawMapper.getThirdPayForType();
			userWithdraw.setPayForCode(thirdPayForType);
			int row = userWithdrawMapper.updateUserWithdrawStatus0To3(userWithdraw);
			if (row == 1) {
				BaseResult result = operation(rspSCashEntity, sn, userId, Boolean.TRUE);
				log.info("操作提现withdrawSn={}结果 code={},msg={}", sn, result == null ? "" : result.getCode(), result == null ? "" : result.getMsg());
				return ResultGenerator.genSuccessResult("已审核");
			} else {
				log.error("操作提现withdrawSn={},更新结果0to3失败,updateRow={}", sn, row);
				return ResultGenerator.genFailResult("审核失败,数据已发生变动");
			}
		} else {
			log.info("后台管理审核拒绝，提现单状态为失败...");
			// 更新提现单失败状态
			UserWithdraw userWithdraw = new UserWithdraw();
			userWithdraw.setWithdrawalSn(sn);
			userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
			int row = userWithdrawMapper.updateUserWithdrawStatus0To4(userWithdraw);
			if (row == 1) {
				// 增加提现流水为失敗
				log.info("后台管理审核拒绝，增加提现单log日志...");
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_FAILURE.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_FAILURE.getMsg());
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(sn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genSuccessResult("后台管理审核拒绝成功...");
			} else {
				log.error("操作提现withdrawSn={},更新结果0to4失败,updateRow={}", sn, row);
				return ResultGenerator.genFailResult("审核失败,数据已发生变动");
			}
		}
	}

	public void withdrawNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map parameters = request.getParameterMap();// 保存request请求参数的临时变量
		String dataValue = "";// 保存业务数据加密值
		// 打印先锋支付返回值
		log.info("服务器端通知-接收到先锋支付返回报文：");
		Iterator paiter = parameters.keySet().iterator();
		while (paiter.hasNext()) {
			String key = paiter.next().toString();
			String[] values = (String[]) parameters.get(key);
			log.info(key + "-------------" + values[0]);
			if (key.equals("data")) {
				dataValue = values[0];
				try {// String dataValue = AESCoder.decrypt(signVal,
						// Constants.MER_RSAKEY);
					String dataJson = AESCoder.decrypt(dataValue, xFConstants.getMER_RSAKEY());
					RspSingleCashEntity rspSingleCashEntity = JSON.parseObject(dataJson, RspSingleCashEntity.class);
					String withDrawSn = rspSingleCashEntity.merchantNo;
					if (!StringUtils.isEmpty(withDrawSn)) {
						BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
						log.info("[withdrawNotify]" + " data:" + baseResult.getData() + " code:" + baseResult.getCode());
						if (baseResult.getCode() == 0) {
							UserWithdraw userWithDraw = baseResult.getData();
							PrintWriter writer = response.getWriter();
							writer.write("SUCCESS");
							writer.flush();
							log.info("============SUCESS返回====================");
							// 提现单没有达最终态
							if (userWithDraw != null && !ProjectConstant.STATUS_FAILURE.equals(userWithDraw.getStatus()) && !ProjectConstant.STATUS_FAIL_REFUNDING.equals(userWithDraw.getStatus()) && !ProjectConstant.STATUS_SUCC.equals(userWithDraw.getStatus())) {
								int userId = userWithDraw.getUserId();
								log.info("[withdrawNotify]" + " userId:" + userId + " withDrawSn:" + withDrawSn);
								operation(rspSingleCashEntity, rspSingleCashEntity.merchantNo, userId, Boolean.FALSE);
							}
						}
					}
					log.info("[withdrawNotify]" + " jsonObject:" + dataJson);
				} catch (Exception e) {
					log.error("[withdrawNotify]", e);
				}
			}
		}
	}

	@Async
	private void goWithdrawMessageSuccess(String withDrawSn) {
		UserWithdraw userWithdraw = userWithdrawService.queryUserWithdraw(withDrawSn).getData();
		if (userWithdraw == null) {
			return;
		}
		Integer userId = userWithdraw.getUserId();
		UserIdParam userIdParam = new UserIdParam();
		userIdParam.setUserId(userId);
		UserDTO userDto = userService.queryUserInfo(userIdParam).getData();
		if (userDto == null) {
			return;
		}
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(1);
		// 消息
		String status = userWithdraw.getStatus();
		MessageAddParam messageAddParam = new MessageAddParam();
		if (ProjectConstant.STATUS_SUCC.equals(status)) {
			messageAddParam.setTitle(CommonConstants.FORMAT_WITHDRAW_SUC_TITLE);
			messageAddParam.setContentDesc(CommonConstants.FORMAT_WITHDRAW_SUC_CONTENT_DESC);
		} else {
			return;
		}
		BigDecimal amount = userWithdraw.getAmount();
		messageAddParam.setContent(MessageFormat.format(CommonConstants.FORMAT_WITHDRAW_CONTENT, amount.toString()));
		messageAddParam.setSender(-1);
		messageAddParam.setMsgType(0);
		messageAddParam.setReceiver(userWithdraw.getUserId());
		messageAddParam.setReceiveMobile(userDto.getMobile());
		messageAddParam.setObjectType(2);
		messageAddParam.setMsgUrl("");
		messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
		Integer addTime = userWithdraw.getAddTime();
		String addTimeStr = this.getTimeStr(addTime);
		Integer checkTime = DateUtil.getCurrentTimeLong();
		String checkTimeStr = this.getTimeStr(checkTime);
		Integer payTime = userWithdraw.getPayTime();
		String payTimeStr = this.getTimeStr(payTime);
		String strDesc = CommonConstants.FORMAT_WITHDRAW_MSG_DESC;
		if (ProjectConstant.STATUS_FAILURE.equals(status)) {
			strDesc = CommonConstants.FORMAT_WITHDRAW_MSG_FAIL_DESC;
		}
		messageAddParam.setMsgDesc(MessageFormat.format(strDesc, addTimeStr, checkTimeStr, payTimeStr));
		params.add(messageAddParam);
		addParam.setParams(params);
		userMessageService.add(addParam);
	}

	private String getTimeStr(Integer addTime) {
		if (addTime <= 0) {
			return "";
		}
		String addTimeStr = DateUtil.getCurrentTimeString(Long.valueOf(addTime), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
		// LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0,
		// ZoneOffset.UTC);
		// String addTimeStr =
		// loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:dd"));
		return addTimeStr;
	}

	@Transactional
	public BaseResult<Object> queryCash(String withDrawSn) {
		UserWithdraw userWithdraw = userWithdrawService.queryUserWithdraw(withDrawSn).getData();
		if (userWithdraw == null) {
			return ResultGenerator.genFailResult("该订单不存在...sn:" + withDrawSn);
		}
		Boolean withDrawByPersonOprateOpen = userWithdrawService.queryWithDrawPersonOpen();
		if (withDrawByPersonOprateOpen) {
			if (Integer.valueOf(1).equals(userWithdraw.getStatus())) {
				return ResultGenerator.genSuccessResult();
			} else if (Integer.valueOf(2).equals(userWithdraw.getStatus())) {
				return ResultGenerator.genResult(PayEnums.CASH_FAILURE.getcode(), "提现失败");
			} else {
				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_APPLY_SUC.getcode(), PayEnums.PAY_WITHDRAW_APPLY_SUC.getMsg());
			}
		}
		int userId = userWithdraw.getUserId();
		String cardNo = userWithdraw.getCardNo();
		UserIdRealParam userIdPara = new UserIdRealParam();
		userIdPara.setUserId(userId);
		BaseResult<UserDTO> userInfoExceptPass = userService.queryUserInfoReal(userIdPara);
		if (userInfoExceptPass.getCode() != 0 || userInfoExceptPass.getData() == null) {
			return ResultGenerator.genFailResult("该用户不存在 userID:" + userId);
		}
		// 银行信息
		UserBankQueryParam userBQP = new UserBankQueryParam();
		userBQP.setUserId(userId);
		userBQP.setBankCardCode(cardNo);
		BaseResult<UserBankDTO> base = userBankService.queryUserBankByCondition(userBQP);
		if (base.getCode() != 0 || base.getData() == null) {
			return ResultGenerator.genFailResult("查询银行信息失败", null);
		}
		RspSingleCashEntity rspEntity;
		try {
			rspEntity = xianfengUtil.queryCash(withDrawSn);
			if (rspEntity != null) {
				return operation(rspEntity, withDrawSn, userId, Boolean.FALSE);
			}
		} catch (Exception e) {
			log.info("调取先锋支付查询报错", e);
		}
		return ResultGenerator.genFailResult("查询失败~", null);
	}

	/**
	 * 提现状态轮询
	 */
	public void timerCheckCashReq() {
		List<UserWithdraw> userWithdrawList = userWithdrawMapper.queryUserWithdrawIng();
		for (UserWithdraw userWithdraw : userWithdrawList) {
			try {
				queryWithdrawResult(userWithdraw);
			} catch (Exception e) {
				log.error("提现查询失败withdrawSn={}", userWithdraw.getWithdrawalSn(), e);
			}
		}
	}

	private void queryWithdrawResult(UserWithdraw userWithdraw) throws Exception {
		String withDrawSn = userWithdraw.getWithdrawalSn();
		Integer userId = userWithdraw.getUserId();
		Integer companyCode = userWithdraw.getPayForCode();
		RspSingleCashEntity rspEntity = null;
		log.info("companyCode====================={}", companyCode);
		if (null == companyCode || companyCode.equals(PayForCompanyEnum.XF_PAYFOR.getCode())) {
			log.info("走先锋支付通道轮询============");
			rspEntity = xianfengUtil.queryCash(withDrawSn);
		} else if (companyCode.equals(PayForCompanyEnum.TX_PAYFOR1.getCode()) || companyCode.equals(PayForCompanyEnum.TX_PAYFOR2.getCode())) {
			log.info("走先天下支付通道轮询============");
			TXScanRequestPaidByOthersBalanceQuery txScanPayForBalanceQuery = new TXScanRequestPaidByOthersBalanceQuery();
			txScanPayForBalanceQuery.setOrderId(userWithdraw.getWithdrawalSn());
			txScanPayForBalanceQuery.setTranDate(TdExpBasicFunctions.GETDATE());
			String merchantStr = companyCode.toString();
			rspEntity = txScanPay.payforQuery1(txScanPayForBalanceQuery, merchantStr);
		} else {
			log.info("未匹配到通道轮询==============");
		}
		if (rspEntity != null) {
			this.operation(rspEntity, withDrawSn, userId, Boolean.FALSE);
		}
	}

	/**
	 * 人工提现成功
	 * 
	 * @param successPersonWithDraw
	 */
	public void userWithDrawPersonSuccess(List<String> successPersonWithDraw) {
		if (CollectionUtils.isEmpty(successPersonWithDraw)) {
			return;
		}
		log.info("批量处理提现单号成功，处理size={},userWithDrawSns={}", successPersonWithDraw.size(), successPersonWithDraw);
		int updateRow = userWithdrawMapper.batchUpdateUserWithDrawSuccess(successPersonWithDraw);
		userWithdrawLogMapper.batchInsertUserWithDrawLogsSuccess(successPersonWithDraw);
		log.info("批量处理提现单号成功，处理完成updateRow={}", updateRow);
	}

	/**
	 * 人工提现失败
	 * 
	 * @param failPersonWithDraw
	 */
	public void userWithDrawPersonFail(List<String> failPersonWithDraw) {
		if (CollectionUtils.isEmpty(failPersonWithDraw)) {
			return;
		}
		log.info("批量处理提现单号失败，处理size={},userWithDrawSns={}", failPersonWithDraw.size(), failPersonWithDraw);
		int updateRow = userWithdrawMapper.batchUpdateUserWithDrawFail(failPersonWithDraw);
		userWithdrawLogMapper.batchInsertUserWithDrawLogsFail(failPersonWithDraw);
		log.info("批量处理提现单号失败，处理完成updateRow={}", updateRow);
	}

}
