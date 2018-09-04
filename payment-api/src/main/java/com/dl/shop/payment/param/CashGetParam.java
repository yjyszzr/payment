package com.dl.shop.payment.param;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CashGetParam {
	@ApiModelProperty("提现单号")
	private String withdrawSn;
	@ApiModelProperty("是否审核通过")
	private boolean isPass;	
}
