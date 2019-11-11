package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("先锋支付申请返回")
@Data
public class XianFengApplyDTO {
	@ApiModelProperty(value="token信息")
	private String token;
}
