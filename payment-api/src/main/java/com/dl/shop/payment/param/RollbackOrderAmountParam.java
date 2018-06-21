package com.dl.shop.payment.param;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RollbackOrderAmountParam {

	@ApiModelProperty("订单号")
	private String orderSn;
	
	@ApiModelProperty("退回金额数据")
	@NotBlank(message = "金额，元")
	private BigDecimal amt;
}
