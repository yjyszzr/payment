package com.dl.shop.payment.pay.yifutong.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PayFinishRedirectUrlTDTO;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTPayEntity;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTSignEntity;
import com.dl.shop.payment.pay.yifutong.entity.RspYFTEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.ReqPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqQRPayEntity;
import com.dl.shop.payment.pay.yinhe.entity.ReqSignEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.web.PaymentController;

@Component
public class PayYFTUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private ReqYFTPayEntity reqYFTPayEntity;
	@Resource 
	private ReqPayEntity reqPayEntity;
	@Resource
	private PayMentService payMentService;
	/**
	 * 获取请求参数
	 * @param treeMap
	 * @return
	 */
	public final String getPayParams(ReqYFTPayEntity reqEntity) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("mchNo="+reqEntity.getMchNo()+"&");
		buffer.append("notifyUrl="+reqEntity.getNotifyUrl()+"&");
		buffer.append("orderCode="+reqEntity.getOrderCode()+"&");
		buffer.append("price="+reqEntity.getPrice()+"&");
		buffer.append("succPage="+reqEntity.getSuccPage()+"&");
		buffer.append("token="+reqEntity.getToken()+"&");
		buffer.append("ts="+reqEntity.getTs()+"&");
		buffer.append("type="+reqEntity.getType());
		String sign = MD5Utils.MD5(buffer.toString());
		buffer.append("&sign="+sign);
		logger.info("请求参数拼接结果:" + buffer.toString());
		return buffer.toString();
	}
	
	/**
	 * 获取微信支付url
	 */
	public final RspYFTEntity getWechatPayUrl(boolean isZfb,String ip,String amount,String orderNo){
		logger.info("调取微信支付订单orderSn={},amount={}",orderNo,amount);
		if("true".equals(cfgPay.getDEBUG())) {
			amount = "2";
		}
		RspYFTEntity rEntity = null;
		ReqYFTPayEntity reqEntity = null;
		 
		reqEntity = reqYFTPayEntity.buildReqEntity(amount,orderNo);
		
		//获取拼接参数
		String paraStr = getPayParams(reqEntity);
	 
		RspHttpEntity rspHttpEntity = null;
		if(isZfb) {	//yinHePay->动态二维码方式   yinHePayPublic
			rspHttpEntity = HttpUtil.sendMsg(paraStr,cfgPay.getURL_PAY()+"/yinHePayPublic.action",true);
		}else {
			//发送  yinHePay.action  -> yinHePayH5.action
			rspHttpEntity = HttpUtil.sendMsg(paraStr,cfgPay.getURL_PAY()+"/yinHePayH5.action",true);			
		}
		logger.info("resultStr:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = JSON.parseObject(rspHttpEntity.msg,RspYFTEntity.class);
			
		}else {
			rEntity = new RspYFTEntity();
			rEntity.msg = rspHttpEntity.msg;
		}
		return rEntity;
	}
}
