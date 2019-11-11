package com.dl.shop.payment.pay.xianfeng.cash.util;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqQueryCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqRealCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.ucf.sdk.util.AESCoder;

@Component
public class XianFengCashUtil {
	private final Logger logger = LoggerFactory.getLogger(XianFengCashUtil.class);
	
	@Resource
	private Constants xFConstants;
	
	@Resource
	private ReqQueryCashEntity reqQueryCashEntity;
	
	//String secID,String version,String merID,String notifyURL,String mer_rsakey
	public RspSingleCashEntity reqCash(String orderNo,String amt,String accNo,String accName,String phone,String bankNo) throws Exception {
		if(xFConstants.getIS_DEBUBG()&&xFConstants.getTESTMODE()){
			amt="2";
		}
		RspSingleCashEntity rEntity;
		ReqSingleCashEntity reqEntity = null;
		reqEntity = ReqSingleCashEntity.buildReqSingleCashEntity(orderNo, amt, accNo, accName, phone, bankNo
				,xFConstants.getSEC_ID(),xFConstants.getVERSION(),xFConstants.getMER_ID(),
				 xFConstants.getNOTICE_URL(),xFConstants.getMER_RSAKEY());
		ReqRealCashEntity reqRCEntity = reqEntity.buildRealReqCashEntity();
		String url = xFConstants.getUCF_GATEWAY_URL() + "?" + reqRCEntity.buildReqStr();
		logger.info("请求参数:" + url);
		RspHttpEntity rspEntity = HttpUtil.sendMsg(null,url,false);
		logger.info(rspEntity.toString());
		if(rspEntity.isSucc) {
			String signVal = rspEntity.msg;
			//AESCoder.decrypt
			String dataValue = AESCoder.decrypt(signVal, xFConstants.getMER_RSAKEY());
			rEntity = JSON.parseObject(dataValue,RspSingleCashEntity.class);
		}else {
			rEntity = new RspSingleCashEntity();
			rEntity.resMessage = rspEntity.msg;
		}
		return rEntity;
	}
	
	public RspSingleCashEntity queryCash(String orderNo) throws Exception {
		RspSingleCashEntity rspEntity = null;//String orderNo,String secId,String version,String merID,String MER_RSAKEY
		ReqQueryCashEntity reqEntity = reqQueryCashEntity.buildReqQueryEntity(orderNo,
				xFConstants.getSEC_ID(),xFConstants.getVERSION(),xFConstants.getMER_ID(),xFConstants.getMER_RSAKEY());
		String url = xFConstants.getUCF_GATEWAY_URL() + "?" + reqEntity.buildReqStr();
		logger.info("queryCash orderNo={},url={}",orderNo,url);
		RspHttpEntity rspHttpEntity = HttpUtil.sendMsg(null,url,false);
		logger.info("[queryCash]" + rspHttpEntity.toString());
		if(rspHttpEntity.isSucc){
			String signVal = rspHttpEntity.msg;
			//AESCoder.decrypt
			String dataValue = AESCoder.decrypt(signVal, xFConstants.getMER_RSAKEY());
			rspEntity = JSON.parseObject(dataValue,RspSingleCashEntity.class);
			logger.info("[queryCash]" + dataValue);
		}else {
			rspEntity = new RspSingleCashEntity();
			rspEntity.resMessage = rspHttpEntity.msg;
		}
		return rspEntity;
	}
}
