package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RollbackOrderAmountParam {

	@ApiModelProperty("订单号")
	private String orderSn;
	
}
