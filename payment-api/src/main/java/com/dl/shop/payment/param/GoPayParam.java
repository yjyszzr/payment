package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GoPayParam {

	@ApiModelProperty("支付信息token")
	private String payToken;
	@ApiModelProperty("支付方式编码")
	private String payCode;
	@ApiModelProperty("是否在微信内部打开支付？0不是微信内部 1微信内部")
	private int innerWechat; 
	@ApiModelProperty("是否H5打开该页   1 -> 端请求  0||null -> app端请求")
	private String isH5;
	@ApiModelProperty("订单编号")
	private String orderSn;
	@ApiModelProperty("支付宝唯一用户id")
	private String userId;
}
