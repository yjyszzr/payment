package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengBankRemoveParam {
	
	@ApiModelProperty("唯一标识")
	private String recordId;
}
