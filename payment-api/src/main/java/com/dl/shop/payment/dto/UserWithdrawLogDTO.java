package com.dl.shop.payment.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserWithdrawLogDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("提现单号")
    private String withdrawSn;
	@ApiModelProperty("提现单号")
    private String logTime;
	@ApiModelProperty("提现单号")
    private Integer logCode;
	@ApiModelProperty("提现单号")
    private String logName;
	@ApiModelProperty("提现金额")
    private String amount;
	@ApiModelProperty("提现银行")
    private String card;
}