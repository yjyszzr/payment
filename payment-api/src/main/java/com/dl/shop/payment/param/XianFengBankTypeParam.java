package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengBankTypeParam {
	@ApiModelProperty("银行账号")
	private String bankCardNo;
}
