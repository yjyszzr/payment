package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("银行卡类型")
@Data
public class BankTypeDTO {
	@ApiModelProperty(value="1储蓄卡，2信用卡")
    private Integer bankType;
	@ApiModelProperty(value="银行名称")
    private String bankName;
}
