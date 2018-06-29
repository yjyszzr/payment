package com.dl.shop.payment.dto;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("先锋支付银行列表配置")
@Data
public class XianFengApplyCfgDTO {
	@ApiModelProperty(value="历史银行列表")
	private List<PayBankRecordDTO> bankList;
	@ApiModelProperty(value="支付金额")
	private String amt;
}
