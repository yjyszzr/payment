package com.dl.shop.payment.pay.xianfeng.entity;

import org.springframework.util.StringUtils;

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
		return !StringUtils.isEmpty(status)&&"S".equalsIgnoreCase(status);
	}
	public boolean isDoing() {
		return StringUtils.isEmpty(status)||"I".equalsIgnoreCase(status);
	}
	public boolean isFail() {
		if(!StringUtils.isEmpty(status)&&"F".equalsIgnoreCase(status)){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 验证码校验失败
	 * @return
	 */
	public boolean isVerfyCodeWrong() {
		return "20005".equals(resCode);
	}
	
	/**
	 * 短信超出限制
	 * @return
	 */
	public boolean isVerifyCodeInValid() {
		return "20004".equals(resCode);
	}
}
