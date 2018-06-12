package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 先锋支付请求应答
 */
@Api("支付成功返回信息")
@Data
public class PayXianFengApplyDTO {
	@ApiModelProperty
	private String payOrderSn;
}
