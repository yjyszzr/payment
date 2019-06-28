package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class SftPayQueryOrderRequest extends SftPayBaseRequest {
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("transaction_id")
    private String orderNo;
    @JsonProperty("account_no")
    private String bankcardNo;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBankcardNo() {
        return bankcardNo;
    }

    public void setBankcardNo(String bankcardNo) {
        this.bankcardNo = bankcardNo;
    }
}
