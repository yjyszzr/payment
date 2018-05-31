package com.dl.shop.payment.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("是否结果")
@Data
public class YesOrNoDTO {

	@ApiModelProperty("0-未充值过 1-充值过")
    private String yesOrNo;

}