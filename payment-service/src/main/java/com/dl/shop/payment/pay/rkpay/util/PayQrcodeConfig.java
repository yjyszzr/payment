package com.dl.shop.payment.pay.rkpay.util;

//支付相关参数
public class PayQrcodeConfig extends Config {

    public String mp_id;//	商户池ID	string		MP0000000000000001	发起交易的商户池编号，由运营完成商户池注册后提供，mch_id和mp_id两者只能提供一个。
    public String ds_trade_no;//	渠道商系统付款交易号	string(32)	否	DS1608261827551467	渠道商系统内部的交易单号 ,32个字符内（可包含字母和数字,确保在渠道方系统唯一），ds_trade_no是渠道商系统和我们系统沟通的桥梁，使用ds_trade_no编号查询交易数据，也可以在接受交易通知时用以识别渠道商系统的交易数据，因此建议渠道商系统提供这个参数。
    public String pay_fee;//	付款金额，精确到小数2位	numeric	是	1.20 	2位小数，如果是整数金额，用.00来格式化。举例：100.00，90.10【注意必须提供2位小数格式的金额，例如 100.00】
    public String trade_type;//	交易类型	string	是	WX	WX-微信支付；AP-支付宝支付；【注意大写WX/AP】
    public String expire_time;//	付款有效期，单位分钟	numeric	否	10	取值范围：1~1440分钟，如不提供，则系统取值1440分钟【24小时】。请在设定的时间范围内完成付款，如超过设定时间，将无法操作付款。
    public String trade_subject;//	交易描述	string(50)	否	欢乐天天-游戏充值、动力鸡车-上海南站店	交易商品描述 ,50个字符内，显示格式建议：【商户名称/网站名称】-【商品描述/门店描述】。交易商品描述将在微信/支付宝APP中的交易记录里做为商品描述被显示。
    public String trade_memo;//	交易备注	string(200)	否	request_source=123,user_id=456	使用交易备注允许渠道商系统传递自定义交易参数，200个字符内，系统在交易查询和交易异步通知返回结果时将原样返回此交易备注，方便渠道商系统处理返回的请求结果。
    public String notify_url;//	交易异步通知地址	string(200)	是	http://pay.abc.com/pay/trade_notify	由客户端提供URL，接收来自系统推送的交易数据，200个字符内详情参考本文档【交易异步通知】

    public void initParams(String ds_trade_no,String pay_fee,String trade_type,
    		String expire_time,String trade_subject,String trade_memo,String notify_url){
        this.mp_id=StaticV.mpid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_type=trade_type;
        this.expire_time=expire_time;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=notify_url;
    }
}
