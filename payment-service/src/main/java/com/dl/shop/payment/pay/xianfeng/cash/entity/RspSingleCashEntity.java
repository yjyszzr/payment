package com.dl.shop.payment.pay.xianfeng.cash.entity;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.util.StringUtils;

public class RspSingleCashEntity {
	public String transCur;
	public String tradeNo;
	public String status;
	public String resMessage;
	public String amount;
	public String resCode;
	public String merchantId;
	public String merchantNo;
	
	//notify专用
	public String tradeTime;
	public String sign;
	public String memo;
//	public boolean isSucc() {
//		return "00000".equals(resCode);
//	}
	private static Map<String,String> notErrorRetCode = null; 
	static{
		notErrorRetCode = new HashedMap(); 
		notErrorRetCode.put("00000", "");
		notErrorRetCode.put("00001", "");
		notErrorRetCode.put("00002", "");
		notErrorRetCode.put("10005", "");
		notErrorRetCode.put("00000", "");
	}
	
	public boolean isTradeSucc() {
		return !StringUtils.isEmpty(status)&&"S".equalsIgnoreCase(status);
	}
	public boolean isTradeFail(Boolean isApply) {
		if(!StringUtils.isEmpty(status)&&"F".equalsIgnoreCase(status)){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
////		10009 交易记录不存在
//		Boolean notApplyAndSomeError = !isApply&&StringUtils.isEmpty(status)&&(!notErrorRetCode.containsKey(resCode));
//		return notApplyAndSomeError;
	}
	public boolean isTradeDoing() {
		return StringUtils.isEmpty(status)||"I".equalsIgnoreCase(status);
	}

//	//00001已受理    00002订单处理中
//	public boolean isHandleing() {
//		return "00001".equals(resCode) || "00002".equals(resCode);
//	}
//	/**
//	 * 10008	业务处理中 10005	订单重复提交
//	 * @return
//	 */
//	public boolean isNotErrorCode() {
////		10008	业务处理中 10005	订单重复提交
//		return "10005".equals(resCode) || "10008".equals(resCode);
//	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "transCur:" + transCur + " tradeNo:" + tradeNo + " status:" + status + " resMessage:" + resMessage
				+" amount:" + amount + " resCode:" + resCode + " merchantId:" + merchantId + " merchantNo:" + merchantNo;
		return str;
	}
}
