package com.dl.shop.payment.pay.rkpay.util;
//退款相关参数
public class ReFundQueryConfig extends Config {
    public String mch_id;
    public String refund_no;

    public void initParams(String refund_no){
        this.mch_id=StaticV.mchid;
        this.refund_no=refund_no;
    }
}
