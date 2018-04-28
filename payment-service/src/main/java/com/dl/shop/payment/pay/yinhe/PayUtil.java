package com.dl.shop.payment.pay.yinhe;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.dl.base.util.MD5Utils;

public class PayUtil {

	/**
	 * 获取请求参数
	 * @param treeMap
	 * @return
	 */
	public static final String getPayParams(TreeMap<String, Object> treeMap) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iterator = treeMap.entrySet().iterator();  
		while(iterator.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String key = entry.getKey();
			String val = (String) entry.getValue();
			builder.append(key + "=" + val + "&");
		}
		System.out.println("原串:" +builder.toString());
		//remove the last item &
		if(builder.length() > 0) {
			builder.delete(builder.length()-1,builder.length());
		}
		System.out.println("结果:" + builder.toString());
		return builder.toString();
	}
	
	/**
	 * 获取signcode
	 * @param paraUrl
	 * @param s
	 * @return
	 */
	public static final String getSignCode(String paraUrl,String s) {
		String str = paraUrl + s;
		String strMD5 = MD5Utils.MD5(str);
		return strMD5;
	}
}
