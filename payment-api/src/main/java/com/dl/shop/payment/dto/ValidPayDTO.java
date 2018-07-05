package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("是否支付过")
@Data
public class ValidPayDTO {
	
	@ApiModelProperty("是否支付过:0-未支付过 ,1-已支付过")
    private String hasPaid = "0";

}
