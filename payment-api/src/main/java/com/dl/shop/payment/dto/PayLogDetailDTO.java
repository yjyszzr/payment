package com.dl.shop.payment.dto;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付日志")
@Data
public class PayLogDetailDTO {
	@ApiModelProperty(value=" 编号")
    private Integer logId;
	
	@ApiModelProperty(value="用户ID")
    private Integer userId;
	
	@ApiModelProperty(value="订单关联")
    private String orderSn;
	
    private String parentSn;
    
    private BigDecimal orderAmount;
    
    private String payCode;
    
    private String payName;
    
    private Integer addTime;
    
    private Integer lastTime;
    
    private Integer payType;
    
    private Integer isPaid;
    
    private Integer payTime;
    
    private String tradeNo;
    
    private String payIp;
    
    private String payMsg;
    
    private String payOrderSn;
}
