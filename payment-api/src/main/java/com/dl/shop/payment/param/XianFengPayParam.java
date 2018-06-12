package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengPayParam {
	@ApiModelProperty("payLogId")
	private int payLogId;
}
