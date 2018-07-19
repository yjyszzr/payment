package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengPayConfirmParam {
	@ApiModelProperty("payLogId")
	private int payLogId;
	@ApiModelProperty("支付验证码")
	private String code;
	@ApiModelProperty("上下文")
	private String token;
}
