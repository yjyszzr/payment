package com.dl.shop.payment.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.IUserBankService;
import com.dl.member.dto.BankDTO;
import com.dl.shop.payment.dto.BankTypeDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.XianFengBankTypeParam;
import com.dl.shop.payment.param.XianFengPayConfirmParam;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.XianFengService;
import io.swagger.annotations.ApiOperation;

/**
 * 先锋支付
 * @date 2018.06.08
 */
@Controller
@RequestMapping("/payment/xianfeng")
public class XianFengController {
	private final static Logger logger = LoggerFactory.getLogger(XianFengController.class);
	
	@Resource
	private PayLogService payLogService;
	@Resource
	private XianFengService xianFengService;
	@Resource
	private IUserBankService userBankService;
	
	@ApiOperation(value="先锋支付回调")
	@PostMapping("/notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		
	}
	
	@ApiOperation(value="先锋支付请求")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<Object> appPay(@RequestBody XianFengPayParam payParam) {
		return xianFengService.appPay(payParam);
	}
	
	@ApiOperation(value="先锋支付确认")
	@PostMapping("/appConfirm")
	@ResponseBody
	public BaseResult<Object> appPayCfm(@RequestBody XianFengPayConfirmParam payParam) {
		return xianFengService.appPayCfm(payParam);
	}
	
	@ApiOperation(value="根据银行账号获取卡类型 目前只识别借记卡和贷记卡")
	@PostMapping("/getBankType")
	@ResponseBody
	public BaseResult<BankTypeDTO> getBankType(@RequestBody XianFengBankTypeParam param){
		String bankCardNo = param.getBankCardNo();
		BaseResult<BankDTO> baseResult = xianFengService.queryBankType(bankCardNo);
		if(baseResult.getCode() != 0) {
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANKTYPE_FAILURE.getcode(),PayEnums.PAY_XIANFENG_BANKTYPE_FAILURE.getMsg());
		}
		BankDTO bankDTO = baseResult.getData();
		BankTypeDTO bankTypeDTO = null;
		String cardtype = bankDTO.getCardtype();
		if("借记卡".equals(cardtype)) {
			bankTypeDTO = new BankTypeDTO();
			bankTypeDTO.setBankType(1);
		}else if("贷记卡".equals(cardtype)) {
			bankTypeDTO = new BankTypeDTO();
			bankTypeDTO.setBankType(2);
		}
		if(bankTypeDTO == null) {
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANKTYPE_UNKNOW.getcode(),PayEnums.PAY_XIANFENG_BANKTYPE_UNKNOW.getMsg());
		}
		return ResultGenerator.genSuccessResult("succ",bankTypeDTO);
	}
	
	
	@ApiOperation(value="先锋支付获取支付验证码")
	@PostMapping("/sms")
	@ResponseBody
	public BaseResult<Object> getPaySms(@RequestBody XianFengPayParam payParam){
		int payLogId = payParam.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			logger.info("[getPaySms]" + "订单号查询失败");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_ORDER_BLANK.getcode(),PayEnums.PAY_XIANFENG_ORDER_BLANK.getMsg());	
		}
		String payOrderSn = payLog.getPayOrderSn();
		BaseResult<Object> baseResult = xianFengService.getPaySms(payOrderSn);
		if(baseResult == null) {
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_SMS_EXCEPTION.getcode(),PayEnums.PAY_XIANFENG_SMS_EXCEPTION.getMsg());
		}else {
			return baseResult;
		}
	}
	
	/***
	 * 先锋订单查询
	 * @param payParam
	 * @return
	 */
	@ApiOperation(value="先锋支付信息查询")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<String> query(@RequestBody XianFengPayParam payParam) {
		logger.info("[query]" +" payParams:" + payParam.getPayLogId());
		int payLogId = payParam.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			logger.info("[getPaySms]" + "订单号查询失败");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_ORDER_BLANK.getcode(),PayEnums.PAY_XIANFENG_ORDER_BLANK.getMsg());	
		}
		int isPaid = payLog.getIsPaid();
		int payType = payLog.getPayType();
		String payOrderSn = payLog.getPayOrderSn();
		if(isPaid == 1) {
			logger.info("[query]" + " 订单:" + payLogId +" 已支付" + " payType:" + payType + " payOrderSn:" + payOrderSn);
			if(payType == 0) {
				return ResultGenerator.genSuccessResult("订单已支付");
			}else {
				return ResultGenerator.genSuccessResult("充值成功");				
			}
		}
		BaseResult<String> baseResult = xianFengService.query(payLog,payOrderSn);
		if(baseResult == null) {
			return ResultGenerator.genFailResult("先锋查询异常");
		}
		return baseResult;
	}
}
