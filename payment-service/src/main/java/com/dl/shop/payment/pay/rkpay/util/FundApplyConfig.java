package com.dl.shop.payment.pay.rkpay.util;

//支付相关参数
public class FundApplyConfig extends Config {

    public String mch_id;
    public String trade_no;
    public String ds_trade_no;
    public String trade_subject;
    public String trade_memo;
    public String apply_mode;
    public String trade_fee;
    public String account_no;
    public String account_name;
    public String notify_url;
    
    public void initParams() {//代付账户余额查询
    	this.mch_id=StaticV.mchid;
    }

    public void initParams(String trade_no,String ds_trade_no) {//代付状态查询
    	this.mch_id=StaticV.mchid;
    	this.trade_no=trade_no;
    	this.ds_trade_no=ds_trade_no;
    }
    
    public void initParams(String ds_trade_no,String trade_subject,String trade_memo,
    		String apply_mode,String trade_fee,String account_no,String account_name){//代付
        this.mch_id=StaticV.mchid;
        this.ds_trade_no=ds_trade_no;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.apply_mode=apply_mode;
        this.trade_fee=trade_fee;
        this.account_no=account_no;
        this.account_name=account_name;
        this.notify_url=StaticV.notify_url;
    }
}
