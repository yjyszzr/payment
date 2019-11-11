package com.dl.shop.payment.param;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserIdParam {
	
	@ApiModelProperty("userId")
	@NotNull(message= "userId 不能为空")
	private Integer userId;

}
