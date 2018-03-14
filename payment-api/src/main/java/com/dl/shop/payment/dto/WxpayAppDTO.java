package com.dl.shop.payment.dto;

import io.swagger.annotations.Api;
import lombok.Data;

@Api("微信支付信息")
@Data
public class WxpayAppDTO {

	private String appid;// 应用ID appid 是
    private String partnerid;
    private String prepayid;
    private String packageValue;
    private String noncestr;
    private String timestamp;
    private String sign;
}
