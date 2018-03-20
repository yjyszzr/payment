package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RechargeParam {

	@ApiModelProperty("充值金额")
	private int totalAmount;
	@ApiModelProperty("支付方式编码")
	private String payCode;
}
