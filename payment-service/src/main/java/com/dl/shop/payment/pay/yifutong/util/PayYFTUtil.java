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
import com.dl.shop.payment.pay.yifutong.config.ConfigerYFTPay;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTPayEntity;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTSignEntity;
import com.dl.shop.payment.pay.yifutong.entity.RspYFTEntity;
import com.dl.shop.payment.pay.yifutong.entity.RspYFTEntity.ResultYFTData;
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
	private ConfigerYFTPay cfgPay;
//	@Resource
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
		logger.info("请求参数拼接前:reqEntity={}",reqEntity);
		StringBuffer buffer = new StringBuffer();
		buffer.append("mchNo="+reqEntity.getMchNo()+"&");
		buffer.append("notifyUrl="+reqEntity.getNotifyUrl()+"&");
		buffer.append("orderCode="+reqEntity.getOrderCode()+"&");
		buffer.append("price="+reqEntity.getPrice()+"&");
		buffer.append("succPage="+reqEntity.getSuccPage()+"&");
		buffer.append("ts="+reqEntity.getTs()+"&");
		buffer.append("type="+reqEntity.getType()+"&");
		buffer.append("token="+reqEntity.getToken());
		String sign = MD5Utils.MD5(buffer.toString());
		buffer.append("&sign="+sign);
		logger.info("请求参数拼接结果:patam={}",buffer.toString());
		return buffer.toString();
	}
	/**
	 * 校验支付返回信息签名
	 * @param args
	 */
	public final Boolean getPayParams(RspYFTEntity rEntity) {
		ResultYFTData data = rEntity.data;
		StringBuffer buffer = new StringBuffer();
		buffer.append("account="+data.account+"&");
		buffer.append("mchNo="+data.mchNo+"&");
		buffer.append("orderCode="+data.orderCode+"&");
		buffer.append("payUrl="+data.payUrl+"&");
		buffer.append("price="+data.price+"&");
		buffer.append("realPrice="+data.realPrice);
		String sign = MD5Utils.MD5(buffer.toString());
		if(!sign.equals(data.sign)) {
			logger.info("易富通支付请求返回签名错误patam={},sign={}",buffer.toString(),sign);
			return false;
		}
		return true;
	}
	public static void main(String[] args) {
		String str = "mchNo=1536317691tXgyQz&notifyUrl=http://39.106.18.39:7076/cash/notify&orderCode=1236668&price=0.01&succPage=https://www.baidu.com&ts=1536379050935&type=1&token=afef76a5aed6ba63a0c010c40e104cd4d156cd6e";
		System.out.println(str+"&sign="+ MD5Utils.MD5(str));
	}
	 
	/**
	 * 获取微信支付url
	 */
	public final RspYFTEntity getWechatPayUrl(String amount,String payOrderSn){
		logger.info("调取易富通支付宝支付订单payOrderSn={},amount={}",payOrderSn,amount);
		RspYFTEntity rEntity = null;
		ReqYFTPayEntity reqEntity = null;
		ReqYFTPayEntity req = new ReqYFTPayEntity();
		reqEntity = req.buildReqEntity(amount,payOrderSn);
		
		//获取拼接参数
		String paraStr = getPayParams(reqEntity);
	 
		RspHttpEntity rspHttpEntity = null;
		rspHttpEntity = HttpUtil.sendMsg(paraStr,"http://www.lanjunshop.com/api/getQrcode",false);
		logger.info("易富通支付请求返回信息:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = JSON.parseObject(rspHttpEntity.msg,RspYFTEntity.class);
			boolean rspSign = getPayParams(rEntity);
			if(!rspSign) {
				rspHttpEntity.isSucc = false;
				rEntity = new RspYFTEntity();
				rEntity.msg = "错误信息";
			}
		}else {
			logger.info("易富通支付请求失败code={},msgDetail={}" ,rEntity.code, rEntity.getCodeMsgDetail());
			rEntity = new RspYFTEntity();
			rEntity.msg = rspHttpEntity.msg;
		}
		return rEntity;
	}
}
