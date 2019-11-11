package com.dl.shop.payment.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "xml")
public class WxpayAppModel {

	private String appid;// 应用ID appid 是
    private String partnerid;
    private String prepayid;
    private String packageValue;
    private String noncestr;
    private String timestamp;
    private String sign;
    
}
