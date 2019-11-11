package com.dl.shop.payment.param;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("伊蚊快捷支付的入参")
@Data
public class YWQucikBankPayParam {
	
	@ApiModelProperty("订单号")
	private String orderSn;
	
	@ApiModelProperty("订单金额")
	private String orderAmount;

}
