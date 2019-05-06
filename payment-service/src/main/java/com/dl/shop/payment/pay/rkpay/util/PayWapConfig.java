package com.dl.shop.payment.pay.rkpay.util;

//支付相关参数
public class PayWapConfig extends Config {

    public String mp_id;
    public String ds_trade_no;
    public String pay_fee;
    public String trade_type;
    public int expire_time;
    public String user_ip;
    public String trade_subject;
    public String trade_memo;
    public String notify_url;
    public String callback_url;

    public void initParams(String ds_trade_no,String pay_fee,String trade_type,String trade_subject,String trade_memo,String notify_url,String callback_url){
        this.mp_id=StaticV.mpid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_type=trade_type;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=notify_url;
        this.callback_url=callback_url;
        this.user_ip="127.0.0.1";
        this.expire_time=StaticV.expire_time;
    }
}
