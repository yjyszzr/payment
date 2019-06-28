/**
 * 
 */
package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SftPayBaseRequest {
    private String version = "1.0";
    private String charset = "UTF-8";
    @JsonProperty("nonce_str")
    private String nonceStr;
    @JsonProperty("auth_mode")
    private Integer authMode = 1;
    @JsonProperty("mch_no")
    private String mchNo;
    @JsonProperty("auth_mch_no")
    private String authMchNo;
    @JsonProperty("mch_key_ver")
    private Integer mchKeyVer = 1;
    @JsonProperty("plat_key_ver")
    private Integer platKeyVer = 1;
    @JsonProperty("sign_type")
    private String signType = "SHA1WithRSA";
    private String sign;
    @JsonProperty("req_time")
    private String reqTime;//请求时间，格式为yyyyMMddHHmmss，时区是时区为GMT+8。
    
    @JsonProperty("notify_url")
    private String notifyUrl;
    @JsonProperty("user_contract_id")
    private String userContractId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public Integer getAuthMode() {
        return authMode;
    }

    public void setAuthMode(Integer authMode) {
        this.authMode = authMode;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public String getAuthMchNo() {
        return authMchNo;
    }

    public void setAuthMchNo(String authMchNo) {
        this.authMchNo = authMchNo;
    }

    public Integer getMchKeyVer() {
        return mchKeyVer;
    }

    public void setMchKeyVer(Integer mchKeyVer) {
        this.mchKeyVer = mchKeyVer;
    }

    public Integer getPlatKeyVer() {
        return platKeyVer;
    }

    public void setPlatKeyVer(Integer platKeyVer) {
        this.platKeyVer = platKeyVer;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getUserContractId() {
        return userContractId;
    }

    public void setUserContractId(String userContractId) {
        this.userContractId = userContractId;
    }
    
}
