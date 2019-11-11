package com.dl.shop.payment.param;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WithDrawSnAndUserIdParam {
	@ApiModelProperty("提现单号")
	@NotNull
	private String withDrawSn;
	
	@ApiModelProperty("用户id")
	@NotNull
	private Integer userId;
}
