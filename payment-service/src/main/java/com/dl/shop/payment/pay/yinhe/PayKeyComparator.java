package com.dl.shop.payment.pay.yinhe;

import java.util.Comparator;

public class PayKeyComparator implements Comparator<String>{

	@Override
	public int compare(String left, String right) {
		// TODO Auto-generated method stub
		if(left != null && right != null) {
			return left.compareTo(right);
		}
		return 0;
	}

}
