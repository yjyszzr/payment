package com.dl.shop.payment.dto;

import java.math.BigDecimal;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付日志")
@Data
public class PayLogDTO {
	
	@ApiModelProperty(value=" 编号")
    private Integer  logId;
	
	@ApiModelProperty(value=" 用户id")
    private Integer  userId;
	
	@ApiModelProperty(value="付款金额")
    private BigDecimal  orderAmount;
	
}
