package com.dl.shop.payment.pay.rkpay.util;

//查询相关参数
public class QueryConfig extends Config{

    public String mch_id;
    public String trade_no;
    public String ds_trade_no;
    public String tp_trade_no;

    public void initParams(String mchid,String trade_no,String ds_trade_no,String tp_trade_no){
        this.mch_id=mchid;
        if(trade_no!=""){
            this.trade_no=trade_no;
        }else if(ds_trade_no!=""){
            this.ds_trade_no=ds_trade_no;
        }else if(tp_trade_no!=""){
            this.tp_trade_no=tp_trade_no;
        }
    }
}
