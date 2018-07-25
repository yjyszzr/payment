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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

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
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
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
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.UserWithdrawMapper;
import com.dl.shop.payment.dto.WithdrawalSnDTO;
import com.dl.shop.payment.enums.CashEnums;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.UpdateUserWithdrawParam;
import com.dl.shop.payment.param.UserWithdrawParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.ucf.sdk.util.AESCoder;

/**
 * 代支付
 * @date 2018.05.05
 */
@Service
@Slf4j
public class CashService {
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
	@Resource
	private XianFengCashUtil xianfengUtil;
	@Resource
	private Constants xFConstants;

    @Resource
    private UserWithdrawMapper userWithdrawMapper;
	
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		Integer userId = SessionUtil.getUserId();
		long time1 = System.currentTimeMillis();
		log.info("time1:"+System.currentTimeMillis());
//		Long mTime = System.currentTimeMillis();
//		String userIdInRedis = stringRedisTemplate.opsForValue().get("WS:"+String.valueOf(userId));
//		if(!StringUtils.isEmpty(userIdInRedis)) {
//			return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_REPEAT.getcode(),PayEnums.PAY_WITHDRAW_REPEAT.getMsg());
//		}
//		stringRedisTemplate.opsForValue().set("WS:"+String.valueOf(userId),String.valueOf(mTime));
		
		String loggerId = "withdrawForApp_" + System.currentTimeMillis();
		log.info(loggerId + " int /payment/withdraw, userId="+SessionUtil.getUserId()+", totalAmount="+param.getTotalAmount()+",userBankId="+param.getUserBankId());

		//bank判断
		int userBankId = param.getUserBankId();
		if(userBankId < 1) {
			log.info(loggerId + "用户很行卡信息id提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(),PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		IDParam idParam = new IDParam();
		idParam.setId(userBankId);
		BaseResult<UserBankDTO> queryUserBank = userBankService.queryUserBank(idParam);
		if(queryUserBank.getCode() != 0) {
			log.info(loggerId+"用户银行卡信息获取有误！");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getcode(),PayEnums.PAY_RONGBAO_BANK_QUERY_ERROR.getMsg());
		}
		String strTotalAmount = param.getTotalAmount();
		//长度超过1000000 -> 7位数
		if(StringUtils.isEmpty(strTotalAmount) || strTotalAmount.length() > 10) {
			log.info(loggerId+"输入金额超出有效范围");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(), PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		Double totalAmount = null;
		try {
			totalAmount = Double.valueOf(strTotalAmount);
		}catch(Exception ee) {
			log.error("提现金额转换异常",ee);
		}
		if(totalAmount == null || totalAmount <= 0) {
			log.info(loggerId+"提现金额提供有误！");
			return ResultGenerator.genResult(PayEnums.PAY_TOTAL_NOTRANGE.getcode(),PayEnums.PAY_TOTAL_NOTRANGE.getMsg());
		}
		//是否小于3元钱
		if(totalAmount < 3) {
			log.info(loggerId+"最低提现金额大于3元~");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_LOW_LIMIT.getcode(),PayEnums.PAY_RONGBAO_LOW_LIMIT.getMsg()); 
		}
		//限制1天最多能提现1次
		int countUserWithdraw = userWithdrawService.countUserWithdraw(userId);
		log.info(userId+"一天提现次数:"+countUserWithdraw);
		if(countUserWithdraw >= 1) {
			return ResultGenerator.genResult(PayEnums.PAY_MAX_COUNT_WITHDRAW.getcode(),PayEnums.PAY_MAX_COUNT_WITHDRAW.getMsg()); 
		}
		UserBankDTO userBankDTO = queryUserBank.getData();
		String bankCode = userBankDTO.getAbbreviation();
		String realName = userBankDTO.getRealName();
		String cardNo = userBankDTO.getCardNo();
		SysConfigParam cfg = new SysConfigParam();
		cfg.setBusinessId(8);//提现
		log.info("[withdrawForApp]" + " 扣除用户余额成功:" + totalAmount);
		StrParam strParam = new StrParam();
		strParam.setStr("");
		BaseResult<UserDTO> userInfoExceptPass = userService.userInfoExceptPassReal(strParam);
		if(userInfoExceptPass.getCode() != 0) {
			return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
		}
		UserDTO userDTO = userInfoExceptPass.getData();
		String mobile = userDTO.getMobile();
		String strMoney = userDTO.getUserMoney();
		Double dMoney = null;
		log.info("用户提现金额:" + strMoney);
		if(!TextUtils.isEmpty(strMoney)) {
			try {
				dMoney = Double.valueOf(strMoney);
			}catch(Exception ee) {
				log.error("金额转换异常",ee);
			}
		}
		if(dMoney == null) {
			log.info(loggerId+"金额转换失败！");
			return ResultGenerator.genFailResult("用户钱包金额转换失败！",null);
		}
		//提现金额大于可提现金额
		if(totalAmount > dMoney) {
			log.info(loggerId+"提现金额超出用户可提现金额数值~");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_NOT_ENOUGH.getcode(),PayEnums.PAY_RONGBAO_NOT_ENOUGH.getMsg()); 
		}
		 //满足条件，先减少账户余额
	    String withdrawalSn = SNGenerator.nextSN(SNBusinessCodeEnum.WITHDRAW_SN.getCode());
		//生成提现单
		UserWithdrawParam userWithdrawParam = new UserWithdrawParam();
		userWithdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
		userWithdrawParam.setCardNo(cardNo);
		userWithdrawParam.setRealName(realName);
		userWithdrawParam.setStatus(ProjectConstant.STATUS_UNCOMPLETE);
		userWithdrawParam.setWithDrawSn(withdrawalSn);
		WithdrawalSnDTO withdrawalSnDTO = userWithdrawService.saveWithdraw(userWithdrawParam);
		if(StringUtils.isEmpty(withdrawalSnDTO.getWithdrawalSn())) {
			log.info(loggerId+" 生成提现单失败");
			return ResultGenerator.genFailResult("提现失败！", null);
		}
		log.info("[withdrawForApp]" + "提现单号:"+ withdrawalSn +"生成提现单成功");
//		stringRedisTemplate.delete("WS:"+String.valueOf(userId));
		String widthDrawSn = withdrawalSnDTO.getWithdrawalSn();
		//保存提现进度
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
		if(withdrawRst==null || withdrawRst.getCode() != 0) {
			log.info(loggerId+"用户可提现余额提现失败,用户资金钱包未变化");
			log.info("userId={}提现扣款失败，设置提现单withdrawsn={}失败",SessionUtil.getUserId(),withdrawalSn);
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
			log.info("扣除用户余额返回={}",withdrawRst==null?"":withdrawRst.getCode()+":"+withdrawRst.getMsg()+":"+withdrawRst.getData());
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_NOT_ENOUGH.getcode(),PayEnums.PAY_RONGBAO_NOT_ENOUGH.getMsg());
		}
		userWithdrawLog = new UserWithdrawLog();
		userWithdrawLog.setLogCode(CashEnums.CASH_REVIEWING.getcode());
		userWithdrawLog.setLogName(CashEnums.CASH_REVIEWING.getMsg());
		userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
		userWithdrawLog.setWithdrawSn(widthDrawSn);
		userWithdrawLogService.save(userWithdrawLog);
		BaseResult<SysConfigDTO> baseResult = userAccountService.queryBusinessLimit(cfg);
		double limit = 100;	//默认100提现阈值数
		if(baseResult.getData() != null) {
			limit = baseResult.getData().getValue().doubleValue();
		}
		if(totalAmount > limit) {
			log.info("单号:"+widthDrawSn+"超出提现阈值,进入审核通道  系统阈值:" + limit);
			return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_APPLY_SUC.getcode(),PayEnums.PAY_WITHDRAW_APPLY_SUC.getMsg());
		}else {
			//先减少用户钱包余额
			log.info("进入第三方提现流程...系统阈值:" + limit + " widthDrawSn:" + widthDrawSn);
            UserWithdraw userWithdraw = new UserWithdraw();
            userWithdraw.setWithdrawalSn(withdrawalSn);
            userWithdrawMapper.updateUserWithdrawStatus0To3(userWithdraw);
			RspSingleCashEntity rEntity = callThirdGetCash(widthDrawSn,totalAmount,cardNo,realName,mobile,bankCode);
			long time3 = System.currentTimeMillis();
			log.info("time3为："+time3);
			log.info("提现所用时间为："+(time3-time1));
			return operation(rEntity,widthDrawSn,userId,Boolean.TRUE);
		}
	}
	
	/**
	 * 调用第三方扣款流程
	 * @param orderSn
	 * @param totalAmount
	 * @return
	 */
	private RspSingleCashEntity callThirdGetCash(String orderSn,double totalAmount,String accNo,String accName,String phone,String bankNo) {
		log.info("=====callThirdGetCash======");
		log.info("orderSn:" + orderSn + " total:" + totalAmount + " accNo:" + accNo + " accName:" + accName + " phone:" + phone + " bankNo:" + bankNo);
//		//test code
//		//========================
//		accNo = "6222021001115704287";
//		accName = "王泽武";
//		phone = "18100000000";
//		bankNo = "CCB";
//		//======================
		BigDecimal bigDec = BigDecimal.valueOf(totalAmount);
		BigDecimal bigFen = bigDec.multiply(new BigDecimal(100));
		RspSingleCashEntity rEntity = new RspSingleCashEntity();
		String tips = null;
		try {
			rEntity = xianfengUtil.reqCash(orderSn,bigFen.intValue()+"", accNo, accName, phone, bankNo);
			log.info("RspCashEntity->"+rEntity);
		} catch (Exception e) {
			log.error("提现异常, ordersn="+orderSn, e);
			tips = e.getMessage();
			rEntity.resMessage = tips;
		}
		return rEntity;
	}

	public BaseResult<Object> operationSucc(RspSingleCashEntity rEntity,String withDrawSn){
		log.info("单号:"+withDrawSn+"第三方提现成功，扣除用户余额");
		//更新提现单
		log.info("提现单号:"+withDrawSn+"更新提现单位成功状态");
		UpdateUserWithdrawParam updateParams = new UpdateUserWithdrawParam();
//		updateParams.setWithdrawalSn(withdrawalSnDTO.getWithdrawalSn());
		updateParams.setWithdrawalSn(withDrawSn);
		updateParams.setStatus(ProjectConstant.STATUS_SUCC);
		updateParams.setPayTime(DateUtil.getCurrentTimeLong());
		updateParams.setPaymentId(withDrawSn);
		updateParams.setPaymentName("管理后台发起提现");
		userWithdrawService.updateWithdraw(updateParams);
		this.goWithdrawMessageSuccess(withDrawSn);
		
		//提现中，提现成功两条记录到 withdraw_log中
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
	 * @param rEntity
	 * @param widthDrawSn
	 * @param userId
	 * @param isApply 是否申请响应信息 true 申请接口返回 false 是主动查询或者异步通知接口响应
	 * @return
	 */
	public BaseResult<Object> operation(RspSingleCashEntity rEntity,String widthDrawSn,Integer userId,Boolean isApply) {
		if(rEntity.isTradeSucc()) {
			log.info("提现单号:"+widthDrawSn+"更新提现单为成功状态");
			UserWithdraw userWithdraw = new UserWithdraw();
	    	userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
	    	userWithdraw.setWithdrawalSn(widthDrawSn);
			int row = userWithdrawMapper.updateUserWithdrawStatus3To1(userWithdraw);
			if(row==1){
				this.goWithdrawMessageSuccess(widthDrawSn);
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_SUCC.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_SUCC.getMsg());
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(widthDrawSn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genSuccessResult("提现成功");
			}else{
				log.warn("withdrawSn={},更新数据状态为1（成功），更新失败,",widthDrawSn);
				return ResultGenerator.genSuccessResult("提现成功");
			}
		}else if(rEntity.isTradeFail(isApply)){
			log.info("提现订单号={}，提现失败信息={}",widthDrawSn,rEntity.resMessage);
			UserWithdraw userWithdraw = new UserWithdraw();
	    	userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
	    	userWithdraw.setWithdrawalSn(widthDrawSn);
			int updateRowNum = userWithdrawMapper.updateUserWithdrawStatus3To4(userWithdraw);
			if(updateRowNum==1){	
				//保存提现中状态记录位失败到数据库中...
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_FAILURE.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_FAILURE.getMsg()+"[" +rEntity.resCode+":"+rEntity.resMessage+"]");
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(widthDrawSn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genResult(PayEnums.CASH_FAILURE.getcode(),"提现失败");
			}else{
				log.warn("withdrawSn={}",widthDrawSn);
				return ResultGenerator.genResult(PayEnums.CASH_FAILURE.getcode(),"提现失败");
			}
		}else if(rEntity.isTradeDoing()){
			return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_APPLY_SUC.getcode(),PayEnums.PAY_WITHDRAW_APPLY_SUC.getMsg());
		}
		return null;
	}
	
	public BaseResult<Object> getCash(UserWithdraw userWithDraw,Boolean approvePass){
		String sn = userWithDraw.getWithdrawalSn();
		if(approvePass) {
			int userId = userWithDraw.getUserId();
			String realName = userWithDraw.getRealName();
			String cardNo = userWithDraw.getCardNo();
			UserIdRealParam params = new UserIdRealParam();
			params.setUserId(userId);
			//通过UserService查询到手机号码
			BaseResult<UserDTO> bR = userService.queryUserInfoReal(params);
			UserDTO userDTO = null;
			String phone = "";
			if(bR.getCode() == 0 && bR.getData() != null) {
				userDTO = bR.getData();
				phone = userDTO.getMobile();
			}
			if(StringUtils.isEmpty(phone)) {
				return  ResultGenerator.genFailResult("手机号码查询失败",null);
			}
			//银行信息
			String bankCode = "";
			UserBankQueryParam userBQP = new UserBankQueryParam();
			userBQP.setUserId(userId);
			userBQP.setBankCardCode(cardNo);
			BaseResult<UserBankDTO> base = userBankService.queryUserBankByCondition(userBQP);
			if(base.getCode() != 0 || base.getData() == null) {
				return ResultGenerator.genFailResult("查询银行信息失败",null);
			}
			UserBankDTO userBankDTO = base.getData();
			bankCode = userBankDTO.getAbbreviation();
			log.info("[queryUserBankByCondition]" +" bankAcc:" + userBankDTO.getCardNo() +" bankName:" + userBankDTO.getBankName() +" bankCode:" + userBankDTO.getAbbreviation());
			if(StringUtils.isEmpty(bankCode)) {
				return ResultGenerator.genResult(PayEnums.PAY_WITHDRAW_BIND_CARD_RETRY.getcode(),PayEnums.PAY_WITHDRAW_BIND_CARD_RETRY.getMsg());
			}
			BigDecimal amt = userWithDraw.getAmount();
			log.info("=================后台管理审核通过====================");
			log.info("进入到第三方提现流程，金额:" + amt.doubleValue() +" 用户名:" +userWithDraw.getUserId()  + " sn:" + sn + " realName:" + realName + " phone:" + phone + " amt:" + amt + " bankCode:" + bankCode);
			log.info("=================后台管理审核通过====================");
			RspSingleCashEntity rspSCashEntity = callThirdGetCash(sn,amt.doubleValue(),cardNo,realName,phone,bankCode);
			//后台点击的都变为提现审核中
	    	UserWithdraw userWithdraw = new UserWithdraw();
	    	userWithdraw.setWithdrawalSn(sn);
			int row = userWithdrawMapper.updateUserWithdrawStatus0To3(userWithdraw);
			if(row==1){
				return operation(rspSCashEntity,sn,userId,Boolean.TRUE);
			}
		}else {
			log.info("后台管理审核拒绝，提现单状态为失败...");
//			更新提现单失败状态
			UserWithdraw userWithdraw = new UserWithdraw();
			userWithdraw.setWithdrawalSn(sn);
			userWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
			int row=userWithdrawMapper.updateUserWithdrawStatus0To4(userWithdraw);
			if(row==1){				
				//增加提现流水为失敗
				log.info("后台管理审核拒绝，增加提现单log日志...");
				UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
				userWithdrawLog.setLogCode(CashEnums.CASH_FAILURE.getcode());
				userWithdrawLog.setLogName(CashEnums.CASH_FAILURE.getMsg());
				userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
				userWithdrawLog.setWithdrawSn(sn);
				userWithdrawLogService.save(userWithdrawLog);
				return ResultGenerator.genFailResult("后台管理审核拒绝成功...");
			}
		}
		return ResultGenerator.genFailResult("已审核");
	}
	
	public void withdrawNotify(HttpServletRequest request, HttpServletResponse response) throws IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type","text/html;charset=UTF-8");
	    Map parameters = request.getParameterMap();//保存request请求参数的临时变量
        String dataValue = "";//保存业务数据加密值
        //打印先锋支付返回值
        log.info("服务器端通知-接收到先锋支付返回报文：");
        Iterator paiter = parameters.keySet().iterator();
        while (paiter.hasNext()) {
            String key = paiter.next().toString();
            String[] values = (String[])parameters.get(key);                        
            log.info(key+"-------------"+values[0]);
            if(key.equals("data")) {
            	dataValue = values[0];
            	try {//String dataValue = AESCoder.decrypt(signVal, Constants.MER_RSAKEY);
					String dataJson= AESCoder.decrypt(dataValue, xFConstants.getMER_RSAKEY());
					RspSingleCashEntity rspSingleCashEntity = JSON.parseObject(dataJson,RspSingleCashEntity.class);
					String withDrawSn = rspSingleCashEntity.merchantNo;
					if(!StringUtils.isEmpty(withDrawSn)) {
						BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
						log.info("[withdrawNotify]" + " data:" + baseResult.getData() + " code:" + baseResult.getCode());
						if(baseResult.getCode() == 0) {
							UserWithdraw userWithDraw = baseResult.getData();
							PrintWriter writer = response.getWriter();
				        	writer.write("SUCCESS");
				        	writer.flush();
				        	log.info("============SUCESS返回====================");
				        	//提现单没有达最终态
							if(userWithDraw != null 
							   && !ProjectConstant.STATUS_FAILURE.equals(userWithDraw.getStatus())
									   && !ProjectConstant.STATUS_FAIL_REFUNDING.equals(userWithDraw.getStatus())
							   && !ProjectConstant.STATUS_SUCC.equals(userWithDraw.getStatus())) {
								int userId = userWithDraw.getUserId();
								log.info("[withdrawNotify]" + " userId:" + userId +  " withDrawSn:" + withDrawSn);
								operation(rspSingleCashEntity,rspSingleCashEntity.merchantNo, userId,Boolean.FALSE);
							}
						}
					}
					log.info("[withdrawNotify]" + " jsonObject:" + dataJson);
            	} catch (Exception e) {
					log.error("[withdrawNotify]",e);
				}
            }
        }
	}
	
	@Async
	private void goWithdrawMessageSuccess(String withDrawSn) {
		UserWithdraw userWithdraw = userWithdrawService.queryUserWithdraw(withDrawSn).getData();
		if(userWithdraw == null) {
			return;
		}
		Integer userId = userWithdraw.getUserId();
		UserIdParam userIdParam = new UserIdParam();
		userIdParam.setUserId(userId);
		UserDTO userDto = userService.queryUserInfo(userIdParam).getData();
		if(userDto == null) {
			return;
		}
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(1);
		//消息
		String status = userWithdraw.getStatus();
		MessageAddParam messageAddParam = new MessageAddParam();
		if(ProjectConstant.STATUS_SUCC.equals(status)) {
			messageAddParam.setTitle(CommonConstants.FORMAT_WITHDRAW_SUC_TITLE);
			messageAddParam.setContentDesc(CommonConstants.FORMAT_WITHDRAW_SUC_CONTENT_DESC);
		}else {
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
		Integer addTime =userWithdraw.getAddTime();
		String addTimeStr = this.getTimeStr(addTime);
		Integer checkTime = DateUtil.getCurrentTimeLong();
		String checkTimeStr = this.getTimeStr(checkTime);
		Integer payTime = userWithdraw.getPayTime();
		String payTimeStr = this.getTimeStr(payTime);
		String strDesc = CommonConstants.FORMAT_WITHDRAW_MSG_DESC;
		if(ProjectConstant.STATUS_FAILURE.equals(status)) {
			strDesc = CommonConstants.FORMAT_WITHDRAW_MSG_FAIL_DESC;
		}
		messageAddParam.setMsgDesc(MessageFormat.format(strDesc,addTimeStr, checkTimeStr, payTimeStr));
		params.add(messageAddParam);
		addParam.setParams(params);
		userMessageService.add(addParam);
	}

	private String getTimeStr(Integer addTime) {
		if(addTime <= 0) {
			return "";
		}
		String addTimeStr = DateUtil.getCurrentTimeString(Long.valueOf(addTime), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
//		LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.UTC);
//		String addTimeStr = loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:dd"));
		return addTimeStr;
	}
	
	@Transactional
	public BaseResult<Object> queryCash(String withDrawSn){
		UserWithdraw userWithdraw = userWithdrawService.queryUserWithdraw(withDrawSn).getData();
		if(userWithdraw == null) {
			return ResultGenerator.genFailResult("该订单不存在...sn:" + withDrawSn);
		}
		int userId = userWithdraw.getUserId();
		String cardNo = userWithdraw.getCardNo();
		UserIdRealParam userIdPara = new UserIdRealParam();
		userIdPara.setUserId(userId);
		BaseResult<UserDTO> userInfoExceptPass = userService.queryUserInfoReal(userIdPara);
		if(userInfoExceptPass.getCode() != 0 || userInfoExceptPass.getData() == null) {
			return ResultGenerator.genFailResult("该用户不存在 userID:" + userId);
		}
		//银行信息
		UserBankQueryParam userBQP = new UserBankQueryParam();
		userBQP.setUserId(userId);
		userBQP.setBankCardCode(cardNo);
		BaseResult<UserBankDTO> base = userBankService.queryUserBankByCondition(userBQP);
		if(base.getCode() != 0 || base.getData() == null) {
			return ResultGenerator.genFailResult("查询银行信息失败",null);
		}
		RspSingleCashEntity rspEntity;
		try {
			rspEntity = xianfengUtil.queryCash(withDrawSn);
			if(rspEntity != null) {
				return operation(rspEntity,withDrawSn, userId,Boolean.FALSE);
			}
		} catch (Exception e) {
			log.info("调取先锋支付查询报错",e);
		}
		return ResultGenerator.genFailResult("查询失败~",null);
	}
	
	/**
	 * 提现状态轮询
	 */
    public void timerCheckCashReq() {
    	List<UserWithdraw> userWithdrawList = userWithdrawMapper.queryUserWithdrawIng();
    	for(UserWithdraw userWithdraw:userWithdrawList){
    		try {
				queryWithdrawResult(userWithdraw);
			} catch (Exception e) {
				log.error("提现查询失败withdrawSn={}",userWithdraw.getWithdrawalSn(),e);
			}
    	}
	}
	
	
	private void queryWithdrawResult(UserWithdraw userWithdraw) throws Exception{
		String withDrawSn = userWithdraw.getWithdrawalSn();
		Integer userId = userWithdraw.getUserId();
		RspSingleCashEntity rspEntity = xianfengUtil.queryCash(withDrawSn);
		if(rspEntity != null) {
			this.operation(rspEntity,withDrawSn, userId,Boolean.FALSE);
		}
	}
	
}
