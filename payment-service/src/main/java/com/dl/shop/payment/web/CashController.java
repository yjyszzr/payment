package com.dl.shop.payment.web;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.CashGetParam;
import com.dl.shop.payment.param.CashReqParam;
import com.dl.shop.payment.param.UserWithDarwPersonOpenParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengCashUtil;
import com.dl.shop.payment.service.CashService;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.UserWithdrawService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 代支付
 * @date 2018.05.05
 */
@Controller
@RequestMapping("/cash")
@Slf4j
public class CashController {
	private static Boolean CHECKCASH_TASKRUN = Boolean.FALSE;
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
			log.error(e.getMessage());
		}
	}
	
	@ApiOperation(value="app提现调用", notes="")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param, HttpServletRequest request){
		//20181203 加入提示
		//return ResultGenerator.genResult(PayEnums.PAY_STOP_SERVICE.getcode(), PayEnums.PAY_STOP_SERVICE.getMsg());
		return cashService.withdrawForApp(param,request);
	}
	
	@ApiOperation(value="财务提现调用", notes="")
	@PostMapping("/withdrawCw")
	@ResponseBody
	public BaseResult<Object> withdrawForAppByCw(@RequestBody WithdrawParam param, HttpServletRequest request){
		//20181203 加入提示
		//return ResultGenerator.genResult(PayEnums.PAY_STOP_SERVICE.getcode(), PayEnums.PAY_STOP_SERVICE.getMsg());
		return cashService.withdrawForAppCw(param,request);
	}
	
	@ApiOperation(value="后台管理提现调用", notes="")
	@PostMapping("/getcash")
	@ResponseBody
	public BaseResult<Object> getCash(@RequestBody CashGetParam param, HttpServletRequest request){
		log.info("[getCash]" + " sn:{},approve={}",param.getWithdrawSn(),param.isPass());
		BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(param.getWithdrawSn());
		if(baseResult.getCode() != 0 || baseResult.getData() == null) {
			log.error("提现单号withdrawSn={}查不到对应的提现数据",param.getWithdrawSn());
			return ResultGenerator.genFailResult("未找到对应的提现单号信息",null);
		}
		UserWithdraw userWithDraw = baseResult.getData();
		log.info("后台审核状态={},withdrawsn={},status={}",userWithDraw.getWithdrawalSn(),userWithDraw.getStatus());
		if(!"0".equals(userWithDraw.getStatus())){
			log.error("提现单号withdrawSn={}查到的对应的提现状态={}",param.getWithdrawSn(),userWithDraw.getStatus());
			return ResultGenerator.genFailResult("未找到对应的提现单号信息",null);
		}
		return cashService.getCash(userWithDraw,param.isPass());
	}
	
	@ApiOperation(value="后台管理提现调用", notes="")
	@PostMapping("/userWithDrawPersonOpen")
	@ResponseBody
	public BaseResult<Object> userWithDrawPersonOpen(@RequestBody UserWithDarwPersonOpenParam param){
		Boolean withDrawByPersonOprateOpen = userWithdrawService.queryWithDrawPersonOpen();
		if(!withDrawByPersonOprateOpen){
			log.error("提现人工打款方式开关关闭,此接口不能处理业务");
			return ResultGenerator.genFailResult("提现人工打款方式开关关闭,此接口不能处理业务",null);
		}
		cashService.userWithDrawPersonSuccess(param.getSucessPersonWithdrawSns());
		cashService.userWithDrawPersonFail(param.getFailPersonWithdrawSns());
		return ResultGenerator.genSuccessResult("",null);
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
		if(CHECKCASH_TASKRUN){
			log.info("check cash is running ...... 请稍后重试");
			return ResultGenerator.genSuccessResult("success","check cash is running ...... 请稍后重试");
		}
		CHECKCASH_TASKRUN = Boolean.TRUE;
		cashService.timerCheckCashReq();
		CHECKCASH_TASKRUN = Boolean.FALSE;
		return ResultGenerator.genSuccessResult("success");
	}
	
}
