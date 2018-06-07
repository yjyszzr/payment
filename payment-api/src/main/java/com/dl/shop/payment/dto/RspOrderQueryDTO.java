package com.dl.shop.payment.dto;

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("融宝支付信息")
@Data
public class RspOrderQueryDTO {
	@ApiModelProperty
	private int code;
	@ApiModelProperty
	private String msg;
	@ApiModelProperty
	private String payCode;
	@ApiModelProperty(value="支付类型,0订单支付 1充值支付")
	private int payType;
	
	@ApiModelProperty(value="0-没有 充值活动 1-有充值活动")
	private Integer isHaveRechargeAct;
	@ApiModelProperty(value="赠送的钱(value)")
	private String donationPrice;
	
	
}
