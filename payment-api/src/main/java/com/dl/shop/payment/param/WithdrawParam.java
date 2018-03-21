package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WithdrawParam {

	@ApiModelProperty("提现金额")
	private int totalAmount;
	@ApiModelProperty("用户银行卡帐号信息id")
	private int userBankId;
}
