package com.dl.shop.payment.web;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.CashGetParam;
import com.dl.shop.payment.param.CashReqParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleQueryEntity;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
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
	@Resource
	private UserWithdrawLogService userWithdrawLogService;
	@Resource
	private IUserMessageService userMessageService;
	@Resource
	private CashService cashService;
	@Autowired
	private UserWithdrawService userWithdrawService;
	@Resource
	private XianFengCashUtil xianfengUtil;
	
	@ApiOperation(value="先锋提现notify", notes="")
	@PostMapping("/notify")
	@ResponseBody
	public void withdrawNotify(HttpServletRequest request, HttpServletResponse response){
		try {
			cashService.withdrawNotify(request,response);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		//test code
		for(int i = 0;i < 10;i++) {
			cashService.withdrawForApp(param, request);
		}
		return ResultGenerator.genSuccessResult("succ");
	}
	
	
	@ApiOperation(value="后台管理提现调用", notes="")
	@PostMapping("/getcash")
	@ResponseBody
	public BaseResult<Object> getCash(@RequestBody CashGetParam param, HttpServletRequest request){
		logger.info("[getCash]" + " sn:" + param.getWithdrawSn());
		if(param.isPass()) {
			//如果数据库提现单已经成功
			String withDrawSn = param.getWithdrawSn();
			BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
			if(baseResult.getCode() != 0 || baseResult.getData() == null) {
				return ResultGenerator.genFailResult("查询提现单失败",null);
			}
			//提现状态,2-失败,1-已完成，0-未完成
			UserWithdraw userWithDraw = baseResult.getData();
			logger.info("[getCash]" + " 提现单状态:" + userWithDraw.getStatus());
			if("1".equals(userWithDraw.getStatus()) || "2".equals(userWithDraw.getStatus())) {
				logger.info("[getCash]" + " 提现单状态已达终态");
				return ResultGenerator.genSuccessResult("提现单已达终态");
			}
			//查询第三方是否提现成功
			RspSingleCashEntity rspEntity = null;
			try {
				RspSingleQueryEntity sEntity = xianfengUtil.queryCash(withDrawSn);
				rspEntity = CashService.convert2RspSingleCashEntity(sEntity);
				logger.info("[getCash]" +" 提现单第三方查询结果:" + rspEntity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(rspEntity != null) {
				//查询第三方已提现成功
				if(rspEntity.isSucc()) {
					//更改订单成功状态,增加提现金日志
					logger.info("[getCash]" +" 第三方提现成功，提现单状态修改为成功...");
					return cashService.operationSucc(rspEntity,withDrawSn);
				}
			}
		}
		return cashService.getCash(param, request);
	}
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/querycash")
	@ResponseBody
	public BaseResult<Object> queryCash(@RequestBody CashReqParam param){
		logger.info("[queryCash]" + " withDrawSn:" + param.getWithdrawSn());
		return cashService.queryCash(param.getWithdrawSn());
	}
	
	@ApiOperation(value="提现状态轮询", notes="提现状态轮询")
	@PostMapping("/timerCheckCashReq")
	@ResponseBody
	public BaseResult<String> timerCheckCashReq(@RequestBody EmptyParam emptyParam){
		//test code
//		cashService.timerCheckCashReq();
		return ResultGenerator.genSuccessResult("success");
	}
	
}
