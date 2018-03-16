package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GoPayParam {

	@ApiModelProperty("支付信息token")
	private String payToken;
	@ApiModelProperty("支付方式编码")
	private String payCode;
}
