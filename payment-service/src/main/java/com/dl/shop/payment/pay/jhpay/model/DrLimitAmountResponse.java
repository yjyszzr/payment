package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by kai.li@swiftpass.cn
 * <p>
 * DATE: 2019/1/17 16:30
 */
public class DrLimitAmountResponse extends SftPayBaseResponse {

    @JsonProperty("limit_amount")
    private String limitAmount;

    public String getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(String limitAmount) {
        this.limitAmount = limitAmount;
    }
}
