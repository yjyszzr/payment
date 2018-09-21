package com.dl.shop.payment.dto;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("ubey银行列表")
@Data
public class BankUbeyCodeDTO {
	   @ApiModelProperty
	   private String url;
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
