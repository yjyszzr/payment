package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengBankListCfgParam {
	@ApiModelProperty("recordId")
	private Integer recordId;
	@ApiModelProperty("支付token信息")
	private String token;
}
