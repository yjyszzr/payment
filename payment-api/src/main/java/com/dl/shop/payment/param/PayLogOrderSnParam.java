package com.dl.shop.payment.param;


import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("orderSn")
@Data
public class PayLogOrderSnParam {
	
    @ApiModelProperty(value = "orderSn")
    @NotNull(message = "orderSn")
    private String orderSn;

}
