package com.dl.shop.payment.pay.xianfeng.cash.entity;

public class ReqEncodeCashEntity {
	public String merchantNo;	//商户订单号
	public String source;		//来源
	public String amount;		//金额
	public String transCur;		//币种
	public String userType;		//用户类型
	public String accountNo;	//卡号
	public String accountName;	//持卡人姓名
	public String accountType;	//账户类型
	public String mobileNo;		//手机号码
	public String bankNo;		//银行编码
	public String issuer;		//联行号
	public String branchProvince;//开户省份
	public String branchCity;	//开户市
	public String branchName;	//开户行支行
	public String noticeUrl;	//后台通知地址
	public String tradeProduct;	//代发产品
	public String memo;			//保留域
}
