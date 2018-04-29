package com.dl.shop.payment.pay.rongbao.demo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dl.shop.payment.pay.rongbao.entity.ReqRongEntity;
import com.google.gson.Gson;

public class RDemo {

	public RDemo() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String strTransTime = sdf.format(date);
		
		ReqRongEntity reqEntity = new ReqRongEntity();
		reqEntity.setOrderId(100012+"" + System.currentTimeMillis());
		reqEntity.setUserId(100007+"");
		reqEntity.setTotal(1.2);
		reqEntity.setPName("彩小秘");
		reqEntity.setPDesc("彩小秘足彩支付");
		reqEntity.setTransTime(strTransTime);
		
		Gson gson = new Gson();
		String data = gson.toJson(reqEntity);
		try {
			data = URLEncoder.encode(data,"UTF-8");
			String url = "http://localhost:8080/reapal-h5-api/h5/indexH5.jsp?data="+data;
			System.out.println(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new RDemo();
	}
}
