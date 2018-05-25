package com.dl.shop.payment.pay.xianfeng.cash.util;

import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqRealCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.ReqSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.ucf.sdk.util.AESCoder;

public class XianFengUtil {

	public static RspSingleCashEntity reqCash(String orderNo,String amt,String accNo,String accName,String phone,String bankNo) throws Exception {
		RspSingleCashEntity rEntity;
		ReqSingleCashEntity reqEntity = null;
		reqEntity = ReqSingleCashEntity.buildReqSingleCashEntity(orderNo, amt, accNo, accName, phone, bankNo);
		ReqRealCashEntity reqRCEntity = reqEntity.buildRealReqCashEntity();
		String url = Constants.UCF_GATEWAY_URL + "?" + reqRCEntity.buildReqStr();
		System.out.println("请求参数:" + url);
		RspHttpEntity rspEntity = HttpUtil.sendMsg(null,url,false);
		System.out.println(rspEntity);
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
}
