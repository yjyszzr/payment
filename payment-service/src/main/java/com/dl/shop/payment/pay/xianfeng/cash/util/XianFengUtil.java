package com.dl.shop.payment.pay.xianfeng.cash.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqQueryCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqRealCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleQueryEntity;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.util.AESCoder;

public class XianFengUtil {
	private final static Logger logger = LoggerFactory.getLogger(XianFengUtil.class);
	
	public static RspSingleCashEntity reqCash(String orderNo,String amt,String accNo,String accName,String phone,String bankNo) throws Exception {
		RspSingleCashEntity rEntity;
		ReqSingleCashEntity reqEntity = null;
		reqEntity = ReqSingleCashEntity.buildReqSingleCashEntity(orderNo, amt, accNo, accName, phone, bankNo);
		ReqRealCashEntity reqRCEntity = reqEntity.buildRealReqCashEntity();
		String url = Constants.UCF_GATEWAY_URL + "?" + reqRCEntity.buildReqStr();
		logger.info("请求参数:" + url);
		RspHttpEntity rspEntity = HttpUtil.sendMsg(null,url,false);
		logger.info(rspEntity.toString());
		if(rspEntity.isSucc) {
			String signVal = rspEntity.msg;
			//AESCoder.decrypt
			String dataValue = AESCoder.decrypt(signVal, Constants.MER_RSAKEY);
			rEntity = JSON.parseObject(dataValue,RspSingleCashEntity.class);
		}else {
			rEntity = new RspSingleCashEntity();
			rEntity.resMessage = rspEntity.msg;
		}
		return rEntity;
	}
	
	public static RspSingleQueryEntity queryCash(String orderNo) throws Exception {
		RspSingleQueryEntity rspEntity = null;
		ReqQueryCashEntity reqEntity = ReqQueryCashEntity.buildReqQueryEntity(orderNo);
		String url = Constants.UCF_GATEWAY_URL + "?" + reqEntity.buildReqStr();
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		logger.info(rspHttpEntity.toString());
		if(rspHttpEntity.isSucc){
			String signVal = rspHttpEntity.msg;
			//AESCoder.decrypt
			String dataValue = AESCoder.decrypt(signVal, Constants.MER_RSAKEY);
			rspEntity = JSON.parseObject(dataValue,RspSingleQueryEntity.class);
		}else {
			rspEntity = new RspSingleQueryEntity();
			rspEntity.resMessage = rspHttpEntity.msg;
		}
		return rspEntity;
	}
}
