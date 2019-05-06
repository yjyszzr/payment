package com.dl.shop.payment.pay.rkpay.util;

import lombok.Data;

//网银快捷支付相关参数
public class PayQuickConfig extends Config {
	public String mch_id;//	商户ID	string	是	MC0000000000000001	发起交易的商户编号，由运营完成商户注册后提供。
	public String ds_trade_no;//	渠道商系统付款交易号	string(32)	否	DS1608261827551467	渠道商系统内部的交易单号 ,32个字符内（可包含字母和数字,确保在渠道方系统唯一），ds_trade_no是渠道商系统和我们系统沟通的桥梁，使用ds_trade_no编号查询交易数据，也可以在接受交易通知时用以识别渠道商系统的交易数据，因此建议渠道商系统提供这个参数。
	public String pay_fee;//	付款金额，精确到小数2位	numeric	是	1.20 	2位小数，如果是整数金额，用.00来格式化。举例：100.00，90.10【注意必须提供2位小数格式的金额，例如 100.00】
	public String trade_subject;//	交易描述	string(50)	否	欢乐天天-游戏充值、动力鸡车-上海南站店	交易商品描述 ,50个字符内，显示格式建议：【商户名称/网站名称】-【商品描述/门店描述】。交易商品描述将在微信/QQ/支付宝APP中的交易记录里做为商品描述被显示。
	public String trade_memo;//	交易备注	string(200)	否	request_source=123,user_id=456	使用交易备注允许渠道商系统传递自定义交易参数，200个字符内，系统在交易查询和交易异步通知返回结果时将原样返回此交易备注，方便渠道商系统处理返回的请求结果。
	public String notify_url;//	交易异步通知地址	string(200)	是	http:/pay.abc.com/pay/trade_notify	由客户端提供URL，接收来自系统推送的交易数据，200个字符内详情参考本文档【交易异步通知】
	public String callback_url;//	支付完成后页面回调地址	string(200)	否	http:/pay.abc.com/pay/trade_callback?ds_trade_no=DS1608261827551467	由客户端提供URL，系统在支付完成后将回调该URL，如果没有提供callback_url，支付完成后，将回到发起支付的页面。
	public String quick_mode;//	网银快捷支付模式	string	是	NORMAL	目前支持的模式有：NORMAL-普通模式/YT/RK/GM；【注意大写NORMAL/YT/RK/GM】，由于网银快捷支付通道的多样性，不同的交易通道支持的模式有差异，请对接前咨询运营。
	//    以下应用参数在quick_mode=YT时必填或者有特定的要求					
	public String account_no;//	持卡人储蓄卡号/信用卡卡号	string	是	9558801001177120303	
	public String account_name;//	持卡人开户姓名	string	是	张飞	银行卡账户开户时提供的开户姓名
	public String id_no;//	持卡人身份证编号	string	是	310102198804215237	银行卡账户开户时提供的身份证编号
	public String mobile_phone;//	持卡人预留手机号码	string	是	13888889999	银行卡账户开户时提供的手机号码
	//    以下应用参数在quick_mode=RK时必填或者有特定的要求【网银支付】					
	public String bank_name;//	发起网银支付的银行名称	string	是	工商银行	网银发起银行名称，bank_name有规定的银行名称写法规范，支持的银行【工商银行/农业银行/建设银行/交通银行/光大银行/民生银行/广东发展银行/招商银行/中国邮政储蓄银行/上海银行/北京银行】，考虑到支付通道的多样性，对接时请咨询运营支持那些银行。
	//    以下应用参数在quick_mode=GM时必填或者有特定的要求					
	//	public String id_no;//	付款人身份证编号	string	是	310102198804215237	
	public String id_name;//	付款人身份证姓名	string	是	张飞	
	//默认NORMAL支付模式参数 
    public void initParams(String ds_trade_no,String pay_fee,String trade_subject,String trade_memo,String quick_mode){
        this.mch_id=StaticV.mchid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=StaticV.notify_url;
        this.callback_url=StaticV.callback_url;
        this.quick_mode=quick_mode;
    }
    //默认YT支付模式参数 
    public void initParams(String ds_trade_no,String pay_fee,String trade_subject,String trade_memo,
    		String quick_mode,String account_no,String account_name,String id_no,String mobile_phone){
    	this.mch_id=StaticV.mchid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=StaticV.notify_url;
        this.callback_url=StaticV.callback_url;
        this.quick_mode=quick_mode;
        this.account_no=account_no;
        this.account_name=account_name;
        this.id_no=id_no;
        this.mobile_phone=mobile_phone;
    }
    //默认RK支付模式参数 
    public void initParams(String ds_trade_no,String pay_fee,String trade_subject,String trade_memo,
    		String quick_mode,String bank_name){
    	this.mch_id=StaticV.mchid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=StaticV.notify_url;
        this.callback_url=StaticV.callback_url;
        this.quick_mode=quick_mode;
        this.bank_name=bank_name;
    }
    //默认GM支付模式参数 
    public void initParams(String ds_trade_no,String pay_fee,String trade_subject,String trade_memo,
    		String quick_mode,String id_no,String id_name){
    	this.mch_id=StaticV.mchid;
        this.ds_trade_no=ds_trade_no;
        this.pay_fee=pay_fee;
        this.trade_subject=trade_subject;
        this.trade_memo=trade_memo;
        this.notify_url=StaticV.notify_url;
        this.callback_url=StaticV.callback_url;
        this.quick_mode=quick_mode;
        this.id_no=id_no;
        this.id_name=id_name;
    }
}
