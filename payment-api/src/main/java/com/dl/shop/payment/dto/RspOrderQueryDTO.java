package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import lombok.Data;

@Api("融宝支付信息")
@Data
public class RspOrderQueryDTO {
	private int code;
	private String msg;
}
