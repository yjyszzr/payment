package com.dl.shop.payment.model;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "xml")
public class WxpayUnifiedOrder {

	private String appid;// 应用ID appid 是
    private String mch_id;// 商户号 mch_id 是
    private String device_info;// 设备号 device_info 否
    private String nonce_str;// 随机字符串 nonce_str 是
    private String sign;// 签名 sign 是
    private String body;// 商品描述 body 是
    private String detail;// 商品详情 detail 否
    private String attach;// 附加数据 attach 否
    private String out_trade_no;// 商户订单号 out_trade_no 是
    private String fee_type;// 货币类型 fee_type 否 默认人民币：CNY
    private Integer total_fee;// 总金额 total_fee 是 单位分
    private String spbill_create_ip;// 终端IP spbill_create_ip 是
    private String time_start;// 交易起始时间 time_start 否
    private String time_expire;// 交易结束时间 time_expire 否
    private String goods_tag;// 商品标记 goods_tag 否
    private String notify_url;// 通知地址 notify_url 是
    private String trade_type;// 交易类型 trade_type 是
    private String limit_pay;// 指定支付方式 limit_pay 否
    private String openid;// 用户标识 否
    private String product_id; //trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
    
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
        private String trade_type;// 交易类型 trade_type 是 APP
        private String prepay_id;// 预支付交易会话标识 prepay_id 是
        private String code_url;//trade_type为NATIVE时有返回，用于生成二维码，展示给用户进行扫码支付
        private String mweb_url;//trade_type为MWEB返回
    }
}
