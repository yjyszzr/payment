package com.dl.shop.payment.dto;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("账户余额信息")
@Data
public class YmoneyDTO {

	@ApiModelProperty(value="返回状态")
    private String status;
	@ApiModelProperty(value="返回消息")
    private String message;
	@ApiModelProperty(value="账户余额")
    private String account_balance;

}
