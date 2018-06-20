package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RollbackThirdOrderAmountParam {

	@ApiModelProperty("订单号")
	private String orderSn;

	@ApiModelProperty("金额")
	private String amt;

	@ApiModelProperty("金额")
	private String payCode;
}
