package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CashGetParam {
	@ApiModelProperty("提现单号")
	private String withdrawSn;
}