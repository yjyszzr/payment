package com.dl.shop.payment.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "xml")
public class WxpayNotifyModel {

	private String appid;// 应用ID appid 是
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String result_code;
    private String openid;
    private String trade_type;
    private String bank_type;
    private int total_fee;
    private int cash_fee;
    private String transaction_id;
    private String out_trade_no;
    private String time_end;
    private String return_code;
    private String return_msg;
}
