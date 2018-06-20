package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("是否支付过")
@Data
public class ValidPayDTO {
	
	@ApiModelProperty("是否支付过")
    private String hasPaid;

}
