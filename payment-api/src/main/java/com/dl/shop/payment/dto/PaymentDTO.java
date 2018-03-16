package com.dl.shop.payment.dto;

import javax.persistence.Column;
import javax.persistence.Id;

import io.swagger.annotations.Api;
import lombok.Data;

@Api("支付类型信息")
@Data
public class PaymentDTO {

    private Integer payId;

    private String payCode;

    private String payName;

    private Integer payType;

    private String payFee;

    private Integer paySort;

    private Integer isEnable;

    private String payTitle;

    private String payImg;

    private String payConfig;

    private String payDesc;

}
