package com.dl.shop.payment.param;


import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("活动充值钱数参数")
@Data
public class PayLogIdParam {
	
    @ApiModelProperty(value = "支付成功后的id")
    @NotNull(message = "支付id不能为空")
    private Integer payLogId;

}
