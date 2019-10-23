package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RechargeParam {

	@ApiModelProperty("充值金额")
	private double totalAmount;
	@ApiModelProperty("赠送金额:可不传")
	private int giveAmount;
	@ApiModelProperty("支付方式编码")
	private String payCode;
	@ApiModelProperty("是否在微信内部打开支付？0不是微信内部 1微信内部")
	private int innerWechat;
	@ApiModelProperty("是否H5打开该页 1->h5请求，0||null->app端请求")
	private String isH5;
	@ApiModelProperty("支付宝唯一用户id")
	private String userId;
	@ApiModelProperty("订单编号")
	private String orderSn;
	//市民卡/惠民支付转有参数列
	@ApiModelProperty("用户Id")
	private Integer merCustId;
	private String reqSeq;
	private String randomKey;
	private String dateTime;
	@ApiModelProperty("用户手机号")
	private String phone;
	@ApiModelProperty("手机验证码")
	private String verCode;
	@ApiModelProperty("四要素令牌")
	private String token;
	@ApiModelProperty("验证码令牌")
	private String phoneToken;
}
