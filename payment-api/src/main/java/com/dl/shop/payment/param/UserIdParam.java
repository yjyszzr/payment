package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserIdParam {
	
	@ApiModelProperty("userId")
	private Integer userId;

}
