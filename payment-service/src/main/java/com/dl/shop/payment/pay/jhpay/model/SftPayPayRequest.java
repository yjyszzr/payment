package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class SftPayPayRequest extends SftPayBaseRequest {
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("total_fee")
    private Long totalFee;
    @JsonProperty("fee_type")
    private String feeType = "CNY";
    private String attach;
    @JsonProperty("payer_info")
    private String payerInfo;
    @JsonProperty("encrypted_key")
    private String encryptedKey;
    @JsonProperty("notify_url")
    private String notifyUrl;
    @JsonProperty
    private String remark;  // 备注，描述
    
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Long getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Long totalFee) {
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

    public String getPayerInfo() {
        return payerInfo;
    }

    public void setPayerInfo(String payerInfo) {
        this.payerInfo = payerInfo;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
