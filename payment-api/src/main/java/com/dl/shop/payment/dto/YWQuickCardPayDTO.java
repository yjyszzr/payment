package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("伊蚊快捷支付返回的DTO")
@Data
public class YWQuickCardPayDTO {
	
	@ApiModelProperty(value="前台的通知地址")
	private String fontNoticeUrl = "";
	
	@ApiModelProperty(value="完整的html数据")
	private String showHtmlData = "";
	
}
