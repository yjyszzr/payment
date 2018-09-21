package com.dl.shop.payment.param;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UbeyBankTypeParam {

	@ApiModelProperty("订单号")
	private Integer payLogId;
	@ApiModelProperty("退回金额数据")
	private String orderId;
	@ApiModelProperty("退回金额数据")
	private String bankType;
}
