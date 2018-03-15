package com.dl.shop.payment.web;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SessionUtil;
import com.dl.shop.payment.dto.WxpayAppDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.model.WxpayOrderQuery;
import com.dl.shop.payment.param.GoPayParam;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.utils.WxpayUtil;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/payment/wxpay")
public class WxPayController {

	private final static Logger logger = LoggerFactory.getLogger(WxPayController.class);
	
	@Autowired
	private PayLogService payLogService;
	@Resource
	private WxpayUtil wxpayUtil;
	
	@ApiOperation(value="微信app支付调用", notes="微信app支付,orderSn,parentSn最少一个有值，orderAmount必须为非0的值，payType取值为0或1，payCode=app_weixin")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<WxpayAppDTO> unifiedOrderForApp(@RequestBody GoPayParam param, HttpServletRequest request) {
		String loggerId = "wxpay_app_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/wxpay/app, userId="+SessionUtil.getUserId()+" ,payCode="+param.getPayCode()+" , paytype="+param.getPayType()+" , orderSn="+param.getOrderSn()+ " , orderAmount="+param.getOrderAmount());
		if(!"app_weixin".equals(param.getPayCode())) {
			return ResultGenerator.genFailResult("对不起，操作错误！", null);
		}
		if(StringUtils.isBlank(param.getOrderSn()) ) {
			return ResultGenerator.genFailResult("请提供有效的订单号！", null);
		}
		if(param.getOrderAmount() == 0) {
			return ResultGenerator.genFailResult("请提供有效的支付金额！", null);
		}
		int payType = param.getPayType();
		if(payType !=0 && payType != 1) {
			return ResultGenerator.genFailResult("请选择有效的支付类型！", null);
		}
		UnifiedOrderParam unifiedOrderParam = new UnifiedOrderParam();
		if(0 == payType) {//订单校验
			
		}else {//充值校验
			
		}
		String ip = this.getIpAddr(request);
		unifiedOrderParam.setIp(ip);
		PayLog payLog = this.newPayLog(unifiedOrderParam, param, "微信app支付");
		int status = payLogService.findPayStatus(payLog);
		if(status > 0) {
			logger.info(loggerId + " 该订单已完成操作！payLog_status="+status); 
			return ResultGenerator.genFailResult("该订单已完成操作！", null);
		}
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if(null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！"); 
			return ResultGenerator.genFailResult("请求失败！", null);
		}
		//更新订单号为paylogId
		unifiedOrderParam.setOrderNo(savePayLog.getLogId());
		BaseResult<WxpayAppDTO> rst = wxpayUtil.unifiedOrderForApp(unifiedOrderParam);
		if(1==rst.getCode()) {
			try {
				savePayLog.setIsPaid(0);
				savePayLog.setPayMsg(rst.getMsg());
				payLogService.updatePayMsg(savePayLog);
			} catch (Exception e) {
				logger.error("保存失败记录时出错", e);
			}
		}else {
			//user 扣除余额
			BigDecimal orderAmount = payLog.getOrderAmount();
			
		}
		return rst;
	}
	
	@ApiOperation(value="微信app支付订单结果 查询 ", notes="")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<Object> orderquery(String payLogId) {
		if(StringUtils.isBlank(payLogId) ) {
			return ResultGenerator.genFailResult("订单号不能为空！", null);
		}
		PayLog payLog = payLogService.findById(Integer.parseInt(payLogId));
		if(null == payLog) {
			return ResultGenerator.genFailResult("请提供有效的订单号！", null);
		}
		int isPaid = payLog.getIsPaid();
		if(1== isPaid) {
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}
		BaseResult<WxpayOrderQuery.Response> baseResult = wxpayUtil.orderQuery(payLogId);
		if(1 == baseResult.getCode()) {
			logger.error(baseResult.getMsg());
			return ResultGenerator.genFailResult("请求异常！", null);
		}
		WxpayOrderQuery.Response response = baseResult.getData();
		String tradeState = response.getTrade_state();
		if("SUCCESS".equals(tradeState)) {
			PayLog updatePayLog = new PayLog();
			updatePayLog.setIsPaid(1);
			updatePayLog.setPayMsg("支付成功");
			payLogService.updatePayMsg(updatePayLog);
			return ResultGenerator.genSuccessResult("订单已支付成功！", null);
		}else if("USERPAYING".equals(tradeState)) {//????
			return ResultGenerator.genSuccessResult("订单支付中...", null);
		}else {
			//退回用户余额
			BigDecimal orderAmount = payLog.getOrderAmount();
			
			PayLog updatePayLog = new PayLog();
			updatePayLog.setIsPaid(0);
			updatePayLog.setPayMsg(response.getTrade_state()+"_" + response.getTrade_state_desc());
			payLogService.updatePayMsg(updatePayLog);
			return ResultGenerator.genFailResult("请求失败！", null);
		}
	}
	
	protected PayLog newPayLog(UnifiedOrderParam param, GoPayParam goPayParam, String payName) {
		Integer userId = SessionUtil.getUserId();
		PayLog payLog = new PayLog();
		payLog.setUserId(userId);
		payLog.setPayType(goPayParam.getPayType());
		payLog.setPayName(payName);
		payLog.setPayCode(goPayParam.getPayCode());
		payLog.setParentSn("");
		payLog.setOrderSn(goPayParam.getOrderSn());
		payLog.setOrderAmount(BigDecimal.valueOf(param.getTotalAmount()));
		Integer current = DateUtil.getCurrentTimeLong();
		payLog.setLastTime(current);
		payLog.setAddTime(current);
		payLog.setIsPaid(0);
		payLog.setPayIp(param.getIp());
		return payLog;
	}
	/**
	 * 获取客户端ip
	 * @param request
	 * @return
	 */
	protected String getIpAddr(HttpServletRequest request) {   
		String ip = request.getHeader("x-forwarded-for");   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("Proxy-Client-IP");   
		}   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("WL-Proxy-Client-IP");   
		}   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getRemoteAddr(); 
			if(ip.equals("127.0.0.1")){     
				//根据网卡取本机配置的IP     
				InetAddress inet=null;     
				try {     
					inet = InetAddress.getLocalHost();     
				} catch (UnknownHostException e) {     
					e.printStackTrace();     
				}     
				ip= inet.getHostAddress();     
			}  
		}   
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
		if(ip != null && ip.length() > 15){    
			if(ip.indexOf(",")>0){     
				ip = ip.substring(0,ip.indexOf(","));     
			}     
		}     
		return ip;   
	}
}
