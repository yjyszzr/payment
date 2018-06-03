package com.dl.shop.payment.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("充值用户具体信息")
@Data
public class RechargeUserDTO {
	
	@ApiModelProperty(value="0-未充过值，即新用户 1- 充过值，即老用户")
    private Integer oldUserBz;
	
	@ApiModelProperty(value="用户充值赠送金额")
	private List<DonationPriceDTO> donationPriceList;

}
