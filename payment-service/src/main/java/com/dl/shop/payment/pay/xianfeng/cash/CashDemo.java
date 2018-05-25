package com.dl.shop.payment.pay.xianfeng.cash;

import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.xianfeng.cash.util.XianFengUtil;

public class CashDemo {

	public CashDemo() throws Exception {
		String orderNo = "2018052400006999";
		String amt = "10";
		String accNo = "6222021001115704287";
		String accName = "王泽武";
		String phone = "18100000000";
		String bankNo = "CCB";
		RspSingleCashEntity rEntity = XianFengUtil.reqCash(orderNo,amt,accNo,accName,phone,bankNo);
		System.out.println("========"+rEntity+"========");
	}
	
	public static void main(String[] args) {
		try {
			new CashDemo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
