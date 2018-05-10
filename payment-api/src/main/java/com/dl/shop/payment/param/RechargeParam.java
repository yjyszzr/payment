package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RechargeParam {

	@ApiModelProperty("充值金额")
	private int totalAmount;
	@ApiModelProperty("支付方式编码")
	private String payCode;
	@ApiModelProperty("是否在微信内部打开支付？0不是微信内部 1微信内部")
	private int innerWechat;
}
