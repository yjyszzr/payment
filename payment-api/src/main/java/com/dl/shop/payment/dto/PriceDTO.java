package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("充值赠送的钱的字符串")
@Data
public class PriceDTO {

	@ApiModelProperty(value=" ")
    private String  payLogId;
	
	@ApiModelProperty(value="")
    private String  price = "0.00";
	
}
