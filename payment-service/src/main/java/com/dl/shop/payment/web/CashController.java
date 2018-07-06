package com.dl.shop.payment.web;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.CashGetParam;
import com.dl.shop.payment.param.CashReqParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.dl.shop.payment.service.CashService;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.UserWithdrawService;

/**
 * 代支付
 * @date 2018.05.05
 */
@Controller
@RequestMapping("/cash")
@Slf4j
public class CashController {
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
			log.error(e.getMessage());
		}
	}
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		return cashService.withdrawForApp(param,request);
	}
	
	@ApiOperation(value="后台管理提现调用", notes="")
	@PostMapping("/getcash")
	@ResponseBody
	public BaseResult<Object> getCash(@RequestBody CashGetParam param, HttpServletRequest request){
//		FIXME  胡贺东 俩个管理员同时点击同一单的审核，存在并发问题，但是目前这种情况几乎不可能，因此暂时先不增加对并发的控制
		log.info("[getCash]" + " sn:" + param.getWithdrawSn());
		BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(param.getWithdrawSn());
		if(baseResult.getCode() != 0 || baseResult.getData() == null) {
			log.error("提现单号withdrawSn={}查不到对应的提现数据",param.getWithdrawSn());
			return ResultGenerator.genFailResult("未找到对应的提现单号信息",null);
		}
		UserWithdraw userWithDraw = baseResult.getData();
		if(!"0".equals(userWithDraw.getStatus())){
			log.error("提现单号withdrawSn={}查不到对应的提现数据",param.getWithdrawSn());
			return ResultGenerator.genFailResult("未找到对应的提现单号信息",null);
		}
		return cashService.getCash(userWithDraw,param.isPass());
	}
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/querycash")
	@ResponseBody
	public BaseResult<Object> queryCash(@RequestBody CashReqParam param){
		log.info("[queryCash]" + " withDrawSn:" + param.getWithdrawSn());
		return cashService.queryCash(param.getWithdrawSn());
	}
	
	@ApiOperation(value="提现状态轮询", notes="提现状态轮询")
	@PostMapping("/timerCheckCashReq")
	@ResponseBody
	public BaseResult<String> timerCheckCashReq(@RequestBody EmptyParam emptyParam){
		cashService.timerCheckCashReq();
		return ResultGenerator.genSuccessResult("success");
	}
	
}
