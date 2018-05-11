package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WithdrawDetailParam {

	@ApiModelProperty("流水id")
	private String withdraw_sn;
	
}
