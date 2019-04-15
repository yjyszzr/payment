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
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.MD5Utils;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PayFinishRedirectUrlTDTO;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.yifutong.config.ConfigerYFTPay;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTPayEntity;
import com.dl.shop.payment.pay.yifutong.entity.ReqYFTSignEntity;
import com.dl.shop.payment.pay.yifutong.entity.RespYFTnotifyEntity;
import com.dl.shop.payment.pay.yifutong.entity.RspQueryYFTEntity;
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
	@Resource
	private ReqYFTPayEntity reqYFTPayEntity;
	/**
	 * 获取请求参数
	 * @param treeMap
	 * @return
	 */
	public final String getPayParams(ReqYFTPayEntity reqEntity) {
		logger.info("易富通请求参数拼接前:reqEntity={}",reqEntity);
		StringBuffer buffer = new StringBuffer();
		buffer.append("mchNo="+reqEntity.getMchNo()+"&");
		buffer.append("notifyUrl="+reqEntity.getNotifyUrl()+"&");
		buffer.append("orderCode="+reqEntity.getOrderCode()+"&");
		buffer.append("price="+reqEntity.getPrice()+"&");
		buffer.append("succPage="+reqEntity.getSuccPage()+"&");
		buffer.append("ts="+reqEntity.getTs()+"&");
		buffer.append("type="+reqEntity.getType());
		String sign = MD5Utils.MD5(buffer.toString()+"&token="+cfgPay.getAPP_TOKEN());
		buffer.append("&sign="+sign);
		logger.info("易富通请求参数拼接结果:patam={}",buffer.toString());
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
		buffer.append("realPrice="+data.realPrice+"&");
		buffer.append("token="+cfgPay.getAPP_TOKEN());
		String sign = MD5Utils.MD5(buffer.toString());
		if(!sign.equals(data.sign)) {
			logger.info("易富通支付请求返回签名错误patam={},sign={}",buffer.toString(),sign);
			return false;
		}
		return true;
	}
	 
	/**
	 * 获取支付url
	 */
	public final RspYFTEntity getYFTPayUrl(String amount,String payOrderSn){
		logger.info("调取易富通支付宝支付payOrderSn={},amount={}",payOrderSn,amount);
		if("true".equals(cfgPay.getDEBUG())) {
			amount = "0.01";
		}
		RspYFTEntity rEntity = null;
		ReqYFTPayEntity reqEntity = null;
		reqEntity = reqYFTPayEntity.buildReqEntity(amount,payOrderSn);
		
		//获取拼接参数
		String paraStr = getPayParams(reqEntity);
	 
		RspHttpEntity rspHttpEntity = null;
		rspHttpEntity = HttpUtil.sendMsg(paraStr,cfgPay.getPAY_URL(),false);
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
	
	/**
	 * 易富通订单支付查询接口
	 */
	public BaseResult<RspOrderQueryEntity> queryPayResult(String payCode, String orderNo) {
//		logger.info("调取易富通查询订单支付结果orderNo={}",orderNo);
		return ResultGenerator.genFailResult("请求易富通支付回调失败[]");
		/*RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
		RspQueryYFTEntity rEntity = null;
		RspHttpEntity rspHttpEntity = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("mchNo="+cfgPay.getAPP_MCHNO()+"&");
		buffer.append("orderCode="+orderNo+"&");
		buffer.append("ts="+cfgPay.getPayTime());
		String sign = MD5Utils.MD5(buffer.toString()+"&token="+cfgPay.getAPP_TOKEN());
		buffer.append("&sign="+sign);
		rspHttpEntity = HttpUtil.sendMsg(buffer.toString(),cfgPay.getQUERY_URL(),false);
		logger.info("易富通查询请求返回信息:" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc) {
			rEntity = JSON.parseObject(rspHttpEntity.msg,RspQueryYFTEntity.class);
			rspOrderQueryEntity.setResult_code(rEntity.data.status);
			rspOrderQueryEntity.setPayCode(payCode);
			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_YIFUTONG);
			rspOrderQueryEntity.setTrade_no(rEntity.data.tradeNo);
			return ResultGenerator.genSuccessResult("succ",rspOrderQueryEntity);
		}else {
			return ResultGenerator.genFailResult("请求易富通支付回调失败[" + rspHttpEntity.msg + "]");
		}
		*/
	}
	
	/**
	 * 验证回调参数是否合法
	 * @param yftNotify
	 * @return
	 */
	public Boolean booleanCheckSign(RespYFTnotifyEntity yftNotify) {
		StringBuffer signStrBuffer = new StringBuffer();
		signStrBuffer.append("mchNo="+yftNotify.getMchNo());
		signStrBuffer.append("&");
		signStrBuffer.append("orderCode="+yftNotify.getOrderCode());
		signStrBuffer.append("&");
		signStrBuffer.append("price="+yftNotify.getPrice());
		signStrBuffer.append("&");
		signStrBuffer.append("realPrice="+yftNotify.getRealPrice());
		signStrBuffer.append("&");
		signStrBuffer.append("tradeNo="+yftNotify.getTradeNo());
		signStrBuffer.append("&");
		if(StringUtils.isNotBlank(yftNotify.getRemarks())){			
			signStrBuffer.append("remarks="+yftNotify.getRemarks());
			signStrBuffer.append("&");
		}
		signStrBuffer.append("ts="+yftNotify.getTs());
		signStrBuffer.append("&token="+cfgPay.getAPP_TOKEN());
		String sign = MD5Utils.MD5(signStrBuffer.toString());
		if(sign.equals(yftNotify.getSign())){
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
	}
}
