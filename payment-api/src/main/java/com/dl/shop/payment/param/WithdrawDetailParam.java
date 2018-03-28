package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WithdrawDetailParam {

	@ApiModelProperty("提现单号")
	private String withdawSn;
	
}
