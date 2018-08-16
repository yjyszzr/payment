package com.dl.shop.payment.web;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.shop.payment.model.PayLog;

public abstract class AbstractBaseController {

	private final static Logger logger = Logger.getLogger(AbstractBaseController.class);
	/**
	 * 
	 * @param orderSn 订单编码
	 * @param totalAmount 订单需支付金额
	 * @param payType 支付类型：0-订单支付 1-充值
	 * @param payCode 支付方式编码
	 * @param payName 支付方式名称
	 * @param payIp 客户端ip
	 * @return
	 */
	protected PayLog newPayLog(int userId,String orderSn, BigDecimal totalAmount, Integer payType, String payCode, String payName, String payIp) {
		PayLog payLog = new PayLog();
		payLog.setUserId(userId);
		payLog.setPayType(payType);
		payLog.setPayName(payName);
		payLog.setPayCode("app_weixin");
		payLog.setParentSn("");
		payLog.setOrderSn(orderSn);
		payLog.setOrderAmount(totalAmount);
		Integer current = DateUtil.getCurrentTimeLong();
		payLog.setLastTime(current);
		payLog.setAddTime(current);
		payLog.setIsPaid(0);
		payLog.setPayIp(payIp);
		String sn = SNGenerator.nextSN(SNBusinessCodeEnum.PAY_SN.getCode());
		payLog.setPayOrderSn(sn);
		return payLog;
	}
	/**
	 * 获取客户端ip
	 * @param request
	 * @return
	 */
	protected String getIpAddr(HttpServletRequest request) { 
		String ip = request.getHeader("X-Real-IP");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("X-Forwarded-For");   
		}  
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("Proxy-Client-IP");   
		}   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("WL-Proxy-Client-IP");   
		}
		if (ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {   
		    ip = request.getHeader("HTTP_CLIENT_IP");   
		}  
		if (ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {   
		    ip = request.getHeader("HTTP_X_FORWARDED_FOR");   
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getRemoteAddr(); 
		}
		if(ip != null && ip.equals("127.0.0.1")){     
			//根据网卡取本机配置的IP     
			InetAddress inet=null;     
			try {     
				inet = InetAddress.getLocalHost();     
			} catch (UnknownHostException e) {     
				e.printStackTrace();     
			}     
			ip= inet.getHostAddress();     
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
