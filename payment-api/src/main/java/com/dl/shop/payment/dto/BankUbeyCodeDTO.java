package com.dl.shop.payment.dto;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付成功返回信息")
@Data
public class BankUbeyCodeDTO {
	   @ApiModelProperty(value="页面地址")
	   private String bankUrl;
	   @ApiModelProperty
	   private String url;
	   @ApiModelProperty
	   private String orderId;
	   @ApiModelProperty
	   private Integer payLogId;
	   @ApiModelProperty
	   private List<BankCode> bank;
	   @Data
	   public static class BankCode{
		   @ApiModelProperty(value="银行编码")
		   private String code;
		   @ApiModelProperty(value="图片地址")
		   private String imageUrl;
		   @ApiModelProperty(value="名称")
		   private String name;
	   }
}
