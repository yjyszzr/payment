package com.dl.shop.payment.pay.jhpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SftPayBaseResponse {
    private String version;
    private String charset;
    @JsonProperty("nonce_str")
    private String nonceStr;
    private String status;
    // status非0时返回
    private String message;
    // status为0时返回
    @JsonProperty("auth_mode")
    private Integer authMode;
    @JsonProperty("mch_no")
    private String mchNo;
    @JsonProperty("auth_mch_no")
    private String authMchNo;
    @JsonProperty("mch_key_ver")
    private Integer mchKeyVer;
    @JsonProperty("plat_key_ver")
    private Integer platKeyVer;
    @JsonProperty("sign_type")
    private String signType;
    private String sign;
    @JsonProperty("err_code")
    private String errCode = "0";
    @JsonProperty("err_msg")
    private String errMsg;
    @JsonProperty("need_query")
    private Integer needQuery;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Integer getNeedQuery() {
        return needQuery;
    }

    public void setNeedQuery(Integer needQuery) {
        this.needQuery = needQuery;
    }
}
