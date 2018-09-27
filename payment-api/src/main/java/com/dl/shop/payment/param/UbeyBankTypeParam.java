package com.dl.shop.payment.param;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UbeyBankTypeParam {

	@ApiModelProperty("支付订单号")
	private Integer payLogId;
	@ApiModelProperty("订单")
	private String orderId;
}
