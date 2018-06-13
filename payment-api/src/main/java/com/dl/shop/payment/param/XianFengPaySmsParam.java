package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengPaySmsParam {
	@ApiModelProperty("支付订单号")
	private String payOrderSn;
}
