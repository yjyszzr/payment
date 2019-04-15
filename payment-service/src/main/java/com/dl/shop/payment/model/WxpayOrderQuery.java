package com.dl.shop.payment.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "xml")
public class WxpayOrderQuery {

	private String appid;// 应用ID appid 是
    private String mch_id;// 商户号 mch_id 是
    private String out_trade_no;// 商户订单号 out_trade_no 是
    private String nonce_str;// 随机字符串 nonce_str 是
    private String sign;// 签名 sign 是
    
    @Data
    @XmlRootElement(name = "xml")
    public static class Response {
        private String appid;
        private String mch_id;
        private String device_info;
        private String nonce_str;
        private String sign;// 签名 sign 是
        private String result_code;// 业务结果 result_code 是 SUCCESS/FAIL
        private String return_msg;//
        private String return_code;
        private String err_code;// 错误代码 err_code 否
        private String err_code_des;// 错误代码描述 err_code_des 否
        private String openid;//用户openid
        private String is_subscribe;
        private String trade_state;//SUCCESS—支付成功REFUND—转入退款NOTPAY—未支付CLOSED—已关闭REVOKED—已撤销（刷卡支付）USERPAYING--用户支付中PAYERROR--支付失败(其他原因，如银行返回失败)
        private String trade_state_desc;
        private String transaction_id;//微信支付订单号
        private String trade_type;// 交易类型 trade_type 是 APP
        private String time_end;//交易完成时间
    }
}
