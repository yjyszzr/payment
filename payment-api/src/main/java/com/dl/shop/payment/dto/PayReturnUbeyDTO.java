package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("返回ubey直连网银请求信息")
@Data
public class PayReturnUbeyDTO {
	   @ApiModelProperty
	   private String payUrl;
	   @ApiModelProperty
	   private String payLogId;
	   @ApiModelProperty(value="订单id")
	   private String orderId;
	   @ApiModelProperty(value="彩票种类")
	   private String lotteryClassifyId;
	   @ApiModelProperty(value="data参数")
	   private String data;
	   @ApiModelProperty(value="signatrue签名")
	   private String signature;
}
