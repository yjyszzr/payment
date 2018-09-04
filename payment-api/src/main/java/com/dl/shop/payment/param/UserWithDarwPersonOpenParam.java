package com.dl.shop.payment.param;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserWithDarwPersonOpenParam {
	@ApiModelProperty("成功提现单号集合")
	private List<String> sucessPersonWithdrawSns;
	@ApiModelProperty("失败提现单号集合")
	private List<String> failPersonWithdrawSns;	
}
