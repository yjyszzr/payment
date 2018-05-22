package com.dl.shop.payment.web;

import java.math.BigDecimal;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserDTO;
import com.dl.member.dto.WithdrawalSnDTO;
import com.dl.member.param.IDParam;
import com.dl.member.param.MemWithDrawSnParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SysConfigParam;
import com.dl.member.param.WithDrawParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.enums.CashEnums;
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
import com.dl.shop.payment.service.CashService;
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
	
	@Resource
	private CashService cashService;
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		return cashService.withdrawForApp(param, request);
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
		return cashService.getCash(param, request);
	}
}
