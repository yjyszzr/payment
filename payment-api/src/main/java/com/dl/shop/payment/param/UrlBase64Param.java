package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UrlBase64Param {
	@ApiModelProperty("base64Id")
	private Integer base64Id;
}
