package com.dl.shop.payment.pay.rongbao.cash.entity;

import lombok.Data;
/**
 * 序号,银行账户,开户名,开户行,分行,支行,公/私,金额,币种,省,市,手机号,证件类型,证件号,用户协议号,商户订单号,备注|……|……|
 * 1,62220215080205389633,jack-cooper,工商银行,分行,支行,私,0.01,CNY,北京,北京,18910116131,身份证,420321199202150718,0001,12306,hehe
 */
@Data
public class ReqCashContentEntity{
	private String no;
	private String acc;
	private String name;
	private String bankName;
	private String bankBranch;
	private String bankSubBranch;
	private String publiz;
	private String amt;//金额
	private String currency;
	private String province;
	private String city;
	private String phone;
	private String DocType;//证件类型
	private String docNum;//证件号
	private String protocalNo;//用户协议号
	private String merchantOrderNo;//用户订单号
	private String msg;	//备注
	
	public static ReqCashContentEntity buildTestReqCashEntity(String no,String amt,String phone) {
		ReqCashContentEntity reqEntity = new ReqCashContentEntity();
		reqEntity.setNo(no);
		reqEntity.setAcc("62220215080205389633");
		reqEntity.setName("jack-cooper");
		reqEntity.setBankName("工商银行");
		reqEntity.setBankBranch("分行");
		reqEntity.setBankSubBranch("支行");
		reqEntity.setPubliz("私");
		reqEntity.setAmt(amt);
		reqEntity.setCurrency("CNY");
		reqEntity.setProvince("北京");
		reqEntity.setCity("北京");
		reqEntity.setPhone(phone);
		reqEntity.setDocType("身份证");
		reqEntity.setDocNum("420321199202150718");
		reqEntity.setProtocalNo("0001");
		reqEntity.setMerchantOrderNo("12306");
		reqEntity.setMsg("彩小秘充值信息");
		return reqEntity;
	}
	
	public String buildContent() {
		StringBuilder builder = new StringBuilder();
		builder.append(no);
		builder.append(",");
		builder.append(acc);
		builder.append(",");
		builder.append(name);
		builder.append(",");
		builder.append(bankName);
		builder.append(",");
		builder.append(bankBranch);
		builder.append(",");
		builder.append(bankSubBranch);
		builder.append(",");
		builder.append(publiz);
		builder.append(",");
		builder.append(amt);
		builder.append(",");
		builder.append(currency);
		builder.append(",");
		builder.append(province);
		builder.append(",");
		builder.append(city);
		builder.append(",");
		builder.append(phone);
		builder.append(",");
		builder.append(DocType);
		builder.append(",");
		builder.append(docNum);
		builder.append(",");
		builder.append(protocalNo);
		builder.append(",");
		builder.append(merchantOrderNo);
		builder.append(",");
		builder.append(msg);
		return builder.toString();
	}
}

