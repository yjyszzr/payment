package com.dl.shop.payment.utils;

import org.apache.commons.lang3.StringUtils;

public class BankNoUtil {

	/**
	 * 获取银行号码后四位号
	 * @param cardNo
	 * @return
	 */
	public static final String getTailFourCardNo(String cardNo) {
		String result = "";
		if(!StringUtils.isBlank(cardNo) && cardNo.length() > 4){
			int len = cardNo.length();
			result = cardNo.substring(len-4);
		}
		return result;
	}
}
