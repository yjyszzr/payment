package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import lombok.Data;

@Api("银河支付信息")
@Data
public class YinHeResultDTO {
	private String payUrl;
	private String payLogId;
}
