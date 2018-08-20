package com.dl.shop.payment.web;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.RegexUtil;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IUserBankService;
import com.dl.member.dto.BankDTO;
import com.dl.shop.payment.dao.PayBankRecordMapper;
import com.dl.shop.payment.dto.BankTypeDTO;
import com.dl.shop.payment.dto.XianFengApplyCfgDTO;
import com.dl.shop.payment.dto.XianFengApplyDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.PayBankRecordModel;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.XianFengBankListCfgParam;
import com.dl.shop.payment.param.XianFengBankRemoveParam;
import com.dl.shop.payment.param.XianFengBankTypeParam;
import com.dl.shop.payment.param.XianFengCfgParam;
import com.dl.shop.payment.param.XianFengPayConfirmParam;
import com.dl.shop.payment.param.XianFengPayParam;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.entity.RspNotifyEntity;
import com.dl.shop.payment.pay.xianfeng.entity.RspNotifySignEntity;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.XianFengService;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.AESCoder;

import io.swagger.annotations.ApiModelProperty;
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
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private PayBankRecordMapper payBankRMapper;
	@Resource
	private PayMentService paymentService;
	
	@Resource
	private Constants xfConstants;
	
	private final String SIGN = "sign";
	private final String SECID = "RSA";//签名算法
	

	
	@ApiOperation(value="先锋支付请求")
	@PostMapping("/bank/remove")
	@ResponseBody
	public BaseResult<XianFengApplyDTO> bankRemove(@RequestBody XianFengBankRemoveParam bankRemove) {
		if(bankRemove==null||StringUtils.isEmpty(bankRemove.getRecordId())){
			logger.error("先锋支付移除支付卡记录recordId is empty");
			return ResultGenerator.genSuccessResult();
		}
		xianFengService.bankRemove(bankRemove.getRecordId());
		return ResultGenerator.genSuccessResult();
	}
	
	@ApiOperation(value="先锋支付请求")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<XianFengApplyDTO> appPay(@RequestBody XianFengPayParam payParam) {
		String token = payParam.getToken();
		if(StringUtils.isEmpty(payParam.getAccNo())){
			logger.info("先锋支付申请卡号输入不正确");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_QUICKPAY_CARDNO_ERROR.getcode(),PayEnums.PAY_XIANFENG_QUICKPAY_CARDNO_ERROR.getMsg());
		}
		if(StringUtils.isEmpty(payParam.getPhone())||!RegexUtil.checkMobile(payParam.getPhone())){
			logger.info("先锋支付申请手机号输入不正确");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_QUICKPAY_PHONE_ERROR.getcode(),PayEnums.PAY_XIANFENG_QUICKPAY_PHONE_ERROR.getMsg());
		}
		if(StringUtils.isEmpty(payParam.getCertNo())||!RegexUtil.checkIdCard(payParam.getCertNo())){
			logger.info("先锋支付申请身份证号输入不正确");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_QUICKPAY_ID_ERROR.getcode(),PayEnums.PAY_XIANFENG_QUICKPAY_ID_ERROR.getMsg());
		}
		//只是校验token信息是否为空
		if(!StringUtils.isEmpty(token)) {
			logger.info("[appPay]" +" token:" + token);
			XianFengPayParam p = new XianFengPayParam();
			p.setAccNo(payParam.getAccNo());
			p.setCertNo(payParam.getCertNo());
			p.setName(payParam.getName());
			p.setPayLogId(payParam.getPayLogId());
			p.setPhone(payParam.getPhone());
			p.setToken(token);
			return getPaySms(p);
		}else {
			token = SNGenerator.nextSN(SNBusinessCodeEnum.PAY_SN.getCode());
			logger.info("[appPay]" + " appPay call token:" + token);
			return xianFengService.appPay(payParam,token);	
		}
	}
	
	@ApiOperation(value="先锋支付确认")
	@PostMapping("/appConfirm")
	@ResponseBody
	public BaseResult<Object> appPayCfm(@RequestBody XianFengPayConfirmParam payParam) {
		int payLogId = payParam.getPayLogId();
		String code = payParam.getCode();
		String token = payParam.getToken();
		PayLog payLog = payLogService.findById(payLogId);
		if(StringUtils.isEmpty(token)){
			logger.info("[appPayCfm]" + " code:" + code+"还未获取验证码");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getMsg());
		}
		if(StringUtils.isEmpty(code)) {
			logger.info("[appPayCfm]" + " code:" + code);
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getcode(),PayEnums.PAY_XIANFENG_VERIFYCODE_WRONG.getMsg());
		}
		if(payLog == null) {
			logger.info("[appPayCfm]" + "查询PayLog失败");
			return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),PayEnums.PAY_RONGBAO_EMPTY.getMsg());
		}
		return xianFengService.appPayCfm(payLog,code);
	}
	
	@ApiOperation(value="获取先锋银行列表配置")
	@PostMapping("/appCfg")
	@ResponseBody
	public BaseResult<XianFengApplyCfgDTO> appCfg(@RequestBody XianFengCfgParam cfgParam) {
		int payLogId = cfgParam.getPayLogId();
		if(payLogId <= 0) {
			return ResultGenerator.genFailResult("PayLogId不合法");
		}
		return xianFengService.appPayCfg(payLogId);
	}
	
	@ApiOperation(value="银行列表支付信息")
	@PostMapping("/appBankListPay")
	@ResponseBody
	public BaseResult<XianFengApplyDTO> appBankListCfg(@RequestBody XianFengBankListCfgParam cfgParam){
		int recordId = cfgParam.getRecordId();
		String token = cfgParam.getToken();
		int payLogId = cfgParam.getPayLogId();
		if(recordId < 0) {
			return ResultGenerator.genFailResult("recordId不合法");
		}
		if(payLogId < 0) {
			return ResultGenerator.genFailResult("payLogId不合法");
		}
		PayBankRecordModel record = new PayBankRecordModel();
		record.setId(recordId);
		List<PayBankRecordModel> mList = payBankRMapper.queryPayBankRecordModelById(record);
		if(mList == null || mList.size() <= 0) {
			return ResultGenerator.genFailResult("未查询到支付记录");
		}
		PayBankRecordModel model = mList.get(0);
		XianFengPayParam xianFengPayParam = new XianFengPayParam();
		xianFengPayParam.setAccNo(model.getBankCardNo());
		xianFengPayParam.setCertNo(model.getCertNo());
		xianFengPayParam.setCvn2(model.getCvn2());
		xianFengPayParam.setName(model.getUserName());
		xianFengPayParam.setPayLogId(payLogId);
		xianFengPayParam.setPhone(model.getPhone());
		xianFengPayParam.setToken(token);
		xianFengPayParam.setValidDate(model.getValidDate());
		logger.info("[appBankListCfg]" + " bankCardNo:" + xianFengPayParam.getAccNo() + " certNo:" + xianFengPayParam.getAccNo()
		+" cvn2:" + xianFengPayParam.getCvn2() +" name:" + xianFengPayParam.getName() +" payLogId:" + payLogId + " phone:" + xianFengPayParam.getPhone()
		+" vaildeDate:" + xianFengPayParam.getValidDate());
		return appPay(xianFengPayParam);
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
		String bankName = bankDTO.getBankname();
		if("借记卡".equals(cardtype)) {
			bankTypeDTO = new BankTypeDTO();
			bankTypeDTO.setBankType(0);
			bankTypeDTO.setBankName(bankName);
		}else if("贷记卡".equals(cardtype)) {
			bankTypeDTO = new BankTypeDTO();
			bankTypeDTO.setBankType(1);
			bankTypeDTO.setBankName(bankName);
		}
		if(bankTypeDTO == null) {
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_BANKTYPE_UNKNOW.getcode(),PayEnums.PAY_XIANFENG_BANKTYPE_UNKNOW.getMsg());
		}
		return ResultGenerator.genSuccessResult("succ",bankTypeDTO);
	}
	
	
	@ApiOperation(value="先锋支付获取支付验证码")
	@PostMapping("/sms")
	@ResponseBody
	public BaseResult<XianFengApplyDTO> getPaySms(@RequestBody XianFengPayParam payParam){
		String token = payParam.getToken();
		if(StringUtils.isEmpty(payParam.getPhone())||!RegexUtil.checkMobile(payParam.getPhone())){
			logger.info("先锋支付申请手机号输入不正确");
			return ResultGenerator.genFailResult("手机号输入不正确");
		}
		int payLogId = payParam.getPayLogId();
		PayLog payLog = payLogService.findById(payLogId);
		if(payLog == null) {
			logger.info("[getPaySms]" + "订单号查询失败");
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_ORDER_BLANK.getcode(),PayEnums.PAY_XIANFENG_ORDER_BLANK.getMsg());	
		}
		if(Integer.valueOf(3).equals(payLog.getIsPaid())){
			logger.info("[getPaySms]" + "订单号pay_order_sn={},order_sn={}已失败,不能获取验证码",payLog.getPayOrderSn(),payLog.getOrderSn());
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_CANCEL_ERROR.getcode(),PayEnums.PAY_XIANFENG_CANCEL_ERROR.getMsg());
		}
		String payOrderSn = payLog.getPayOrderSn();
		BaseResult<XianFengApplyDTO> baseResult = xianFengService.getPaySms(payOrderSn,token);
		if(baseResult == null) {
			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_SMS_EXCEPTION.getcode(),PayEnums.PAY_XIANFENG_SMS_EXCEPTION.getMsg());
		}else {
			return baseResult;
		}
	}
	
	@ApiOperation(value="先锋支付回调")
	@PostMapping("/notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type","text/html;charset=UTF-8");
	    Map<?,?> parameters = request.getParameterMap();//保存request请求参数的临时变量
        String dataValue = "";//保存业务数据加密值
        JSONObject jsonData = null;
        //打印先锋支付返回值
        logger.info("服务器端通知-接收到先锋支付返回报文：");
        Iterator<?> paiter = parameters.keySet().iterator();
        while (paiter.hasNext()) {
            String key = paiter.next().toString();
            String[] values = (String[])parameters.get(key);                        
            logger.info(key+"-------------"+values[0]);
            if(key.equals("data")) {
            	dataValue = values[0];
            	logger.info("===========payNotify==============");
            	try {
					String dataJson= AESCoder.decrypt(dataValue, xfConstants.getMER_RSAKEY());
					RspNotifyEntity rspEntity = JSON.parseObject(dataJson,RspNotifyEntity.class);
					logger.info("[payNotify]" + " dataJson:" + dataJson);
		        	//处理返回数据
					if(rspEntity != null) {
						//进行验签,忽略该步骤
						RspNotifySignEntity signEntity = rspEntity.buildSignEntity();
						String signJsonStr = JSON.toJSONString(signEntity);
						Map<String,String> signMap = JSON.parseObject(signJsonStr,HashMap.class);
						boolean verifyResult = UcfForOnline.verify(xfConstants.getMER_RSAKEY(), SIGN,rspEntity.sign, signMap, SECID);
						logger.info("[payNotify]" + " 验签结果:" + verifyResult);
						if(verifyResult) {
							boolean isSucc = xianFengService.payNotify(rspEntity);
							logger.info("[payNotify]" + " isSucc:" + isSucc);
							if(isSucc) {
								//通知先锋成功
								PrintWriter writer = response.getWriter();
								writer.write("SUCCESS");
								writer.flush();
							}
						}
					}
            	} catch (Exception e) {
					e.printStackTrace();
					logger.info("[payNotify] exception msg:", e);
				}
            }
        }
	}
	
//	/***
//	 * 先锋订单查询,先锋单独使用
//	 * @param payParam
//	 * @return
//	 */
//	@ApiOperation(value="先锋支付信息查询")
//	@PostMapping("/query")
//	@ResponseBody
//	public BaseResult<String> query(@RequestBody XianFengPayParam payParam) {
//		logger.info("[query]" +" payParams:" + payParam.getPayLogId());
//		int payLogId = payParam.getPayLogId();
//		PayLog payLog = payLogService.findById(payLogId);
//		if(payLog == null) {
//			logger.info("[getPaySms]" + "订单号查询失败");
//			return ResultGenerator.genResult(PayEnums.PAY_XIANFENG_ORDER_BLANK.getcode(),PayEnums.PAY_XIANFENG_ORDER_BLANK.getMsg());	
//		}
//		int isPaid = payLog.getIsPaid();
//		int payType = payLog.getPayType();
//		String payOrderSn = payLog.getPayOrderSn();
//		if(isPaid == 1) {
//			logger.info("[query]" + " 订单:" + payLogId +" 已支付" + " payType:" + payType + " payOrderSn:" + payOrderSn);
//			if(payType == 0) {
//				return ResultGenerator.genSuccessResult("订单已支付");
//			}else {
//				return ResultGenerator.genSuccessResult("充值成功");
//			}
//		}
//		BaseResult<String> baseResult = xianFengService.query(payLog,payOrderSn);
//		if(baseResult == null) {
//			return ResultGenerator.genFailResult("先锋查询异常");
//		}
//		return baseResult;
//	}
}
