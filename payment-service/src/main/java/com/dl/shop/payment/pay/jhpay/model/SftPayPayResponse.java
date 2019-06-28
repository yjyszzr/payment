package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class SftPayPayResponse extends SftPayBaseResponse {
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("transaction_id")
    private String orderNo;
    @JsonProperty("trade_state")
    private String tradeState;
    @JsonProperty("total_fee")
    private Integer totalFee;
    @JsonProperty("fee_type")
    private String feeType;
    private String attach;
    @JsonProperty("time_end")
    private String tradeTimeStr;

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

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getTradeTimeStr() {
        return tradeTimeStr;
    }

    public void setTradeTimeStr(String tradeTimeStr) {
        this.tradeTimeStr = tradeTimeStr;
    }
}
