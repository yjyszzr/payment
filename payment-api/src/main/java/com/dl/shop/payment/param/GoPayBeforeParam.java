package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GoPayBeforeParam {

	@ApiModelProperty("支付信息token")
	private String payToken;
	@ApiModelProperty("红包id")
	private String bonusId;
	 
}
