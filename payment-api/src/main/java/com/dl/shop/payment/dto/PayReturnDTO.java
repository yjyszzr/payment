package com.dl.shop.payment.dto;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api("支付成功返回信息")
@Data
public class PayReturnDTO {
   @ApiModelProperty
   private String payUrl;
   @ApiModelProperty
   private String payLogId;
   @ApiModelProperty(value="订单id")
   private String orderId;
   @ApiModelProperty(value="历史银行列表")
   private List<PayBankRecordDTO> mListBanks;
}
