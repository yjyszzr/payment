package com.dl.shop.payment.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserWithdrawLogDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("提现时间")
    private String logTime;
	@ApiModelProperty("提现")
    private Integer logCode;
	@ApiModelProperty("提现名称")
    private String logName;
}
