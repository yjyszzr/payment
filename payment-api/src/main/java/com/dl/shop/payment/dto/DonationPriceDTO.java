package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("充值赠送的钱的字符串")
@Data
public class DonationPriceDTO {

	@ApiModelProperty(value=" ")
    private Integer  minRechargeAmount;
	
	@ApiModelProperty(value="")
    private Integer  donationAmount;
	
}
