package com.dl.shop.payment.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianFengPayParam {
	@ApiModelProperty("payLogId")
	private int payLogId;
	@ApiModelProperty("真实姓名")
	private String name;
	@ApiModelProperty("身份证号")
	private String certNo;
	@ApiModelProperty("银行账号")
	private String accNo;
	@ApiModelProperty("手机号码")
	private String phone;
	@ApiModelProperty("上下文")
	private String token;
	@ApiModelProperty("信用卡安全码")
	private String cvn2;
	@ApiModelProperty("信用卡有效期")
	private String validDate;
}
