package com.dl.shop.payment.dto;

import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付方式数据：包含了是否有充值活动的数据")
@Data
public class PayWaysDTO {
	
	@ApiModelProperty(value="支付方式")
    private List<PaymentDTO> paymentDTOList; 
	
	@ApiModelProperty(value="0-没有充值活动  1-有充值活动")
	private Integer isHaveRechargeAct;
	
	@ApiModelProperty(value="充值用户具体信息")
	private RechargeUserDTO rechargeUserDTO;

}
