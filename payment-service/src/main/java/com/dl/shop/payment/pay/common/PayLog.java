package com.dl.shop.payment.pay.common;

public class PayLog {
	public static boolean isDebug = true;
	
	public static final void debug(String info) {
		if(!isDebug) {
			return;
		}
		System.out.println(info);
	}
	
}
