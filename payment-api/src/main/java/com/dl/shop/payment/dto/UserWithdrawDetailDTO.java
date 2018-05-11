package com.dl.shop.payment.dto;

import java.io.Serializable;
import java.util.List;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserWithdrawDetailDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("提现单号")
    private String withdrawSn;
	@ApiModelProperty("提现金额")
    private String amount;
	@ApiModelProperty("提现银行")
    private String card;
	@ApiModelProperty("提现状态")
    private String status;
	@ApiModelProperty("提现进度")
	private List<UserWithdrawLogDTO> userWithdrawLogs; 
}
