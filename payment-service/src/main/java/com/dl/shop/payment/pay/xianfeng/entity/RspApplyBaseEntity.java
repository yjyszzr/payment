package com.dl.shop.payment.pay.xianfeng.entity;

import com.dl.shop.payment.pay.common.RspOrderQueryEntity;

public class RspApplyBaseEntity{
	public String resCode;
	public String resMessage;
	public String merchantId;
	public String merchantNo;
	public String tradeNo;
	public String status;
	public String tradeTime;
	public String memo;

	
	public RspOrderQueryEntity buildRspOrderQueryEntity(String payCode) {
		RspOrderQueryEntity rspEntity = new RspOrderQueryEntity();
		rspEntity.setMerchant_id(merchantId);
		rspEntity.setOrder_no(merchantNo);
		rspEntity.setResult_code(resCode);
		rspEntity.setResult_msg(resMessage);
		rspEntity.setTrade_no(tradeNo);
		rspEntity.setPayCode(payCode);
		rspEntity.setType(RspOrderQueryEntity.TYPE_XIANFENG);
		return rspEntity;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "resCode:" + resCode + " resMessage:" + resMessage + " merchantId:" + merchantId + 
				" merchantNo:" + merchantNo + " tradeNo:" + tradeNo + " status:" + status + " tradeTime:" + tradeTime;
	}
	
	public boolean isSucc() {
		return "00000".equals(resCode);
	}
}
