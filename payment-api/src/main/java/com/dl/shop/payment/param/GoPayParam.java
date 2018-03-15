package com.dl.shop.payment.param;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GoPayParam {

	@ApiModelProperty("订单号")
	private String orderSn;
//	@ApiModelProperty("父订单号")
//	private String parentSn;
	@ApiModelProperty("订单总额")
	private double orderAmount;
	@ApiModelProperty("支付类型：0-订单支付 1-充值")
	private int payType;
	@NotBlank(message="请提供有效的支付编码")
	@ApiModelProperty("支付方式编码")
	private String payCode;
	@ApiModelProperty("支付密码")
	private String surplusPassword;
	@ApiModelProperty("指纹")
	private String surplusPayCode;
}
