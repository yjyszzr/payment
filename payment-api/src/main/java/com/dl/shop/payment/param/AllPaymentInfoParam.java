package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AllPaymentInfoParam {
	@ApiModelProperty("支付方式")
	private String payCode;
	@ApiModelProperty("订单编号")
	private String orderSn;
	@ApiModelProperty("支付信息token")
	private String payToken;
}
