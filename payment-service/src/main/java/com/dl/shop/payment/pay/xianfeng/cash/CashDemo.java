package com.dl.shop.payment.pay.xianfeng.cash;

import com.dl.shop.payment.pay.common.HttpUtil;
import com.dl.shop.payment.pay.common.RspHttpEntity;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;

public class CashDemo {

	public CashDemo() throws Exception {
//		String orderNo = "2018052400006999";
//		String amt = "10";
//		String accNo = "6222021001115704287";
//		String accName = "王泽武";
//		String phone = "18100000000";
//		String bankNo = "CCB";
//		RspSingleCashEntity rEntity = XianFengUtil.reqCash(orderNo,amt,accNo,accName,phone,bankNo);
//		System.out.println("========"+rEntity+"========");
		
//		RspSingleQueryEntity rspEntity = XianFengUtil.queryCash(orderNo);
//		System.out.println("======="+rspEntity+"=======");
		
		String url = "http://api.caixiaomi.net/api/payment/cash/querycash?withdrawSn=2018061512409198300010";
		RspHttpEntity rspEntity = HttpUtil.sendMsg(null,url,false);
		System.out.println(rspEntity);
	}
	
	public static void main(String[] args) {
		try {
			new CashDemo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
