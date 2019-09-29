package com.dl.shop.payment.dto;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付类型信息")
@Data
public class PaymentDTO {

	@ApiModelProperty(value=" 编号")
    private Integer payId;
	@ApiModelProperty(value="支付方式代码")
    private String payCode;
	@ApiModelProperty(value="支付方式名称")
    private String payName;
	@ApiModelProperty(value="支付类型")
    private Integer payType;
	@ApiModelProperty(value="排序")
    private Integer paySort;
	@ApiModelProperty(value="支付标题")
    private String payTitle;
	@ApiModelProperty(value="图标")
    private String payImg;
	@ApiModelProperty(value="描述")
    private String payDesc;
	@ApiModelProperty(value="是否固额")
    private Integer isReadonly;
	@ApiModelProperty(value="固定额度")
	private List<Map<String,String>> readMoney;
	@ApiModelProperty(value="H5链接")
	private String payUrl;

}
