package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AuthorizParam {
	@ApiModelProperty("支付宝回调地址传参app_id")
	private String app_id;
	@ApiModelProperty("支付宝回调地址传参scope")
	private String scope;
	@ApiModelProperty("支付宝回调地址传参auth_code")
	private String auth_code;
	@ApiModelProperty("支付宝回调地址传参source")
	private String source;
	
	
}
